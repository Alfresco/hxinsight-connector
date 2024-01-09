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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.storage.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.HxInsightApiConfig;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequest;

@ExtendWith(MockitoExtension.class)
class PreSignedUrlRequesterTest
{
    private static final int STATUS_CODE_201 = 201;
    private static final String STORAGE_LOCATION = "http://dummy-url";
    private static final String RESPONSE_BODY = "{\"" + STORAGE_LOCATION_PROPERTY + "\": \"" + STORAGE_LOCATION + "\"}";

    @Mock
    CamelContext camelContextMock;
    @Mock
    ProducerTemplate producerTemplateMock;
    @Mock
    Exchange exchangeMock;
    @Mock(strictness = Mock.Strictness.LENIENT)
    Message messageMock;
    @Mock
    ObjectMapper objectMapperMock;
    @Mock
    HxInsightApiConfig.Properties configPropertiesMock;

    PreSignedUrlRequester preSignedUrlRequester;

    @BeforeEach
    void setUp()
    {
        given(configPropertiesMock.url()).willReturn(new HxInsightApiConfig.Url(null));
        preSignedUrlRequester = new PreSignedUrlRequester(camelContextMock, objectMapperMock, configPropertiesMock);
        given(camelContextMock.createProducerTemplate()).willReturn(producerTemplateMock);
        given(producerTemplateMock.send(any(String.class), any(Processor.class))).willReturn(exchangeMock);
        given(exchangeMock.getMessage()).willReturn(messageMock);
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(STATUS_CODE_201);
        given(messageMock.getBody(any())).willReturn(RESPONSE_BODY);
    }

    @Test
    void testRequestStorageLocation() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        given(objectMapperMock.readValue(any(String.class), any(TypeReference.class))).willReturn(Map.of(STORAGE_LOCATION_PROPERTY, STORAGE_LOCATION));

        // when
        URL url = preSignedUrlRequester.requestStorageLocation(request);

        // then
        then(messageMock).should().getBody(eq(String.class));
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(objectMapperMock).should().readValue(eq(RESPONSE_BODY), anyTypeReferenceOfMap());
        assertThat(url).asString().isEqualTo(STORAGE_LOCATION);
    }

    @Test
    void testRequestStorageLocation_invalidResponseStatusCode()
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(500);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperMock).shouldHaveNoInteractions();
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", 500);
    }

    @Test
    void testRequestStorageLocation_missingStorageLocationPropertyInResponse() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String unexpectedProperty = "unexpectedProperty";
        String responseBodyWithoutUrl = "{\"" + unexpectedProperty + "\": \"" + STORAGE_LOCATION + "\"}";
        given(messageMock.getBody(any())).willReturn(responseBodyWithoutUrl);
        given(objectMapperMock.readValue(any(String.class), any(TypeReference.class))).willReturn(Map.of(unexpectedProperty, STORAGE_LOCATION));

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperMock).should().readValue(eq(responseBodyWithoutUrl), anyTypeReferenceOfMap());
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Missing", STORAGE_LOCATION_PROPERTY);
    }

    @Test
    void testRequestStorageLocation_invalidJsonResponse() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String invalidJsonBody = "{\"" + STORAGE_LOCATION_PROPERTY + "\" \"" + STORAGE_LOCATION + "\"}";
        given(messageMock.getBody(any())).willReturn(invalidJsonBody);
        given(objectMapperMock.readValue(eq(invalidJsonBody), any(TypeReference.class)))
                .willThrow(JsonMappingException.from((JsonParser) null, "Dummy error message"));

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation(request));

        // then
        then(objectMapperMock).should().readValue(eq(invalidJsonBody), anyTypeReferenceOfMap());
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("Parsing JSON response failed!")
                .hasRootCauseMessage("Dummy error message");
    }

    @Test
    void testRequestStorageLocation_invalidUrlInResponse() throws IOException
    {
        // given
        StorageLocationRequest request = createStorageLocationRequestMock();
        String invalidUrl = "invalidUrl";
        given(objectMapperMock.readValue(any(String.class), any(TypeReference.class))).willReturn(Map.of(STORAGE_LOCATION_PROPERTY, invalidUrl));

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

    @SuppressWarnings("unchecked")
    private static <K, V, M extends Map<? extends K, ? extends V>> TypeReference<M> anyTypeReferenceOfMap()
    {
        return any(TypeReference.class);
    }
}
