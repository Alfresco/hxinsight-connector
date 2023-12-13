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

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.NodeProperty;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

@Component
public class RepoEventMapper
{

    public IngestNewNodeEvent mapToIngestNewNodeEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        NodeResource node = event.getData().getResource();

        return new IngestNewNodeEvent(
                event.getTime().toInstant().toEpochMilli(),
                new Node(
                        node.getId(),
                        node.getName(),
                        node.getPrimaryAssocQName(),
                        node.getNodeType(),
                        node.getCreatedByUser().getId(),
                        node.getModifiedByUser().getId(),
                        node.getAspectNames(),
                        node.isFile(),
                        node.isFolder(),
                        node.getCreatedAt().toInstant().toEpochMilli(),
                        getNodeCustomProperties(node)));
    }

    public UpdateNodeEvent mapToUpdateNodeMetadataEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return new UpdateNodeEvent(
                event.getTime().toInstant().toEpochMilli(),
                calculateFieldDelta(event, NodeResource::getName),
                calculateFieldDelta(event, NodeResource::getPrimaryAssocQName),
                calculateFieldDelta(event, NodeResource::getNodeType),
                calculateFieldDelta(event, this::getModifiedByUserWithId),
                calculateFieldDelta(event, NodeResource::getAspectNames),
                calculateFieldDelta(event, NodeResource::isFile),
                calculateFieldDelta(event, NodeResource::isFolder),
                calculateFieldDelta(event, this::getNodeCustomProperties));
    }

    private String getModifiedByUserWithId(NodeResource node)
    {
        return Optional.ofNullable(node.getModifiedByUser())
                .map(UserInfo::getId)
                .orElse(null);
    }

    private Set<NodeProperty<?>> getNodeCustomProperties(NodeResource node)
    {
        if (node.getProperties() == null)
        {
            return Collections.emptySet();
        }

        return node.getProperties()
                .entrySet()
                .stream()
                .filter(property -> Objects.nonNull(property.getKey()) && Objects.nonNull(property.getValue()))
                .map(property -> new NodeProperty<>(property.getKey(), property.getValue()))
                .collect(Collectors.toSet());
    }

    private <T> UpdateNodeEvent.FieldDelta<T> calculateFieldDelta(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, T> fieldGetter)
    {
        if (wasFieldUpdated(event, fieldGetter))
        {
            return UpdateNodeEvent.FieldDelta.updated(
                    fieldGetter.apply(event.getData().getResource()),
                    fieldGetter.apply(event.getData().getResourceBefore()));
        }

        return UpdateNodeEvent.FieldDelta.notUpdated(
                fieldGetter.apply(event.getData().getResource()));
    }

    private boolean wasFieldUpdated(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, ?> fieldGetter)
    {
        return Optional.of(event.getData())
                .map(DataAttributes::getResourceBefore)
                .map(fieldGetter::apply)
                .isPresent();
    }
}
