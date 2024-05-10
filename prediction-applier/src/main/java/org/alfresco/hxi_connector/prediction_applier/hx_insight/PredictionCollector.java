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
import static org.apache.camel.support.builder.PredicateBuilder.and;

import java.util.Collection;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthSupport;
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
    private static final String DIRECT_ENDPOINT = "direct:" + PredictionCollector.class.getSimpleName();
    private static final String TIMER_ROUTE_ID = "predictions-collector-timer";
    private static final String COLLECTOR_ROUTE_ID = "prediction-collector";
    private static final String IS_PREDICTION_PROCESSING_PENDING_KEY = "is-prediction-processing-pending";
    private static final String BATCH_ID_HEADER = "batchId";
    private static final String BATCHES_PAGE_NO_HEADER = "batchesPageNo";
    private static final String PREDICTIONS_PAGE_NO_HEADER = "predictionsPageNo";
    private static final String BATCHES_URL_PATTERN = "%s/prediction-batches?httpMethod=GET&status=APPROVED&page=${headers.%s}";
    private static final String PREDICTIONS_URL_PATTERN = "%s/prediction-batches/${headers.%s}?httpMethod=GET&page=${headers.%s}";

    private final InsightPredictionsProperties insightPredictionsProperties;

    // @formatter:off
    /**
     * Polls all available prediction batches and sends them (one by one) to the internal buffer for further processing.
     * <p>
     * You can imagine that you have the following predictions waiting in hxi: 1. [prediction1, prediction2] 2. [prediction3, prediction4]
     * <p>
     * Processor PREDICTION_PROCESSOR after being triggered will:
     * 1. Fetch the first batch (prediction1, prediction2)
     *  1.1. Send prediction1 to the internal buffer
     *  1.2. Send prediction2 to the internal buffer
     * 2. Fetch the second batch (prediction3, prediction4)
     *  2.1. Send prediction3 to the internal buffer
     *  2.2. Send prediction4 to the internal buffer
     * 3. Exit and wait for the next trigger
     * <p>
     * By default, the processor is triggered by the quartz scheduler every 5 minutes.
     */
    @Override
    public void configure()
    {
        JacksonDataFormat predictionsBatchDataFormat = new LinkedListJacksonDataFormat(PredictionBatch.class);
        JacksonDataFormat predictionsDataFormat = new LinkedListJacksonDataFormat(PredictionEntry.class);
        JacksonDataFormat predictionDataFormat = new JacksonDataFormat(PredictionEntry.class);

        from(insightPredictionsProperties.collectorTimerEndpoint())
            .routeId(TIMER_ROUTE_ID)
            .delay(5000) // workaround to slow down first call and wait for the authentication to pass. More info in: InsightPredictionsProperties
                .choice().when(this::isProcessingPending)
                    .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
                .otherwise()
                    .log(DEBUG, log, "Triggering prediction processing")
                    .to(DIRECT_ENDPOINT)
                .end()
            .end()
        .end();

        String batchesUrl = BATCHES_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCHES_PAGE_NO_HEADER);
        String predictionsUrl = PREDICTIONS_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCH_ID_HEADER, PREDICTIONS_PAGE_NO_HEADER);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        from(DIRECT_ENDPOINT)
            .routeId(COLLECTOR_ROUTE_ID)
            .process(setProcessingPending(true))
            .onCompletion()
                .process(setProcessingPending(false))
            .end()
            .process(exchange -> AuthSupport.setAuthorizationToken(securityContext, exchange))
            .setBody(constant("temp-val-for-init"))
            .setHeader(BATCHES_PAGE_NO_HEADER, constant(1))
            .loopDoWhile(bodyNotEmpty())
                .log(DEBUG, log, "Calling URL: " + batchesUrl)
                .toD(batchesUrl)
                .log(DEBUG, log, "Processing prediction batches: ${body}")
                .choice().when(bodyNotEmpty())
                    .unmarshal(predictionsBatchDataFormat)
                    .split(body())
                        .setHeader(BATCH_ID_HEADER, simple("${body.id}"))
                        // notify HxI -> started batch processing
                        .setHeader(PREDICTIONS_PAGE_NO_HEADER, constant(1))
                        .loopDoWhile(bodyNotEmpty())
                            .log(DEBUG, log, "Calling URL: " + predictionsUrl)
                            .toD(predictionsUrl)
                            .log(DEBUG, log, "Sending predictions to internal buffer: ${body}")
                            .choice().when(bodyNotEmpty())
                                .unmarshal(predictionsDataFormat)
                                .split(body())
                                    .marshal(predictionDataFormat)
                                    .log(TRACE, log, "Sending prediction to internal buffer: ${body}")
                                    .to(insightPredictionsProperties.bufferEndpoint())
                                .end()
                            .end()
                            .setHeader(PREDICTIONS_PAGE_NO_HEADER).spel("#{request.headers['%s'] + 1}".formatted(PREDICTIONS_PAGE_NO_HEADER))
                        .end()
                        // notify HxI -> finished batch processing
                    .end()
                .end()
                .setHeader(BATCHES_PAGE_NO_HEADER).spel("#{request.headers['%s'] + 1}".formatted(BATCHES_PAGE_NO_HEADER))
            .end()
            .log(DEBUG, log, "Finished processing predictions")
        .end();
    }
    // @formatter:on

    private Predicate bodyNotEmpty()
    {
        return and(body().isNotNull(), bodyAs(Collection.class).method("isEmpty").isEqualTo(false));
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
