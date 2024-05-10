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

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.test.util.LoggingUtils.createLogsListAppender;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionBufferStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionSourceStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionTriggerStub;

@Slf4j
@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"},
        classes = {
                PredictionCollector.class, PredictionCollectorIntegrationTest.IntegrationPropertiesTestConfig.class,
                PredictionSourceStub.class, PredictionTriggerStub.class, PredictionBufferStub.class
        })
@EnableAutoConfiguration
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LongVariable"})
class PredictionCollectorIntegrationTest
{

    @Autowired
    private PredictionSourceStub predictionSourceStub;
    @Autowired
    private PredictionTriggerStub predictionTriggerStub;
    @Autowired
    private PredictionBufferStub predictionBufferStub;

    @AfterEach
    void tearDown()
    {
        predictionBufferStub.reset();
    }

    @Test
    void shouldDoNothingIfPredictionsCollectingNotTriggered()
    {
        // given
        List<PredictionEntry> predictionsBatch1 = List.of(new PredictionEntry("1", "1", null, null));
        List<PredictionEntry> predictionsBatch2 = List.of(new PredictionEntry("2", "2", null, null));

        predictionSourceStub.shouldReturnPredictions(predictionsBatch1, predictionsBatch2);

        // when

        // then
        predictionBufferStub.assertAllPredictionsHandled(Collections.emptyList());
    }

    @Test
    void shouldProcessPredictionsIfCollectingTriggered()
    {
        // given
        List<PredictionEntry> predictionsBatch1 = List.of(new PredictionEntry("1", "1", null, null), new PredictionEntry("2", "2", null, null));
        List<PredictionEntry> predictionsBatch2 = List.of(new PredictionEntry("3", "3", null, null), new PredictionEntry("4", "4", null, null));

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

        List<PredictionEntry> predictions = List.of(new PredictionEntry("1", "1", null, null), new PredictionEntry("2", "2", null, null), new PredictionEntry("3", "3", null, null));
        List<List<PredictionEntry>> predictionsBatches = IntStream.range(0, 11).boxed().map(i -> predictions).toList();

        predictionSourceStub.shouldReturnPredictions(5, predictionsBatches);

        // when
        predictionTriggerStub.triggerPredictionsCollectingAsync();
        predictionTriggerStub.triggerPredictionsCollectingAsync(50);

        Thread.sleep(75);
        // then
        List<String> logMessages = logs.list.stream()
                .map(ILoggingEvent::getMessage)
                .toList();

        assertTrue(logMessages.stream().anyMatch(logMessage -> logMessage.contains("Triggering prediction processing")));
        assertTrue(logMessages.stream().anyMatch(logMessage -> logMessage.contains("Prediction processing is pending, no need to trigger it")));
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
    }
}
