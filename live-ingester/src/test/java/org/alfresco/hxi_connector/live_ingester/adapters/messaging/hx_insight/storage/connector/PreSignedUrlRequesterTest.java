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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.mockito.MockitoAnnotations.openMocks;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.PreSignedUrlRequester.CONTENT_ID_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.language.ConstantExpression;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Storage;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreSignedUrlRequesterTest
{
    private static final String MOCK_ENDPOINT = "mock:hxi-endpoint";
    private static final int STATUS_CODE_200 = 200;
    private static final String STORAGE_LOCATION = "http://dummy-url";
    private static final String CONTENT_ID = "CONTENT ID";
    private static final String RESPONSE_BODY = createResponseBodyWith(STORAGE_LOCATION_PROPERTY, STORAGE_LOCATION);

    @Mock
    private AuthService mockAuthService;
    CamelContext camelContext;
    MockEndpoint mockEndpoint;

    PreSignedUrlRequester preSignedUrlRequester;

    @BeforeAll
    @SneakyThrows
    void beforeAll()
    {
        openMocks(this);
        camelContext = new DefaultCamelContext();
        IntegrationProperties integrationProperties = integrationPropertiesOf(MOCK_ENDPOINT);
        preSignedUrlRequester = new PreSignedUrlRequester(camelContext, integrationProperties, mockAuthService);
        camelContext.addRoutes(preSignedUrlRequester);
        camelContext.start();

        mockEndpoint = camelContext.getEndpoint(MOCK_ENDPOINT, MockEndpoint.class);
    }

    @AfterEach
    void afterEach()
    {
        reset(mockAuthService);
    }

    @AfterAll
    void afterAll()
    {
        camelContext.stop();
    }

    @Test
    void testRequestStorageLocation() throws Exception
    {
        // given
        mockEndpointWillRespondWith(STATUS_CODE_200, RESPONSE_BODY);

        // when
        PreSignedUrlResponse preSignedUrlResponse = preSignedUrlRequester.requestStorageLocation();

        // then
        mockEndpoint.assertIsSatisfied();
        PreSignedUrlResponse expected = new PreSignedUrlResponse(new URL(STORAGE_LOCATION), CONTENT_ID);
        assertThat(preSignedUrlResponse).isEqualTo(expected);
        then(mockAuthService).should().setHxIAuthorizationHeaders(any());
        then(mockAuthService).shouldHaveNoMoreInteractions();
    }

    @Test
    void testRequestStorageLocation_invalidResponseStatusCode5xx()
    {
        // given
        mockEndpointWillRespondWith(500);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .hasMessageContaining("received:", 500);
    }

    @Test
    void testRequestStorageLocation_invalidResponseStatusCode4xx()
    {
        // given
        mockEndpointWillRespondWith(400);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointClientErrorException.class)
                .hasMessageContaining("received:", 400);
    }

    @Test
    void testRequestStorageLocation_missingStorageLocationPropertyInResponse()
    {
        // given
        String responseBodyWithoutUrl = createResponseBodyWith("unexpectedProperty", STORAGE_LOCATION);
        mockEndpointWillRespondWith(STATUS_CODE_200, responseBodyWithoutUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .hasMessageContaining("Missing", STORAGE_LOCATION_PROPERTY);
    }

    @Test
    void testRequestStorageLocation_invalidJsonResponse()
    {
        // given
        String invalidJsonBody = removeLastCharacter(RESPONSE_BODY);
        mockEndpointWillRespondWith(STATUS_CODE_200, invalidJsonBody);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(JsonEOFException.class)
                .message().isNotEmpty();
    }

    @Test
    void testRequestStorageLocation_emptyBodyInResponse()
    {
        // given
        String emptyBody = "";
        mockEndpointWillRespondWith(STATUS_CODE_200, emptyBody);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(MismatchedInputException.class)
                .message().isNotEmpty();
    }

    @Test
    void testRequestStorageLocation_invalidUrlInResponse()
    {
        // given
        String responseBodyWithInvalidUrl = createResponseBodyWith(STORAGE_LOCATION_PROPERTY, "invalidUrl");
        mockEndpointWillRespondWith(STATUS_CODE_200, responseBodyWithInvalidUrl);

        // when
        Throwable thrown = catchThrowable(() -> preSignedUrlRequester.requestStorageLocation());

        // then
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .hasMessageContaining("Parsing URL from response property failed!")
                .rootCause().isInstanceOf(MalformedURLException.class)
                .message().isNotEmpty();
    }

    private IntegrationProperties integrationPropertiesOf(String endpoint)
    {
        Storage storageProperties = new Storage(new Storage.Location(endpoint, new Retry()), new Storage.Upload(new Retry()));
        IntegrationProperties.HylandExperience hylandExperienceProperties = new IntegrationProperties.HylandExperience(storageProperties, null);
        IntegrationProperties.Application applicationProperties = new IntegrationProperties.Application("dummy-source-id", "dummy-version");
        return new IntegrationProperties(null, hylandExperienceProperties, applicationProperties);
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

    private static String createResponseBodyWith(String storageLocationProperty, String storageLocation)
    {
        return String.format("[{\"%s\": \"%s\", \"%s\": \"%s\"}]", storageLocationProperty, storageLocation, CONTENT_ID_PROPERTY, CONTENT_ID);
    }

    private static String removeLastCharacter(String string)
    {
        return string.replaceFirst(".$", "");
    }
}
