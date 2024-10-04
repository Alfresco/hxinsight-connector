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

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getRepoJavaOptsWithTransforms;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions"})
public class PredictionApplierWithUnreachableACSE2eTest
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
            .dependsOn(postgres, activemq, transformRouter, sfs);
    @Container
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer()
            .waitingFor(Wait.forLogMessage(".*Started LiveIngesterApplication.*", 1))
            .withEnv("ALFRESCO_REPOSITORY_BASE_URL", "http://localhost:1938/alfresco") // <- random unreachable port
            .dependsOn(activemq, hxInsightMock, awsMock, repository);

    @Container
    private static final GenericContainer<?> predictionApplier = createPredictionApplierContainer()
            .waitingFor(Wait.forLogMessage(".*Started PredictionApplierApplication.*", 1))
            .withEnv("ALFRESCO_REPOSITORY_BASE_URL", "http://localhost:1938/alfresco") // <- random unreachable port
            .dependsOn(activemq, hxInsightMock, repository, liveIngester);

    @BeforeAll
    public void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SneakyThrows
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void shouldNotProcessPredictionsIfRepoIsUnreachable()
    {
        // when
        Thread.sleep(2000);

        // then
        assertEquals(0, WireMock.findAll(RequestPatternBuilder.allRequests()).size());
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(getRepoJavaOptsWithTransforms(postgres, activemq));
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network)
                .withEnv("ALFRESCO_REPOSITORY_HEALTH_PROBE_INTERVAL_SECONDS", "1");
    }

    private static GenericContainer<?> createPredictionApplierContainer()
    {
        return DockerContainers.createPredictionApplierContainerWithin(network)
                .withEnv("SPRING_ACTIVEMQ_BROKERURL",
                        "nio://%s:61616".formatted(activemq.getNetworkAliases().stream().findFirst().get()))
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
