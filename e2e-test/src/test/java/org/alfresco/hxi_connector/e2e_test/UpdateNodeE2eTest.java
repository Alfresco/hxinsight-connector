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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Cleanup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class UpdateNodeE2eTest
{
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";
    private static final String PREDICTION_APPLIED_ASPECT = "hxi:predictionApplied";
    private static final String PROPERTY_TO_UPDATE = "cm:description";
    private static final String PREDICTED_VALUE = "New description";
    private static final String USER_VALUE = "User updates description";
    private static final String PREDICTED_VALUE_2 = "Second prediction";
    private static final String LIST_PREDICTION_BATCHES_SCENARIO = "List-prediction-batches";
    private static final String LIST_PREDICTIONS_SCENARIO = "List-predictions";
    private static final String PREDICTIONS_AVAILABLE_STATE = "Available";
    private static final String PREDICTIONS_LIST = """
            [
              {
                "prediction": [
                  {
                    "field": "%s",
                    "confidence": 0.9999999403953552,
                    "value": "%s"
                  }
                ],
                "objectId": "%s",
                "modelId": "56785678-5678-5678-5678-567856785678",
                "enrichmentType": "AUTOCORRECT"
              }
            ]
            """;
    private static final String UPDATE_NODE_PROPERTIES = """
            {
                "properties": {
                    "cm:versionLabel": "1.1",
                    "cm:description": "User updates description"
                }
            }
            """;

    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq);
    @Container
    private static final GenericContainer<?> predictionApplier = createPredictionApplierContainer()
            .dependsOn(activemq, hxInsightMock);

    RepositoryClient repositoryNodesClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
        GenericContainer<?> liveIngester = createLiveIngesterContainer().dependsOn(activemq, hxInsightMock);
        liveIngester.start();
    }

    @Test
    void testApplyPredictionToNode() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        Node createdNode = repositoryNodesClient.createNodeWithContent(PARENT_ID, "dummy.txt", fileContent, "text/plain");
        RetryUtils.retryWithBackoff(() -> verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(createdNode.id()))));
        WireMock.reset();
        prepareHxInsightMockToReturnPredictionFor(createdNode.id(), PREDICTED_VALUE);

        // when
        WireMock.setScenarioState(LIST_PREDICTIONS_SCENARIO, PREDICTIONS_AVAILABLE_STATE);
        WireMock.setScenarioState(LIST_PREDICTION_BATCHES_SCENARIO, PREDICTIONS_AVAILABLE_STATE);

        // then
        assertThat(createdNode.aspects()).doesNotContain(PREDICTION_APPLIED_ASPECT);
        assertThat(createdNode.properties()).doesNotContainKey(PROPERTY_TO_UPDATE);
        RetryUtils.retryWithBackoff(() -> {
            Node actualNode = repositoryNodesClient.getNode(createdNode.id());
            assertThat(actualNode.aspects()).contains(PREDICTION_APPLIED_ASPECT);
            assertThat(actualNode.properties())
                    .containsKey(PROPERTY_TO_UPDATE)
                    .extracting(map -> map.get(PROPERTY_TO_UPDATE)).isEqualTo(PREDICTED_VALUE);
        });
        verify(exactly(0), anyRequestedFor(urlEqualTo("/ingestion-events")));
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testApplyPredictionToUpdatedNode() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        Node createdNode = repositoryNodesClient.createNodeWithContent(PARENT_ID, "dummy2.txt", fileContent, "text/plain");
        RetryUtils.retryWithBackoff(() -> verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(createdNode.id()))));
        WireMock.reset();
        prepareHxInsightMockToReturnPredictionFor(createdNode.id(), PREDICTED_VALUE);

        WireMock.setScenarioState(LIST_PREDICTIONS_SCENARIO, PREDICTIONS_AVAILABLE_STATE);
        WireMock.setScenarioState(LIST_PREDICTION_BATCHES_SCENARIO, PREDICTIONS_AVAILABLE_STATE);

        RetryUtils.retryWithBackoff(() -> {
            Node actualNode = repositoryNodesClient.getNode(createdNode.id());
            assertThat(actualNode.aspects()).contains(PREDICTION_APPLIED_ASPECT);
        });

        // when
        Node updatedNode = repositoryNodesClient.updateNodeWithContent(createdNode.id(), UPDATE_NODE_PROPERTIES);
        RetryUtils.retryWithBackoff(() -> verify(exactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(updatedNode.id()))));
        WireMock.reset();
        prepareHxInsightMockToReturnPredictionFor(updatedNode.id(), PREDICTED_VALUE_2);

        WireMock.setScenarioState(LIST_PREDICTIONS_SCENARIO, PREDICTIONS_AVAILABLE_STATE);
        WireMock.setScenarioState(LIST_PREDICTION_BATCHES_SCENARIO, PREDICTIONS_AVAILABLE_STATE);

        // then
        RetryUtils.retryWithBackoff(() -> {
            Node actualNode2 = repositoryNodesClient.getNode(updatedNode.id());
            assertThat(actualNode2.aspects()).contains(PREDICTION_APPLIED_ASPECT);
            assertThat(actualNode2.properties())
                    .containsKey(PROPERTY_TO_UPDATE)
                    .extracting(map -> map.get(PROPERTY_TO_UPDATE)).isEqualTo(USER_VALUE);
        });
        verify(exactly(0), anyRequestedFor(urlEqualTo("/ingestion-events")));
    }

    private void prepareHxInsightMockToReturnPredictionFor(String nodeId, String predictedValue)
    {
        givenThat(get(urlPathTemplate("/v1/prediction-batches/{batchId}"))
                .inScenario(LIST_PREDICTIONS_SCENARIO)
                .whenScenarioStateIs(PREDICTIONS_AVAILABLE_STATE)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(PREDICTIONS_LIST.formatted(PROPERTY_TO_UPDATE, predictedValue, nodeId)))
                .willSetStateTo(STARTED));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
                .withJavaOpts(getMinimalRepoJavaOpts(postgres, activemq));
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, network)
                .withEnv("ALFRESCO_REPOSITORY_DISCOVERY-ENDPOINT", "http://%s:8080/alfresco/api/discovery".formatted(repository.getNetworkAliases().stream().findFirst().get()));
    }

    private static GenericContainer<?> createPredictionApplierContainer()
    {
        return DockerContainers.createPredictionApplierContainerWithin(network)
                .withEnv("SPRING_ACTIVEMQ_BROKERURL",
                        "nio://%s:61616".formatted(activemq.getNetworkAliases().stream().findFirst().get()))
                .withEnv("ALFRESCO_REPOSITORY_BASE-URL",
                        "http://%s:8080".formatted(repository.getNetworkAliases().stream().findFirst().get()))
                .withEnv("ALFRESCO_REPOSITORY_RETRY_ATTEMPTS", "1")
                .withEnv("ALFRESCO_REPOSITORY_RETRY_INITIAL-DELAY", "0")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_CLIENT-ID", "dummy-client-id")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_USERNAME", "admin")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_PASSWORD", "admin")
                .withEnv("AUTH_PROVIDERS_HYLAND-EXPERIENCE_TOKEN-URI",
                        "http://%s:8080/token".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("HYLAND-EXPERIENCE_INSIGHT_PREDICTIONS_SOURCE-BASE-URL",
                        "http://%s:8080/v1".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("HYLAND-EXPERIENCE_INSIGHT_PREDICTIONS_POLL-PERIOD-MILLIS", "100");
    }
}
