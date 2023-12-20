package org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.RepoEventTestUtils.setNodeResource;
import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.RepoEventTestUtils.setNodeResourceBefore;
import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.RepoEventTestUtils.setType;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.mapWith;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.property.resolver.CustomPropertyResolver;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

class PropertiesMapperTest
{
    private static final String NODE_NAME = "test-name";
    List<CustomPropertyResolver<?>> customPropertyResolvers = Collections.emptyList();
    PropertiesMapper propertiesMapper = new PropertiesMapper(customPropertyResolvers);

    @Test
    void shouldHandlePropertyUpdate_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setName(NODE_NAME)
                .build();

        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getName);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.updated(NODE_NAME);

        assertEquals(expectedNameDelta, nameDelta);
    }

    @Test
    void shouldHandlePropertyUpdate_NodeUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_UPDATED);

        NodeResource nodeResourceBefore = NodeResource.builder()
                .setName("previous name")
                .build();
        NodeResource nodeResource = NodeResource.builder()
                .setName(NODE_NAME)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getName);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.updated(NODE_NAME);

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
                .setName(NODE_NAME)
                .build();

        setNodeResourceBefore(event, nodeResourceBefore);
        setNodeResource(event, nodeResource);

        // when
        PropertyDelta<String> nameDelta = propertiesMapper.calculatePropertyDelta(event, NodeResource::getName);

        // then
        PropertyDelta<String> expectedNameDelta = PropertyDelta.unchanged(NODE_NAME);

        assertEquals(expectedNameDelta, nameDelta);
    }

    @Test
    void shouldHandleAllCustomPropertiesUpdated_NodeCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        setType(event, NODE_CREATED);

        NodeResource nodeResource = NodeResource.builder()
                .setProperties(mapWith("cm:title", "some title", "cm:description", "some description"))
                .build();

        setNodeResource(event, nodeResource);

        // when
        Set<CustomPropertyDelta<?>> customPropertyDeltas = propertiesMapper.calculateCustomPropertiesDelta(event);

        // then
        Set<CustomPropertyDelta<?>> expectedPropertyDeltas = Set.of(
                CustomPropertyDelta.updated("cm:title", "some title"),
                CustomPropertyDelta.updated("cm:description", "some description"));

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

}
