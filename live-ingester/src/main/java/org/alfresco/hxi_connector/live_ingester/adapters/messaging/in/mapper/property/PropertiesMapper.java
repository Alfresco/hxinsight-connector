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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.mapper.property;

import static java.util.Optional.ofNullable;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.utils.EventUtils.isEventTypeCreated;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
public class PropertiesMapper
{
    private static final String NAME_PROPERTY_KEY = "cm:name";

    public <T> PropertyDelta<T> calculatePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, T> fieldGetter)
    {
        if (shouldNotUpdateField(event, fieldGetter))
        {
            return PropertyDelta.unchanged(fieldGetter.apply(event.getData().getResource()));
        }

        return PropertyDelta.updated(fieldGetter.apply(event.getData().getResource()));
    }

    public Set<CustomPropertyDelta<?>> calculateCustomPropertiesDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event))
        {
            return allCustomPropertiesUpdated(event);
        }

        return someCustomPropertiesUpdated(event);
    }

    private Set<CustomPropertyDelta<?>> allCustomPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Stream<CustomPropertyDelta<?>> properties = customPropertiesStream(event.getData().getResource())
                .filter(property -> Objects.nonNull(property.getValue()))
                .map(property -> CustomPropertyDelta.updated(property.getKey(), property.getValue()));

        Stream<CustomPropertyDelta<?>> nameProperty = calculateNamePropertyDelta(event);

        return Stream.concat(properties, nameProperty)
                .collect(Collectors.toSet());
    }

    private Set<CustomPropertyDelta<?>> someCustomPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return Stream.concat(
                calculatePropertiesDelta(event),
                calculateNamePropertyDelta(event)).collect(Collectors.toSet());
    }

    private Stream<CustomPropertyDelta<?>> calculatePropertiesDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isFieldUnchanged(event, NodeResource::getProperties))
        {
            return Stream.empty();
        }

        return customPropertiesStream(event.getData().getResourceBefore())
                .map(oldPropertyEntry -> toCustomPropertyDelta(event.getData(), oldPropertyEntry));
    }

    private Stream<CustomPropertyDelta<?>> calculateNamePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (shouldNotUpdateField(event, NodeResource::getName))
        {
            return Stream.empty();
        }

        return Optional.ofNullable(event.getData().getResource().getName())
                .stream()
                .filter(Objects::nonNull)
                .map(name -> CustomPropertyDelta.updated(NAME_PROPERTY_KEY, name));
    }

    private boolean shouldNotUpdateField(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, ?> fieldGetter)
    {
        return !isEventTypeCreated(event) && isFieldUnchanged(event, fieldGetter);
    }

    private boolean isFieldUnchanged(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, ?> fieldGetter)
    {
        return Optional.of(event.getData())
                .map(DataAttributes::getResourceBefore)
                .map(fieldGetter::apply)
                .isEmpty();
    }

    private CustomPropertyDelta<?> toCustomPropertyDelta(DataAttributes<NodeResource> eventData, Map.Entry<String, ?> oldPropertyEntry)
    {
        String changedPropertyName = oldPropertyEntry.getKey();
        Object oldValue = oldPropertyEntry.getValue();
        Object propertyValue = eventData.getResource().getProperties().get(changedPropertyName);

        if (Objects.equals(propertyValue, oldValue))
        {
            return CustomPropertyDelta.unchanged(changedPropertyName);
        }
        if (propertyValue == null)
        {
            return CustomPropertyDelta.deleted(changedPropertyName);
        }
        return CustomPropertyDelta.updated(changedPropertyName, propertyValue);
    }

    private Stream<Map.Entry<String, ?>> customPropertiesStream(NodeResource node)
    {
        return ofNullable(node)
                .stream()
                .map(NodeResource::getProperties)
                .flatMap(properties -> properties.entrySet().stream());
    }
}
