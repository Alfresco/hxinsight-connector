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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.adapters.auth.AccessTokenProvider;
import org.alfresco.hxi_connector.prediction_applier.config.HxInsightProperties;
import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionBatch;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.util.LinkedListJacksonDataFormat;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.TRACE;
import static org.apache.camel.language.spel.SpelExpression.spel;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter", "PMD.LinguisticNaming", "PMD.LongVariable"})
public class PredictionCollector extends RouteBuilder
{
    private static final String TIMER_ROUTE_ID = "predictions-collector-timer";
    private static final String COLLECTOR_ROUTE_ID = "prediction-collector";
    private static final String BATCH_PROCESSOR_ROUTE_ID = "prediction-batch-processor";
    private static final String PREDICTIONS_PROCESSOR_ENDPOINT = "direct:" + COLLECTOR_ROUTE_ID + "-" + PredictionCollector.class.getSimpleName();
    private static final String BATCH_PROCESSOR_ENDPOINT = "direct:" + BATCH_PROCESSOR_ROUTE_ID + "-" + PredictionCollector.class.getSimpleName();
    private static final String IS_PREDICTION_PROCESSING_PENDING_KEY = "is-prediction-processing-pending";
    private static final String BATCH_ID_HEADER = "batchId";
    private static final String BATCHES_PAGE_NO_HEADER = "batchesPageNo";
    private static final String PREDICTIONS_PAGE_NO_HEADER = "predictionsPageNo";
    private static final String BATCHES_URL_PATTERN = "%s/v1/prediction-batches?httpMethod=GET&status=APPROVED&page=${headers.%s}";
    private static final String PREDICTIONS_URL_PATTERN = "%s/v1/prediction-batches/${headers.%s}?httpMethod=GET&page=${headers.%s}";
    private static final String PREDICTIONS_CONFIRMATION_URL_PATTERN = "%s/v1/prediction-batches/${headers.%s}?httpMethod=PUT";
    private static final String ENVIRONMENT_HEADER = "hxai-environment";

    private final InsightPredictionsProperties insightPredictionsProperties;
    private final AccessTokenProvider accessTokenProvider;
    private final HxInsightProperties hxInsightProperties;

    // @formatter:off
    /**
     * Polls all available prediction batches and sends them (one by one) to the internal buffer for further processing.
     * <a href="https://hyland.atlassian.net/wiki/spaces/HxAI/pages/1387364585/Insight+Enrichment+API+Design">Hx Insight api design</a>
     */
    @Override
    public void configure()
    {
        JacksonDataFormat predictionsBatchDataFormat = new LinkedListJacksonDataFormat(PredictionBatch.class);
        JacksonDataFormat predictionsDataFormat = new LinkedListJacksonDataFormat(PredictionEntry.class);
        JacksonDataFormat predictionDataFormat = new JacksonDataFormat(PredictionEntry.class);

        from(insightPredictionsProperties.collectorTimerEndpoint())
            .routeId(TIMER_ROUTE_ID)
            .choice().when(this::isProcessingPending)
                .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
            .otherwise()
                .log(DEBUG, log, "Triggering prediction processing")
                .to(PREDICTIONS_PROCESSOR_ENDPOINT);

        String batchesUrl = BATCHES_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCHES_PAGE_NO_HEADER);
        String predictionsUrl = PREDICTIONS_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCH_ID_HEADER, PREDICTIONS_PAGE_NO_HEADER);
        String predictionsConfirmationUrl = PREDICTIONS_CONFIRMATION_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCH_ID_HEADER);

        from(PREDICTIONS_PROCESSOR_ENDPOINT)
            .routeId(COLLECTOR_ROUTE_ID)
            .process(setProcessingPending(true))
            .onCompletion()
                .process(setProcessingPending(false))
            .end()
            .process(this::setAuthorizationHeaders)
            .setHeader(BATCHES_PAGE_NO_HEADER, constant(1))
            .loopDoWhile(statusCodeNot204())
                .toD(batchesUrl)
                .choice().when(statusCodeNot204())
                    .log(DEBUG, log, "Processing prediction batches page ${headers.%s} started".formatted(BATCHES_PAGE_NO_HEADER))
                    .unmarshal(predictionsBatchDataFormat)
                    .split(body())
                        .to(BATCH_PROCESSOR_ENDPOINT)
                    .end()
                    .setHeader(BATCHES_PAGE_NO_HEADER, spel("#{request.headers['%s'] + 1}".formatted(BATCHES_PAGE_NO_HEADER)))
                    .toD(batchesUrl)
                .end()
            .end()
            .log(DEBUG, log, "Finished processing predictions");

        from(BATCH_PROCESSOR_ENDPOINT)
                .routeId(BATCH_PROCESSOR_ROUTE_ID)
                .log(DEBUG, log, "Processing prediction batch ${body.id} started")
                .setHeader(BATCH_ID_HEADER, simple("${body.id}"))
                .setHeader(PREDICTIONS_PAGE_NO_HEADER, constant(1))
                .loopDoWhile(statusCodeNot204())
                    .toD(predictionsUrl)
                    .choice().when(statusCodeNot204())
                        .log(TRACE, log, "Processing page ${headers.%s} of predictions in batch ${headers.%s}, ${body}, ${header.CamelHttpResponseCode}".formatted(PREDICTIONS_PAGE_NO_HEADER, BATCH_ID_HEADER))
                        .unmarshal(predictionsDataFormat)
                        .split(body())
                            .marshal(predictionDataFormat)
                            .to(insightPredictionsProperties.bufferEndpoint())
                        .end()
                        .setBody(simple("{\"status\": \"COMPLETE\", \"currentPage\": ${headers.%s}}".formatted(PREDICTIONS_PAGE_NO_HEADER)))
                        .toD(predictionsConfirmationUrl)
                        .log(TRACE, log, "Processing prediction batch ${headers.%s} page ${headers.%s} completed".formatted(BATCH_ID_HEADER, PREDICTIONS_PAGE_NO_HEADER))
                        .setHeader(PREDICTIONS_PAGE_NO_HEADER).spel("#{request.headers['%s'] + 1}".formatted(PREDICTIONS_PAGE_NO_HEADER))
                    .end()
                .end()
                .log(DEBUG, log, "Processing prediction batch ${headers.%s} finished".formatted(BATCH_ID_HEADER));
    }
    // @formatter:on

    private ValueBuilder statusCodeNot204() {
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

    private void setAuthorizationHeaders(Exchange exchange)
    {
        final String token = "Bearer " + accessTokenProvider.getAccessToken("hyland-experience-auth");
        String environmentKey = hxInsightProperties.hylandExperience().authorization().environmentKey();
        exchange.getIn().setHeader(AUTHORIZATION, token);
        exchange.getIn().setHeader(ENVIRONMENT_HEADER, environmentKey);
    }
}
