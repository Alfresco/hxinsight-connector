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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.FileUploader;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.StorageLocationRequester;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.model.IngestContentResponse;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class HttpHxInsightStorageClientTest
{
    private static final String FILE_CONTENT_TYPE = "plain/text";
    private static final String NODE_ID = "node-ref";
    private static final String CONTENT_ID = "CONTENT ID";

    @Mock
    StorageLocationRequester storageLocationRequesterMock;
    @Mock
    FileUploader fileUploaderMock;

    @InjectMocks
    HttpHxInsightStorageClient httpStorageClient;

    @Test
    void testUploadDataFromInputStream()
    {
        // given
        StorageLocationRequest storageLocationRequest = new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE);
        URL url = mock();
        PreSignedUrlResponse preSignedUrlResponse = new PreSignedUrlResponse(url, CONTENT_ID);
        given(storageLocationRequesterMock.requestStorageLocation(storageLocationRequest)).willReturn(preSignedUrlResponse);
        File testData = mock();

        // when
        IngestContentResponse ingestContentResponse = httpStorageClient.upload(testData, FILE_CONTENT_TYPE, NODE_ID);

        // then
        assertThat(ingestContentResponse).isEqualTo(new IngestContentResponse(url, CONTENT_ID, FILE_CONTENT_TYPE));
        StorageLocationRequest expectedStorageLocationRequest = new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE);
        then(storageLocationRequesterMock).should().requestStorageLocation(expectedStorageLocationRequest);
        FileUploadRequest expectedFileUploadRequest = new FileUploadRequest(testData, FILE_CONTENT_TYPE, url);
        then(fileUploaderMock).should().upload(expectedFileUploadRequest);
    }
}
