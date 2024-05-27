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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.HxAuthenticationClientTest;
import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.prediction_applier.auth.PredictionApplierHxAuthClient;
import org.alfresco.hxi_connector.prediction_applier.config.HxInsightProperties;
import org.alfresco.hxi_connector.prediction_applier.config.RepositoryApiProperties;
import org.alfresco.hxi_connector.prediction_applier.config.SecurityConfig;

@SpringBootTest(properties = "logging.level.org.alfresco=DEBUG",
        classes = {HxInsightProperties.class, SecurityConfig.class, PredictionApplierHxAuthClient.class, PredictionApplierHxAuthClientIntegrationTest.PredictionApplierHxAuthClientTestConfig.class})
@EnableAutoConfiguration
@EnableConfigurationProperties
@EnableRetry
@Testcontainers
@SuppressWarnings("PMD.TestClassWithoutTestCases")
class PredictionApplierHxAuthClientIntegrationTest extends HxAuthenticationClientTest
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
        String clientRegistrationId = "alfresco";
        AuthenticationResult authenticationResult = authenticationClient.authenticate(clientRegistrationId);

        // then
        then(authenticationClient).should().authenticate(clientRegistrationId);
        String authRequestBody = AuthUtils.createAuthRequestBody();
        WireMock.verify(postRequestedFor(urlPathEqualTo(AuthUtils.TOKEN_PATH))
                .withHeader(HOST, new EqualToPattern(ACS_MOCK.getHost() + ":" + ACS_MOCK.getPort()))
                .withHeader(Exchange.CONTENT_TYPE, new EqualToPattern(APPLICATION_FORM_URLENCODED.getMimeType()))
                .withHeader(Exchange.CONTENT_LENGTH, new EqualToPattern(String.valueOf(authRequestBody.getBytes(UTF_8).length)))
                .withRequestBody(new EqualToPattern(authRequestBody)));
        AuthenticationResult expectedAuthenticationResult = AuthUtils.createExpectedAuthResult();
        assertThat(authenticationResult).isEqualTo(expectedAuthenticationResult);
    }

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, ACS_MOCK.getBaseUrl(), "alfresco");
    }

    @TestConfiguration
    public static class PredictionApplierHxAuthClientTestConfig
    {
        @Bean
        public RepositoryApiProperties nodesApiProperties()
        {
            return new RepositoryApiProperties("http://localhost:8002", "dummy-user", "dummy-password", null);
        }
    }
}
