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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.storage.endpoint.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@ExtendWith(MockitoExtension.class)
class PreSignedUrlRequesterTest
{
    private static final int STATUS_CODE_201 = 201;
    private static final int STATUS_CODE_500 = 500;
    private static final String STORAGE_LOCATION = "http://dummy-url";
    private static final String RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    private static final String RESPONSE_BODY = responseBodyWith(STORAGE_LOCATION_PROPERTY, STORAGE_LOCATION);

    @Mock
    CamelContext camelContextMock;
    @Mock
    ProducerTemplate producerTemplateMock;
    @Mock
    Exchange exchangeMock;
    @Mock
    Message messageMock;
    @Spy
    ObjectMapper objectMapperSpy = new ObjectMapper();

    @InjectMocks
    PreSignedUrlRequester preSignedUrlRequester;

    @BeforeEach
    void setUp()
    {
        given(camelContextMock.createProducerTemplate()).willReturn(producerTemplateMock);
        given(producerTemplateMock.send(any(String.class), any(Processor.class))).willReturn(exchangeMock);
        given(exchangeMock.getMessage()).willReturn(messageMock);
    }

    @Test
    void testRequestStorageLocation() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        httpClientWillRespondWith(STATUS_CODE_201, RESPONSE_BODY);

        // when
        URL url = preSignedUrlRequester.requestStorageLocation(request);

        // then
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(messageMock).should().getBody(String.class);
        then(objectMapperSpy).should().readValue(eq(RESPONSE_BODY), anyTypeReferenceOfMap());
        assertThat(url).asString().isEqualTo(STORAGE_LOCATION);
    }

    @Test
    void testRequestStorageLocation_invalidResponseStatusCode()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        httpClientWillRespondWith(STATUS_CODE_500);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperSpy).shouldHaveNoInteractions();
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", 500);
    }

    @Test
    void testRequestStorageLocation_missingStorageLocationPropertyInResponse() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String responseBodyWithoutUrl = responseBodyWith("unexpectedProperty", STORAGE_LOCATION);
        httpClientWillRespondWith(STATUS_CODE_201, responseBodyWithoutUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperSpy).should().readValue(eq(responseBodyWithoutUrl), anyTypeReferenceOfMap());
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Missing", STORAGE_LOCATION_PROPERTY);
    }

    @Test
    void testRequestStorageLocation_invalidJsonResponse() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String invalidJsonBody = RESPONSE_BODY.replaceAll(".$", "");
        httpClientWillRespondWith(STATUS_CODE_201, invalidJsonBody);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperSpy).should().readValue(eq(invalidJsonBody), anyTypeReferenceOfMap());
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Parsing JSON response failed!")
                .hasRootCauseInstanceOf(JsonParseException.class);
    }

    @Test
    void testRequestStorageLocation_invalidUrlInResponse()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String responseBodyWithInvalidUrl = responseBodyWith(STORAGE_LOCATION_PROPERTY, "invalidUrl");
        httpClientWillRespondWith(STATUS_CODE_201, responseBodyWithInvalidUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Parsing URL from response property failed!")
                .rootCause()
                .isInstanceOf(MalformedURLException.class)
                .message().isNotEmpty();
    }

    private StorageLocationRequest createStorageLocationRequestMock()
    {
        StorageLocationRequest request = mock(StorageLocationRequest.class);
        given(request.nodeId()).willReturn("node-ref");
        given(request.contentType()).willReturn("content/type");

        return request;
    }

    private void httpClientWillRespondWith(int statusCode)
    {
        httpClientWillRespondWith(statusCode, null);
    }

    private void httpClientWillRespondWith(int statusCode, String responseBody)
    {
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(statusCode);
        given(messageMock.getBody(any())).willReturn(responseBody);
    }

    private static String responseBodyWith(String propertyName, String propertyValue)
    {
        return String.format(RESPONSE_BODY_PATTERN, propertyName, propertyValue);
    }

    @SuppressWarnings("unchecked")
    private static <K, V, M extends Map<? extends K, ? extends V>> TypeReference<M> anyTypeReferenceOfMap()
    {
        return any(TypeReference.class);
    }
}
