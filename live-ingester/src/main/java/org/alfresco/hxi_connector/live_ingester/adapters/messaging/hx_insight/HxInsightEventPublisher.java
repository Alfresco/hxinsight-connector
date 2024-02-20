/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import static org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationService.setAuthorizationToken;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class HxInsightEventPublisher extends RouteBuilder implements IngestionEngineEventPublisher
{
    private static final String LOCAL_ENDPOINT = "direct:" + HxInsightEventPublisher.class.getSimpleName();
    private static final String ROUTE_ID = "insight-event-publisher";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        // @formatter:off
        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Body: ${body}")
            .process(this::wrapErrorIfNecessary)
            .stop();

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .marshal()
            .json()
            .log("Sending event ${body}")
            .to(integrationProperties.hylandExperience().ingester().endpoint())
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER')")
    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.ingester.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.ingester.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.ingester.retry.delayMultiplier}"))
    @Override
    public void publishMessage(NodeEvent event)
    {
        camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .withProcessor(exchange -> {
                    exchange.getIn().setBody(event);
                    setAuthorizationToken(exchange);
                })
                .request();
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

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void wrapErrorIfNecessary(Exchange exchange)
    {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Set<Class<? extends Throwable>> retryReasons = integrationProperties.hylandExperience().ingester().retry().reasons();

        ErrorUtils.wrapErrorIfNecessary(cause, retryReasons);
    }
}
