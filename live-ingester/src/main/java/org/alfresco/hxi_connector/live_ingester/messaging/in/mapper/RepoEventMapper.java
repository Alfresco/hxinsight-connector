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

package org.alfresco.hxi_connector.live_ingester.messaging.in.mapper;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;
import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.EventUtils.isEventTypeUpdated;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

@Slf4j
@Component
public class RepoEventMapper
{

    public IngestContentCommand mapToIngestContentCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        ensureThat(isEventTypeCreated(event), "Unsupported event type");

        return new IngestContentCommand(
                event.getTime().toInstant().toEpochMilli(),
                event.getData().getResource().getId());
    }

    public IngestMetadataCommand mapToIngestMetadataCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        ensureThat(isEventTypeCreated(event) || isEventTypeUpdated(event), "Unsupported event type");

        return new IngestMetadataCommand(
                toMilliseconds(event.getTime()),
                event.getData().getResource().getId(),
                calculatePropertyDelta(event, NodeResource::getName),
                calculatePropertyDelta(event, NodeResource::getPrimaryAssocQName),
                calculatePropertyDelta(event, NodeResource::getNodeType),
                calculatePropertyDelta(event, node -> getUserId(node, NodeResource::getCreatedByUser)),
                calculatePropertyDelta(event, node -> getUserId(node, NodeResource::getModifiedByUser)),
                calculatePropertyDelta(event, NodeResource::getAspectNames),
                calculatePropertyDelta(event, NodeResource::isFile),
                calculatePropertyDelta(event, NodeResource::isFolder),
                calculatePropertyDelta(event, node -> toMilliseconds(node.getCreatedAt())),
                calculateCustomPropertiesDelta(event));
    }

    private Long toMilliseconds(ZonedDateTime time)
    {
        return time == null ? null : time.toInstant().toEpochMilli();
    }

    private String getUserId(NodeResource node, Function<NodeResource, UserInfo> userInfoGetter)
    {
        return ofNullable(node)
                .map(userInfoGetter)
                .map(UserInfo::getId)
                .orElse(null);
    }

    private <T> PropertyDelta<T> calculatePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, T> fieldGetter)
    {
        if (shouldNotUpdateField(event, fieldGetter))
        {
            return PropertyDelta.unchanged(fieldGetter.apply(event.getData().getResource()));
        }

        return PropertyDelta.updated(fieldGetter.apply(event.getData().getResource()));
    }

    private Set<CustomPropertyDelta<?>> calculateCustomPropertiesDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (shouldNotUpdateField(event, NodeResource::getProperties))
        {
            return emptySet();
        }

        if (isEventTypeCreated(event))
        {
            return allCustomPropertiesUpdated(event);
        }

        return someCustomPropertiesUpdated(event);
    }

    private Set<CustomPropertyDelta<?>> someCustomPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return customPropertiesStream(event.getData().getResourceBefore())
                .map(Map.Entry::getKey)
                .map(changedPropertyName -> toCustomPropertyDelta(event.getData(), changedPropertyName))
                .collect(Collectors.toSet());
    }

    private CustomPropertyDelta<?> toCustomPropertyDelta(DataAttributes<NodeResource> event, String changedPropertyName)
    {
        Serializable propertyValue = event.getResource().getProperties().get(changedPropertyName);

        return propertyValue == null ? CustomPropertyDelta.deleted(changedPropertyName) : CustomPropertyDelta.updated(changedPropertyName, propertyValue);
    }

    private Set<CustomPropertyDelta<?>> allCustomPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return customPropertiesStream(event.getData().getResource())
                .filter(property -> Objects.nonNull(property.getValue()))
                .map(property -> CustomPropertyDelta.updated(property.getKey(), property.getValue()))
                .collect(Collectors.toSet());
    }

    private Stream<Map.Entry<String, ?>> customPropertiesStream(NodeResource node)
    {
        return Optional.ofNullable(node)
                .map(NodeResource::getProperties)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream);
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

}
