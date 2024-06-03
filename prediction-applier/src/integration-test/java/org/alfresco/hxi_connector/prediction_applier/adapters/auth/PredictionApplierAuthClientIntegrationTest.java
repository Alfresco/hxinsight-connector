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
package org.alfresco.hxi_connector.prediction_applier.adapters.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.hc.core5.http.HttpHeaders.HOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.ALFRESCO_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.HXI_AUTH_PROVIDER;

import java.util.Collections;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.apache.camel.Exchange;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAuthenticationClientTest;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.prediction_applier.auth.PredictionApplierAuthClient;

@SpringBootTest(properties = "logging.level.org.alfresco=DEBUG",
        classes = {PredictionApplierAuthClient.class, PredictionApplierAuthClientIntegrationTest.PredictionApplierAuthClientTestConfig.class})
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableRetry
@Testcontainers
@SuppressWarnings("PMD.TestClassWithoutTestCases")
class PredictionApplierAuthClientIntegrationTest extends DefaultAuthenticationClientTest
{
    @Container
    private static final WireMockContainer ACS_MOCK = DockerContainers.createWireMockContainer();

    @Test
    protected void testAuthorize_alfresco()
    {
        // given
        WireMock.configureFor(ACS_MOCK.getHost(), ACS_MOCK.getPort());
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(AuthUtils.createAuthResponseBody())));

        // when
        AuthenticationResult authenticationResult = authenticationClient.authenticate(ALFRESCO_AUTH_PROVIDER);

        // then
        then(authenticationClient).should().authenticate(ALFRESCO_AUTH_PROVIDER);
        String authRequestBody = AuthUtils.createAuthRequestBody();
        WireMock.verify(postRequestedFor(urlPathEqualTo(AuthUtils.TOKEN_PATH))
                .withHeader(HOST, new EqualToPattern(ACS_MOCK.getHost() + ":" + ACS_MOCK.getPort()))
                .withHeader(Exchange.CONTENT_TYPE, new EqualToPattern(APPLICATION_FORM_URLENCODED.getMimeType()))
                .withHeader(Exchange.CONTENT_LENGTH, new EqualToPattern(String.valueOf(authRequestBody.getBytes(UTF_8).length)))
                .withRequestBody(new EqualToPattern(authRequestBody)));
        AuthenticationResult expectedAuthenticationResult = AuthUtils.createExpectedAuthResult();
        assertThat(authenticationResult).isEqualTo(expectedAuthenticationResult);
    }

    @TestConfiguration
    public static class PredictionApplierAuthClientTestConfig
    {
        @Bean
        public AuthProperties authorizationProperties()
        {
            AuthProperties authProperties = new AuthProperties();
            AuthProperties.AuthProvider hXauthProvider = AuthUtils.createAuthProvider(hxAuthMock.getBaseUrl() + AuthUtils.TOKEN_PATH);
            AuthProperties.AuthProvider alfrescoAuthProvider = AuthUtils.createAuthProvider(ACS_MOCK.getBaseUrl() + AuthUtils.TOKEN_PATH);
            authProperties.setProviders(Map.of(HXI_AUTH_PROVIDER, hXauthProvider, ALFRESCO_AUTH_PROVIDER, alfrescoAuthProvider));
            authProperties.setRetry(
                    new org.alfresco.hxi_connector.common.config.properties.Retry(RETRY_ATTEMPTS, RETRY_DELAY_MS, 1,
                            Collections.emptySet()));
            return authProperties;
        }
    }
}
