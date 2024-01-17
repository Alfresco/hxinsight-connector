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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.in;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.utils.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.utils.EventUtils.isEventTypeUpdated;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.mapper.RepoEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommandHandler;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProcessor
{

    private final IngestMetadataCommandHandler ingestMetadataCommandHandler;

    private final TriggerContentIngestionCommandHandler triggerContentIngestionCommandHandler;

    private final RepoEventMapper repoEventMapper;

    public void process(RepoEvent<DataAttributes<NodeResource>> event)
    {
        handleMetadataPropertiesChange(event);
        handleContentChange(event);
    }

    private void handleMetadataPropertiesChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event) || isEventTypeUpdated(event))
        {
            IngestMetadataCommand ingestMetadataCommand = repoEventMapper.mapToIngestMetadataCommand(event);

            ingestMetadataCommandHandler.handle(ingestMetadataCommand);
        }
    }

    private void handleContentChange(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event) && containsContent(event))
        {
            TriggerContentIngestionCommand command = repoEventMapper.mapToIngestContentCommand(event);

            triggerContentIngestionCommandHandler.handle(command);
        }
    }

    private boolean containsContent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return Optional.ofNullable(event.getData().getResource())
                .map(NodeResource::getContent)
                .isPresent();
    }
}
