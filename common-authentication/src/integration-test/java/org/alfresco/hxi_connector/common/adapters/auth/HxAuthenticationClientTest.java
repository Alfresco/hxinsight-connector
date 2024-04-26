/*-
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
package org.alfresco.hxi_connector.common.adapters.auth;

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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HxAuthenticationClientTest
{
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;
    private static String tokenUri;
    private static ClientRegistration clientRegistration;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    protected static final WireMockContainer wireMockServer = DockerContainers.createWireMockContainer();

    @SpyBean
    protected AuthenticationClient authenticationClient;

    @BeforeAll
    protected static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
        tokenUri = wireMockServer.getBaseUrl() + AuthUtils.TOKEN_PATH;
        clientRegistration = AuthUtils.creatClientRegistration(tokenUri);
    }

    @Test
    protected void testAuthorize()
    {
        // given
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
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
    protected void testAuthorize_serverError_doRetry()
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
    protected void testAuthorize_clientError_dontRetry()
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
    protected static void overrideProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, wireMockServer.getBaseUrl());
        registry.add("hyland-experience.authentication.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.authentication.retry.initialDelay", () -> RETRY_DELAY_MS);
    }
}
