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

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.common.test.util.DockerTags;
import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
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
    private static final String LOCALSTACK_IMAGE = "localstack/localstack";
    private static final String LOCALSTACK_TAG = DockerTags.getLocalStackTag();
    private static final String QUEUE_NAME = "hxinsight-prediction-queue";

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final LocalStackContainer localStackServer = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE).withTag(LOCALSTACK_TAG));

    @Autowired
    LocalSqsPublisher localSqsPublisher;
    @Value("${hyland-experience.insight.prediction.endpoint}")
    String predictionEndpoint;

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
    void testReceiveMessage()
    {
        // given
        String predictionProperty = "prediction-id";
        String predictionValue = "prediction-value";
        Map<String, Object> message = Map.of(predictionProperty, predictionValue);

        // when
        localSqsPublisher.publish(predictionEndpoint, message);

        // then
        List<String> actualLogs = RetryUtils.retryWithBackoff(() -> {
            List<String> logs = predictionListenerLogsAppender.list.stream().map(Object::toString).toList();
            assertThat(logs).isNotEmpty();
            return logs;
        });
        List<String> expectedLogs = List.of("[DEBUG] Prediction body: {\"%s\":\"%s\"}".formatted(predictionProperty, predictionValue));
        assertThat(actualLogs).isEqualTo(expectedLogs);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("camel.component.aws2-sqs.uri-endpoint-override", localStackServer.getEndpointOverride(SQS)::toString);
        registry.add("camel.component.aws2-sqs.access-key", localStackServer::getAccessKey);
        registry.add("camel.component.aws2-sqs.secret-key", localStackServer::getSecretKey);
    }
}
