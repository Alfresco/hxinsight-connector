/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.TestJsonUtils.asSet;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.Cleanup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.FieldNamingConventions")
public class UpdateNodeE2eTest
{
    private static final int DELAY_MS = 1200;
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
    private static final String ALLOW_ACCESS_PROPERTY = "ALLOW_ACCESS";
    private static final String DENY_ACCESS_PROPERTY = "DENY_ACCESS";
    public static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    public static final String ALICE = "abeecher";
    public static final String MIKE = "mjackson";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Network network = Network.newNetwork();
    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq);
    @Container
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer()
            .dependsOn(activemq, hxInsightMock, repository);
    @Container
    private static final GenericContainer<?> predictionApplier = createPredictionApplierContainer()
            .dependsOn(activemq, hxInsightMock, repository, liveIngester);

    private RepositoryClient repositoryClient;
    private Node createdNode;

    @BeforeAll
    public void beforeAll()
    {
        repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @BeforeEach
    public void setUp() throws IOException
    {
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        createdNode = repositoryClient.createNodeWithContent(PARENT_ID, "dummy.txt", fileContent, "text/plain");
        RetryUtils.retryWithBackoff(() -> WireMock.verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(createdNode.id()))), DELAY_MS);
        WireMock.reset();
    }

    @Test
    void testApplyPredictionToUpdatedNode()
    {
        // given
        prepareHxInsightMockToReturnPredictionFor(createdNode.id(), PREDICTED_VALUE);

        WireMock.setScenarioState(LIST_PREDICTIONS_SCENARIO, PREDICTIONS_AVAILABLE_STATE);
        WireMock.setScenarioState(LIST_PREDICTION_BATCHES_SCENARIO, PREDICTIONS_AVAILABLE_STATE);

        RetryUtils.retryWithBackoff(() -> {
            Node actualNode = repositoryClient.getNode(createdNode.id());
            assertThat(actualNode.aspects()).contains(PREDICTION_APPLIED_ASPECT);
        });

        // when
        Node updatedNode = repositoryClient.updateNodeWithContent(createdNode.id(), UPDATE_NODE_PROPERTIES);
        RetryUtils.retryWithBackoff(() -> WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(updatedNode.id()))
                .withHeader(USER_AGENT, matching(getAppInfoRegex()))));
        WireMock.reset();
        prepareHxInsightMockToReturnPredictionFor(updatedNode.id(), PREDICTED_VALUE_2);

        WireMock.setScenarioState(LIST_PREDICTIONS_SCENARIO, PREDICTIONS_AVAILABLE_STATE);
        WireMock.setScenarioState(LIST_PREDICTION_BATCHES_SCENARIO, PREDICTIONS_AVAILABLE_STATE);

        // then
        RetryUtils.retryWithBackoff(() -> {
            Node actualNode2 = repositoryClient.getNode(updatedNode.id());
            assertThat(actualNode2.aspects()).contains(PREDICTION_APPLIED_ASPECT);
            assertThat(actualNode2.properties())
                    .containsKey(PROPERTY_TO_UPDATE)
                    .extracting(map -> map.get(PROPERTY_TO_UPDATE)).isEqualTo(USER_VALUE);
            List<LoggedRequest> requests = WireMock.findAll(anyRequestedFor(urlEqualTo("/ingestion-events")));
            assertThat(requests).isEmpty();
        }, DELAY_MS);

    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testSendTimestampToHxi()
    {
        // when
        Node updatedNode = repositoryClient.updateNodeWithContent(createdNode.id(), UPDATE_NODE_PROPERTIES);

        // then
        RetryUtils.retryWithBackoff(() -> WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(updatedNode.id()))
                .withRequestBody(containing("sourceTimestamp"))
                .withHeader(USER_AGENT, matching(getAppInfoRegex()))));
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testSendPermissionUpdateToHxi()
    {
        // when
        repositoryClient.setReadAccess(createdNode.id(), ALICE, MIKE);

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events")));

            assertFalse(requests.isEmpty());

            Optional<LoggedRequest> permissionsUpdatedEvent = requests.stream()
                    .filter(request -> request.getBodyAsString().contains(createdNode.id()))
                    .filter(request -> request.getBodyAsString().contains("createOrUpdate"))
                    .filter(request -> request.getBodyAsString().contains(ALLOW_ACCESS_PROPERTY))
                    .findFirst();

            assertTrue(permissionsUpdatedEvent.isPresent());

            JsonNode properties = objectMapper.readTree(permissionsUpdatedEvent.get().getBodyAsString())
                    .get(0)
                    .get("properties");

            assertTrue(properties.has(ALLOW_ACCESS_PROPERTY));
            assertEquals(Set.of(GROUP_EVERYONE, ALICE), asSet(properties.get(ALLOW_ACCESS_PROPERTY).get("value")));

            assertTrue(properties.has(DENY_ACCESS_PROPERTY));
            assertEquals(Set.of(MIKE), asSet(properties.get(DENY_ACCESS_PROPERTY).get("value")));
        });
    }

    private void prepareHxInsightMockToReturnPredictionFor(String nodeId, String predictedValue)
    {
        givenThat(get(urlPathTemplate("/prediction-batches/{batchId}"))
                .inScenario(LIST_PREDICTIONS_SCENARIO)
                .whenScenarioStateIs(PREDICTIONS_AVAILABLE_STATE)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(PREDICTIONS_LIST.formatted(PROPERTY_TO_UPDATE, predictedValue, nodeId)))
                .willSetStateTo(STARTED));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(getMinimalRepoJavaOpts(postgres, activemq));
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network);
    }

    private static GenericContainer<?> createPredictionApplierContainer()
    {
        return DockerContainers.createPredictionApplierContainerWithin(network)
                .withEnv("SPRING_ACTIVEMQ_BROKERURL",
                        "nio://%s:61616".formatted(activemq.getNetworkAliases().stream().findFirst().get()))
                .withEnv("ALFRESCO_REPOSITORY_BASEURL",
                        "http://%s:8080/alfresco".formatted(repository.getNetworkAliases().stream().findFirst().get()))
                .withEnv("ALFRESCO_REPOSITORY_RETRY_ATTEMPTS", "1")
                .withEnv("ALFRESCO_REPOSITORY_RETRY_INITIALDELAY", "0")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_CLIENTID", "dummy-client-id")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_USERNAME", "admin")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_PASSWORD", "admin")
                .withEnv("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI",
                        "http://%s:8080/token".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_BASEURL",
                        "http://%s:8080".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("HYLANDEXPERIENCE_INSIGHT_PREDICTIONS_POLLPERIODMILLIS", "100");
    }
}
