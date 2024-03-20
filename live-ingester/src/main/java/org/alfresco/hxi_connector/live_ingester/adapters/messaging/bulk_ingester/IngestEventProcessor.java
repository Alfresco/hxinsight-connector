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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent.ContentInfo;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestEventProcessor
{
    private final IngestNodeCommandHandler ingestNodeCommandHandler;
    private final IngestContentCommandHandler ingestContentCommandHandler;

    public void process(@Validated IngestEvent ingestEvent)
    {
        Map<String, Serializable> properties = ingestEvent.properties().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        IngestNodeCommand ingestNodeCommand = new IngestNodeCommand(
                ingestEvent.nodeId(),
                CREATE,
                mapToPropertiesDelta(ingestEvent.contentInfo(), properties));

        ingestNodeCommandHandler.handle(ingestNodeCommand);

        if (ingestEvent.contentInfo() != null)
        {
            IngestContentCommand ingestContentCommand = new IngestContentCommand(ingestEvent.nodeId());

            ingestContentCommandHandler.handle(ingestContentCommand);
        }
    }

    private Set<PropertyDelta<?>> mapToPropertiesDelta(ContentInfo contentInfo, Map<String, Serializable> properties)
    {
        Stream<PropertyDelta<?>> metadataDelta = properties.entrySet()
                .stream()
                .map(property -> updated(property.getKey(), property.getValue()));
        if (contentInfo != null && (contentInfo.mimetype() != null || !Objects.equals(contentInfo.contentSize(), 0L)))
        {
            PropertyDelta<?> contentDelta = contentMetadataUpdated(CONTENT_PROPERTY, contentInfo.mimetype(), contentInfo.contentSize(), (String) properties.get(NAME_PROPERTY));
            metadataDelta = Stream.concat(metadataDelta, Stream.of(contentDelta));
        }
        return metadataDelta.collect(Collectors.toSet());
    }
}
