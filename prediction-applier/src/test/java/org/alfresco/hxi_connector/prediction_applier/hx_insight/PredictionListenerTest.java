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
package org.alfresco.hxi_connector.prediction_applier.hx_insight;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.prediction_applier.hx_insight.PredictionListener.ROUTE_ID;

import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ToDefinition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.model.repository.Node;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PredictionListenerTest
{
    private static final String TEST_ENDPOINT = "direct:predictions-queue";
    private static final String MOCK_ENDPOINT = "mock:direct-nodes-endpoint";

    CamelContext camelContext;
    MockEndpoint mockEndpoint;
    FluentProducerTemplate producerTemplate;

    PredictionMapper predictionMapperMock;

    @BeforeAll
    @SneakyThrows
    void beforeAll()
    {
        camelContext = new DefaultCamelContext();
        predictionMapperMock = mock(PredictionMapper.class);
        InsightPredictionsProperties properties = new InsightPredictionsProperties(null, 0L, null, TEST_ENDPOINT);
        PredictionListener predictionListener = new PredictionListener(predictionMapperMock, properties);
        camelContext.addRoutes(predictionListener);
        camelContext.start();

        adviceWith(camelContext, ROUTE_ID, route -> route.weaveByType(ToDefinition.class).replace().to(MOCK_ENDPOINT));
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
    @Disabled
    void testApplyPrediction() throws InterruptedException, JsonProcessingException
    {
        // given
        PredictionEntry prediction = new PredictionEntry("prediction-id", "node-id", null, null);
        String predictionJson = new ObjectMapper().writeValueAsString(prediction);
        Node node = new Node("node-id", null);
        given(predictionMapperMock.map(any())).willReturn(node);
        mockEndpointWillExpectInRequestBody(node);

        // when
        Throwable thrown = catchThrowable(() -> producerTemplate.to(TEST_ENDPOINT)
                .withBody(predictionJson)
                .request());

        // then
        mockEndpoint.assertIsSatisfied();
        then(predictionMapperMock).should().map(prediction);
        assertThat(thrown).doesNotThrowAnyException();
    }

    private void mockEndpointWillExpectInRequestBody(Node... expectedNodes)
    {
        Stream.of(expectedNodes).forEach(node -> mockEndpoint.message(0).body().contains(node));
    }
}
