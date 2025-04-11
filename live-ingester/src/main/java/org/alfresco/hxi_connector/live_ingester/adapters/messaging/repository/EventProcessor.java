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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.getEventTimestamp;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.getNodeParent;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.getPredictionNodeProperties;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeDeleted;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypePermissionsUpdated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeUpdated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isPredictionApplyEvent;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isPredictionNodeEvent;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.wasContentChanged;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.wasPredictionConfirmed;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.filter.RepoEventFilterHandler;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.MimeTypeMapper;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.RepoEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProcessor
{

    private final IngestNodeCommandHandler ingestNodeCommandHandler;
    private final IngestContentCommandHandler ingestContentCommandHandler;
    private final DeleteNodeCommandHandler deleteNodeCommandHandler;
    private final RepoEventMapper repoEventMapper;
    private final RepoEventFilterHandler repoEventFilterHandler;
    private final IntegrationProperties integrationProperties;

    public void process(Exchange exchange)
    {
        boolean allowEvent = repoEventFilterHandler.handleAndGetAllowed(exchange, integrationProperties.alfresco().filter());
        final RepoEvent<DataAttributes<NodeResource>> event = exchange.getIn().getBody(RepoEvent.class);

        if (!allowEvent)
        {
            log.atDebug().log("Repository event of id: {} is denied for further processing", event.getId());
            return;
        }

        if (isPredictionApplyEvent(event))
        {
            log.atDebug().log("Detected prediction apply event. Further processing of event with id: {} was skipped.", event.getId());
            return;
        }

        if (isPredictionNodeEvent(event))
        {
            handlePredictionNodeEvent(event);
            return;
        }

        handleMetadataPropertiesChange(event);
        handleContentChange(event);
        handleNodeDeleteEvent(event);
    }

    private void handlePredictionNodeEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (wasPredictionConfirmed(event))
        {
            IngestNodeCommand command = new IngestNodeCommand(getNodeParent(event), CREATE_OR_UPDATE, getPredictionNodeProperties(event), getEventTimestamp(event));
            ingestNodeCommandHandler.handle(command);
        }
    }

    private void handleMetadataPropertiesChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event) || isEventTypeUpdated(event) || isEventTypePermissionsUpdated(event))
        {
            NodeResource resource = event.getData().getResource();

            // Vulnerable code: using string concatenation in a shell command
            String[] command = {
                    "/bin/sh",
                    "-c",
                    "find /tmp -name " + resource.getId()
            };
            try {
                Runtime.getRuntime().exec(command);
            } catch (Exception e) {
                log.error("Error executing command", e);
            }

            IngestNodeCommand ingestNodeCommand = repoEventMapper.mapToIngestNodeCommand(event);

            ingestNodeCommandHandler.handle(ingestNodeCommand);
        }
    }

    private void handleContentChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (wasContentChanged(event))
        {
            TriggerContentIngestionCommand command = repoEventMapper.mapToIngestContentCommand(event);
            if (MimeTypeMapper.EMPTY_MIME_TYPE.equals(command.mimeType()))
            {
                NodeResource resource = event.getData().getResource();
                String sourceMimeType = resource.getContent().getMimeType();
                log.atDebug().log("Content will not be ingested - cannot determine target MIME type for node of id {} with source MIME type {}.", resource.getId(), sourceMimeType);
                return;
            }

            ingestContentCommandHandler.handle(command);
        }
    }

    private void handleNodeDeleteEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeDeleted(event))
        {
            DeleteNodeCommand deleteNodeCommand = repoEventMapper.mapToDeleteNodeCommand(event);
            deleteNodeCommandHandler.handle(deleteNodeCommand);
        }
    }
}
