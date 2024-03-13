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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.util.auth.AuthUtils.AUTH_HEADER;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.util.DockerTags;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.util.auth.WithMockOAuth2User;
import org.alfresco.hxi_connector.live_ingester.util.auth.WithoutAnyUser;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        PreSignedUrlRequester.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableMethodSecurity
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
@WithMockOAuth2User
class PreSignedUrlRequesterIntegrationTest
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getWiremockTag();
    private static final String NODE_ID = "some-node-ref";
    private static final String PRE_SIGNED_URL_PATH = "/pre-signed-url";
    private static final String CAMEL_ENDPOINT_PATTERN = "%s%s?httpMethod=POST&throwExceptionOnFailure=false";
    private static final String FILE_CONTENT_TYPE = "plain/text";
    private static final String HX_INSIGHT_RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    private static final int HX_INSIGHT_RESPONSE_CODE = 201;
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG))
            .withEnv("WIREMOCK_OPTIONS", "--verbose");

    @SpyBean
    StorageLocationRequester locationRequester;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
    }

    @Test
    void testRequestStorageLocation()
    {
        // given
        String preSignedUrl = "http://s3-storage-location";
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody(hxInsightResponse)));

        // when
        URL actualUrl = locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE));

        // then
        WireMock.verify(postRequestedFor(urlPathEqualTo(PRE_SIGNED_URL_PATH))
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_HEADER))
                .withRequestBody(new ContainsPattern(NODE_ID))
                .withRequestBody(new ContainsPattern(FILE_CONTENT_TYPE)));
        assertThat(actualUrl).asString().isEqualTo(preSignedUrl);
    }

    @Test
    void testRequestStorageLocation_serverError_doRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(serverError()));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation(any());
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @Test
    void testRequestStorageLocation_clientError_dontRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(badRequest()));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(1)).requestStorageLocation(any());
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @Test
    void testRequestStorageLocation_emptyBody_doRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody("")));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation(any());
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void testRequestStorageLocation_invalidJsonBody_doRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody("{")));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation(any());
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(JsonEOFException.class);
    }

    @Test
    void testRequestStorageLocation_invalidUrlInBody_dontRetry()
    {
        // given
        String preSignedUrl = "invalid-url";
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody(hxInsightResponse)));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation(any());
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(MalformedURLException.class);
    }

    @Test
    void testRequestStorageLocation_serverDown_doRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation(any());
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(NoHttpResponseException.class);
    }

    @Test
    @WithoutAnyUser
    void testPublishMessage_withoutAuth_dontRetry()
    {
        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).shouldHaveNoInteractions();
        assertThat(thrown).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithAnonymousUser
    void testPublishMessage_authError_dontRetry()
    {
        // when
        Throwable thrown = catchThrowable(() -> locationRequester.requestStorageLocation(new StorageLocationRequest(NODE_ID, FILE_CONTENT_TYPE)));

        // then
        then(locationRequester).shouldHaveNoInteractions();
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("hyland-experience.storage.location.endpoint", PreSignedUrlRequesterIntegrationTest::createEndpointUrl);
        registry.add("hyland-experience.storage.location.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.storage.location.retry.initialDelay", () -> RETRY_DELAY_MS);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static String createEndpointUrl()
    {
        return CAMEL_ENDPOINT_PATTERN.formatted(wireMockServer.getBaseUrl(), PRE_SIGNED_URL_PATH);
    }
}
