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

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;
import static org.alfresco.hxi_connector.live_ingester.messaging.in.utils.EventUtils.isEventTypeCreated;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Slf4j
@Component
public class RepoEventMapper
{

    public IngestContentCommand mapToIngestContentCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return new IngestContentCommand(
                event.getTime().toInstant().toEpochMilli(),
                event.getData().getResource().getId());
    }

    public IngestMetadataCommand mapToIngestMetadataCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        ensureThat(isEventTypeCreated(event), "Unsupported event type");

        return new IngestMetadataCommand(
                event.getTime().toInstant().toEpochMilli(),
                event.getData().getResource().getId(),
                updated(event.getData().getResource().getName()),
                updated(event.getData().getResource().getPrimaryAssocQName()),
                updated(event.getData().getResource().getNodeType()),
                updated(event.getData().getResource().getCreatedByUser().getId()),
                updated(event.getData().getResource().getModifiedByUser().getId()),
                updated(event.getData().getResource().getAspectNames()),
                updated(event.getData().getResource().isFile()),
                updated(event.getData().getResource().isFolder()),
                updated(event.getData().getResource().getCreatedAt().toInstant().toEpochMilli()),
                allCustomPropertiesUpdated(event));
    }

    private Set<CustomPropertyDelta<?>> allCustomPropertiesUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return Optional.ofNullable(event.getData().getResource())
                .map(NodeResource::getProperties)
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream)
                .filter(property -> Objects.nonNull(property.getValue()))
                .map(property -> CustomPropertyDelta.updated(property.getKey(), property.getValue()))
                .collect(Collectors.toSet());
    }
}
