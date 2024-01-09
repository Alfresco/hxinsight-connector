/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.FileUploader;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequester;

@Component
@RequiredArgsConstructor
@Slf4j
public class UrlStorageClient implements StorageClient
{

    private final StorageLocationRequester storageLocationRequester;
    private final FileUploader fileUploader;

    @Override
    public void upload(File file, String contentType, String nodeId)
    {
        try (InputStream fileInputStream = Files.newInputStream(file.toPath()))
        {
            this.upload(fileInputStream, contentType, nodeId);
        }
        catch (IOException e)
        {
            throw new LiveIngesterRuntimeException("Accessing file with name: " + file.getName() + " failed", e);
        }
    }

    @Override
    public void upload(InputStream inputStream, String contentType, String nodeId)
    {
        URL preSignedUrl = storageLocationRequester.requestStorageLocation(new StorageLocationRequest(nodeId, contentType));
        fileUploader.upload(new FileUploadRequest(inputStream, contentType, preSignedUrl));
    }
}
