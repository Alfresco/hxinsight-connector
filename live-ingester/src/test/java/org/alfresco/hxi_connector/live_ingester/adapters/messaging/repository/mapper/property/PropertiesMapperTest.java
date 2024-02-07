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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.mapWith;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

class PropertiesMapperTest
{
    private static final String NODE_TYPE = "test-type";
    private static final String NAME_PROPERTY_KEY = "cm:name";
    PropertiesMapper propertiesMapper = new PropertiesMapper();

    @Test
    void shouldHandlePropertyUpdate_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setNodeType(NODE_TYPE)
                .build();

        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getNodeType);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.updated(NODE_TYPE);

        assertEquals(expectedNameDelta, nameDelta);
    }

    @Test
    void shouldHandlePropertyUpdate_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setNodeType("previous type")
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setNodeType(NODE_TYPE)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getNodeType);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.updated(NODE_TYPE);

        assertEquals(expectedNameDelta, nameDelta);
    }

    @Test
    void shouldHandlePropertyUnchanged_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder().build();
        NodeResource nodeResource = NodeResource.builder()
                .setNodeType(NODE_TYPE)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getNodeType);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.unchanged(NODE_TYPE);

        assertEquals(expectedNameDelta, nameDelta);
    }

    @Test
    void shouldHandleAllCustomPropertiesUpdated_NodeCreated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(
                CustomPropertyDelta.updated("cm:name", "some name"),
                CustomPropertyDelta.updated("cm:title", "some title"),
                CustomPropertyDelta.updated("cm:description", "some description"));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
    }

    @Test
    void shouldPutNameInCustomProperties_NodeCreated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(
                CustomPropertyDelta.updated(NAME_PROPERTY_KEY, name));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of();

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
    }

    @Test
    void shouldHandleNoCustomPropertiesChange_NodeUpdated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of();

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(
                CustomPropertyDelta.updated(NAME_PROPERTY_KEY, name));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
    }

    @Test
    void shouldHandleCustomPropertyCreation_NodeUpdated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(CustomPropertyDelta.updated("cm:description", "some description"));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
    }

    @Test
    void shouldHandleCustomPropertyUpdate_NodeUpdated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(CustomPropertyDelta.updated("cm:description", "new description"));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
    }

    @Test
    void shouldHandleCustomPropertyDeletion_NodeUpdated()
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(CustomPropertyDelta.deleted("cm:title"));

        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
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
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(CustomPropertyDelta.unchanged("cm:taggable"));
        assertEquals(expectedPropertyDeltas, customPropertyDeltas);
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
