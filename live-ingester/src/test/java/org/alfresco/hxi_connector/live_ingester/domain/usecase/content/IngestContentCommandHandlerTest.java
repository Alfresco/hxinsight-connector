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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageClient;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class IngestContentCommandHandlerTest
{
    static final long TIMESTAMP = 1_234_567_890L;
    static final String NODE_ID = "12341234-1234-1234-1234-123412341234";
    static final String PDF_MIMETYPE = "application/pdf";

    @Mock
    TransformRequester transformRequester;
    @Mock
    StorageClient storageClient;

    @InjectMocks
    IngestContentCommandHandler ingestContentCommandHandler;

    @Test
    void shouldRequestNodeContentTransformation()
    {
        // given
        IngestContentCommand command = new IngestContentCommand(TIMESTAMP, NODE_ID);

        // when
        ingestContentCommandHandler.handle(command);

        // then
        TransformRequest expectedTransformationRequest = new TransformRequest(TIMESTAMP, NODE_ID, PDF_MIMETYPE);

        then(transformRequester).should().requestTransform(expectedTransformationRequest);
        then(storageClient).should().upload(any(InputStream.class), eq(PDF_MIMETYPE), eq(NODE_ID));
    }
}
