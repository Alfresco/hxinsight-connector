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

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.config.PredictionListenerConfig;
import org.alfresco.hxi_connector.prediction_applier.util.InternalPredictionBufferStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionSourceStub;
import org.alfresco.hxi_connector.prediction_applier.util.PredictionsTriggerStub;

@Slf4j
@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"},
        classes = {
                HxPredictionReceiver.class, HxPredictionReceiverIntegrationTest.IntegrationPropertiesTestConfig.class,
                InternalPredictionBufferStub.class, PredictionSourceStub.class, PredictionsTriggerStub.class
        })
@EnableAutoConfiguration
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.LongVariable"})
class HxPredictionReceiverIntegrationTest
{
    @Autowired
    private InternalPredictionBufferStub internalPredictionBufferStub;

    @Autowired
    private PredictionSourceStub predictionSourceStub;

    @Autowired
    private PredictionsTriggerStub predictionsTriggerStub;

    @AfterEach
    void tearDown()
    {
        internalPredictionBufferStub.reset();
    }

    @Test
    void shouldDoNothingIfPredictionProcessingNotTriggered()
    {
        // given
        List<Prediction> predictions1 = List.of(new Prediction("1", "1"));
        List<Prediction> predictions2 = List.of(new Prediction("2", "2"));

        predictionSourceStub.shouldReturnPredictions(predictions1, predictions2);

        // then
        internalPredictionBufferStub.assertAllPredictionsHandled(Collections.emptyList());
    }

    @Test
    void shouldProcessPredictionsIfProcessingTriggered()
    {
        // given
        List<Prediction> predictions1 = List.of(new Prediction("1", "1"), new Prediction("2", "2"));
        List<Prediction> predictions2 = List.of(new Prediction("3", "3"), new Prediction("4", "4"));

        List<Prediction> allPredictions = Stream.concat(predictions1.stream(), predictions2.stream()).toList();

        predictionSourceStub.shouldReturnPredictions(predictions1, predictions2);

        // when
        predictionsTriggerStub.triggerPredictionsProcessing();

        // then
        internalPredictionBufferStub.assertAllPredictionsHandled(allPredictions);
    }

    @Test
    @SneakyThrows
    void shouldIgnoreTriggerSignalIfProcessingIsPending()
    {
        // given
        ListAppender<ILoggingEvent> logs = createLogsListAppender(HxPredictionReceiver.class);

        List<Prediction> predictions = List.of(new Prediction("1", "1"), new Prediction("2", "2"), new Prediction("3", "3"));
        List<List<Prediction>> predictionsBatches = IntStream.range(0, 11).boxed().map(i -> predictions).toList();

        predictionSourceStub.shouldReturnPredictions(5, predictionsBatches);

        // when
        predictionsTriggerStub.triggerPredictionsProcessingAsync();
        predictionsTriggerStub.triggerPredictionsProcessingAsync(50);

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
        public PredictionListenerConfig predictionListenerConfig()
        {
            return new PredictionListenerConfig(
                    "direct:prediction-processor-trigger",
                    null,
                    "direct:predictions-source",
                    "direct:internal-predictions-buffer");
        }
    }
}
