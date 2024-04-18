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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
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

@Slf4j
@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"},
        classes = {
                HxPredictionReceiver.class, HxPredictionReceiverTest.IntegrationPropertiesTestConfig.class,
                InternalPredictionBufferStub.class, PredictionSourceStub.class
        })
@EnableAutoConfiguration
@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.FieldNamingConventions"})
class HxPredictionReceiverTest
{
    public static final String TRIGGER_ENDPOINT = "direct:prediction-processor-trigger";
    public static final String PREDICTIONS_SOURCE_ENDPOINT = "direct:predictions-source";
    public static final String INTERNAL_PREDICTIONS_BUFFER_ENDPOINT = "direct:internal-predictions-buffer";

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private InternalPredictionBufferStub internalPredictionBufferStub;

    @Autowired
    private PredictionSourceStub predictionSourceStub;

    @AfterEach
    void tearDown()
    {
        internalPredictionBufferStub.reset();
    }

    @Test
    void shouldDoNothingIfPredictionProcessingNotTriggered()
    {
        // given
        List<Prediction> predictions = List.of(new Prediction("1", "1"));

        predictionSourceStub.shouldReturnPredictions(predictions);

        // then
        internalPredictionBufferStub.assertAllPredictionsHandled(Collections.emptyList());
    }

    @Test
    void shouldProcessPredictionsIfProcessingTriggered()
    {
        // given
        List<Prediction> predictions = List.of(
                new Prediction("1", "1"),
                new Prediction("2", "2"));

        predictionSourceStub.shouldReturnPredictions(predictions);

        // when
        triggerPredictionsProcessing();

        // then
        internalPredictionBufferStub.assertAllPredictionsHandled(predictions);
    }

    @RepeatedTest(100)
    void shouldIgnoreTriggerSignalIfProcessingIsPending()
    {
        // given
        ListAppender<ILoggingEvent> logs = createLogsListAppender(HxPredictionReceiver.class);

        List<Prediction> predictions = List.of(
                new Prediction("1", "1"),
                new Prediction("2", "2"));

        predictionSourceStub.shouldReturnPredictions(10, predictions);

        // when
        new Thread(() -> triggerPredictionsProcessing(5)).start();
        triggerPredictionsProcessing();

        // then
        internalPredictionBufferStub.assertAllPredictionsHandled(predictions);

        List<String> logMessages = logs.list.stream()
                .map(ILoggingEvent::getMessage)
                .toList();

        assertTrue(logMessages.get(0).contains("Triggering prediction processing"));
        assertTrue(logMessages.get(1).contains("Prediction processing is pending, no need to trigger it"));
        assertTrue(logMessages.get(2).contains("Sending predictions to internal buffer"));
        assertTrue(logMessages.get(3).contains("Sending predictions to internal buffer: []"));
        assertTrue(logMessages.get(4).contains("Finished processing predictions"));
    }

    @SneakyThrows
    private void triggerPredictionsProcessing(long delayInMs)
    {
        Thread.sleep(delayInMs);
        triggerPredictionsProcessing();
    }

    private void triggerPredictionsProcessing()
    {
        producerTemplate.sendBody(TRIGGER_ENDPOINT, null);
    }

    @TestConfiguration
    public static class IntegrationPropertiesTestConfig
    {
        @Bean
        public PredictionListenerConfig predictionListenerConfig()
        {
            return new PredictionListenerConfig(
                    TRIGGER_ENDPOINT,
                    PREDICTIONS_SOURCE_ENDPOINT,
                    INTERNAL_PREDICTIONS_BUFFER_ENDPOINT);
        }
    }
}
