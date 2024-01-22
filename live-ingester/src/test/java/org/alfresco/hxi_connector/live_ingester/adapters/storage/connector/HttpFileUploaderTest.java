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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.connector;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.HttpFileUploader.ROUTE_ID;

import java.net.URL;

import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ToDynamicDefinition;
import org.apache.camel.model.language.ConstantExpression;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpFileUploaderTest
{
    private static final String MOCK_ENDPOINT = "mock:s3-endpoint";
    private static final int STATUS_CODE_200 = 200;
    private static final int STATUS_CODE_500 = 500;
    private static final String CONTENT_TYPE = "content/type";

    CamelContext camelContext;
    MockEndpoint mockEndpoint;

    HttpFileUploader httpFileUploader;

    @BeforeAll
    @SneakyThrows
    void beforeAll()
    {
        camelContext = new DefaultCamelContext();
        httpFileUploader = new HttpFileUploader(camelContext);
        camelContext.addRoutes(httpFileUploader);
        camelContext.start();

        adviceWith(camelContext, ROUTE_ID, route -> route.weaveByType(ToDynamicDefinition.class).replace().to(MOCK_ENDPOINT));
        mockEndpoint = camelContext.getEndpoint(MOCK_ENDPOINT, MockEndpoint.class);
    }

    @AfterAll
    void afterAll()
    {
        camelContext.stop();
    }

    @Test
    void testUpload() throws InterruptedException
    {
        // given
        FileUploadRequest request = createFileUploadRequestMock();
        mockEndpointWillRespondWith(STATUS_CODE_200);
        mockEndpointWillExpectInRequestHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request));

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(thrown).doesNotThrowAnyException();
    }

    @Test
    void testUpload_invalidResponseStatusCode()
    {
        // given
        FileUploadRequest request = createFileUploadRequestMock();
        mockEndpointWillRespondWith(STATUS_CODE_500);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", STATUS_CODE_500);
    }

    private FileUploadRequest createFileUploadRequestMock()
    {
        FileUploadRequest request = mock(FileUploadRequest.class);
        URL url = mock(URL.class);
        File file = mock(File.class);
        given(request.file()).willReturn(file);
        given(request.contentType()).willReturn(CONTENT_TYPE);
        given(request.storageLocation()).willReturn(url);
        given(url.toString()).willReturn(MOCK_ENDPOINT);
        return request;
    }

    private void mockEndpointWillRespondWith(int statusCode)
    {
        mockEndpoint.returnReplyHeader(HTTP_RESPONSE_CODE, new ConstantExpression(String.valueOf(statusCode)));
    }

    private void mockEndpointWillExpectInRequestHeader(String headerName, String expectedValue)
    {
        mockEndpoint.expectedHeaderReceived(headerName, expectedValue);
    }
}
