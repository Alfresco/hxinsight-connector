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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.PreSignedUrlRequester.CONTENT_TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.PreSignedUrlRequester.NODE_ID_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.adapters.storage.connector.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.language.ConstantExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

class PreSignedUrlRequesterTest
{
    private static final String MOCK_ENDPOINT = "mock:hxi-endpoint";
    private static final int STATUS_CODE_201 = 201;
    private static final int STATUS_CODE_500 = 500;
    private static final String NODE_REF = "node-ref";
    private static final String CONTENT_TYPE = "content/type";
    private static final String STORAGE_LOCATION = "http://dummy-url";
    private static final String RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    private static final String RESPONSE_BODY = createResponseBodyWith(STORAGE_LOCATION_PROPERTY, STORAGE_LOCATION);

    CamelContext camelContext;
    MockEndpoint mockEndpoint;

    PreSignedUrlRequester preSignedUrlRequester;

    @BeforeEach
    @SneakyThrows
    void setUp()
    {
        camelContext = new DefaultCamelContext();
        preSignedUrlRequester = new PreSignedUrlRequester(camelContext, MOCK_ENDPOINT);
        camelContext.addRoutes(preSignedUrlRequester);
        camelContext.start();

        mockEndpoint = camelContext.getEndpoint(MOCK_ENDPOINT, MockEndpoint.class);
    }

    @AfterEach
    void tearDown()
    {
        camelContext.stop();
    }

    @Test
    void testRequestStorageLocation() throws InterruptedException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        mockEndpointWillRespondWith(STATUS_CODE_201, RESPONSE_BODY);
        mockEndpointExpectInRequestBody(NODE_ID_PROPERTY, NODE_REF, CONTENT_TYPE_PROPERTY, CONTENT_TYPE);

        // when
        URL url = preSignedUrlRequester.requestStorageLocation(request);

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(url).asString().isEqualTo(STORAGE_LOCATION);
    }

    @Test
    void testRequestStorageLocation_invalidResponseStatusCode()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        mockEndpointWillRespondWith(STATUS_CODE_500);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", STATUS_CODE_500);
    }

    @Test
    void testRequestStorageLocation_missingStorageLocationPropertyInResponse()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String responseBodyWithoutUrl = createResponseBodyWith("unexpectedProperty", STORAGE_LOCATION);
        mockEndpointWillRespondWith(STATUS_CODE_201, responseBodyWithoutUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Missing", STORAGE_LOCATION_PROPERTY);
    }

    @Test
    void testRequestStorageLocation_invalidJsonResponse()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String invalidJsonBody = RESPONSE_BODY.replaceAll(".$", "");
        mockEndpointWillRespondWith(STATUS_CODE_201, invalidJsonBody);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(JsonParseException.class)
                .message().isNotEmpty();
    }

    @Test
    void testRequestStorageLocation_invalidUrlInResponse()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String responseBodyWithInvalidUrl = createResponseBodyWith(STORAGE_LOCATION_PROPERTY, "invalidUrl");
        mockEndpointWillRespondWith(STATUS_CODE_201, responseBodyWithInvalidUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        assertThat(thrown)
                .cause()
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Parsing URL from response property failed!")
                .rootCause()
                .isInstanceOf(MalformedURLException.class)
                .message().isNotEmpty();
    }

    private StorageLocationRequest createStorageLocationRequestMock()
    {
        StorageLocationRequest request = mock(StorageLocationRequest.class);
        given(request.nodeId()).willReturn(NODE_REF);
        given(request.contentType()).willReturn(CONTENT_TYPE);

        return request;
    }

    private void mockEndpointWillRespondWith(int statusCode)
    {
        mockEndpoint.returnReplyHeader(HTTP_RESPONSE_CODE, new ConstantExpression(String.valueOf(statusCode)));
    }

    private void mockEndpointWillRespondWith(int statusCode, String responseBody)
    {
        mockEndpoint.whenAnyExchangeReceived(exchange -> {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, statusCode);
            exchange.getMessage().setBody(responseBody);
        });
    }

    private void mockEndpointExpectInRequestBody(String... expectedProperties)
    {
        Stream.of(expectedProperties).forEach(property -> mockEndpoint.message(0).body(String.class).contains(property));
    }

    private static String createResponseBodyWith(String propertyName, String propertyValue)
    {
        return String.format(RESPONSE_BODY_PATTERN, propertyName, propertyValue);
    }
}
