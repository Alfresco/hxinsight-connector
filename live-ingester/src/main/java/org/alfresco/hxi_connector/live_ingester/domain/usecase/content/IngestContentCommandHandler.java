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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.content;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentPropertyUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.IngestionEngineStorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.model.IngestContentResponse;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.EmptyRenditionException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestContentCommandHandler
{
    private static final String PDF_MIMETYPE = "application/pdf";

    private final TransformRequester transformRequester;
    private final IngestNodeCommandHandler ingestNodeCommandHandler;
    private final TransformEngineFileStorage transformEngineFileStorage;
    private final IngestionEngineStorageClient ingestionEngineStorageClient;

    public void handle(TriggerContentIngestionCommand command)
    {
        TransformRequest transformRequest = new TransformRequest(command.nodeId(), command.mimeType());
        transformRequester.requestTransform(transformRequest);
    }

    public void handle(IngestContentCommand command)
    {
        String fileId = command.transformedFileId();
        String nodeId = command.nodeId();
        File downloadedFile = transformEngineFileStorage.downloadFile(fileId);

        ensureThat(!downloadedFile.isEmpty(), () -> new EmptyRenditionException(nodeId));

        log.atDebug().log("Transform :: Downloaded from SFS content of node: {} in file with ID: {}", nodeId, fileId);

        IngestContentResponse ingestContentResponse = ingestionEngineStorageClient.upload(downloadedFile, PDF_MIMETYPE, nodeId);

        Set<PropertyDelta<?>> properties = Set.of(
                contentPropertyUpdated(CONTENT_PROPERTY, ingestContentResponse.transferId(), ingestContentResponse.mimeType()));
        IngestNodeCommand ingestNodeCommand = new IngestNodeCommand(nodeId, UPDATE, properties);
        log.atDebug().log("Ingestion :: Notifying about node: {} content upload within transfer with ID: {}", nodeId, ingestContentResponse.transferId());
        ingestNodeCommandHandler.handle(ingestNodeCommand);
    }
}
