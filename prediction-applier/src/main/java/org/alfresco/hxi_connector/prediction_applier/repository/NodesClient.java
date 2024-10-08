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

import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.TRACE;
import static org.apache.camel.component.http.HttpMethods.PUT;
import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.apache.hc.core5.http.HttpStatus.SC_CREATED;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.prediction_applier.config.RepositoryApiProperties;
import org.alfresco.hxi_connector.prediction_applier.model.repository.PredictionModelResponse;
import org.alfresco.hxi_connector.prediction_applier.util.LinkedListJacksonDataFormat;

@Component
@RequiredArgsConstructor
public class NodesClient extends RouteBuilder
{
    public static final String NODES_DIRECT_ENDPOINT = "direct:" + NodesClient.class.getSimpleName();
    private static final String RETRYABLE_ROUTE = "direct:retryable-" + NodesClient.class.getSimpleName();
    static final String ROUTE_ID = "repository-nodes";
    public static final String NODE_ID_HEADER = "nodeId";
    private static final String URI_PATTERN = "%s/api/-default-/private/hxi/versions/1/nodes/${headers.nodeId}/predictions?httpMethod=POST&throwExceptionOnFailure=false";
    private static final int EXPECTED_STATUS_CODE = SC_CREATED;
    public static final String UNEXPECTED_STATUS_CODE_MESSAGE = "Unexpected response status code - expecting: %d, received: %d";
    private static final Set<Class<? extends Throwable>> RETRY_REASONS = Set.of(
            EndpointServerErrorException.class,
            UnknownHostException.class,
            JsonEOFException.class,
            MismatchedInputException.class,
            HttpHostConnectException.class,
            NoHttpResponseException.class,
            MalformedChunkCodingException.class);

    private final RepositoryApiProperties repositoryApiProperties;
    private final AuthService authService;

    @Override
    @SuppressWarnings("unchecked")
    public void configure()
    {
        // @formatter:off
        onException(RETRY_REASONS.toArray(Class[]::new))
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .logExhaustedMessageBody(true)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
            .maximumRedeliveries(repositoryApiProperties.retry().attempts())
            .redeliveryDelay(repositoryApiProperties.retry().initialDelay())
            .backOffMultiplier(repositoryApiProperties.retry().delayMultiplier())
            .stop();

        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
            .stop();

        from(NODES_DIRECT_ENDPOINT)
            .marshal()
            .json(JsonLibrary.Jackson)
            .to(RETRYABLE_ROUTE)
            .end();

        from(RETRYABLE_ROUTE)
            .id(ROUTE_ID)
            .errorHandler(noErrorHandler())
            .setHeader(HTTP_METHOD, constant(PUT))
            .process(authService::setAlfrescoAuthorizationHeaders)
            .log(DEBUG, log, "Prediction :: applying to node: ${headers.nodeId}")
            .toD(URI_PATTERN.formatted(repositoryApiProperties.baseUrl()))
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .otherwise()
                .choice().when(body().startsWith("[")) // If the response is a list of predictions
                    .unmarshal(new LinkedListJacksonDataFormat(PredictionModelResponse.class))
                .otherwise()
                    .unmarshal()
                    .json(JsonLibrary.Jackson, PredictionModelResponse.class)
                    .setBody(exchange -> List.of(exchange.getIn().getBody(PredictionModelResponse.class)))
                .end()
                .choice().when(bodyNotEmpty())
                    .log(DEBUG, log, "Prediction :: successfully applied to node: ${headers.nodeId}")
                .otherwise()
                    .log(INFO, log, "Prediction :: failed to apply to node: ${headers.nodeId}")
                    .log(TRACE, log, "Headers: ${headers}")
                .end()
            .end();
        // @formatter:on
    }

    private Predicate bodyNotEmpty()
    {
        return and(body().isNotNull(), bodyAs(Collection.class).method("isEmpty").isEqualTo(false));
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
