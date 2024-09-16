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

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.ResourceNotFoundException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.ATSTransformRequester;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.EmptyRenditionException;

@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"},
        classes = {ATSTransformResponseHandler.class, ATSTransformResponseHandlerTest.IntegrationPropertiesTestConfig.class})
@EnableAutoConfiguration
@SuppressWarnings("PMD.FieldNamingConventions")
class ATSTransformResponseHandlerTest
{
    private static final long TIMESTAMP = Instant.now().toEpochMilli();
    private static final String RESPONSE_ENDPOINT = "direct:transform-response-test";
    private static final Retry retryIngestion = new Retry(
            5,
            0,
            0,
            Set.of());
    private static final Retry retryTransformation = new Retry(
            5,
            0,
            0,
            Set.of());

    @MockBean
    private ATSTransformRequester atsTransformRequester;
    @MockBean
    private IngestContentCommandHandler ingestContentCommandHandler;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @BeforeEach
    void setUp() throws Exception
    {
        camelContext.getRouteController().startAllRoutes();
    }

    @Test
    void shouldSkipProcessingIfTransformationFailedWith400Error()
    {
        // given
        TransformResponse transformResponse = new TransformResponse(
                "e5bcc533-853c-44f1-a02d-3ab1e36a03e6",
                new ClientData(
                        "dcc9e89b-c1c0-48a4-814f-98eafab72fdb", "application/pdf", 0, TIMESTAMP),
                400,
                "Something went wrong!");

        // when
        simulateResponse(transformResponse);

        // then
        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldRetryRouteGivenNumberOfTimeOnAnyNonTransformRelatedError()
    {
        // given
        String nodeId = "dcc9e89b-c1c0-48a4-814f-98eafab72fdb";
        String transformedFileId = "e5bcc533-853c-44f1-a02d-3ab1e36a03e6";

        TransformResponse transformResponse = new TransformResponse(
                transformedFileId,
                new ClientData(
                        nodeId, "application/pdf", 0, TIMESTAMP),
                202,
                "");

        IngestContentCommand expectedCommand = new IngestContentCommand(
                transformedFileId,
                nodeId,
                transformResponse.clientData().targetMimeType(),
                TIMESTAMP);

        doThrow(new LiveIngesterRuntimeException("Some exception")).when(ingestContentCommandHandler).handle(expectedCommand);

        // when
        catchThrowable(() -> simulateResponse(transformResponse));

        // then
        then(ingestContentCommandHandler).should(times(retryIngestion.attempts() + 1)).handle(expectedCommand);
    }

    @ParameterizedTest
    @ValueSource(classes = {EmptyRenditionException.class, ResourceNotFoundException.class})
    void shouldRetryTransformationOnSpecificExceptions(Class<? extends Throwable> exception)
    {
        // given
        String nodeId = "dcc9e89b-c1c0-48a4-814f-98eafab72fdb";
        String transformedFileId = "e5bcc533-853c-44f1-a02d-3ab1e36a03e6";
        String targetMimeType = "application/pdf";
        int retryAttempt = 1;

        TransformResponse transformResponse = new TransformResponse(
                transformedFileId,
                new ClientData(
                        nodeId, targetMimeType, 0, TIMESTAMP),
                202,
                "");

        IngestContentCommand expectedCommand = new IngestContentCommand(
                transformedFileId,
                nodeId,
                transformResponse.clientData().targetMimeType(),
                TIMESTAMP);

        doThrow(exception).when(ingestContentCommandHandler).handle(expectedCommand);

        TransformRequest expectedTransformRequest = new TransformRequest(nodeId, targetMimeType, TIMESTAMP);

        // when
        catchThrowable(() -> simulateResponse(transformResponse));

        // then
        then(ingestContentCommandHandler).should(times(1)).handle(expectedCommand);
        then(atsTransformRequester).should().requestTransformRetry(expectedTransformRequest, retryAttempt);
    }

    @ParameterizedTest
    @ValueSource(classes = {EmptyRenditionException.class, ResourceNotFoundException.class})
    void shouldNotRetryTransformationIfMaxAttemptsNumberIsExceeded(Class<? extends Throwable> exception)
    {
        // given
        String nodeId = "dcc9e89b-c1c0-48a4-814f-98eafab72fdb";
        String transformedFileId = "e5bcc533-853c-44f1-a02d-3ab1e36a03e6";
        String targetMimeType = "application/pdf";

        TransformResponse transformResponse = new TransformResponse(
                transformedFileId,
                new ClientData(
                        nodeId, targetMimeType, retryTransformation.attempts(), TIMESTAMP),
                202,
                "");

        IngestContentCommand expectedCommand = new IngestContentCommand(
                transformedFileId,
                nodeId,
                transformResponse.clientData().targetMimeType(),
                TIMESTAMP);

        doThrow(exception).when(ingestContentCommandHandler).handle(expectedCommand);

        // when
        catchThrowable(() -> simulateResponse(transformResponse));

        // then
        then(ingestContentCommandHandler).should(times(1)).handle(expectedCommand);
        then(atsTransformRequester).shouldHaveNoInteractions();
    }

    @SneakyThrows
    private void simulateResponse(TransformResponse response)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        producerTemplate.sendBody(RESPONSE_ENDPOINT, objectMapper.writeValueAsString(response));
    }

    @TestConfiguration
    public static class IntegrationPropertiesTestConfig
    {

        @Bean
        public IntegrationProperties integrationProperties()
        {
            return new IntegrationProperties(
                    new IntegrationProperties.Alfresco(
                            mock(),
                            mock(),
                            new Transform(
                                    mock(),
                                    new Transform.Response(
                                            RESPONSE_ENDPOINT,
                                            RESPONSE_ENDPOINT,
                                            retryIngestion,
                                            retryTransformation),
                                    mock(),
                                    mock()),
                            mock()),
                    mock(),
                    new Application("dummy-source-id", "dummy-version"));
        }
    }
}
