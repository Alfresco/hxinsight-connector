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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.WARN;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.exception.ResourceNotFoundException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.ATSTransformRequester;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DeadLetterChannels;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DlqMetric;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DlqMetricsRecorder;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LiveIngesterMetrics;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.EmptyRenditionException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.FailedTransformResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformResponseHandler extends RouteBuilder
{
    private static final String ROUTE_ID = "transform-events-consumer";
    private static final int EXPECTED_STATUS_CODE = 201;
    private static final int FAILED_TRANSFORM_STATUS_CODE = 400;
    static final String EXPECTED_STATUS_CODE_REGEX = "[\\s\\S]*\"status\"\\s*:\\s*%s[^0-9][\\s\\S]*".formatted(EXPECTED_STATUS_CODE);
    private static final DlqMetric DLQ_METRIC = new DlqMetric(
            LiveIngesterMetrics.Dlq.TRANSFORM_RESPONSE,
            LiveIngesterMetrics.Dlq.TRANSFORM_RESPONSE_DESCRIPTION);
    static final String SILENT_DROP_LOG_FRAGMENT = "Transform :: Silently dropped failed transform-response";

    private final IngestContentCommandHandler ingestContentCommandHandler;
    private final IntegrationProperties integrationProperties;
    private final ATSTransformRequester atsTransformRequester;
    private final DlqMetricsRecorder dlqMetricsRecorder;
    private final MeterRegistry meterRegistry;

    @Override
    public void configure()
    {
        Transform.Response response = integrationProperties.alfresco().transform().response();
        String transformationSource = response.endpoint();

        if (response.deadLetterEnabled())
        {
            errorHandler(DeadLetterChannels.forRoute(response, dlqMetricsRecorder, DLQ_METRIC, log));
            if (response.retryIngestion().attempts() < 0)
            {
                log.warn("Transform :: dead-letter channel enabled but alfresco.transform.response.retry-ingestion.attempts={} (unbounded) — DLC will never be reached. Set a finite value (e.g. 6).",
                        response.retryIngestion().attempts());
            }
        }
        else
        {
            log.info("Transform :: dead-letter channel disabled (default). Set alfresco.transform.response.dead-letter-enabled=true to enable.");
        }

        onException(EmptyRenditionException.class, ResourceNotFoundException.class)
                .log(WARN, log, "Transform :: Unexpected state while processing rendition from: %s due to: ${exception.message}. Body: ${body}".formatted(transformationSource))
                .process(this::retryContentTransformation);

        // Deterministic transform failures (status=400) — opt-in via throwFailedTransforms. Skip retries
        // (deterministic, no point) and ACK the message. With deadLetterEnabled=true the exchange is also
        // copied to the DLQ + counter incremented for operator inventory; without it, only the WARN logs
        // remain (matches the default-deployment silent-drop contract minus the opt-in throw signal).
        onException(FailedTransformResponseException.class)
                .log(WARN, log, "Transform :: Deterministic transform failure: ${exception.message}. Routing to error handler without retry.")
                .handled(true)
                .process(this::handleDeterministicTransformFailure);

        // The broad onException governs in-route redelivery (retry-ingestion config). When the dead-letter
        // channel is enabled the chain also consumes the exhausted exchange and copies it to the DLQ
        // explicitly: Camel's onException policy short-circuits the route's errorHandler for matched
        // exceptions, so the route-level deadLetterChannel never fires on its own here.
        var broadOnException = onException(Exception.class)
                .log(ERROR, log, "Transform :: Retrying ${routeId}, attempt ${header.CamelRedeliveryCounter} due to ${exception.message}. Body: ${body}")
                .maximumRedeliveries(response.retryIngestion().attempts())
                .redeliveryDelay(response.retryIngestion().initialDelay())
                .backOffMultiplier(response.retryIngestion().delayMultiplier());

        if (response.deadLetterEnabled())
        {
            broadOnException
                    .handled(true)
                    .process(exchange -> {
                        LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR);
                        dlqMetricsRecorder.record(exchange, DLQ_METRIC);
                    })
                    .to(response.deadLetterUri());
        }

        from(transformationSource)
                .routeId(ROUTE_ID)
                .choice().when(body().regex(EXPECTED_STATUS_CODE_REGEX))
                .log(DEBUG, log, "Transform :: Received transform completed event: ${body}")
                .otherwise()
                .log(WARN, log, "Transform :: Transformation failed. Body: ${body}")
                .end()
                .unmarshal()
                .json(JsonLibrary.Jackson, TransformResponse.class)
                .process(this::ingestContent)
                .end();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void ingestContent(Exchange exchange)
    {
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);
        if (transformResponse.status() == FAILED_TRANSFORM_STATUS_CODE)
        {
            Transform.Response config = integrationProperties.alfresco().transform().response();
            if (config.throwFailedTransforms())
            {
                throw new FailedTransformResponseException(
                        transformResponse.clientData().nodeRef(),
                        transformResponse.status(),
                        transformResponse.errorDetails());
            }
            recordSilentDrop(transformResponse);
            return;
        }

        IngestContentCommand command = new IngestContentCommand(
                transformResponse.targetReference(),
                transformResponse.clientData().nodeRef(),
                transformResponse.clientData().targetMimeType(),
                transformResponse.clientData().timestamp());

        ingestContentCommandHandler.handle(command);
    }

    /**
     * Failure processor for deterministic transform failures handled via {@code onException(FailedTransformResponseException.class)}: mirrors the {@link DeadLetterChannels#forRoute} on-prepare callback (masked exchange-state log + Micrometer counter) and, when {@code deadLetterEnabled=true}, copies the original exchange to the configured dead-letter URI for operator inventory. With the DLC opt-in off, only the WARN line and the route-level {@code Transformation failed} log survive — matching the default-deployment silent-drop contract minus the opt-in's diagnostic value.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleDeterministicTransformFailure(Exchange exchange)
    {
        LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR);
        Transform.Response config = integrationProperties.alfresco().transform().response();
        if (config.deadLetterEnabled())
        {
            dlqMetricsRecorder.record(exchange, DLQ_METRIC);
            exchange.getContext().createProducerTemplate().send(config.deadLetterUri(), exchange);
        }
    }

    private void recordSilentDrop(TransformResponse transformResponse)
    {
        log.info("{} for nodeRef={} status={} errorDetails={}",
                SILENT_DROP_LOG_FRAGMENT,
                transformResponse.clientData().nodeRef(),
                transformResponse.status(),
                transformResponse.errorDetails());
        Counter.builder(LiveIngesterMetrics.Drop.TRANSFORM_RESPONSE_SILENT)
                .description(LiveIngesterMetrics.Drop.TRANSFORM_RESPONSE_SILENT_DESCRIPTION)
                .register(meterRegistry)
                .increment();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void retryContentTransformation(Exchange exchange)
    {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);

        int maxAttempts = integrationProperties.alfresco().transform().response().retryTransformation().attempts();
        int retryAttempt = transformResponse.clientData().retryAttempt() + 1;

        if (retryAttempt > maxAttempts)
        {
            log.error("Transform :: Transformation of node {} failed with error {}, max number of retries ({}) exceeded", transformResponse.clientData().nodeRef(), exception.getMessage(), maxAttempts);
            return;
        }

        log.info("Transform :: Transformation of node {} failed with error {}, retrying (attempt: {})", transformResponse.clientData().nodeRef(), exception.getMessage(), retryAttempt);

        TransformRequest transformRequest = new TransformRequest(
                transformResponse.clientData().nodeRef(),
                transformResponse.clientData().targetMimeType(),
                transformResponse.clientData().timestamp());
        atsTransformRequester.requestTransformRetry(transformRequest, retryAttempt);
    }
}
