/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.deleted;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;
import static org.alfresco.repo.event.v1.model.EventType.PERMISSION_UPDATED;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertiesMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class RepoEventMapperTest
{
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";

    @Mock
    PropertiesMapper propertiesMapper;
    @Mock
    private MimeTypeMapper mimeTypeMapper;
    @InjectMocks
    RepoEventMapper repoEventMapper;

    @Test
    void shouldMapToIngestNodeContentCommand()
    {
        // given
        String mimeType = "application/pdf";
        given(mimeTypeMapper.mapMimeType(mimeType)).willReturn(mimeType);
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(NODE_CREATED, mimeType);

        // when
        TriggerContentIngestionCommand actualCommand = repoEventMapper.mapToIngestContentCommand(event);

        // then
        TriggerContentIngestionCommand expectedCommand = new TriggerContentIngestionCommand(NODE_ID, mimeType);

        assertEquals(expectedCommand, actualCommand);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(NODE_CREATED);

        // when
        IngestNodeCommand actualEvent = repoEventMapper.mapToIngestNodeCommand(event);

        // then
        IngestNodeCommand expectedEvent = new IngestNodeCommand(
                NODE_ID,
                CREATE,
                emptySet());

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldAllowToMapToIngestNodeMetadataCommand_whenNodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(NODE_UPDATED);

        // then
        assertDoesNotThrow(() -> repoEventMapper.mapToIngestNodeCommand(event));
    }

    @Test
    void shouldFailToMapToIngestNodeMetadataCommand_whenNodeDeleted()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_DELETED);

        // then
        assertThrows(ValidationException.class, () -> repoEventMapper.mapToIngestNodeCommand(event));
    }

    @Test
    void shouldAllowToMapToDeleteNodeCommand_whenNodeDeleted()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(NODE_DELETED);

        // when
        DeleteNodeCommand deleteNodeCommand = repoEventMapper.mapToDeleteNodeCommand(event);

        // then
        DeleteNodeCommand expectedCommand = new DeleteNodeCommand(NODE_ID);
        assertEquals(expectedCommand, deleteNodeCommand);
    }

    @Test
    void shouldFailToMapToDeleteNodeCommand_whenNodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_CREATED);

        // then
        assertThrows(ValidationException.class, () -> repoEventMapper.mapToDeleteNodeCommand(event));
    }

    @Test
    void shouldNoticeContentDeleted_whenContentRemoved()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(NODE_UPDATED);
        given(propertiesMapper.mapToPropertyDeltas(event)).willReturn(Set.of(deleted(CONTENT_PROPERTY)));

        // when
        IngestNodeCommand ingestNodeCommand = repoEventMapper.mapToIngestNodeCommand(event);

        // then
        Set<PropertyDelta<?>> expected = Set.of(deleted(CONTENT_PROPERTY));
        assertEquals(expected, ingestNodeCommand.properties(), "Expected content to be removed");
    }

    public static void setType(RepoEvent<DataAttributes<NodeResource>> event, EventType type)
    {
        given(event.getType()).willReturn(type.getType());
    }

    public static void setNodeResource(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResource)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResource()).willReturn(nodeResource);
    }

    private static DataAttributes<NodeResource> mockData(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (event.getData() != null)
        {
            return event.getData();
        }

        DataAttributes<NodeResource> data = mock();

        given(event.getData()).willReturn(data);

        return data;
    }

    private static RepoEvent<DataAttributes<NodeResource>> mockMinimalEvent(EventType eventType)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, eventType);
        NodeResource nodeResource = mock();
        given(nodeResource.getId()).willReturn(NODE_ID);
        setNodeResource(event, nodeResource);
        return event;
    }

    private static RepoEvent<DataAttributes<NodeResource>> mockMinimalEvent(EventType eventType, String mimeType)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mockMinimalEvent(eventType);
        mockMimeType(event.getData().getResource(), mimeType);
        return event;
    }

    private static void mockMimeType(NodeResource nodeResource, String mimeType)
    {
        ContentInfo mockContentInfo = mock();
        given(nodeResource.getContent()).willReturn(mockContentInfo);
        given(mockContentInfo.getMimeType()).willReturn(mimeType);
    }
}
