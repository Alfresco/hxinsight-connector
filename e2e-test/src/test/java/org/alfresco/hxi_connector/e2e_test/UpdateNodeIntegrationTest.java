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
package org.alfresco.hxi_connector.e2e_test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.common.model.repository.Node;
import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.TestNodesClient;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class UpdateNodeIntegrationTest
{
    private static final String QUEUE_NAME = "hxinsight-prediction-queue";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";
    private static final String PREDICTION_APPLIED_ASPECT = "hxi:predictionApplied";

    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    static final AlfrescoRepositoryContainer repository = createRepositoryContainer();
    @Container
    private static final LocalStackContainer awsMock = DockerContainers.createLocalStackContainerWithin(network);
    @Container
    private static final GenericContainer<?> predictionApplier = createPredictionApplierContainer();

    TestNodesClient testNodesClient = new TestNodesClient(repository.getBaseUrl(), "admin", "admin");

    @BeforeAll
    public static void beforeAll() throws IOException, InterruptedException
    {
        awsMock.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @AfterAll
    static void afterAll()
    {
        repository.getHost();
    }

    @Test
    void testUpdateFile() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        Node createdNode = testNodesClient.createFileNode("-my-", "dummy.txt", fileContent, "text/plaint");

        // when
        publishPrediction(mockPredictionFor(createdNode.id()));

        // then
        RetryUtils.retryWithBackoff(() -> {
            Node actualNode = testNodesClient.getNode(createdNode.id());
            assertThat(actualNode.aspects())
                    .contains(PREDICTION_APPLIED_ASPECT);
        });
    }

    private Prediction mockPredictionFor(String nodeId)
    {
        return new Prediction(UUID.randomUUID().toString(), nodeId);
    }

    @SneakyThrows
    private void publishPrediction(Prediction prediction)
    {
        awsMock.execInContainer("awslocal", "sqs", "send-message",
                "--queue-url", "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/%s".formatted(QUEUE_NAME),
                "--message-body", new ObjectMapper().writeValueAsString(prediction));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
            .withJavaOpts("""
            -Ddb.driver=org.postgresql.Driver
            -Ddb.username=%s
            -Ddb.password=%s
            -Ddb.url=jdbc:postgresql://%s:5432/%s
            -Dmessaging.broker.url="failover:(nio://%s:61616)?timeout=3000&jms.useCompression=true"
            -Dalfresco.host=localhost
            -Dalfresco.port=8080
            -Dtransform.service.enabled=false
            -Dalfresco.restApi.basicAuthScheme=true
            -Ddeployment.method=DOCKER_COMPOSE
            -Xms1500m -Xmx1500m
            """.formatted(
                    postgres.getUsername(),
                    postgres.getPassword(),
                    postgres.getNetworkAliases().stream().findFirst().get(),
                    postgres.getDatabaseName(),
                    activemq.getNetworkAliases().stream().findFirst().get())
                .replace("\n", " "));
        // @formatter:on
    }

    private static GenericContainer<?> createPredictionApplierContainer()
    {
        return DockerContainers.createPredictionApplierContainerWithin(network)
                .withEnv("CAMEL_COMPONENT_AWS2-SQS_URI-ENDPOINT-OVERRIDE", "http://%s:4566".formatted(awsMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("CAMEL_COMPONENT_AWS2-SQS_ACCESS-KEY", awsMock.getAccessKey())
                .withEnv("CAMEL_COMPONENT_AWS2-SQS_SECRET-KEY", awsMock.getSecretKey())
                .withEnv("ALFRESCO_REPOSITORY_NODES_BASE-URL", "http://%s:8080".formatted(repository.getNetworkAliases().stream().findFirst().get()))
                .withEnv("ALFRESCO_REPOSITORY_NODES_USERNAME", "admin")
                .withEnv("ALFRESCO_REPOSITORY_NODES_PASSWORD", "admin")
                .withEnv("ALFRESCO_REPOSITORY_NODES_RETRY_ATTEMPTS", "1")
                .withEnv("ALFRESCO_REPOSITORY_NODES_RETRY_INITIAL-DELAY", "0");
    }
}
