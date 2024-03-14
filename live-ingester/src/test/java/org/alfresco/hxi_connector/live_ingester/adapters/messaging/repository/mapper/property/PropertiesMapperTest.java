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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property;

import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.deleted;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.mapWith;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

class PropertiesMapperTest
{
    PropertiesMapper propertiesMapper = new PropertiesMapper();

    @Test
    void shouldHandleAllPropertiesUpdated_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setName("some name")
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                PropertyDelta.updated("cm:name", "some name"),
                PropertyDelta.updated("cm:title", "some title"),
                PropertyDelta.updated("cm:description", "some description"));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldPutNameInProperties_NodeCreated()
    {
        // given
        String name = "some name";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setName(name)
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                PropertyDelta.updated(NAME_PROPERTY, name));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldIgnoreNullName_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setName(null)
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of();

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandleNoPropertiesChange_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder().build();
        NodeResource nodeResource = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of();

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandleNamePropertyChange_NodeUpdated()
    {
        // given
        String name = "some name";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setName("previous name")
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setName(name)
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                PropertyDelta.updated(NAME_PROPERTY, name));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandlePropertyCreation_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:description", null))
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(PropertyDelta.updated("cm:description", "some description"));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandlePropertyUpdate_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);
        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:description", "some description"))
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "new description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(PropertyDelta.updated("cm:description", "new description"));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandlePropertyDeletion_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);
        NodeResource nodeResourceBefore = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title"))
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setProperties(mapWith("cm:title", null, "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(deleted("cm:title"));

        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    /**
     * Test that we can cope with update requests from null to null. See https://docs.alfresco.com/content-services/latest/develop/oop-ext-points/events/ for an example of this.
     */
    @Test
    void shouldHandleNullPropertyUnchanged_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);
        // Use same null property in before and after.
        NodeResource nodeResource = NodeResource.builder().setProperties(mapWith("cm:taggable", null)).build();
        setNodeResourceBefore(event, nodeResource);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(PropertyDelta.unchanged("cm:taggable"));
        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldHandleContentDeleted_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_UPDATED);

        ContentInfo contentInfo = new ContentInfo(null, 123L, null);
        NodeResource nodeResourceBefore = NodeResource.builder().setContent(contentInfo).build();
        setNodeResourceBefore(event, nodeResourceBefore);
        NodeResource nodeResource = NodeResource.builder().setContent(null).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(deleted(CONTENT_PROPERTY));
        assertEquals(expectedPropertyDeltas, propertyDeltas);
    }

    @Test
    void shouldNotMentionContentCreated_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_CREATED);

        ContentInfo contentInfo = new ContentInfo(null, 123L, null);
        NodeResource nodeResource = NodeResource.builder().setContent(contentInfo).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        assertEquals(emptySet(), propertyDeltas);
    }

    @Test
    void shouldNotMentionContentUpdated_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_UPDATED);

        ContentInfo oldContentInfo = new ContentInfo(null, 123L, null);
        NodeResource nodeResourceBefore = NodeResource.builder().setContent(oldContentInfo).build();
        setNodeResourceBefore(event, nodeResourceBefore);
        ContentInfo newContentInfo = new ContentInfo(null, 456L, null);
        NodeResource nodeResource = NodeResource.builder().setContent(newContentInfo).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        assertEquals(emptySet(), propertyDeltas);
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

    public static void setNodeResourceBefore(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResourceBefore)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResourceBefore()).willReturn(nodeResourceBefore);
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
