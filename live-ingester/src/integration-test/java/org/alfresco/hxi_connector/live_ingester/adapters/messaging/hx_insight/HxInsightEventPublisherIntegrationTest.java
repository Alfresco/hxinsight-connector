/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import java.time.Instant;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.ingest.IngestHttpClient;
import org.hyland.sdk.cic.ingest.IngestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.adapters.config.LiveIngestService;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.mapper.NodeEventToIngestEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;

@WireMockTest
class HxInsightEventPublisherIntegrationTest
{
    private static final String INGEST_PATH = "/v2/ingestion-events";
    private static final String TOKEN_PATH = "/token";
    private static final String NODE_ID = "node-id";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();
    private static final NodeEvent NODE_EVENT = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);

    private HxInsightEventPublisher publisher;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmInfo)
    {
        String baseUrl = wmInfo.getHttpBaseUrl();

        stubTokenEndpoint();

        AuthenticationHttpClient.Builder authBuilder = AuthenticationHttpClient.from(baseUrl + TOKEN_PATH)
                .clientId("test-client")
                .clientSecret("test-secret");

        IngestHttpClient ingestHttpClient = IngestHttpClient.from(baseUrl, authBuilder)
                .sourceId(SOURCE_ID)
                .retryPolicy(RetryPolicy.none())
                .build();

        IngestService ingestService = new IngestService(ingestHttpClient);
        LiveIngestService liveIngestService = new LiveIngestService(null);
        liveIngestService.setDelegate(ingestService);

        publisher = new HxInsightEventPublisher(liveIngestService, new NodeEventToIngestEventMapper());
    }

    @Test
    void testPublishMessage()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(aResponse().withStatus(202)));

        // when
        Throwable thrown = catchThrowable(() -> publisher.publishMessage(NODE_EVENT));

        // then
        WireMock.verify(postRequestedFor(urlPathEqualTo(INGEST_PATH))
                .withRequestBody(containing(NODE_ID)));
        assertThat(thrown).isNull();
    }

    @Test
    void testPublishMessage_serverError()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(aResponse().withStatus(500).withBody("{\"error\": \"Internal Server Error\"}")));

        // when
        Throwable thrown = catchThrowable(() -> publisher.publishMessage(NODE_EVENT));

        // then
        assertThat(thrown).isInstanceOf(CICSdkException.class);
    }

    @Test
    void testPublishMessage_clientError()
    {
        // given
        givenThat(post(INGEST_PATH)
                .willReturn(aResponse().withStatus(400).withBody("{\"error\": \"Bad request\"}")));

        // when
        Throwable thrown = catchThrowable(() -> publisher.publishMessage(NODE_EVENT));

        // then
        assertThat(thrown).isInstanceOf(CICSdkException.class);
    }

    private static void stubTokenEndpoint()
    {
        givenThat(post(TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\": \"test-token\", \"token_type\": \"Bearer\", \"expires_in\": 3600}")));
    }
}
