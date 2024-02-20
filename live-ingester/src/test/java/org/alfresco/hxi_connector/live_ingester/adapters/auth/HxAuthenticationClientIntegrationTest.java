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
package org.alfresco.hxi_connector.live_ingester.adapters.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.http.HttpHeaders.HOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.util.AuthUtils;
import org.alfresco.hxi_connector.live_ingester.util.DockerTags;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        HxAuthenticationClient.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@Testcontainers
class HxAuthenticationClientIntegrationTest
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getOrDefault("wiremock.tag", "3.3.1");
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;
    private static String tokenUri;
    private static ClientRegistration clientRegistration;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG))
            .withEnv("WIREMOCK_OPTIONS", "--verbose");

    @SpyBean
    AuthenticationClient authenticationClient;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
        tokenUri = wireMockServer.getBaseUrl() + AuthUtils.TOKEN_PATH;
        clientRegistration = AuthUtils.creatClientRegistration(tokenUri);
    }

    @Test
    void testAuthorize()
    {
        // given
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(AuthUtils.createAuthResponseBody())));

        // when
        AuthenticationResult authenticationResult = authenticationClient.authenticate(tokenUri, clientRegistration);

        // then
        then(authenticationClient).should().authenticate(tokenUri, clientRegistration);
        String authRequestBody = AuthUtils.createAuthRequestBody();
        WireMock.verify(postRequestedFor(urlPathEqualTo(AuthUtils.TOKEN_PATH))
                .withHeader(HOST, new EqualToPattern(wireMockServer.getHost() + ":" + wireMockServer.getPort()))
                .withHeader(Exchange.CONTENT_TYPE, new EqualToPattern(APPLICATION_FORM_URLENCODED.getMimeType()))
                .withHeader(Exchange.CONTENT_LENGTH, new EqualToPattern(String.valueOf(authRequestBody.getBytes(UTF_8).length)))
                .withRequestBody(new EqualToPattern(authRequestBody)));
        AuthenticationResult expectedAuthenticationResult = AuthUtils.createExpectedAuthResult();
        assertThat(authenticationResult).isEqualTo(expectedAuthenticationResult);
    }

    @Test
    void testAuthorize_serverError_doRetry()
    {
        // given
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(serverError()));

        // when
        Throwable thrown = catchThrowable(() -> authenticationClient.authenticate(tokenUri, clientRegistration));

        // then
        then(authenticationClient).should(times(RETRY_ATTEMPTS)).authenticate(tokenUri, clientRegistration);
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @Test
    void testAuthorize_clientError_dontRetry()
    {
        // given
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(badRequest()));

        // when
        Throwable thrown = catchThrowable(() -> authenticationClient.authenticate(tokenUri, clientRegistration));

        // then
        then(authenticationClient).should(times(1)).authenticate(tokenUri, clientRegistration);
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, wireMockServer.getBaseUrl());
        registry.add("hyland-experience.authentication.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.authentication.retry.initialDelay", () -> RETRY_DELAY_MS);
    }
}
