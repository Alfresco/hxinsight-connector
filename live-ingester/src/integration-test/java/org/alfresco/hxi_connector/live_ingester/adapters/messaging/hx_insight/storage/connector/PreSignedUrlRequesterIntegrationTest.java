/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_APP_HEADER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_ENVIRONMENT_HEADER;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.Fault;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.AccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.RepositoryInformation;
import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.docker.util.DockerTags;
import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.IntegrationCamelTestBase;
import org.alfresco.hxi_connector.live_ingester.adapters.auth.LiveIngesterAuthClient;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        PreSignedUrlRequester.class,
        LiveIngesterAuthClient.class,
        PreSignedUrlRequesterIntegrationTest.PreSignedUrlRequesterIntegrationTestConfig.class,
        ApplicationInfoProvider.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
class PreSignedUrlRequesterIntegrationTest extends IntegrationCamelTestBase
{
    private static final String PRE_SIGNED_URL_PATH = "/v1/presigned-urls";
    private static final String CONTENT_ID = "CONTENT ID";
    private static final String ACS_VERSION = "7.4.0";
    private static final String CAMEL_ENDPOINT_PATTERN = "%s%s?httpMethod=POST&throwExceptionOnFailure=false";
    private static final String HX_INSIGHT_RESPONSE_BODY_PATTERN = "[{\"%s\": \"%s\", \"id\": \"CONTENT ID\"}]";
    private static final int HX_INSIGHT_RESPONSE_CODE = 200;
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = DockerContainers.createWireMockContainer();

    @MockitoSpyBean
    StorageLocationRequester locationRequester;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
    }

    @Test
    void testRequestStorageLocation() throws Exception
    {
        // given
        String preSignedUrl = "http://s3-storage-location";
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody(hxInsightResponse)));

        // when
        PreSignedUrlResponse preSignedUrlResponse = locationRequester.requestStorageLocation();

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/get-presigned-urls.yml");

        WireMock.verify(postRequestedFor(urlPathEqualTo(request.url()))
                .withHeader(AUTHORIZATION, matching(request.headers().get("authorization")))
                // content-type is not generated by Camel for presigned-urls endpoint
                // .withHeader(CONTENT_TYPE, equalTo(request.headers().get("content-type")))
                .withHeader(HXP_ENVIRONMENT_HEADER, matching(request.headers().get("hxp-environment")))
                .withHeader(USER_AGENT, matching(request.headers().get("user-agent")))
                .withHeader(HXP_APP_HEADER, equalTo("hxai-discovery"))
                .withRequestBody(absent()));
        PreSignedUrlResponse expected = new PreSignedUrlResponse(new URL(preSignedUrl), CONTENT_ID);
        assertThat(preSignedUrlResponse).isEqualTo(expected);
    }

    @Test
    void testRequestStorageLocation_serverError_doRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(serverError()));

        // when
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation();
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @Test
    void testRequestStorageLocation_clientError_dontRetry()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(badRequest()));

        // when
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(1)).requestStorageLocation();
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
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation();
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
                        .withBody("[")));

        // when
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation();
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
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation();
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
        Throwable thrown = catchThrowable(locationRequester::requestStorageLocation);

        // then
        then(locationRequester).should(times(RETRY_ATTEMPTS)).requestStorageLocation();
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .cause().isInstanceOf(NoHttpResponseException.class);
    }

    @Test
    @SuppressWarnings({"ThrowableNotThrown", "ResultOfMethodCallIgnored"})
    void testLoggingOnError()
    {
        // given
        givenThat(post(PRE_SIGNED_URL_PATH)
                .willReturn(badRequest().withResponseBody(new Body("{\"error\": \"Bad request\"}"))));
        ListAppender<ILoggingEvent> logEntries = LoggingUtils.createLogsListAppender(PreSignedUrlRequester.class);

        // when
        catchThrowable(locationRequester::requestStorageLocation);

        // then
        List<String> logs = logEntries.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(logs)
                .isNotEmpty()
                .last().asString()
                .contains("Authorization=***", "Body: {\"error\": \"Bad request\"}");
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("hyland-experience.storage.location.endpoint", PreSignedUrlRequesterIntegrationTest::createEndpointUrl);
        registry.add("hyland-experience.storage.location.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.storage.location.retry.initial-delay", () -> RETRY_DELAY_MS);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static String createEndpointUrl()
    {
        return CAMEL_ENDPOINT_PATTERN.formatted(wireMockServer.getBaseUrl(), PRE_SIGNED_URL_PATH);
    }

    @TestConfiguration
    public static class PreSignedUrlRequesterIntegrationTestConfig
    {
        @Bean
        public AuthProperties authorizationProperties()
        {
            AuthProperties authProperties = new AuthProperties();
            AuthProperties.AuthProvider hXauthProvider = AuthUtils.createAuthProvider("http://token-uri");
            authProperties.setProviders(Map.of(HXP_AUTH_PROVIDER, hXauthProvider));
            authProperties.setRetry(
                    new org.alfresco.hxi_connector.common.config.properties.Retry(RETRY_ATTEMPTS, RETRY_DELAY_MS, 1,
                            Collections.emptySet()));
            return authProperties;
        }

        @Bean
        public AccessTokenProvider defaultAccessTokenProvider()
        {
            AuthenticationClient dummyAuthClient = new DefaultAuthenticationClient(authorizationProperties());
            DefaultAccessTokenProvider dummyAccessTokenProvider = new DefaultAccessTokenProvider(dummyAuthClient);
            Map<String, DefaultAccessTokenProvider.Token> tokens = new HashMap<>();
            AuthenticationResult dummyAuthResult = AuthUtils.createExpectedAuthResult();
            tokens.put(HXP_AUTH_PROVIDER, new DefaultAccessTokenProvider.Token(dummyAuthResult.getAccessToken(), OffsetDateTime.now().plusSeconds(3600)));
            ReflectionTestUtils.setField(dummyAccessTokenProvider, "accessTokens", tokens);
            return dummyAccessTokenProvider;
        }

        @Bean
        public AuthService authService()
        {
            return new AuthService(authorizationProperties(), defaultAccessTokenProvider());
        }

        @Bean
        public Application application()
        {
            return new Application("alfresco-dummy-source-id-0a63de491876", DockerTags.getHxiConnectorTag());
        }

        @Bean
        public RepositoryInformation repositoryInformation()
        {
            return () -> ACS_VERSION;
        }
    }
}
