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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.storage;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.ERROR;

import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.io.InputStream;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.event.Level;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@Component
@RequiredArgsConstructor
public class SharedFileStoreClient extends RouteBuilder implements TransformEngineFileStorage
{
    private static final String LOCAL_ENDPOINT = "direct:" + SharedFileStoreClient.class.getSimpleName();
    private static final String ROUTE_ID = "rendition-downloader";
    private static final int EXPECTED_STATUS_CODE = 200;
    private static final String FILE_ID_HEADER = "fileId";
    private static final String ENDPOINT_PATTERN = "%s/${headers." + FILE_ID_HEADER + "}?httpMethod=GET&throwExceptionOnFailure=false";

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        // @formatter:off
        String fileEndpoint = ENDPOINT_PATTERN.formatted(integrationProperties.alfresco().transform().sharedFileStore().fileEndpoint());
        // onException is log-only. Throwing or otherwise mutating the exchange from inside an
        // onException processor triggers Camel's FatalFallbackErrorHandler, which short-circuits
        // the outer transacted route's error handling. Retry classification for transient I/O
        // is delegated to downloadFile()'s catch block, which calls
        // ErrorUtils.wrapErrorAndThrowIfNecessary against the configured retry reasons set so
        // an in-cause-chain HttpHostConnectException / NoHttpResponseException / etc. is
        // re-thrown as EndpointServerErrorException for @Retryable to match.
        onException(Exception.class)
            .log(ERROR, log, "Transform :: Unexpected response while downloading rendition - Endpoint: %s".formatted(fileEndpoint))
            .process(exchange -> LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR));

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .toD(fileEndpoint)
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
    public File downloadFile(String fileId)
    {
        try
        {
            return camelContext.createFluentProducerTemplate()
                    .to(LOCAL_ENDPOINT)
                    .withHeader(FILE_ID_HEADER, fileId)
                    .request(File.class);
        }
        catch (CamelExecutionException e)
        {
            classifyAndRethrow(e);
            throw e;
        }
    }

    /**
     * Translates transient I/O causes (e.g. {@code HttpHostConnectException}, {@code NoHttpResponseException}, {@code SocketException}) into {@link EndpointServerErrorException} so {@code @Retryable} on {@link #downloadFile(String)} matches and Spring Retry's listener ({@code RetryMetricsRecorder}) records the attempt under the stable {@code exception=EndpointServerErrorException} tag — independent of which concrete Apache HC exception class the underlying Camel HTTP component happened to surface.
     */
    private void classifyAndRethrow(CamelExecutionException e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof Exception causeException)
        {
            ErrorUtils.wrapErrorAndThrowIfNecessary(causeException,
                    integrationProperties.alfresco().transform().sharedFileStore().retry().reasons(),
                    LiveIngesterRuntimeException.class);
        }
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
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

}
