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

import java.io.IOException;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryNodesClient;

@Testcontainers
@SuppressWarnings({"PMD.FieldNamingConventions"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS_DEPLOY_TIMEOUT", matches = ".*")
/**
 * As of now this test class is excluded from GitHub Actions workflow but will run with the maven builds (thus, relies on GITHUB_ACTIONS_DEPLOY_TIMEOUT env variable being set).
 */
public class CreateNodeE2eTest extends CreateNodeE2eTestBase
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
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer()
            .dependsOn(activemq, hxInsightMock, awsMock);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq, transformCore, transformRouter, sfs, liveIngester);

    @BeforeAll
    @SneakyThrows
    public void beforeAll()
    {
        repositoryNodesClient = new RepositoryNodesClient(repository.getBaseUrl(), "admin", "admin");
        awsS3Client = new AwsS3Client(awsMock.getHost(), awsMock.getFirstMappedPort(), BUCKET_NAME);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
        awsMock.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingImageFile() throws IOException
    {
        testCreateNodeContainingImageFileBase();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingTextFile() throws IOException
    {
        testCreateNodeContainingTextFileBase();
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network, true)
            .withJavaOpts("""
            -Ddb.driver=org.postgresql.Driver
            -Ddb.username=%s
            -Ddb.password=%s
            -Ddb.url=jdbc:postgresql://%s:5432/%s
            -Dmessaging.broker.url="failover:(nio://%s:61616)?timeout=3000&jms.useCompression=true"
            -Ddeployment.method=DOCKER_COMPOSE
            -Dtransform.service.enabled=true
            -Dtransform.service.url=http://transform-router:8095
            -Dsfs.url=http://shared-file-store:8099/
            -DlocalTransform.core-aio.url=http://transform-core-aio:8090/
            -Dalfresco-pdf-renderer.url=http://transform-core-aio:8090/
            -Djodconverter.url=http://transform-core-aio:8090/
            -Dimg.url=http://transform-core-aio:8090/
            -Dtika.url=http://transform-core-aio:8090/
            -Dtransform.misc.url=http://transform-core-aio:8090/
            -Dcsrf.filter.enabled=false
            -Dalfresco.restApi.basicAuthScheme=true
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

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerWithin(network)
                .withEnv("HYLAND-EXPERIENCE_INSIGHT_BASE-URL",
                        "http://%s:8080".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLAND-EXPERIENCE_TOKEN-URI",
                        "http://%s:8080/token".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLAND-EXPERIENCE_CLIENT-ID", "dummy-client-key");
    }
}
