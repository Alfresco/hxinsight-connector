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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.alfresco.hxi_connector.live_ingester.util.auth.AuthUtils.AUTH_HEADER;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
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
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.util.DockerContainers;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.hxi_connector.live_ingester.util.auth.WithMockOAuth2User;
import org.alfresco.hxi_connector.live_ingester.util.auth.WithoutAnyUser;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        HxInsightEventPublisher.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableMethodSecurity
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
@WithMockOAuth2User
class HxInsightEventPublisherIntegrationTest
{
    private static final String INGEST_PATH = "/ingest";
    private static final String NODE_ID = "node-id";
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;
    private static final NodeEvent NODE_EVENT = new UpdateNodeEvent(NODE_ID, EventType.UPDATE);

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = DockerContainers.createWireMockContainer();

    @SpyBean
    IngestionEngineEventPublisher ingestionEngineEventPublisher;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
    }

    @Test
    void testPublishMessage()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(aResponse().withStatus(200)));

        // when
        Throwable thrown = catchThrowable(() -> ingestionEngineEventPublisher.publishMessage(NODE_EVENT));

        // then
        WireMock.verify(postRequestedFor(urlPathEqualTo(INGEST_PATH))
                .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_HEADER))
                .withRequestBody(new ContainsPattern(NODE_ID)));
        assertThat(thrown).doesNotThrowAnyException();
    }

    @Test
    void testPublishMessage_serverError_doRetry()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(serverError()));

        // when
        Throwable thrown = catchThrowable(() -> ingestionEngineEventPublisher.publishMessage(NODE_EVENT));

        // then
        then(ingestionEngineEventPublisher).should(times(RETRY_ATTEMPTS)).publishMessage(NODE_EVENT);
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @Test
    void testPublishMessage_clientError_dontRetry()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(badRequest()));

        // when
        Throwable thrown = catchThrowable(() -> ingestionEngineEventPublisher.publishMessage(NODE_EVENT));

        // then
        then(ingestionEngineEventPublisher).should(times(1)).publishMessage(NODE_EVENT);
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @Test
    @WithoutAnyUser
    void testPublishMessage_withoutAuth_dontRetry()
    {
        // when
        Throwable thrown = catchThrowable(() -> ingestionEngineEventPublisher.publishMessage(NODE_EVENT));

        // then
        then(ingestionEngineEventPublisher).shouldHaveNoInteractions();
        assertThat(thrown).isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithAnonymousUser
    void testPublishMessage_authError_dontRetry()
    {
        // when
        Throwable thrown = catchThrowable(() -> ingestionEngineEventPublisher.publishMessage(NODE_EVENT));

        // then
        then(ingestionEngineEventPublisher).shouldHaveNoInteractions();
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("hyland-experience.insight.base-url", wireMockServer::getBaseUrl);
        registry.add("hyland-experience.ingester.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.ingester.retry.initialDelay", () -> RETRY_DELAY_MS);
    }
}
