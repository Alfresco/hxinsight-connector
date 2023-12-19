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

package org.alfresco.hxi_connector.live_ingester.messaging.in;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.RepoEventMapper;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.unchanged;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.mapWith;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class RepoEventMapperTest
{
    private static final long EVENT_TIMESTAMP = 1_690_000_000_100L;
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";
    private static final String NODE_NAME = "test-name";
    private static final String NODE_PRIMARY_ASSOC_Q_NAME = "cm:test-name";
    private static final String NODE_TYPE = "cm:folder";
    private static final String NODE_CREATED_BY_USER_WITH_ID = "admin";
    private static final String NODE_MODIFIED_BY_USER_WITH_ID = "hr_user";
    private static final Set<String> NODE_ASPECT_NAMES = Set.of(
            "cm:titled",
            "cm:auditable");
    private static final boolean NODE_IS_FOLDER = true;
    private static final boolean NODE_IS_FILE = false;
    private static final long NODE_CREATED_AT = 1_690_000_000_050L;
    private static final NodeProperty<String> NODE_TITLE = new NodeProperty<>("cm:title", "some title");
    private static final Set<NodeProperty<?>> NODE_PROPERTIES = Set.of(NODE_TITLE);
    private final RepoEventMapper repoEventMapper = new RepoEventMapper();

    @Test
    void shouldMapToIngestNodeContentCommand()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_CREATED);

        NodeResource nodeResource = defaultNodeResource().build();

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
    void shouldMapToIngestNodeMetadataCommand_whenNodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_CREATED);

        NodeResource nodeResource = defaultNodeResource().build();

        setNodeResource(event, nodeResource);

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                updated(NODE_NAME),
                updated(NODE_PRIMARY_ASSOC_Q_NAME),
                updated(NODE_TYPE),
                updated(NODE_CREATED_BY_USER_WITH_ID),
                updated(NODE_MODIFIED_BY_USER_WITH_ID),
                updated(NODE_ASPECT_NAMES),
                updated(NODE_IS_FILE),
                updated(NODE_IS_FOLDER),
                updated(NODE_CREATED_AT),
                NODE_PROPERTIES.stream()
                        .map(nodeProperty -> CustomPropertyDelta.updated(nodeProperty.name(), nodeProperty.value()))
                        .collect(Collectors.toSet()));

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_whenNodeNameAndAspectNamesUpdated()
    {
        // given
        String newName = "some new amazing name";
        Set<String> newAspectNames = Set.of("some new aspect");

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setName(NODE_NAME)
                .setAspectNames(NODE_ASPECT_NAMES)
                .build();
        NodeResource nodeResource = defaultNodeResource()
                .setName(newName)
                .setAspectNames(newAspectNames)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                updated(newName),
                unchanged(NODE_PRIMARY_ASSOC_Q_NAME),
                unchanged(NODE_TYPE),
                unchanged(NODE_CREATED_BY_USER_WITH_ID),
                unchanged(NODE_MODIFIED_BY_USER_WITH_ID),
                updated(newAspectNames),
                unchanged(NODE_IS_FILE),
                unchanged(NODE_IS_FOLDER),
                unchanged(NODE_CREATED_AT),
                Collections.emptySet());

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_whenNodeCustomPropertyCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_UPDATED);
        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:description", null))
                .build();
        NodeResource nodeResource = defaultNodeResource()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                unchanged(NODE_NAME),
                unchanged(NODE_PRIMARY_ASSOC_Q_NAME),
                unchanged(NODE_TYPE),
                unchanged(NODE_CREATED_BY_USER_WITH_ID),
                unchanged(NODE_MODIFIED_BY_USER_WITH_ID),
                unchanged(NODE_ASPECT_NAMES),
                unchanged(NODE_IS_FILE),
                unchanged(NODE_IS_FOLDER),
                unchanged(NODE_CREATED_AT),
                Set.of(CustomPropertyDelta.updated("cm:description", "some description")));

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_whenNodeCustomPropertyUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_UPDATED);
        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:description", "some description"))
                .build();
        NodeResource nodeResource = defaultNodeResource()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "new description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                unchanged(NODE_NAME),
                unchanged(NODE_PRIMARY_ASSOC_Q_NAME),
                unchanged(NODE_TYPE),
                unchanged(NODE_CREATED_BY_USER_WITH_ID),
                unchanged(NODE_MODIFIED_BY_USER_WITH_ID),
                unchanged(NODE_ASPECT_NAMES),
                unchanged(NODE_IS_FILE),
                unchanged(NODE_IS_FOLDER),
                unchanged(NODE_CREATED_AT),
                Set.of(CustomPropertyDelta.updated("cm:description", "new description")));

        assertEquals(expectedEvent, actualEvent);
    }

    @Test
    void shouldMapToIngestNodeMetadataCommand_whenNodeCustomPropertyRemoved()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setTime(event, EVENT_TIMESTAMP);
        setType(event, NODE_UPDATED);
        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title"))
                .build();
        NodeResource nodeResource = defaultNodeResource()
                .setProperties(mapWith("cm:title", null, "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        IngestMetadataCommand actualEvent = repoEventMapper.mapToIngestMetadataCommand(event);

        // then
        IngestMetadataCommand expectedEvent = new IngestMetadataCommand(
                EVENT_TIMESTAMP,
                NODE_ID,
                unchanged(NODE_NAME),
                unchanged(NODE_PRIMARY_ASSOC_Q_NAME),
                unchanged(NODE_TYPE),
                unchanged(NODE_CREATED_BY_USER_WITH_ID),
                unchanged(NODE_MODIFIED_BY_USER_WITH_ID),
                unchanged(NODE_ASPECT_NAMES),
                unchanged(NODE_IS_FILE),
                unchanged(NODE_IS_FOLDER),
                unchanged(NODE_CREATED_AT),
                Set.of(CustomPropertyDelta.deleted("cm:title")));

        assertEquals(expectedEvent, actualEvent);
    }

    private NodeResource.Builder defaultNodeResource()
    {
        return NodeResource.builder()
                .setId(NODE_ID)
                .setName(NODE_NAME)
                .setPrimaryAssocQName(NODE_PRIMARY_ASSOC_Q_NAME)
                .setNodeType(NODE_TYPE)
                .setCreatedByUser(mockUser(NODE_CREATED_BY_USER_WITH_ID))
                .setModifiedByUser(mockUser(NODE_MODIFIED_BY_USER_WITH_ID))
                .setAspectNames(NODE_ASPECT_NAMES)
                .setIsFile(NODE_IS_FILE)
                .setIsFolder(NODE_IS_FOLDER)
                .setCreatedAt(dateFromTimestamp(NODE_CREATED_AT))
                .setProperties(mapWith("cm:title", "some title", "cm:description", null));
    }

    private UserInfo mockUser(String id)
    {
        UserInfo userInfo = mock();
        given(userInfo.getId()).willReturn(id);

        return userInfo;
    }

    private void setTime(RepoEvent<DataAttributes<NodeResource>> event, long timestamp)
    {
        given(event.getTime()).willReturn(dateFromTimestamp(timestamp));
    }

    private ZonedDateTime dateFromTimestamp(long timestamp)
    {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }

    private void setType(RepoEvent<DataAttributes<NodeResource>> event, EventType type)
    {
        given(event.getType()).willReturn(type.getType());
    }

    private void setNodeResource(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResource)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResource()).willReturn(nodeResource);
    }

    private void setNodeResourceBefore(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResourceBefore)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResourceBefore()).willReturn(nodeResourceBefore);
    }

    private DataAttributes<NodeResource> mockData(RepoEvent<DataAttributes<NodeResource>> event)
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
