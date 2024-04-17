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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.getEventType;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeDeleted;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.hxi_connector.live_ingester.domain.util.EnsureUtils.ensureThat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertiesMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
public class RepoEventMapper
{
    private final PropertiesMapper propertiesMapper;
    private final MimeTypeMapper mimeTypeMapper;

    public TriggerContentIngestionCommand mapToIngestContentCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        ensureThat(isEventTypeCreated(event) || isEventTypeUpdated(event), "Unsupported event type");

        final NodeResource resource = event.getData().getResource();
        String mimeType = mimeTypeMapper.mapMimeType(resource.getContent().getMimeType());
        return new TriggerContentIngestionCommand(resource.getId(), mimeType);
    }

    public IngestNodeCommand mapToIngestNodeCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        EventType eventType = getEventType(event);
        ensureThat(eventType == CREATE || eventType == UPDATE, "Unsupported event type");

        return new IngestNodeCommand(
                event.getData().getResource().getId(),
                eventType,
                propertiesMapper.mapToPropertyDeltas(event));
    }

    public DeleteNodeCommand mapToDeleteNodeCommand(RepoEvent<DataAttributes<NodeResource>> event)
    {
        ensureThat(isEventTypeDeleted(event), "Only delete events can be converted to delete commands");
        return new DeleteNodeCommand(event.getData().getResource().getId());
    }
}
