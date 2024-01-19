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

import static org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventType.UPDATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.ASPECTS_NAMES;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.CREATED_AT;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.CREATED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.IS_FILE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.IS_FOLDER;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.MODIFIED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.NAME;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.PRIMARY_ASSOC_Q_NAME;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.TYPE;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventType;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@Component
@RequiredArgsConstructor
public class IngestMetadataCommandHandler
{
    private final EventPublisher eventPublisher;
    private final List<CustomPropertyResolver<?>> customPropertyResolvers;

    public void handle(IngestMetadataCommand command)
    {
        EventType eventType = command.isUpdate() ? UPDATE : CREATE;
        UpdateNodeMetadataEvent updateMetadataEvent = new UpdateNodeMetadataEvent(command.nodeId(), eventType);

        command.name().applyAs(NAME, updateMetadataEvent);
        command.primaryAssocQName().applyAs(PRIMARY_ASSOC_Q_NAME, updateMetadataEvent);
        command.nodeType().applyAs(TYPE, updateMetadataEvent);
        command.createdByUserWithId().applyAs(CREATED_BY_USER_WITH_ID, updateMetadataEvent);
        command.modifiedByUserWithId().applyAs(MODIFIED_BY_USER_WITH_ID, updateMetadataEvent);
        command.aspectNames().applyAs(ASPECTS_NAMES, updateMetadataEvent);
        command.isFile().applyAs(IS_FILE, updateMetadataEvent);
        command.isFolder().applyAs(IS_FOLDER, updateMetadataEvent);
        command.createdAt().applyAs(CREATED_AT, updateMetadataEvent);

        command.properties()
                .stream()
                .map(this::resolve)
                .flatMap(Optional::stream)
                .forEach(customPropertyDelta -> customPropertyDelta.applyOn(updateMetadataEvent));

        eventPublisher.publishMessage(updateMetadataEvent);
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
