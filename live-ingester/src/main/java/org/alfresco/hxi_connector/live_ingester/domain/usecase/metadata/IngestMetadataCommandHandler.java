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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@Component
@RequiredArgsConstructor
public class IngestMetadataCommandHandler
{
    private final IngestionEngineEventPublisher ingestionEngineEventPublisher;
    private final List<CustomPropertyResolver<?>> customPropertyResolvers;

    public void handle(IngestMetadataCommand command)
    {
        EventType eventType = command.eventType();
        UpdateNodeMetadataEvent updateMetadataEvent = new UpdateNodeMetadataEvent(command.nodeId(), eventType);

        command.properties()
                .stream()
                .map(this::resolve)
                .flatMap(Optional::stream)
                .forEach(customPropertyDelta -> customPropertyDelta.applyOn(updateMetadataEvent));

        ingestionEngineEventPublisher.publishMessage(updateMetadataEvent);
    }

    private Optional<CustomPropertyDelta<?>> resolve(CustomPropertyDelta<?> customPropertyDelta)
    {
        Optional<CustomPropertyDelta<?>> resolvedPropertyDelta = Optional.of(customPropertyDelta);

        for (CustomPropertyResolver<?> propertyResolver : customPropertyResolvers)
        {
            resolvedPropertyDelta = resolvedPropertyDelta.flatMap(
                    propertyDelta -> propertyDelta.canBeResolvedWith(propertyResolver) ? propertyDelta.resolveWith(propertyResolver) : Optional.of(propertyDelta));
        }

        return resolvedPropertyDelta;
    }
}
