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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestNodeCommandHandler
{
    private final IngestionEngineEventPublisher ingestionEngineEventPublisher;
    private final List<PropertyResolver<?>> propertyResolvers;
    private final IntegrationProperties integrationProperties;

    public void handle(IngestNodeCommand command)
    {
        EventType eventType = command.eventType();
        ensureThat(eventType != DELETE, "Cannot ingest metadata for DELETE event - nodeId %s", command.nodeId());
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(
                command.nodeId(),
                eventType,
                integrationProperties.application().getSourceId(),
                command.sourceTimestamp());

        command.properties()
                .stream()
                .map(this::resolve)
                .flatMap(Optional::stream)
                .forEach(propertyDelta -> propertyDelta.applyOn(updateNodeEvent));

        if (updateNodeEvent.getEventType() == UPDATE
                && updateNodeEvent.getContentPropertiesToSet().isEmpty()
                && updateNodeEvent.getMetadataPropertiesToSet().isEmpty()
                && updateNodeEvent.getPropertiesToUnset().isEmpty())
        {
            log.debug("Ignoring empty metadata update: {}", updateNodeEvent);
            return;
        }
        ingestionEngineEventPublisher.publishMessage(updateNodeEvent);
    }

    private Optional<PropertyDelta<?>> resolve(PropertyDelta<?> propertyDelta)
    {
        Optional<PropertyDelta<?>> resolvedPropertyDelta = Optional.of(propertyDelta);

        for (PropertyResolver<?> propertyResolver : propertyResolvers)
        {
            resolvedPropertyDelta = resolvedPropertyDelta.flatMap(
                    delta -> delta.canBeResolvedWith(propertyResolver) ? delta.resolveWith(propertyResolver) : Optional.of(delta));
        }

        return resolvedPropertyDelta;
    }
}
