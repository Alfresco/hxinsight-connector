/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.LiveIngestService;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.IngestionEngineStorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.model.IngestContentResponse;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpHxInsightStorageClient implements IngestionEngineStorageClient
{
    private final LiveIngestService ingestService;

    @Override
    public IngestContentResponse upload(File file, String contentType, String nodeId)
    {
        try (InputStream inputStream = file.data())
        {

            CICBlob blob = new CICBlob() {
                @Override
                public InputStream getInputStream()
                {
                    try
                    {
                        return new ByteArrayInputStream(inputStream.readAllBytes());
                    }
                    catch (IOException e)
                    {
                        throw new LiveIngesterRuntimeException("Failed to read content for node: " + nodeId, e);
                    }
                }

                @Override
                public Optional<String> getDigest()
                {
                    return Optional.empty();
                }
            };
            return ingestService.uploadBlobIfNeeded(nodeId, blob)
                    .map(preSignedUrl -> {
                        log.atInfo().log("Storage :: Content of type: {} for node: {} successfully uploaded using pre-signed URL", contentType, nodeId);
                        return new IngestContentResponse(preSignedUrl.id(), contentType);
                    })
                    .orElseGet(() -> {
                        log.atInfo().log("Storage :: Content of type: {} for node: {} already exists, no upload needed", contentType, nodeId);
                        return new IngestContentResponse(null, contentType);
                    });
        }
        catch (IOException e)
        {
            throw new LiveIngesterRuntimeException("Failed to read content for node: " + nodeId, e);
        }

    }
}
