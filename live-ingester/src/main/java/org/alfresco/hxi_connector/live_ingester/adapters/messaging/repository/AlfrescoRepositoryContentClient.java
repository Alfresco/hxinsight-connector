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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.ERROR;

import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.event.Level;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.repository.RepositoryContentStorage;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

/**
 * Client for downloading content directly from Alfresco repository. Used when content does not require transformation (source and target MIME types match).
 */
@Component
@RequiredArgsConstructor
public class AlfrescoRepositoryContentClient extends RouteBuilder implements RepositoryContentStorage
{
    private static final String LOCAL_ENDPOINT = "direct:" + AlfrescoRepositoryContentClient.class.getSimpleName();
    private static final String ROUTE_ID = "repository-content-downloader";
    private static final int EXPECTED_STATUS_CODE = 200;
    private static final String NODE_ID_HEADER = "nodeId";
    private static final String CONTENT_ENDPOINT_PATTERN = "%s/api/-default-/public/alfresco/versions/1/nodes/${headers." + NODE_ID_HEADER + "}/content?httpMethod=GET&throwExceptionOnFailure=false";

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;
    private final AuthService authService;

    @Override
    public void configure()
    {
        String contentEndpoint = CONTENT_ENDPOINT_PATTERN.formatted(integrationProperties.alfresco().repository().baseUrl());
        onException(Exception.class)
                .log(ERROR, log, "Repository :: Unexpected response while downloading content - Endpoint: %s".formatted(contentEndpoint))
                .process(exchange -> LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR))
                .process(this::wrapErrorIfNecessary)
                .stop();

        // @formatter:off
        from(LOCAL_ENDPOINT)
                .id(ROUTE_ID)
                .process(this::setAuthorizationHeader)
                .toD(contentEndpoint)
                .choice()
                .when(header(HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                    .process(this::convertBodyToFile)
                .otherwise()
                    .process(this::throwExceptionOnUnexpectedStatusCode)
                .endChoice()
                .end();
        // @formatter:on
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.alfresco.transform.sharedFileStore.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.alfresco.transform.sharedFileStore.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.alfresco.transform.sharedFileStore.retry.delayMultiplier}"))
    @Override
    public File downloadContent(String nodeId)
    {
        log.atDebug().log("Repository :: Downloading content directly for node: {}", nodeId);
        return camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .withHeader(NODE_ID_HEADER, nodeId)
                .request(File.class);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void setAuthorizationHeader(Exchange exchange)
    {
        authService.setAlfrescoAuthorizationHeaders(exchange);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void convertBodyToFile(Exchange exchange)
    {
        exchange.getMessage().setBody(new File(exchange.getIn().getBody(InputStream.class)), File.class);
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
        // Reuse the same retry reasons as the shared file store client
        var retryReasons = integrationProperties.alfresco().transform().sharedFileStore().retry().reasons();

        ErrorUtils.wrapErrorAndThrowIfNecessary(cause, retryReasons, LiveIngesterRuntimeException.class);
    }
}
