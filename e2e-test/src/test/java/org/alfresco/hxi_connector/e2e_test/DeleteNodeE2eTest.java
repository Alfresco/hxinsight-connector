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

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Cleanup;
import lombok.SneakyThrows;
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

public class DeleteNodeE2eTest
{
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";

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

    RepositoryClient repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
        GenericContainer<?> liveIngester = createLiveIngesterContainer().dependsOn(activemq, hxInsightMock, repository);
        liveIngester.start();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testReceiveDeleteEvent() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, "dummy.txt", fileContent, "text/plain");
        RetryUtils.retryWithBackoff(() -> verify(exactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(createdNode.id()))));

        // when
        repositoryClient.deleteNode(createdNode.id());

        // then
        RetryUtils.retryWithBackoff(() -> verify(exactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(containing(createdNode.id()))));
        verify(exactly(1), postRequestedFor(urlEqualTo("/ingestion-events"))
                .withRequestBody(matching(".*\"objectId\":\"" + createdNode.id() + "\",\"sourceId\":\"alfresco-dummy-source-id-0a63de491876\",\"eventType\":\"delete\".*"))
                .withHeader(USER_AGENT, matching(getAppInfoRegex())));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
                .withJavaOpts(getMinimalRepoJavaOpts(postgres, activemq));
        // @formatter:on
    }

    @SneakyThrows
    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network);
    }
}
