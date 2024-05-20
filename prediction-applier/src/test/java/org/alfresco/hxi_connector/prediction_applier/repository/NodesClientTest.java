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

import static java.util.Collections.emptySet;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.apache.hc.core5.http.HttpStatus.SC_CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.alfresco.hxi_connector.prediction_applier.repository.NodesClient.NODES_DIRECT_ENDPOINT;
import static org.alfresco.hxi_connector.prediction_applier.repository.NodesClient.ROUTE_ID;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.model.UpdateType.AUTOFILL;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ToDynamicDefinition;
import org.apache.camel.model.language.ConstantExpression;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.prediction_applier.config.RepositoryApiProperties;
import org.alfresco.hxi_connector.prediction_applier.model.repository.PredictionModelResponse;
import org.alfresco.hxi_connector.prediction_applier.model.repository.PredictionModelResponseEntry;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.PredictionModel;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NodesClientTest
{
    private static final String MOCK_ENDPOINT = "mock:repo-nodes-endpoint";
    private static final int RETRY_ATTEMPTS = 3;
    private static final Date PREDICTION_DATE_TIME = new Date();

    CamelContext camelContext;
    MockEndpoint mockEndpoint;
    FluentProducerTemplate producerTemplate;

    @BeforeAll
    @SneakyThrows
    void beforeAll()
    {
        camelContext = new DefaultCamelContext();
        NodesClient nodesClient = new NodesClient(createNodesApiProperties());
        camelContext.addRoutes(nodesClient);
        camelContext.start();

        adviceWith(camelContext, ROUTE_ID, route -> route.weaveByType(ToDynamicDefinition.class).replace().to(MOCK_ENDPOINT));
        mockEndpoint = camelContext.getEndpoint(MOCK_ENDPOINT, MockEndpoint.class);
        producerTemplate = camelContext.createFluentProducerTemplate();
    }

    @AfterEach
    void tearDown()
    {
        mockEndpoint.reset();
    }

    @AfterAll
    void afterAll()
    {
        camelContext.stop();
    }

    @Test
    void testUpdateNode() throws Exception
    {
        // given
        PredictionModelResponseEntry predictionResponseEntry = new PredictionModelResponseEntry("prediction-id", "property", PREDICTION_DATE_TIME, 0.5f, "model-id", "new-value", "old-value", AUTOFILL);
        PredictionModelResponse predictionResponse = new PredictionModelResponse(predictionResponseEntry);
        mockEndpointWillRespondWith(SC_CREATED, new ObjectMapper().writeValueAsString(predictionResponse));
        mockEndpoint.expectedMessageCount(1);

        // when
        PredictionModel predictionModel = new PredictionModel("property", PREDICTION_DATE_TIME, 0.5f, "model-id", "new-value", AUTOFILL);
        PredictionModelResponse actualResponse = producerTemplate.to(NODES_DIRECT_ENDPOINT)
                .withBody(predictionModel)
                .request(PredictionModelResponse.class);

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(actualResponse).isEqualTo(predictionResponse);
    }

    @Test
    void testUpdateNode_clientError_dontRetry() throws InterruptedException
    {
        // given
        mockEndpointWillRespondWith(400);
        mockEndpoint.expectedMessageCount(1);

        // when
        Throwable thrown = catchThrowable(() -> producerTemplate.to(NODES_DIRECT_ENDPOINT).request());

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(thrown)
                .cause().isInstanceOf(EndpointClientErrorException.class)
                .hasMessageContaining("received:", 400);
    }

    @Test
    void testUpdateNode_serverError_doRetry() throws InterruptedException
    {
        // given
        mockEndpointWillRespondWith(500, "Server error message");
        mockEndpoint.expectedMessageCount(RETRY_ATTEMPTS + 1);

        // when
        Throwable thrown = catchThrowable(() -> producerTemplate.to(NODES_DIRECT_ENDPOINT).request());

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .hasMessageContaining("received:", 500);
    }

    private RepositoryApiProperties createNodesApiProperties()
    {
        return new RepositoryApiProperties(null, null, null, new Retry(RETRY_ATTEMPTS, 0, 1, emptySet()));
    }

    private void mockEndpointWillRespondWith(int statusCode)
    {
        mockEndpoint.returnReplyHeader(HTTP_RESPONSE_CODE, new ConstantExpression(String.valueOf(statusCode)));
    }

    private void mockEndpointWillRespondWith(int statusCode, String responseBody)
    {
        mockEndpoint.whenAnyExchangeReceived(exchange -> {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, statusCode);
            exchange.getMessage().setBody(responseBody);
        });
    }
}
