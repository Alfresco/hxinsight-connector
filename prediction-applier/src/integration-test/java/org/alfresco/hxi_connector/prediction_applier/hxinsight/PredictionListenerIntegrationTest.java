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
package org.alfresco.hxi_connector.prediction_applier.hxinsight;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.io.IOException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.common.test.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.prediction_applier.repository.NodesClient;
import org.alfresco.hxi_connector.prediction_applier.util.local.LocalSqsPublisher;

@SpringBootTest(classes = {
        LocalSqsPublisher.class,
        PredictionListener.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@ActiveProfiles("test")
@Testcontainers
class PredictionListenerIntegrationTest
{
    private static final String QUEUE_NAME = "hxinsight-prediction-queue";

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final LocalStackContainer localStackServer = DockerContainers.createLocalStackContainer();

    @Autowired
    LocalSqsPublisher localSqsPublisher;
    @Value("${hyland-experience.insight.prediction.endpoint}")
    String predictionEndpoint;
    @MockBean
    PredictionMapper predictionMapper;
    @MockBean
    NodesClient nodesClient;

    ListAppender<ILoggingEvent> predictionListenerLogsAppender;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException
    {
        localStackServer.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @BeforeEach
    void setUp()
    {
        predictionListenerLogsAppender = LoggingUtils.createLogsListAppender(PredictionListener.class);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testReceiveMessage()
    {
        // given
        Prediction prediction = new Prediction("prediction-id", "node-id");

        // when
        localSqsPublisher.publish(predictionEndpoint, prediction);

        // then
        RetryUtils.retryWithBackoff(() -> {
            then(predictionMapper).should().map(prediction);
            then(nodesClient).should().updateNode(any());
        });
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("camel.component.aws2-sqs.uri-endpoint-override", localStackServer.getEndpointOverride(SQS)::toString);
        registry.add("camel.component.aws2-sqs.access-key", localStackServer::getAccessKey);
        registry.add("camel.component.aws2-sqs.secret-key", localStackServer::getSecretKey);
    }
}
