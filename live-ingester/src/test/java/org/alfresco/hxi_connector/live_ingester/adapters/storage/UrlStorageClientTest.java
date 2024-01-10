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

import static java.nio.file.StandardOpenOption.APPEND;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;

import lombok.Cleanup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.FileUploader;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequester;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class UrlStorageClientTest
{
    private static final String FILE_CONTENT = "some test content";
    private static final String FILE_CONTENT_TYPE = "plain/text";
    private static final String NODE_ID = "node-ref";

    @Mock
    StorageLocationRequester storageLocationRequesterMock;
    @Mock
    FileUploader fileUploaderMock;

    @InjectMocks
    UrlStorageClient urlStorageClient;

    @Test
    void testUploadDataFromFile() throws IOException
    {
        // given
        URL url = mock(URL.class);
        File testFile = Files.createTempFile("test", ".txt").toFile();
        Files.write(testFile.toPath(), FILE_CONTENT.getBytes(), APPEND);
        given(storageLocationRequesterMock.requestStorageLocation(any())).willReturn(url);

        // when
        urlStorageClient.upload(testFile, FILE_CONTENT_TYPE, NODE_ID);

        // then
        StorageLocationRequest expectedStorageLocationRequest = new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE);
        then(storageLocationRequesterMock).should().requestStorageLocation(expectedStorageLocationRequest);

        ArgumentCaptor<FileUploadRequest> fileUploadRequestCaptor = ArgumentCaptor.forClass(FileUploadRequest.class);
        then(fileUploaderMock).should().upload(fileUploadRequestCaptor.capture());
        FileUploadRequest actualFileUploadRequest = fileUploadRequestCaptor.getValue();
        assertThat(actualFileUploadRequest).satisfies(request -> {
            assertThat(request.contentType()).isEqualTo(FILE_CONTENT_TYPE);
            assertThat(request.storageLocation()).isEqualTo(url);
            assertThatExceptionOfType(ClosedChannelException.class).isThrownBy(() -> request.inputStream().read());
        });
    }

    @Test
    void testUploadDataFromInputStream() throws IOException
    {
        // given
        @Cleanup
        InputStream testData = mock(InputStream.class);
        URL url = mock(URL.class);
        given(storageLocationRequesterMock.requestStorageLocation(any())).willReturn(url);

        // when
        urlStorageClient.upload(testData, FILE_CONTENT_TYPE, NODE_ID);

        // then
        StorageLocationRequest expectedStorageLocationRequest = new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE);
        then(storageLocationRequesterMock).should().requestStorageLocation(expectedStorageLocationRequest);
        FileUploadRequest expectedFileUploadRequest = new FileUploadRequest(testData, FILE_CONTENT_TYPE, url);
        then(fileUploaderMock).should().upload(expectedFileUploadRequest);
    }
}
