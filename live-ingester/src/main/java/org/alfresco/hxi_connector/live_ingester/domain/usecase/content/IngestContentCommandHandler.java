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

package org.alfresco.hxi_connector.live_ingester.domain.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;

@Component
@RequiredArgsConstructor
public class IngestNewNodeEventHandler
{
    private final UpdateNodeEventMapper updateNodeEventMapper;
    private final EventPublisher eventPublisher;
    private final TransformRequestMapper transformRequestMapper;
    private final TransformRequester transformRequester;

    public void handle(IngestNewNodeEvent event)
    {
        UpdateNodeMetadataEvent updateMetadataEvent = updateNodeEventMapper.map(event);
        eventPublisher.publishMessage(updateMetadataEvent);

        if (event.node().contentMimeType().isPresent())
        {
            TransformRequest transformRequest = transformRequestMapper.map(event);
            transformRequester.requestTransform(transformRequest);
        }
    }
}
