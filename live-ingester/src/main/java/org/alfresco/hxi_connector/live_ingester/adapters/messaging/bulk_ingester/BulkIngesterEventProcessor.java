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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester.model.BulkIngesterEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkIngesterEventProcessor
{
    private final IngestMetadataCommandHandler ingestMetadataCommandHandler;

    public void process(BulkIngesterEvent event)
    {
        IngestMetadataCommand ingestMetadataCommand = new IngestMetadataCommand(
                event.createdAt(),
                event.nodeId(),
                false,
                PropertyDelta.updated(event.type()),
                PropertyDelta.updated(event.creatorId()),
                PropertyDelta.updated(event.modifierId()),
                PropertyDelta.updated(event.aspectNames()),
                PropertyDelta.updated(false),
                PropertyDelta.updated(false),
                PropertyDelta.updated(event.createdAt()),
                mapToCustomPropertiesDelta(event.customProperties()));

        ingestMetadataCommandHandler.handle(ingestMetadataCommand);
    }

    private Set<CustomPropertyDelta<?>> mapToCustomPropertiesDelta(Map<String, Serializable> properties)
    {
        return properties.entrySet()
                .stream()
                .map(property -> CustomPropertyDelta.updated(property.getKey(), property.getValue()))
                .collect(Collectors.toSet());
    }
}
