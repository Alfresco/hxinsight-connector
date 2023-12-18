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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.RepoEventMapper;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

class RepoEventMapperTest
{
    private static final long EVENT_TIMESTAMP = 1_690_000_000_100L;
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";
    private static final String NODE_NAME = "test-name";
    private static final String NODE_PRIMARY_ASSOC_Q_NAME = "cm:test-name";
    private static final String NODE_TYPE = "cm:folder";
    private static final String NODE_CREATED_BY_USER_WITH_ID = "admin";
    private static final String NODE_MODIFIED_BY_USER_WITH_ID = "admin";
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
    void mapToIngestNewNodeEvent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        DataAttributes<NodeResource> data = mock();

        when(event.getTime()).thenReturn(dateFromTimestamp(EVENT_TIMESTAMP));
        when(event.getData()).thenReturn(data);

        NodeResource nodeResource = NodeResource.builder()
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
                .setProperties(createPropertiesMap("cm:title", "some title", "cm:description", null))
                .build();

        when(data.getResource()).thenReturn(nodeResource);

        // when
        IngestNewNodeEvent actualEvent = repoEventMapper.mapToIngestNewNodeEvent(event);

        // then
        Node expectedNode = new Node(
                NODE_ID,
                NODE_NAME,
                NODE_PRIMARY_ASSOC_Q_NAME,
                NODE_TYPE,
                NODE_CREATED_BY_USER_WITH_ID,
                NODE_MODIFIED_BY_USER_WITH_ID,
                Optional.empty(),
                NODE_ASPECT_NAMES,
                NODE_IS_FILE,
                NODE_IS_FOLDER,
                NODE_CREATED_AT,
                NODE_PROPERTIES);

        IngestNewNodeEvent expectedEvent = new IngestNewNodeEvent(
                EVENT_TIMESTAMP,
                expectedNode);

        assertEquals(expectedEvent, actualEvent);
    }

    private UserInfo mockUser(String id)
    {
        UserInfo userInfo = mock();
        when(userInfo.getId()).thenReturn(id);

        return userInfo;
    }

    private ZonedDateTime dateFromTimestamp(long timestamp)
    {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }

    private Map<String, Serializable> createPropertiesMap(String k1, String v1, String k2, String v2)
    {
        Map<String, Serializable> properties = new HashMap<>();

        properties.put(k1, v1);
        properties.put(k2, v2);

        return properties;
    }
}
