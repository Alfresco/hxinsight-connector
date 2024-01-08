/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.messaging.in.mapper;

import org.alfresco.hxi_connector.live_ingester.domain.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.property.PropertiesMapper;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RepoEventMapperTest
{
    private static final long EVENT_TIMESTAMP = 1_690_000_000_100L;
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";

    @Mock
    PropertiesMapper propertiesMapper;
    @InjectMocks
    RepoEventMapper repoEventMapper;

    @BeforeEach
    void setUp()
    {
        lenient().when(propertiesMapper.calculateCustomPropertiesDelta(any())).thenReturn(Collections.emptySet());
    }

    @Test
    void shouldMapToIngestNodeContentCommand()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setId(NODE_ID)
                .build();

        setNodeResource(event, nodeResource);

        // when
        IngestContentCommand actualCommand = repoEventMapper.mapToIngestContentCommand(event);

        // then
        IngestContentCommand expectedCommand = new IngestContentCommand(
                EVENT_TIMESTAMP,
                NODE_ID);

        assertEquals(expectedCommand, actualCommand);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setId(NODE_ID)
                .build();

        setNodeResource(event, nodeResource);

        PropertyDelta regularPropertyDelta = mock();
        given(propertiesMapper.calculatePropertyDelta(eq(event), any())).willReturn(regularPropertyDelta);
        given(propertiesMapper.calculateCustomPropertiesDelta(event)).willReturn(Collections.emptySet());

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                regularPropertyDelta,
                Collections.emptySet());

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldAllowToMapToIngestNodeMetadataCommand_whenNodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_UPDATED);

        NodeResource nodeResource = NodeResource.builder()
                .setId(NODE_ID)
                .build();

        setNodeResource(event, nodeResource);

        given(propertiesMapper.calculatePropertyDelta(eq(event), any())).willReturn(mock());
        given(propertiesMapper.calculateCustomPropertiesDelta(event)).willReturn(mock());

        // then
        assertDoesNotThrow(() -> repoEventMapper.mapToIngestMetadataCommand(event));
    }

    @Test
    void shouldFailToMapToIngestNodeMetadataCommand_whenNodeDeleted()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_DELETED);

        // then
        assertThrows(ValidationException.class, () -> repoEventMapper.mapToIngestMetadataCommand(event));
    }

    public static void setTime(RepoEvent<DataAttributes<NodeResource>> event, long timestamp)
    {
        given(event.getTime()).willReturn(dateFromTimestamp(timestamp));
    }

    public static ZonedDateTime dateFromTimestamp(long timestamp)
    {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
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
}
