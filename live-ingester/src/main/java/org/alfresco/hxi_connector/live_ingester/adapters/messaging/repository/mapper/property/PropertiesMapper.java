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

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateAllowAccessDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateAspectsDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateContentPropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateCreatedAtDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateCreatedByDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateDenyAccessDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateModifiedByDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateNamePropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateTypeDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.isFieldUnchanged;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypePermissionsUpdated;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
public class PropertiesMapper
{
    public Set<PropertyDelta<?>> mapToPropertyDeltas(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event))
        {
            return allPropertiesUpdated(event);
        }

        if (isEventTypePermissionsUpdated(event))
        {
            return permissionPropertiesUpdated(event);
        }

        return somePropertiesUpdated(event);
    }

    private Set<PropertyDelta<?>> allPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Stream<PropertyDelta<?>> propertyDeltas = streamProperties(event.getData().getResource())
                .filter(property -> Objects.nonNull(property.getValue()))
                .map(property -> PropertyDelta.updated(property.getKey(), property.getValue()));
        return createSetOfAllProperties(event, propertyDeltas);
    }

    private Set<PropertyDelta<?>> permissionPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return Stream.concat(
                calculateAllowAccessDelta(event),
                calculateDenyAccessDelta(event)).collect(Collectors.toSet());
    }

    private Set<PropertyDelta<?>> somePropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Stream<PropertyDelta<?>> propertyDeltas = calculatePropertiesDelta(event);
        return createSetOfAllProperties(event, propertyDeltas);
    }

    private Set<PropertyDelta<?>> createSetOfAllProperties(RepoEvent<DataAttributes<NodeResource>> event, Stream<PropertyDelta<?>> propertyDeltas)
    {
        return Stream.of(propertyDeltas,
                calculateNamePropertyDelta(event),
                calculateContentPropertyDelta(event),
                calculateTypeDelta(event),
                calculateCreatedByDelta(event),
                calculateModifiedByDelta(event),
                calculateAspectsDelta(event),
                calculateCreatedAtDelta(event),
                calculateAllowAccessDelta(event),
                calculateDenyAccessDelta(event))
                .flatMap(identity())
                .collect(Collectors.toSet());
    }

    private Stream<PropertyDelta<?>> calculatePropertiesDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isFieldUnchanged(event, NodeResource::getProperties))
        {
            return Stream.empty();
        }

        return streamProperties(event.getData().getResourceBefore())
                .map(oldPropertyEntry -> toPropertyDelta(event.getData(), oldPropertyEntry));
    }

    private PropertyDelta<?> toPropertyDelta(DataAttributes<NodeResource> eventData, Map.Entry<String, ?> oldPropertyEntry)
    {
        String changedPropertyName = oldPropertyEntry.getKey();
        Object oldValue = oldPropertyEntry.getValue();
        Object propertyValue = eventData.getResource().getProperties().get(changedPropertyName);

        if (Objects.equals(propertyValue, oldValue))
        {
            return PropertyDelta.unchanged(changedPropertyName);
        }
        if (propertyValue == null)
        {
            return PropertyDelta.deleted(changedPropertyName);
        }
        return PropertyDelta.updated(changedPropertyName, propertyValue);
    }

    private Stream<Map.Entry<String, ?>> streamProperties(NodeResource node)
    {
        return ofNullable(node)
                .map(NodeResource::getProperties)
                .stream()
                .flatMap(properties -> properties.entrySet().stream());
    }
}
