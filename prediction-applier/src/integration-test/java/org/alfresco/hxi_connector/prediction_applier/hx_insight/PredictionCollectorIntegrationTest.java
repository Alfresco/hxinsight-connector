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

package org.alfresco.hxi_connector.prediction_applier.hx_insight;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.test.util.LoggingUtils.createLogsListAppender;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.DefaultAccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.prediction_applier.auth.PredictionApplierHxAuthClient;
import org.alfresco.hxi_connector.prediction_applier.config.HxInsightProperties;
import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.config.NodesApiProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionBufferStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionSourceStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionTriggerStub;

@Slf4j
@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"},
        classes = {
                PredictionCollector.class, PredictionCollectorIntegrationTest.IntegrationPropertiesTestConfig.class,
                PredictionSourceStub.class, PredictionTriggerStub.class, PredictionBufferStub.class, DefaultAccessTokenProvider.class,
                PredictionApplierHxAuthClient.class, HxInsightProperties.class, OAuth2ClientProperties.class
        })
@EnableAutoConfiguration
@Testcontainers
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LongVariable"})
class PredictionCollectorIntegrationTest
{

    @Autowired
    private PredictionSourceStub predictionSourceStub;
    @Autowired
    private PredictionTriggerStub predictionTriggerStub;
    @Autowired
    private PredictionBufferStub predictionBufferStub;

    @Container
    private static final WireMockContainer HX_MOCK = DockerContainers.createWireMockContainer();

    @BeforeEach
    void setUp()
    {
        WireMock.configureFor(HX_MOCK.getHost(), HX_MOCK.getPort());
        givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(AuthUtils.createAuthResponseBody())));
    }

    @AfterEach
    void tearDown()
    {
        predictionBufferStub.reset();
    }

    @Test
    void shouldDoNothingIfPredictionsCollectingNotTriggered()
    {
        // given
        List<PredictionEntry> predictionsBatch1 = List.of(makePrediction("1"));
        List<PredictionEntry> predictionsBatch2 = List.of(makePrediction("2"));

        predictionSourceStub.shouldReturnPredictions(predictionsBatch1, predictionsBatch2);

        // when

        // then
        predictionBufferStub.assertAllPredictionsHandled(Collections.emptyList());
    }

    @Test
    void shouldProcessPredictionsIfCollectingTriggered()
    {
        // given
        List<PredictionEntry> predictionsBatch1 = List.of(makePrediction("1"), makePrediction("2"));
        List<PredictionEntry> predictionsBatch2 = List.of(makePrediction("3"), makePrediction("4"));

        predictionSourceStub.shouldReturnPredictions(predictionsBatch1, predictionsBatch2);

        // when
        predictionTriggerStub.triggerPredictionsCollecting();

        // then
        List<PredictionEntry> expectedPredictions = Stream.concat(predictionsBatch1.stream(), predictionsBatch2.stream()).toList();
        predictionBufferStub.assertAllPredictionsHandled(expectedPredictions);
    }

    @Test
    @SneakyThrows
    void shouldIgnoreTriggerSignalIfProcessingIsPending()
    {
        // given
        ListAppender<ILoggingEvent> logs = createLogsListAppender(PredictionCollector.class);

        List<PredictionEntry> predictions = List.of(makePrediction("1"), makePrediction("2"), makePrediction("3"));
        List<List<PredictionEntry>> predictionsBatches = IntStream.range(0, 11).boxed().map(i -> predictions).toList();

        predictionSourceStub.shouldReturnPredictions(5, predictionsBatches);

        // when
        predictionTriggerStub.triggerPredictionsCollectingAsync();
        predictionTriggerStub.triggerPredictionsCollectingAsync(50);

        Thread.sleep(750);
        // then
        List<String> logMessages = logs.list.stream()
                .map(ILoggingEvent::getMessage)
                .toList();

        assertTrue(logMessages.stream().anyMatch(logMessage -> logMessage.contains("Triggering prediction processing")));
        assertTrue(logMessages.stream().anyMatch(logMessage -> logMessage.contains("Prediction processing is pending, no need to trigger it")));
    }

    @NotNull private static PredictionEntry makePrediction(String id)
    {
        return new PredictionEntry(id, id, null, null);
    }

    @DynamicPropertySource
    protected static void overrideProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, HX_MOCK.getBaseUrl(), "hyland-experience-auth");
    }

    @TestConfiguration
    public static class IntegrationPropertiesTestConfig
    {

        @Bean
        public InsightPredictionsProperties insightPredictionsProperties()
        {
            return new InsightPredictionsProperties(
                    "direct:predictions-collector-timer",
                    null,
                    "direct:predictions-source",
                    "direct:predictions-buffer");
        }

        @Bean
        public NodesApiProperties nodesApiProperties()
        {
            return new NodesApiProperties("http://localhost:8002", "dummy-user", "dummy-password", null);
        }
    }
}
