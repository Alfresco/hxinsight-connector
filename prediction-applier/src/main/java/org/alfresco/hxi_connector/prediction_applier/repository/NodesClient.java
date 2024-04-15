/*-
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
package org.alfresco.hxi_connector.prediction_applier.repository;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import java.net.UnknownHostException;
import java.util.Set;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.model.repository.NodeEntry;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.prediction_applier.config.NodesApiProperties;

@Component
@RequiredArgsConstructor
public class NodesClient extends RouteBuilder
{
    public static final String DIRECT_ENDPOINT = "direct:" + NodesClient.class.getSimpleName();
    private static final String RETRYABLE_ROUTE = "direct:retryable-" + NodesClient.class.getSimpleName();
    static final String ROUTE_ID = "repository-nodes";
    private static final String NODE_ID_HEADER = "nodeId";
    private static final String URI_PATTERN = "%s/alfresco/api/-default-/public/alfresco/versions/1/nodes/${headers.%s}?httpMethod=PUT&authMethod=Basic&authUsername=%s&authPassword=%s&authenticationPreemptive=true&throwExceptionOnFailure=false";
    private static final int EXPECTED_STATUS_CODE = 200;
    public static final String UNEXPECTED_STATUS_CODE_MESSAGE = "Unexpected response status code - expecting: %d, received: %d";
    private static final Set<Class<? extends Throwable>> RETRY_REASONS = Set.of(
            EndpointServerErrorException.class,
            UnknownHostException.class,
            JsonEOFException.class,
            MismatchedInputException.class,
            HttpHostConnectException.class,
            NoHttpResponseException.class,
            MalformedChunkCodingException.class);

    private final NodesApiProperties nodesApiProperties;

    @Override
    @SuppressWarnings("unchecked")
    public void configure() throws Exception
    {
        // @formatter:off
        onException(RETRY_REASONS.toArray(Class[]::new))
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .logExhaustedMessageBody(true)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
            .maximumRedeliveries(nodesApiProperties.retry().attempts())
            .redeliveryDelay(nodesApiProperties.retry().initialDelay())
            .backOffMultiplier(nodesApiProperties.retry().delayMultiplier())
            .stop();

        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
            .stop();

        from(DIRECT_ENDPOINT)
            .setHeader(NODE_ID_HEADER, simple("${body.id}"))
            .marshal()
            .json(JsonLibrary.Jackson)
            .to(RETRYABLE_ROUTE)
            .end();

        from(RETRYABLE_ROUTE)
            .id(ROUTE_ID)
            .errorHandler(noErrorHandler())
            .toD(URI_PATTERN.formatted(nodesApiProperties.baseUrl(), NODE_ID_HEADER, nodesApiProperties.username(), nodesApiProperties.password()))
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .otherwise()
                .unmarshal()
                .json(JsonLibrary.Jackson, NodeEntry.class)
            .endChoice()
            .end();
        // @formatter:on
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
