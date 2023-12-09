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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.CamelEventMapper;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.RepoEventMapper;
import org.alfresco.repo.event.databind.ObjectMapperFactory;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;
import org.junit.jupiter.api.Test;

class RepoEventMapperTest
{

    private static final long EVENT_TIMESTAMP = 1000;
    private static final String NODE_ID = "test-id";
    private static final String NODE_NAME = "test-name";
    private static final String NODE_PRIMARY_ASSOC_Q_NAME = "cm:test-name";
    private static final String NODE_TYPE = "cm:folder";
    private static final String NODE_CREATED_BY_USER_WITH_ID = "first-user-id";
    private static final String NODE_MODIFIED_BY_USER_WITH_ID = "second-user-id";
    private static final Set<String> NODE_ASPECT_NAMES = Set.of(
        "cm:titled",
        "cm:auditable");
    private static final boolean NODE_IS_FOLDER = true;
    private static final boolean NODE_IS_FILE = false;
    private static final long NODE_CREATED_AT = 1000;
    private static final NodeProperty<String> NODE_TITLE = new NodeProperty<>("cm:title", "some title");
    private static final NodeProperty<String> NODE_DESCRIPTION = new NodeProperty<>("cm:description", "some description");
    private static final Set<NodeProperty<?>> NODE_PROPERTIES = Set.of(NODE_TITLE, NODE_DESCRIPTION);

    private final CamelEventMapper camelEventMapper = new CamelEventMapper(ObjectMapperFactory.createInstance());
    private final RepoEventMapper repoEventMapper = new RepoEventMapper();

    @Test
    void mapToIngestNewNodeEvent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        DataAttributes<NodeResource> eventData = mock();
        NodeResource nodeResource = mock();

        when(event.getTime()).thenReturn(ZonedDateTime.ofInstant(Instant.ofEpochMilli(EVENT_TIMESTAMP), ZoneId.systemDefault()));
        when(event.getData()).thenReturn(eventData);

        when(eventData.getResource()).thenReturn(nodeResource);

        when(nodeResource.getId()).thenReturn(NODE_ID);
        when(nodeResource.getName()).thenReturn(NODE_NAME);
        when(nodeResource.getPrimaryAssocQName()).thenReturn(NODE_PRIMARY_ASSOC_Q_NAME);
        when(nodeResource.getNodeType()).thenReturn(NODE_TYPE);
        when(nodeResource.getCreatedByUser()).thenReturn(mockUser(NODE_CREATED_BY_USER_WITH_ID));
        when(nodeResource.getModifiedByUser()).thenReturn(mockUser(NODE_MODIFIED_BY_USER_WITH_ID));
        when(nodeResource.getAspectNames()).thenReturn(mockUser(NODE_MODIFIED_BY_USER_WITH_ID));

        Node node = new Node(
            NODE_ID,
            NODE_NAME,
            NODE_PRIMARY_ASSOC_Q_NAME,
            NODE_TYPE,
            NODE_CREATED_BY_USER_WITH_ID,
            NODE_MODIFIED_BY_USER_WITH_ID,
            NODE_ASPECT_NAMES,
            NODE_IS_FILE,
            NODE_IS_FOLDER,
            NODE_CREATED_AT,
            NODE_PROPERTIES);

        IngestNewNodeEvent expectedEvent = new IngestNewNodeEvent(
            EVENT_TIMESTAMP,
            node);

        // when
        IngestNewNodeEvent actualEvent = repoEventMapper.mapToIngestNewNodeEvent(event);

        // then
        assertEquals(expectedEvent, actualEvent);
    }

    private UserInfo mockUser(String id)
    {
        UserInfo userInfo = mock();

        when(userInfo.getId()).thenReturn(id);

        return userInfo;
    }
}
