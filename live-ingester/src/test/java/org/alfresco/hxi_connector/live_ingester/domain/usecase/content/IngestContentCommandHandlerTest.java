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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;

import java.net.URL;
import java.util.Set;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.IngestionEngineStorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.storage.model.IngestContentResponse;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.TransformationFailedException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.ContentPropertyUpdated;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class IngestContentCommandHandlerTest
{
    static final int TRANSFORMATION_STATUS = 200;
    static final String NODE_ID = "12341234-1234-1234-1234-123412341234";
    static final String FILE_ID = "43214321-4321-4321-4321-432143214321";
    static final String UPLOADED_CONTENT_ID = "11112222-4321-4321-4321-333343214444";
    static final String PDF_MIMETYPE = "application/pdf";

    @Mock
    TransformRequester transformRequesterMock;
    @Mock
    TransformEngineFileStorage transformEngineFileStorageMock;
    @Mock
    IngestionEngineStorageClient storageClientMock;
    @Mock
    IngestNodeCommandHandler ingestNodeCommandHandler;

    @InjectMocks
    IngestContentCommandHandler ingestContentCommandHandler;

    @Test
    void shouldRequestNodeContentTransformation()
    {
        // given
        TriggerContentIngestionCommand command = new TriggerContentIngestionCommand(NODE_ID);

        // when
        ingestContentCommandHandler.handle(command);

        // then
        TransformRequest expectedTransformationRequest = new TransformRequest(NODE_ID, PDF_MIMETYPE);
        then(transformRequesterMock).should().requestTransform(expectedTransformationRequest);
    }

    @Test
    @SneakyThrows
    void shouldSendTransformedContentToIngestionEngine()
    {
        // given
        IngestContentCommand command = new IngestContentCommand(TRANSFORMATION_STATUS, FILE_ID, NODE_ID);

        File fileToUpload = mock();
        given(transformEngineFileStorageMock.downloadFile(FILE_ID)).willReturn(fileToUpload);
        IngestContentResponse ingestContentResponse = new IngestContentResponse(
                new URL("https://test_url.com"),
                UPLOADED_CONTENT_ID,
                PDF_MIMETYPE);
        given(storageClientMock.upload(fileToUpload, PDF_MIMETYPE, NODE_ID)).willReturn(ingestContentResponse);

        // when
        ingestContentCommandHandler.handle(command);

        // then
        IngestNodeCommand expectedIngestNodeCommand = new IngestNodeCommand(
                NODE_ID,
                EventType.UPDATE,
                Set.of(ContentPropertyUpdated.builder(CONTENT_PROPERTY).id(UPLOADED_CONTENT_ID).mimeType(PDF_MIMETYPE).build()));

        then(transformEngineFileStorageMock).should().downloadFile(FILE_ID);
        then(storageClientMock).should().upload(fileToUpload, PDF_MIMETYPE, NODE_ID);
        then(ingestNodeCommandHandler).should().handle(expectedIngestNodeCommand);
    }

    @Test
    @SneakyThrows
    void shouldThrowIfTransformationFailed()
    {
        // given
        IngestContentCommand command = new IngestContentCommand(400, FILE_ID, NODE_ID);

        // when
        assertThrows(TransformationFailedException.class, () -> ingestContentCommandHandler.handle(command));
    }
}
