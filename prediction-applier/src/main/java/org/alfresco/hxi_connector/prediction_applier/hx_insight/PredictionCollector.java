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

import static org.alfresco.hxi_connector.prediction_applier.hx_insight.HxInsightUrlProducer.*;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.TRACE;
import static org.apache.camel.language.spel.SpelExpression.spel;

import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionBatch;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.util.LinkedListJacksonDataFormat;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter", "PMD.LinguisticNaming", "PMD.LongVariable"})
public class PredictionCollector extends RouteBuilder
{
    private static final String TIMER_ROUTE_ID = "predictions-collector-timer";
    private static final String COLLECTOR_ROUTE_ID = "prediction-collector";
    private static final String BATCH_PROCESSOR_ROUTE_ID = "prediction-batch-processor";
    private static final String BATCHES_PROCESSOR_ENDPOINT = "direct:" + COLLECTOR_ROUTE_ID + "-" + PredictionCollector.class.getSimpleName();
    private static final String PREDICTIONS_PROCESSOR_ENDPOINT = "direct:" + BATCH_PROCESSOR_ROUTE_ID + "-" + PredictionCollector.class.getSimpleName();
    private static final String IS_PREDICTION_PROCESSING_PENDING_KEY = "is-prediction-processing-pending";
    private static final String SET_BATCH_STATUS_BODY_TEMPLATE = "{\"status\": \"%s\", \"currentPage\": ${headers.%s}}";
    private static final String IN_PROGRESS_BATCH_STATUS_BODY = SET_BATCH_STATUS_BODY_TEMPLATE.formatted("IN_PROGRESS", PREDICTIONS_PAGE_NO_HEADER);
    private static final String COMPLETE_BATCH_STATUS_BODY = SET_BATCH_STATUS_BODY_TEMPLATE.formatted("COMPLETE", PREDICTIONS_PAGE_NO_HEADER);

    private final InsightPredictionsProperties insightPredictionsProperties;
    private final HxInsightUrlProducer hxInsightUrlProducer;
    private final AuthService authService;

    // @formatter:off
    /**
     * Polls all available prediction batches and sends them (one by one) to the internal buffer for further processing.
     * <a href="https://hyland.atlassian.net/wiki/spaces/HxAI/pages/1387364585/Insight+Enrichment+API+Design">Hx Insight api design</a>
     */
    @Override
    public void configure()
    {
        from(insightPredictionsProperties.collectorTimerEndpoint())
            .routeId(TIMER_ROUTE_ID)
            .choice().when(this::isProcessingPending)
                .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
            .otherwise()
                .log(DEBUG, log, "Triggering prediction processing")
                .to(BATCHES_PROCESSOR_ENDPOINT);

        String batchesUrl = hxInsightUrlProducer.getBatchesUrl();
        JacksonDataFormat predictionsBatchDataFormat = new LinkedListJacksonDataFormat(PredictionBatch.class);

        from(BATCHES_PROCESSOR_ENDPOINT)
            .routeId(COLLECTOR_ROUTE_ID)
            .process(setProcessingPending(true))
            .onCompletion()
                .process(setProcessingPending(false))
            .end()
            .setHeader(BATCHES_PAGE_NO_HEADER, constant(1))
            .loopDoWhile(statusCodeNot204())
                .process(authService::setHxIAuthorizationHeaders)
                .toD(batchesUrl)
                .choice().when(statusCodeNot204())
                    .log(DEBUG, log, "Processing prediction batches page ${headers.%s} started".formatted(BATCHES_PAGE_NO_HEADER))
                    .unmarshal(predictionsBatchDataFormat)
                    .split(body())
                        .to(PREDICTIONS_PROCESSOR_ENDPOINT)
                    .end()
                    .setHeader(BATCHES_PAGE_NO_HEADER, spel("#{request.headers['%s'] + 1}".formatted(BATCHES_PAGE_NO_HEADER)))
                    .toD(batchesUrl)
                .end()
            .end()
            .log(DEBUG, log, "Finished processing predictions");

        String predictionsUrl = hxInsightUrlProducer.getPredictionsUrl();
        String predictionsConfirmationUrl = hxInsightUrlProducer.getConfirmationUrl();
        JacksonDataFormat predictionsDataFormat = new LinkedListJacksonDataFormat(PredictionEntry.class);
        JacksonDataFormat predictionDataFormat = new JacksonDataFormat(PredictionEntry.class);

        from(PREDICTIONS_PROCESSOR_ENDPOINT)
            .routeId(BATCH_PROCESSOR_ROUTE_ID)
            .log(DEBUG, log, "Processing prediction batch ${body.id} started")
            .setHeader(BATCH_ID_HEADER, simple("${body.id}"))
            .setHeader(PREDICTIONS_PAGE_NO_HEADER, constant(1))
            .loopDoWhile(statusCodeNot204())
                .process(authService::setHxIAuthorizationHeaders)
                .toD(predictionsUrl)
                .choice().when(statusCodeNot204())
                    .log(TRACE, log, "Processing page ${headers.%s} of predictions in batch ${headers.%s}, ${body}, ${header.CamelHttpResponseCode}".formatted(PREDICTIONS_PAGE_NO_HEADER, BATCH_ID_HEADER))
                    .unmarshal(predictionsDataFormat)
                    .split(body())
                        .marshal(predictionDataFormat)
                        .to(insightPredictionsProperties.bufferEndpoint())
                    .end()
                    .setBody(simple(IN_PROGRESS_BATCH_STATUS_BODY))
                    .toD(predictionsConfirmationUrl)
                    .log(TRACE, log, "Processing prediction batch ${headers.%s} page ${headers.%s} completed".formatted(BATCH_ID_HEADER, PREDICTIONS_PAGE_NO_HEADER))
                    .setHeader(PREDICTIONS_PAGE_NO_HEADER).spel("#{request.headers['%s'] + 1}".formatted(PREDICTIONS_PAGE_NO_HEADER))
                .end()
            .end()
            .setBody(simple(COMPLETE_BATCH_STATUS_BODY))
            .toD(predictionsConfirmationUrl)
            .log(DEBUG, log, "Processing prediction batch ${headers.%s} finished".formatted(BATCH_ID_HEADER));
    }
    // @formatter:on

    private ValueBuilder statusCodeNot204()
    {
        return simple("${header.CamelHttpResponseCode} != 204");
    }

    private boolean isProcessingPending(Exchange exchange)
    {
        return Objects.requireNonNullElse(
                getContext().getRegistry().lookupByNameAndType(IS_PREDICTION_PROCESSING_PENDING_KEY, Boolean.class),
                false);
    }

    private Processor setProcessingPending(boolean isProcessingPending)
    {
        return exchange -> getContext().getRegistry().bind(IS_PREDICTION_PROCESSING_PENDING_KEY, isProcessingPending);
    }
}
