/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.common.test.docker.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;

class DockerContainersTest
{
    @Test
    void shouldCreatePostgresContainer()
    {
        PostgreSQLContainer<?> container = DockerContainers.createPostgresContainer();

        assertThat(container).isNotNull();
        assertThat(container.getUsername()).isEqualTo("alfresco");
        assertThat(container.getPassword()).isEqualTo("alfresco");
        assertThat(container.getDatabaseName()).isEqualTo("alfresco");
        assertThat(container.getExposedPorts()).contains(5432);
    }

    @Test
    void shouldCreatePostgresContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        PostgreSQLContainer<?> container = DockerContainers.createPostgresContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("postgres");
    }

    @Test
    void shouldCreateActiveMqContainer()
    {
        GenericContainer<?> container = DockerContainers.createActiveMqContainer();

        assertThat(container).isNotNull();
        assertThat(container.getEnvMap()).containsEntry("JAVA_OPTS", "-Xms512m -Xmx1g");
        assertThat(container.getExposedPorts()).contains(61616, 8161, 5672, 61613);
    }

    @Test
    void shouldCreateActiveMqContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createActiveMqContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("activemq");
    }

    @Test
    void shouldCreateWireMockContainer()
    {
        WireMockContainer container = DockerContainers.createWireMockContainer();

        assertThat(container).isNotNull();
        assertThat(container.getEnvMap()).containsEntry("WIREMOCK_OPTIONS", "--verbose");
    }

    @Test
    void shouldCreateWireMockContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        WireMockContainer container = DockerContainers.createWireMockContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getEnvMap()).containsEntry("WIREMOCK_OPTIONS", "--global-response-templating --verbose");
    }

    @Test
    void shouldCreateLocalStackContainer()
    {
        LocalStackContainer container = DockerContainers.createLocalStackContainer();

        assertThat(container).isNotNull();
        assertThat(container.getExposedPorts()).contains(4566);
    }

    @Test
    void shouldCreateLocalStackContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        LocalStackContainer container = DockerContainers.createLocalStackContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("aws-mock");
    }

    @Test
    void shouldConcatJavaOpts()
    {
        String result = DockerContainers.concatJavaOpts("-Xms512m", "-Xmx1g", "-Dtest=true");

        assertThat(result).isEqualTo("-Xms512m -Xmx1g -Dtest=true");
    }

    @Test
    void shouldGetMinimalRepoJavaOpts()
    {
        PostgreSQLContainer<?> postgres = mock(PostgreSQLContainer.class);
        when(postgres.getUsername()).thenReturn("alfresco");
        when(postgres.getPassword()).thenReturn("alfresco");
        when(postgres.getDatabaseName()).thenReturn("alfresco");
        when(postgres.getNetworkAliases()).thenReturn(List.of("postgres"));

        GenericContainer<?> activeMq = mock(GenericContainer.class);
        when(activeMq.getNetworkAliases()).thenReturn(List.of("activemq"));

        String javaOpts = DockerContainers.getMinimalRepoJavaOpts(postgres, activeMq);

        assertThat(javaOpts).contains("db.driver=org.postgresql.Driver");
        assertThat(javaOpts).contains("db.username=alfresco");
        assertThat(javaOpts).contains("db.password=alfresco");
        assertThat(javaOpts).contains("db.url=jdbc:postgresql://postgres:5432/alfresco");
        assertThat(javaOpts).contains(
                "messaging.broker.url=\"failover:(nio://activemq:61616)?timeout=3000&jms.useCompression=true\"");
        assertThat(javaOpts).contains("transform.service.enabled=false");
    }

    @Test
    void shouldGetRepoJavaOptsWithTransforms()
    {
        PostgreSQLContainer<?> postgres = mock(PostgreSQLContainer.class);
        when(postgres.getUsername()).thenReturn("alfresco");
        when(postgres.getPassword()).thenReturn("alfresco");
        when(postgres.getDatabaseName()).thenReturn("alfresco");
        when(postgres.getNetworkAliases()).thenReturn(List.of("postgres"));

        GenericContainer<?> activeMq = mock(GenericContainer.class);
        when(activeMq.getNetworkAliases()).thenReturn(List.of("activemq"));

        String javaOpts = DockerContainers.getRepoJavaOptsWithTransforms(postgres, activeMq);

        assertThat(javaOpts).contains("transform.service.enabled=true");
        assertThat(javaOpts).contains("transform.service.url=http://transform-router:8095");
        assertThat(javaOpts).contains("sfs.url=http://shared-file-store:8099");
    }

    @Test
    void shouldGetHxInsightRepoJavaOpts()
    {
        WireMockContainer mockContainer = mock(WireMockContainer.class);
        when(mockContainer.getNetworkAliases()).thenReturn(List.of("wiremock"));

        String javaOpts = DockerContainers.getHxInsightRepoJavaOpts(mockContainer);

        assertThat(javaOpts).contains("hxi.discovery.agents-endpoint=http://wiremock:8080");
        assertThat(javaOpts).contains("hxi.discovery.questions-endpoint=http://wiremock:8080");
        assertThat(javaOpts).contains("hxi.auth.providers.hyland-experience.token-uri=http://wiremock:8080/token");
    }

    @Test
    void shouldCreateNucleusSyncContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createNucleusSyncContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("nucleus-sync");
        assertThat(container.getExposedPorts()).contains(8081);
    }

    @Test
    void shouldCreateNucleusSyncContainerWithoutNetwork()
    {
        GenericContainer<?> container = DockerContainers.createNucleusSyncContainerWithin(null);

        assertThat(container).isNotNull();
        assertThat(container.getExposedPorts()).contains(8081);
        assertThat(container.getNetworkAliases()).doesNotContain("nucleus-sync");
    }

    @Test
    void shouldGetAppInfoRegex()
    {
        String regex = DockerContainers.getAppInfoRegex();
        String expectedPattern = "ACS HXI Connector\\/%s ACS\\/.*";
        String escapedVersion = escapeForRegex(DockerTags.getHxiConnectorTag());
        String expectedRegex = String.format(expectedPattern, escapedVersion);

        assertThat(regex).isEqualTo(expectedRegex);
    }

    @Test
    void shouldEscapeSpecialCharsInRegex()
    {
        String testVersion = "test[1.2.3]+{version}";
        String pattern = "prefix/%s/suffix";

        String testRegex = String.format(pattern, escapeForRegex(testVersion));

        assertThat(escapeForRegex(testVersion))
                .isEqualTo("test\\[1\\.2\\.3\\]\\+\\{version\\}");

        assertThat(testRegex)
                .isEqualTo("prefix/test\\[1\\.2\\.3\\]\\+\\{version\\}/suffix");
    }

    @Test
    void shouldCreateTransformRouterContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createTransformRouterContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("transform-router");
        assertThat(container.getExposedPorts()).contains(8095);
        assertThat(container.getEnvMap()).containsEntry("ACTIVEMQ_URL", "nio://activemq:61616");
    }

    @Test
    void shouldCreateTransformCoreAioContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createTransformCoreAioContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("transform-core-aio");
        assertThat(container.getExposedPorts()).contains(8090);
        assertThat(container.getEnvMap()).containsEntry("ACTIVEMQ_URL", "nio://activemq:61616");
    }

    @Test
    void shouldCreateSfsContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createSfsContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("shared-file-store");
        assertThat(container.getExposedPorts()).contains(8099);
        assertThat(container.getEnvMap()).containsEntry("JAVA_OPTS", "-Xms256m -Xmx512m");
    }

    @Test
    void shouldCreateBulkIngesterContainerWithinNetwork()
    {
        Network network = Network.newNetwork();
        PostgreSQLContainer<?> postgres = mock(PostgreSQLContainer.class);
        when(postgres.getUsername()).thenReturn("alfresco");
        when(postgres.getPassword()).thenReturn("alfresco");
        when(postgres.getDatabaseName()).thenReturn("alfresco");
        when(postgres.getNetworkAliases()).thenReturn(List.of("postgres"));

        GenericContainer<?> container = DockerContainers.createBulkIngesterContainerWithin(postgres, network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("bulk-ingester");
        assertThat(container.getExposedPorts()).contains(5008);
        assertThat(container.getEnvMap()).containsEntry("SPRING_DATASOURCE_USERNAME", "alfresco");
        assertThat(container.getEnvMap()).containsEntry("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/alfresco");
    }

    @Test
    void shouldCreateLiveIngesterContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createLiveIngesterContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("live-ingester");
        assertThat(container.getExposedPorts()).contains(8080, 5007);
        assertThat(container.getEnvMap()).containsEntry("SPRING_ACTIVEMQ_BROKERURL", "nio://activemq:61616");
    }

    @Test
    void shouldCreateLiveIngesterContainerForWireMock()
    {
        Network network = Network.newNetwork();
        WireMockContainer wireMock = mock(WireMockContainer.class);
        when(wireMock.getNetworkAliases()).thenReturn(List.of("hxi-mock"));

        GenericContainer<?> container = DockerContainers.createLiveIngesterContainerForWireMock(wireMock, network);

        assertThat(container).isNotNull();
        assertThat(container.getEnvMap()).containsEntry("HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL", "http://hxi-mock:8080");
        assertThat(container.getEnvMap()).containsEntry("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI", "http://hxi-mock:8080/token");
        assertThat(container.getEnvMap()).containsEntry("AUTH_PROVIDERS_ALFRESCO_USERNAME", "admin");
    }

    @Test
    void shouldCreateLiveIngesterContainerForWireMockWithAcs()
    {
        Network network = Network.newNetwork();
        WireMockContainer wireMock = mock(WireMockContainer.class);
        when(wireMock.getNetworkAliases()).thenReturn(List.of("hxi-mock"));
        AlfrescoRepositoryContainer acsContainer = mock(AlfrescoRepositoryContainer.class);
        when(acsContainer.getNetworkAliases()).thenReturn(List.of("alfresco"));

        GenericContainer<?> container = DockerContainers.createLiveIngesterContainerForWireMock(wireMock, acsContainer, network);

        assertThat(container).isNotNull();
        assertThat(container.getEnvMap()).containsEntry("ALFRESCO_REPOSITORY_BASE_URL", "http://alfresco:8080/alfresco");
    }

    @Test
    void shouldCreatePredictionApplierContainerWithinNetwork()
    {
        Network network = Network.newNetwork();

        GenericContainer<?> container = DockerContainers.createPredictionApplierContainerWithin(network);

        assertThat(container).isNotNull();
        assertThat(container.getNetwork()).isEqualTo(network);
        assertThat(container.getNetworkAliases()).contains("prediction-applier");
        assertThat(container.getExposedPorts()).contains(8080, 5009);
        assertThat(container.getEnvMap()).containsEntry("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG");
    }

    private String escapeForRegex(String version)
    {
        return version.replaceAll("[\\\\^$.|?*+()\\[\\]{}]", "\\\\$0");
    }
}
