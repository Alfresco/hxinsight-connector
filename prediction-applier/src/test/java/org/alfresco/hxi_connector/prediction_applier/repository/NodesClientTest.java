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
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import static org.alfresco.hxi_connector.prediction_applier.repository.NodesClient.NODES_DIRECT_ENDPOINT;
import static org.alfresco.hxi_connector.prediction_applier.repository.NodesClient.ROUTE_ID;

import java.util.Set;

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

import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.model.repository.Node;
import org.alfresco.hxi_connector.common.model.repository.NodeEntry;
import org.alfresco.hxi_connector.prediction_applier.config.NodesApiProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NodesClientTest
{
    private static final String MOCK_ENDPOINT = "mock:repo-nodes-endpoint";
    private static final int RETRY_ATTEMPTS = 3;

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
    void testUpdateNode() throws InterruptedException
    {
        // given
        String nodeId = "node-id";
        String aspect = "aspect-name";
        Node node = new Node(nodeId, Set.of(aspect));
        mockEndpointWillRespondWith(200, createResponseBodyWith(nodeId, aspect));
        mockEndpoint.expectedMessageCount(1);

        // when
        Node actualNode = producerTemplate.to(NODES_DIRECT_ENDPOINT)
                .withBody(node)
                .request(NodeEntry.class)
                .node();

        // then
        mockEndpoint.assertIsSatisfied();
        assertThat(actualNode).isEqualTo(new Node(nodeId, Set.of(aspect)));
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

    private NodesApiProperties createNodesApiProperties()
    {
        return new NodesApiProperties(null, null, null, new NodesApiProperties.Retry(RETRY_ATTEMPTS, 0, 1));
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

    @SneakyThrows
    private static String createResponseBodyWith(String nodeId, String... aspects)
    {
        return new ObjectMapper().writeValueAsString(new NodeEntry(new Node(nodeId, Set.of(aspects))));
    }
}
