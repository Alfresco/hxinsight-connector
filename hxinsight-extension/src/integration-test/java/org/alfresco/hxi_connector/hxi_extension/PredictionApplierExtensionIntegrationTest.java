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
package org.alfresco.hxi_connector.hxi_extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.hxi_extension.rest.api.ConfigEntityResource.HxIConfig;
import org.alfresco.hxi_connector.hxi_extension.util.client.TestRepoClient;
import org.alfresco.rest.api.model.Aspect;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class PredictionApplierExtensionIntegrationTest
{
    private static final String EXPECTED_HXI_ASPECT = "hxi:predictionApplied";

    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    static final AlfrescoRepositoryContainer repository = createRepositoryContainer();

    TestRepoClient testRepoClient = new TestRepoClient(repository.getHost(), repository.getPort());

    @Test
    void testHxIModelInstallation()
    {
        // when
        Aspect actualAspect = testRepoClient.getAspectById(EXPECTED_HXI_ASPECT);

        // then
        assertThat(actualAspect).isNotNull();
    }

    @Test
    void testConfigUrl()
    {
        // when
        HxIConfig config = testRepoClient.getConfig();

        // then
        assertThat(config.getKnowledgeRetrievalUrl()).isEqualTo("http://dummy-host.xyz/knowledge-retrieval/bots");
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
            .withEnv("JAVA_OPTS", """
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
}
