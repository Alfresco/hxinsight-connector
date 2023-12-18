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

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.repo.event.v1.model.ContentInfo;
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

    public IngestNewNodeEvent mapToIngestNewNodeEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        log.info("Creating node metadata properties {}", event);

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
                        ofNullable(node.getContent()).map(ContentInfo::getMimeType),
                        node.getAspectNames(),
                        node.isFile(),
                        node.isFolder(),
                        node.getCreatedAt().toInstant().toEpochMilli(),
                        node.getProperties()
                                .entrySet()
                                .stream()
                                .filter(property -> Objects.nonNull(property.getKey()) && Objects.nonNull(property.getValue()))
                                .map(property -> new NodeProperty<>(property.getKey(), property.getValue()))
                                .collect(Collectors.toSet())));
    }
}
