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

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.TRACE;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.config.PredictionListenerConfig;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter", "PMD.LinguisticNaming", "PMD.LongVariable"})
public class HxPredictionReceiver extends RouteBuilder
{
    private static final String PREDICTION_PROCESSOR_TRIGGER_ROUTE_ID = "prediction-processor-trigger-route";
    private static final String PREDICTION_PROCESSOR_ROUTE_ID = "prediction-processor-route";
    private static final String PREDICTION_PROCESSOR = "direct:prediction-processor";
    private static final String IS_PREDICTION_PROCESSING_PENDING_KEY = "is-prediction-processing-pending";
    private static final String HAS_NEXT_PAGE_KEY = "has-next-page";
    private static final String PREDICTIONS_BATCH_KEY = "predictions-batch";

    private final PredictionListenerConfig config;

    /**
     * Polls all available prediction batches and sends them (one by one) to the internal buffer for further processing.
     * <p>
     * You can imagine that you have the following predictions waiting in hxi: 1. [prediction1, prediction2] 2. [prediction3, prediction4]
     * <p>
     * Processor PREDICTION_PROCESSOR after being triggered will: 1. Fetch the first batch (prediction1, prediction2) 1.1. Send prediction1 to the internal buffer 1.2. Send prediction2 to the internal buffer 2. Fetch the second batch (prediction3, prediction4) 2.1. Send prediction3 to the internal buffer 2.2. Send prediction4 to the internal buffer 3. Exit and wait for the next trigger
     * <p>
     * By default, the processor is triggered by the quartz scheduler every 5 minutes.
     */
    @Override
    public void configure()
    {
        // @formatter:off
        JacksonDataFormat predictionsBatchDataFormat = new ListJacksonDataFormat(Prediction.class);
        JacksonDataFormat predictionDataFormat = new JacksonDataFormat(Prediction.class);

        from(config.predictionProcessorTriggerEndpoint())
                .routeId(PREDICTION_PROCESSOR_TRIGGER_ROUTE_ID)
                .choice()
                .when(this::isProcessingPending)
                    .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
                .otherwise()
                    .log(DEBUG, log, "Triggering prediction processing")
                    .to(PREDICTION_PROCESSOR);

        from(PREDICTION_PROCESSOR)
                .routeId(PREDICTION_PROCESSOR_ROUTE_ID)
                .process(setIsProcessingPending(true))
                .loopDoWhile(this::hasNextPage)
                    .log(DEBUG, log, "Fetching predictions")
                    .to(config.hxiPredictionsEndpoint())
                    .log(DEBUG, log, "Sending predictions to internal buffer: ${body}")
                    .unmarshal(predictionsBatchDataFormat)
                    .process(this::savePredictionsBatch)
                    .loopDoWhile(this::predictionsBatchNotEmpty)
                        .process(this::setPredictionToSend)
                        .marshal(predictionDataFormat)
                        .log(TRACE, log, "Sending prediction to internal buffer: ${body}")
                        .to(config.internalPredictionsBufferEndpoint())
                    .end()
                .end()
                .log(DEBUG, log, "Finished processing predictions")
                .process(setIsProcessingPending(false));
        // @formatter:on
    }

    private boolean isProcessingPending(Exchange exchange)
    {
        return Objects.requireNonNullElse(
                getContext().getRegistry().lookupByNameAndType(IS_PREDICTION_PROCESSING_PENDING_KEY, Boolean.class),
                false);
    }

    private Processor setIsProcessingPending(boolean isProcessingPending)
    {
        return exchange -> getContext().getRegistry().bind(IS_PREDICTION_PROCESSING_PENDING_KEY, isProcessingPending);
    }

    private boolean hasNextPage(Exchange exchange)
    {
        return Objects.requireNonNullElse(exchange.getVariable(HAS_NEXT_PAGE_KEY, Boolean.class), true);
    }

    private void savePredictionsBatch(Exchange exchange)
    {
        Queue<Prediction> predictionsBatch = new LinkedList(exchange.getIn().getBody(List.class));

        exchange.setVariable(HAS_NEXT_PAGE_KEY, !predictionsBatch.isEmpty());
        exchange.setVariable(PREDICTIONS_BATCH_KEY, predictionsBatch);
    }

    private boolean predictionsBatchNotEmpty(Exchange exchange)
    {
        return !exchange.getVariable(PREDICTIONS_BATCH_KEY, Queue.class).isEmpty();
    }

    private void setPredictionToSend(Exchange exchange)
    {
        Prediction predictionToSend = (Prediction) exchange.getVariable(PREDICTIONS_BATCH_KEY, Queue.class).poll();
        exchange.getIn().setBody(predictionToSend);
    }
}
