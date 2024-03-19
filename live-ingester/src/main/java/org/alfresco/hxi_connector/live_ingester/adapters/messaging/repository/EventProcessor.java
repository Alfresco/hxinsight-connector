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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static java.util.Optional.ofNullable;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.EventUtils.isEventTypeDeleted;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.EventUtils.isEventTypeUpdated;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.RepoEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.repo.event.v1.model.ContentInfo;
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

    public void process(RepoEvent<DataAttributes<NodeResource>> event)
    {
        handleMetadataPropertiesChange(event);
        handleContentChange(event);
        handleNodeDeleteEvent(event);
    }

    private void handleMetadataPropertiesChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event) || isEventTypeUpdated(event))
        {
            IngestNodeCommand ingestNodeCommand = repoEventMapper.mapToIngestNodeCommand(event);

            ingestNodeCommandHandler.handle(ingestNodeCommand);
        }
    }

    private void handleContentChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (containsNewContent(event))
        {
            IngestContentCommand command = repoEventMapper.mapToIngestContentCommand(event);

            ingestContentCommandHandler.handle(command);
        }
    }

    /**
     * We can determine if there is new content that needs processing by looking at the resourceBefore.content and resource.content fields.
     * <p>
     * For newly created nodes we have:
     * <ul>
     * <li>null -> zero bytes: No content
     * <li>null -> non-zero bytes: New content
     * </ul>
     * For updated nodes we have:
     * <ul>
     * <li>null -> zero bytes: No content
     * <li>null -> non-zero bytes: No change to content
     * <li>non-zero bytes -> zero bytes: Content deleted
     * <li>non-zero bytes -> non-zero bytes : Content updated
     * <li>zero bytes -> non-zero bytes : Content added (no content on node before)
     * </ul>
     */
    private boolean containsNewContent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Optional<ContentInfo> latestContentInfo = ofNullable(event.getData().getResource()).map(NodeResource::getContent);
        // If there's no content info in the current resource then the node cannot contain content.
        if (latestContentInfo.isEmpty())
        {
            return false;
        }
        boolean latestContentPresent = !latestContentInfo.get().getSizeInBytes().equals(0L);
        // If there is content on a new node then we should process it.
        if (isEventTypeCreated(event))
        {
            return latestContentPresent;
        }
        else if (isEventTypeUpdated(event))
        {
            Optional<ContentInfo> oldContentInfo = ofNullable(event.getData().getResourceBefore()).map(NodeResource::getContent);
            // We only need to process the content if it was mentioned in the resourceBefore _and_ is non-zero now.
            return oldContentInfo.isPresent() && latestContentPresent;
        }
        // For events other than create or update then we do not need to process the content.
        return false;
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
