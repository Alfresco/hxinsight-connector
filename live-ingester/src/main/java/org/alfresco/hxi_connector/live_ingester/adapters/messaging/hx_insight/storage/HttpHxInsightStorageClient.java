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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.FileUploader;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.StorageLocationRequester;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.IngestionEngineStorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.model.IngestContentResponse;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpHxInsightStorageClient implements IngestionEngineStorageClient
{

    private final StorageLocationRequester storageLocationRequester;
    private final FileUploader fileUploader;

    @Override
    public IngestContentResponse upload(File file, String contentType, String nodeId)
    {
        PreSignedUrlResponse preSignedUrlResponse = storageLocationRequester.requestStorageLocation(new StorageLocationRequest(nodeId, contentType));
        log.atDebug().log("Storage :: Received target location with transfer ID: {} for node: {}", preSignedUrlResponse.id(), nodeId);
        URL preSignedUrl = preSignedUrlResponse.url();
        try (InputStream fileData = file.data())
        {
            log.atDebug().log("Upload :: Transferring to S3 content of node: {} with size of {} bytes", nodeId, fileData.available());
            fileUploader.upload(new FileUploadRequest(new File(fileData), contentType, preSignedUrl));
        }
        catch (IOException e)
        {
            throw new LiveIngesterRuntimeException(e);
        }

        return new IngestContentResponse(preSignedUrlResponse.id(), contentType);
    }
}
