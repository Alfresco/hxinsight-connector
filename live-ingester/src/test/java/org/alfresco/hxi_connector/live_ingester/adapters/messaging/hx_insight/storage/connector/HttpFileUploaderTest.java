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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.HttpFileUploader.AMZ_SECURITY_TOKEN;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.HttpFileUploader.ROUTE_ID;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.HttpFileUploader.STORAGE_LOCATION_HEADER;

import java.io.InputStream;
import java.net.URL;

import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ToDynamicDefinition;
import org.apache.camel.model.language.ConstantExpression;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Storage;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpFileUploaderTest
{
    private static final String MOCK_ENDPOINT = "mock:s3-endpoint";
    private static final String CONTENT_TYPE = "content/type";
    private static final String NODE_ID = "node-ref";

    CamelContext camelContext;
    MockEndpoint mockEndpoint;

    HttpFileUploader httpFileUploader;

    @BeforeAll
    @SneakyThrows
    void beforeAll()
    {
        camelContext = new DefaultCamelContext();
        httpFileUploader = new HttpFileUploader(camelContext, integrationProperties());
        camelContext.addRoutes(httpFileUploader);
        camelContext.start();

        adviceWith(camelContext, ROUTE_ID, route -> route.weaveByType(ToDynamicDefinition.class).replace().to(MOCK_ENDPOINT));
        mockEndpoint = camelContext.getEndpoint(MOCK_ENDPOINT, MockEndpoint.class);
    }

    @AfterEach
    void tearDown()
    {
        mockEndpoint.reset();
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
        mockEndpointWillRespondWith(200);
        mockEndpointWillExpectInRequestHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request, NODE_ID));

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(thrown).doesNotThrowAnyException();
    }

    @Test
    void testUpload_verifyRawQueryParam() throws InterruptedException
    {
        // given
        URL url = mock(URL.class);
        FileUploadRequest request = createFileUploadRequestMock(url);
        String securityTokenQueryParam = AMZ_SECURITY_TOKEN + "token%2B%2F%3D";
        given(url.getQuery()).willReturn(securityTokenQueryParam);
        given(url.toString()).willReturn(MOCK_ENDPOINT + "?" + securityTokenQueryParam);

        String expectedRawUrl = "%s?%sRAW(%s)".formatted(MOCK_ENDPOINT, AMZ_SECURITY_TOKEN, "token+/=");
        mockEndpointWillRespondWith(200);
        mockEndpointWillExpectInRequestHeader(STORAGE_LOCATION_HEADER, expectedRawUrl);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request, NODE_ID));

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(thrown).doesNotThrowAnyException();
    }

    @Test
    void testUpload_invalidResponseStatusCode5xx()
    {
        // given
        FileUploadRequest request = createFileUploadRequestMock();
        mockEndpointWillRespondWith(500);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request, NODE_ID));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(EndpointServerErrorException.class)
                .hasMessageContaining("received:", 500);
    }

    @Test
    void testUpload_invalidResponseStatusCode4xx()
    {
        // given
        FileUploadRequest request = createFileUploadRequestMock();
        mockEndpointWillRespondWith(400);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request, NODE_ID));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(EndpointClientErrorException.class)
                .hasMessageContaining("received:", 400);
    }

    private IntegrationProperties integrationProperties()
    {
        Storage storageProperties = new Storage(new Storage.Location(null, new Retry()), new Storage.Upload(new Retry()));
        IntegrationProperties.HylandExperience hylandExperienceProperties = new IntegrationProperties.HylandExperience(null, null, storageProperties, null);
        return new IntegrationProperties(null, hylandExperienceProperties);
    }

    private FileUploadRequest createFileUploadRequestMock()
    {
        return createFileUploadRequestMock(mock(URL.class));
    }

    @SuppressWarnings("PMD.CloseResource")
    private FileUploadRequest createFileUploadRequestMock(URL url)
    {
        FileUploadRequest requestMock = mock(FileUploadRequest.class);
        InputStream inputStreamMock = mock(InputStream.class);
        File file = new File(inputStreamMock);
        given(requestMock.file()).willReturn(file);
        given(requestMock.contentType()).willReturn(CONTENT_TYPE);
        given(requestMock.storageLocation()).willReturn(url);
        given(url.toString()).willReturn(MOCK_ENDPOINT);
        return requestMock;
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
