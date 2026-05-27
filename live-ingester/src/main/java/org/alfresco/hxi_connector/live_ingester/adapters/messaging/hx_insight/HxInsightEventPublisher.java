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

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.INFO;

import static org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider.USER_AGENT_DATA;
import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.event.Level;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class HxInsightEventPublisher extends RouteBuilder implements IngestionEngineEventPublisher
{
    private static final String LOCAL_ENDPOINT = "direct:" + HxInsightEventPublisher.class.getSimpleName();
    private static final String ROUTE_ID = "insight-event-publisher";
    private static final int EXPECTED_STATUS_CODE = 202;

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;
    private final AuthService authService;
    private final ApplicationInfoProvider applicationInfoProvider;

    @Override
    public void configure()
    {
        // @formatter:off
        String ingestionEndpoint = buildIngestionEndpoint();
        // onException is log-only. Throwing or otherwise mutating the exchange from inside an
        // onException processor triggers Camel's FatalFallbackErrorHandler, which short-circuits
        // the outer transacted route's error handling. Retry classification for transient I/O
        // is delegated to publishMessage()'s catch block, which calls
        // ErrorUtils.wrapErrorAndThrowIfNecessary against the configured retry reasons set so
        // an in-cause-chain HttpHostConnectException / NoHttpResponseException / etc. is
        // re-thrown as EndpointServerErrorException for @Retryable to match.
        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Ingestion :: Unexpected response - Endpoint: %s".formatted(ingestionEndpoint))
            .process(exchange -> LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR));

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .log(INFO, log, "Ingestion :: Sending event of type: ${body.eventType} for node with ID: ${body.objectId}")
            .marshal()
            .json()
            .setProperty(USER_AGENT_DATA, applicationInfoProvider::getUserAgentData)
            .log(DEBUG, log, "Ingestion :: Sending event: ${body}. Headers: ${headers}. Endpoint: " + ingestionEndpoint)
            .process(authService::setHxIAuthorizationHeaders)
            .toD(ingestionEndpoint)
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    private String buildIngestionEndpoint()
    {
        String base = integrationProperties.hylandExperience().ingester().endpoint();
        int timeoutMs = integrationProperties.hylandExperience().ingester().responseTimeoutMs();
        if (timeoutMs <= 0)
        {
            // 0 means "no per-request timeout"; omit the parameter so the Camel HTTP default applies
            // (matches HttpFileUploader#buildUploadEndpoint, AlfrescoRepositoryContentClient#buildContentEndpoint).
            return base + ApplicationInfoProvider.USER_AGENT_PARAM;
        }
        return base + "&httpClient.responseTimeout=" + timeoutMs + ApplicationInfoProvider.USER_AGENT_PARAM;
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.ingester.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.ingester.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.ingester.retry.delayMultiplier}"))
    @Override
    public void publishMessage(NodeEvent event)
    {
        try
        {
            camelContext.createFluentProducerTemplate()
                    .to(LOCAL_ENDPOINT)
                    .withBody(event)
                    .request();
        }
        catch (CamelExecutionException e)
        {
            classifyAndRethrow(e);
            throw e;
        }
    }

    /**
     * Translates transient I/O causes (e.g. {@code HttpHostConnectException}, {@code NoHttpResponseException}, {@code SocketException}) into {@link EndpointServerErrorException} so {@code @Retryable} on {@link #publishMessage(NodeEvent)} matches and Spring Retry's listener ({@code RetryMetricsRecorder}) records the attempt under the stable {@code exception=EndpointServerErrorException} tag — independent of which concrete Apache HC exception class the underlying Camel HTTP component happened to surface.
     */
    private void classifyAndRethrow(CamelExecutionException e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof Exception causeException)
        {
            ErrorUtils.wrapErrorAndThrowIfNecessary(causeException,
                    integrationProperties.hylandExperience().ingester().retry().reasons(),
                    LiveIngesterRuntimeException.class);
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void throwExceptionOnUnexpectedStatusCode(Exchange exchange)
    {
        int actualStatusCode = exchange.getMessage().getHeader(HTTP_RESPONSE_CODE, Integer.class);
        if (actualStatusCode != EXPECTED_STATUS_CODE)
        {
            log.warn(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(EXPECTED_STATUS_CODE, actualStatusCode));
        }

        ErrorUtils.throwExceptionOnUnexpectedStatusCode(actualStatusCode, EXPECTED_STATUS_CODE);
    }
}
