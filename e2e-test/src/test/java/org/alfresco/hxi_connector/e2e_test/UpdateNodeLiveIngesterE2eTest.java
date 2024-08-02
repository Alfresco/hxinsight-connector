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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getRepoJavaOptsWithTransforms;
import static org.alfresco.hxi_connector.e2e_test.util.TestJsonUtils.getSetProperty;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public class UpdateNodeLiveIngesterE2eTest
{
    private static final Network network = Network.newNetwork();
    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final GenericContainer<?> sfs = DockerContainers.createSfsContainerWithin(network);
    @Container
    private static final GenericContainer<?> transformCore = DockerContainers.createTransformCoreAioContainerWithin(network)
            .dependsOn(activemq);
    @Container
    private static final GenericContainer<?> transformRouter = DockerContainers.createTransformRouterContainerWithin(network)
            .dependsOn(activemq, transformCore);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final LocalStackContainer awsMock = DockerContainers.createLocalStackContainerWithin(network);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq, transformCore, transformRouter, sfs);
    @Container
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer().dependsOn(activemq, hxInsightMock, repository);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ALLOW_ACCESS_PROPERTY = "ALLOW_ACCESS";
    private static final String DENY_ACCESS_PROPERTY = "DENY_ACCESS";
    public static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    public static final String ALICE = "abeecher";
    public static final String MIKE = "mjackson";
    protected RepositoryClient repositoryClient;

    @BeforeAll
    @SneakyThrows
    public void beforeAll()
    {
        repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testCreateNodeWithDefaultPermissions()
    {
        // when
        Node createdNode = repositoryClient.createNodeWithContent(
                "-my-",
                "test file",
                new ByteArrayInputStream("test file content".getBytes()),
                "text/plain");

        repositoryClient.setReadAccess(createdNode.id(), ALICE, MIKE);

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events")));

            assertFalse(requests.isEmpty());

            Optional<LoggedRequest> permissionsUpdatedEvent = requests.stream()
                    .filter(request -> request.getBodyAsString().contains(createdNode.id()))
                    .filter(request -> request.getBodyAsString().contains("update"))
                    .filter(request -> request.getBodyAsString().contains(ALLOW_ACCESS_PROPERTY))
                    .findFirst();

            assertTrue(permissionsUpdatedEvent.isPresent());

            JsonNode properties = objectMapper.readTree(permissionsUpdatedEvent.get().getBodyAsString())
                    .get(0)
                    .get("properties");

            assertTrue(properties.has(ALLOW_ACCESS_PROPERTY));
            assertEquals(Set.of(GROUP_EVERYONE, ALICE), getSetProperty(properties, ALLOW_ACCESS_PROPERTY));

            assertTrue(properties.has(DENY_ACCESS_PROPERTY));
            assertEquals(Set.of(MIKE), getSetProperty(properties, DENY_ACCESS_PROPERTY));
        });
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(getRepoJavaOptsWithTransforms(postgres, activemq));
        // @formatter:on
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network);
    }

}
