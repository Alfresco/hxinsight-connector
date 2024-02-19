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

import java.net.URL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.FileUploader;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.StorageLocationRequester;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpStorageClient implements StorageClient
{

    private final StorageLocationRequester storageLocationRequester;
    private final FileUploader fileUploader;

    @Override
    public URL upload(File file, String contentType, String nodeId)
    {
        URL preSignedUrl = storageLocationRequester.requestStorageLocation(new StorageLocationRequest(nodeId, contentType));
        fileUploader.upload(new FileUploadRequest(file, contentType, preSignedUrl));
        return preSignedUrl;
    }
}
