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

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateAncestorsPropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateAspectsDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateContentPropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateCreatedAtDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateCreatedByDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateModifiedAtDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateModifiedByDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateNamePropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculatePermissionsPropertyDelta;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.calculateTypeDelta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
public class PropertiesMapper
{
    private final AuthorityTypeResolver authorityTypeResolver;
    public Set<PropertyDelta<?>> mapToPropertyDeltas(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Stream<PropertyDelta<?>> customProperties = calculateCustomPropertiesDelta(event);

        List<Optional<PropertyDelta<?>>> knownProperties = List.of(
                calculateNamePropertyDelta(event),
                calculateTypeDelta(event),
                calculateCreatedByDelta(event),
                calculateModifiedAtDelta(event),
                calculateModifiedByDelta(event),
                calculateAspectsDelta(event),
                calculateCreatedAtDelta(event),
                calculateAncestorsPropertyDelta(event),
                calculatePermissionsPropertyDelta(event, authorityTypeResolver));

        return Stream.of(customProperties,
                knownProperties.stream().flatMap(Optional::stream),
                calculateContentPropertyDelta(event).stream())
                .flatMap(identity())
                .collect(Collectors.toSet());
    }

    private Stream<PropertyDelta<?>> calculateCustomPropertiesDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {

        return streamProperties(event.getData().getResource())
                .filter(property -> property.getValue() != null)
                .map(property -> PropertyDelta.updated(property.getKey(), property.getValue()));
    }

    private Stream<Map.Entry<String, ?>> streamProperties(NodeResource node)
    {
        return ofNullable(node)
                .map(NodeResource::getProperties)
                .stream()
                .flatMap(properties -> properties.entrySet().stream());
    }
}
