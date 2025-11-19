/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static java.time.ZoneOffset.UTC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ALLOW_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ANCESTORS_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.DENY_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.mapWith;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;
import static org.alfresco.repo.event.v1.model.EventType.PERMISSION_UPDATED;


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

class PropertiesMapperTest
{
    private static final String EXPECTED_DATE_STRING = "2023-01-01T00:00:00.000Z";

    PropertiesMapper propertiesMapper = new PropertiesMapper();

    @Test
    void shouldHandleAllPropertiesUpdated_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setName("some name")
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated("cm:name", "some name"),
                updated("cm:title", "some title"),
                updated("cm:description", "some description"));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldPutTypeInProperties_NodeCreated()
    {
        // given
        String type = "some type";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setNodeType(type)
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(TYPE_PROPERTY, type));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldHandleFileNamePropertyChange_NodeUpdated()
    {
        // given
        String name = "some file name";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setName("previous file name")
                .build();
        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setName(name)
                .setContent(new ContentInfo("application/jpeg", 123L, "UTF-8"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(NAME_PROPERTY, name),
                contentMetadataUpdated(CONTENT_PROPERTY, "application/jpeg", 123L, name));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldHandleFolderNamePropertyChange_NodeUpdated()
    {
        // given
        String name = "some folder name";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setName("previous folder name")
                .build();
        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setName(name)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(NAME_PROPERTY, name));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
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
        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated("cm:title", "some title"),
                updated("cm:description", "some description"));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
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
        NodeResource nodeResource = nodeResourceWithRequiredFields()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "new description"))
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated("cm:title", "some title"),
                updated("cm:description", "new description"));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldHandleContentProperties_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_CREATED);

        ContentInfo contentInfo = new ContentInfo("application/msword", 123L, null);
        NodeResource nodeResource = nodeResourceWithRequiredFields().setContent(contentInfo).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expected = Set.of(
                contentMetadataUpdated(CONTENT_PROPERTY, "application/msword", 123L, "some name"));
        assertEquals(mergeWithDefaultProperties(expected), propertyDeltas);
    }

    @Test
    void shouldMapContentFieldUpdates_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_UPDATED);

        ContentInfo oldContentInfo = new ContentInfo("image/jpeg", 123L, null);
        NodeResource nodeResourceBefore = NodeResource.builder().setContent(oldContentInfo).build();
        setNodeResourceBefore(event, nodeResourceBefore);
        ContentInfo newContentInfo = new ContentInfo("image/bmp", 456L, null);
        NodeResource nodeResource = nodeResourceWithRequiredFields().setContent(newContentInfo).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expected = Set.of(contentMetadataUpdated(CONTENT_PROPERTY, "image/bmp", 456L, "some name"));
        assertEquals(mergeWithDefaultProperties(expected), propertyDeltas);
    }

    @Test
    void shouldMapContentAndNameFieldUpdates_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        setType(event, NODE_UPDATED);

        ContentInfo oldContentInfo = new ContentInfo("image/jpeg", 123L, null);
        NodeResource nodeResourceBefore = NodeResource.builder().setName("oldName.jpeg").setContent(oldContentInfo).build();
        setNodeResourceBefore(event, nodeResourceBefore);
        ContentInfo newContentInfo = new ContentInfo("image/bmp", 456L, null);
        NodeResource nodeResource = nodeResourceWithRequiredFields().setName("newName.bmp").setContent(newContentInfo).build();
        setNodeResource(event, nodeResource);

        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expected = Set.of(contentMetadataUpdated(CONTENT_PROPERTY, "image/bmp", 456L, "newName.bmp"),
                updated(NAME_PROPERTY, "newName.bmp"));
        assertEquals(mergeWithDefaultProperties(expected), propertyDeltas);
    }

    @Test
    void shouldAddACLInfo_NodeCreated()
    {
        // given
        String groupEveryone = "GROUP_EVERYONE";
        String bob = "bob";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        given(event.getData()).willReturn(mock(EventData.class));
        given(event.getData().getResource()).willReturn(nodeResourceWithRequiredFields().build());
        given(event.getData().getResourceBefore()).willReturn(NodeResource.builder().build());

        given(((EventData) event.getData()).getResourceReaderAuthorities()).willReturn(Set.of(groupEveryone));
        given(((EventData) event.getData()).getResourceDeniedAuthorities()).willReturn(Set.of(bob));
        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(ALLOW_ACCESS, Set.of(groupEveryone)),
                updated(DENY_ACCESS, Set.of(bob)));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldAddDefaultACLInfoIfNotPresent_NodeCreated()
    {
        // given
        String groupEveryone = "GROUP_EVERYONE";
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        given(event.getData()).willReturn(mock(EventData.class));
        given(event.getData().getResource()).willReturn(nodeResourceWithRequiredFields().build());
        given(event.getData().getResourceBefore()).willReturn(NodeResource.builder().build());

        given(((EventData) event.getData()).getResourceReaderAuthorities()).willReturn(null);
        given(((EventData) event.getData()).getResourceDeniedAuthorities()).willReturn(null);
        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(ALLOW_ACCESS, Set.of(groupEveryone)));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }

    @Test
    void shouldAddACLInfo_NodePermissionsUpdated()
    {
        // given
        String groupEveryone = "GROUP_EVERYONE";
        String bob = "bob";

        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, PERMISSION_UPDATED);

        given(event.getData()).willReturn(mock(EventData.class));
        given(event.getData().getResource()).willReturn(nodeResourceWithRequiredFields().build());
        given(event.getData().getResourceBefore()).willReturn(NodeResource.builder().build());

        given(((EventData) event.getData()).getResourceReaderAuthorities()).willReturn(Set.of(groupEveryone));
        given(((EventData) event.getData()).getResourceDeniedAuthorities()).willReturn(Set.of(bob));
        // when
        Set<PropertyDelta<?>> propertyDeltas = propertiesMapper.mapToPropertyDeltas(event);

        // then
        Set<PropertyDelta<?>> expectedPropertyDeltas = Set.of(
                updated(ALLOW_ACCESS, Set.of(groupEveryone)),
                updated(DENY_ACCESS, Set.of(bob)));

        assertEquals(mergeWithDefaultProperties(expectedPropertyDeltas), propertyDeltas);
    }
    @Test
    void shouldCalculateAncestorsPropertyDelta()
    {
        // given
        List<String> primaryHierarchy = List.of("parent-id", "grandparent-id", "root-id");

        RepoEvent<DataAttributes<NodeResource>> event = mock();
        NodeResource nodeResource = NodeResource.builder()
                .setPrimaryHierarchy(primaryHierarchy)
                .build();
        setNodeResource(event, nodeResource);

        // when
        Optional<PropertyDelta<?>> result = PropertyMappingHelper.calculateAncestorsPropertyDelta(event);

        // then
        assertTrue(result.isPresent());

        Map<String, Serializable> expectedAncestorsData = Map.of(
                "primaryParentId", (Serializable) "parent-id",
                "primaryAncestorIds", (Serializable) List.of("root-id", "grandparent-id", "parent-id"));

        PropertyDelta<?> expectedDelta = updated(ANCESTORS_PROPERTY, expectedAncestorsData);
        assertEquals(expectedDelta, result.get());
    }

    @Test
    void shouldReturnEmptyWhenPrimaryHierarchyIsNull()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        NodeResource nodeResource = NodeResource.builder()
                .setPrimaryHierarchy(null)
                .build();
        setNodeResource(event, nodeResource);

        // when
        Optional<PropertyDelta<?>> result = PropertyMappingHelper.calculateAncestorsPropertyDelta(event);

        // then
        assertFalse(result.isPresent());
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

        EventData<NodeResource> data = mock();

        given(event.getData()).willReturn(data);

        return data;
    }

    private static NodeResource.Builder nodeResourceWithRequiredFields()
    {
        return NodeResource.builder()
                .setName("some name")
                .setNodeType("cm:folder")
                .setCreatedAt(ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, UTC))
                .setModifiedAt(ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, UTC))
                .setCreatedByUser(new UserInfo("admin", "admin", "admin"))
                .setModifiedByUser(new UserInfo("admin", "admin", "admin"))
                .setAspectNames(Set.of("cm:auditable"));
    }

    private static Set<PropertyDelta<?>> mergeWithDefaultProperties(Set<PropertyDelta<?>> propertyDeltas)
    {
        Set<PropertyDelta<?>> defaultProperties = Set.of(
                updated(NAME_PROPERTY, "some name"),
                updated(TYPE_PROPERTY, "cm:folder"),
                updated(CREATED_AT_PROPERTY, EXPECTED_DATE_STRING),
                updated(CREATED_BY_PROPERTY, "admin"),
                updated(MODIFIED_AT_PROPERTY, EXPECTED_DATE_STRING),
                updated(MODIFIED_BY_PROPERTY, "admin"),
                updated(ASPECT_NAMES_PROPERTY, Set.of("cm:auditable")));

        Set<PropertyDelta<?>> mergedProperties = new HashSet<>(propertyDeltas);

        Set<String> propertyKeys = propertyDeltas.stream()
                .map(PropertyDelta::getPropertyName)
                .collect(Collectors.toSet());

        defaultProperties.stream()
                .filter(property -> !propertyKeys.contains(property.getPropertyName()))
                .forEach(mergedProperties::add);

        return mergedProperties;
    }
}
