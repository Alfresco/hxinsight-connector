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
package org.alfresco.hxi_connector.common.test.docker.util;

import static org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer.pullRepositoryImage;

import java.time.Duration;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryExtension;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DockerContainers
{
    private static final String POSTGRES_IMAGE = "postgres";
    private static final String POSTGRES_TAG = DockerTags.getPostgresTag();
    private static final String ACTIVE_MQ_IMAGE = "quay.io/alfresco/alfresco-activemq";
    private static final String ACTIVE_MQ_TAG = DockerTags.getActiveMqTag();
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getWiremockTag();
    private static final String LOCALSTACK_IMAGE = "localstack/localstack";
    private static final String LOCALSTACK_TAG = DockerTags.getLocalStackTag();
    private static final String TRANSFORM_ROUTER_IMAGE = "quay.io/alfresco/alfresco-transform-router";
    private static final String TRANSFORM_ROUTER_TAG = DockerTags.getTransformRouterTag();
    private static final String TRANSFORM_CORE_AIO_IMAGE = "quay.io/alfresco/alfresco-transform-core-aio";
    private static final String TRANSFORM_CORE_AIO_TAG = DockerTags.getTransformCoreAioTag();
    private static final String SFS_IMAGE = "quay.io/alfresco/alfresco-shared-file-store";
    private static final String SFS_TAG = DockerTags.getSfsTag();
    private static final String LIVE_INGESTER_IMAGE = "quay.io/alfresco/alfresco-hxinsight-connector-live-ingester";
    private static final String LIVE_INGESTER_TAG = DockerTags.getHxiConnectorTag();
    private static final String PREDICTION_APPLIER_IMAGE = "quay.io/alfresco/alfresco-hxinsight-connector-prediction-applier";
    private static final String PREDICTION_APPLIER_TAG = DockerTags.getHxiConnectorTag();
    private static final String DB_USER = "alfresco";
    private static final String DB_PASS = "alfresco";
    private static final String DB_NAME = "alfresco";
    private static final String REPOSITORY_ALIAS = "repository";
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String ACTIVE_MQ_ALIAS = "activemq";
    private static final String TRANSFORM_ROUTER_ALIAS = "transform-router";
    private static final String TRANSFORM_CORE_AIO_ALIAS = "transform-core-aio";
    private static final String SFS_ALIAS = "shared-file-store";
    private static final String LIVE_INGESTER_ALIAS = "live-ingester";
    private static final String PREDICTION_APPLIER_ALIAS = "prediction-applier";
    private static final String LOCALSTACK_ALIAS = "aws-mock";

    public static AlfrescoRepositoryContainer createExtendedRepositoryContainerWithin(Network network)
    {
        return createExtendedRepositoryContainerWithin(network, false);
    }

    public static AlfrescoRepositoryContainer createExtendedRepositoryContainerWithin(Network network, boolean enterprise)
    {
        pullRepositoryImage(enterprise);
        AlfrescoRepositoryContainer repository = new AlfrescoRepositoryContainer(
                new AlfrescoRepositoryExtension(
                        "alfresco-hxinsight-connector-prediction-applier-extension",
                        "localhost/alfresco/alfresco-content-repository-prediction-applier-extension",
                        enterprise))
                                .withStartupTimeout(Duration.ofMinutes(5));

        Optional.ofNullable(network).ifPresent(n -> repository.withNetwork(n).withNetworkAliases(REPOSITORY_ALIAS));

        return repository;
    }

    public static PostgreSQLContainer<?> createPostgresContainer()
    {
        return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE).withTag(POSTGRES_TAG))
                .withUsername(DB_USER)
                .withPassword(DB_PASS)
                .withDatabaseName(DB_NAME)
                .withExposedPorts(5432)
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    public static PostgreSQLContainer<?> createPostgresContainerWithin(Network network)
    {
        return createPostgresContainer()
                .withNetwork(network)
                .withNetworkAliases(POSTGRES_ALIAS);
    }

    public static GenericContainer<?> createActiveMqContainer()
    {
        return new GenericContainer<>(DockerImageName.parse(ACTIVE_MQ_IMAGE).withTag(ACTIVE_MQ_TAG))
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1g")
                .withExposedPorts(61616, 8161, 5672, 61613)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));
    }

    public static GenericContainer<?> createActiveMqContainerWithin(Network network)
    {
        return createActiveMqContainer()
                .withNetwork(network)
                .withNetworkAliases(ACTIVE_MQ_ALIAS);
    }

    public static GenericContainer<?> createTransformRouterContainerWithin(Network network)
    {
        GenericContainer<?> transformRouter = new GenericContainer<>(DockerImageName.parse(TRANSFORM_ROUTER_IMAGE).withTag(TRANSFORM_ROUTER_TAG))
                .withEnv("JAVA_OPTS", "-Xms256m -Xmx512m")
                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
                .withEnv("CORE_AIO_URL", "http://transform-core-aio:8090")
                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file")
                .withExposedPorts(8095)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> transformRouter.withNetwork(n).withNetworkAliases(TRANSFORM_ROUTER_ALIAS));

        return transformRouter;
    }

    public static GenericContainer<?> createTransformCoreAioContainerWithin(Network network)
    {
        GenericContainer<?> transformCoreAio = new GenericContainer<>(DockerImageName.parse(TRANSFORM_CORE_AIO_IMAGE).withTag(TRANSFORM_CORE_AIO_TAG))
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1024m")
                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file")
                .withExposedPorts(8090)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> transformCoreAio.withNetwork(n).withNetworkAliases(TRANSFORM_CORE_AIO_ALIAS));

        return transformCoreAio;
    }

    public static GenericContainer<?> createSfsContainerWithin(Network network)
    {
        GenericContainer<?> sfs = new GenericContainer<>(DockerImageName.parse(SFS_IMAGE).withTag(SFS_TAG))
                .withEnv("JAVA_OPTS", "-Xms256m -Xmx512m")
                .withEnv("scheduler.content.age.millis", "86400000")
                .withEnv("scheduler.cleanup.interval", "86400000")
                .withExposedPorts(8099)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> sfs.withNetwork(n).withNetworkAliases(SFS_ALIAS));

        return sfs;
    }

    public static GenericContainer<?> createLiveIngesterContainerWithin(Network network)
    {
        GenericContainer<?> liveIngester = new GenericContainer<>(DockerImageName.parse(LIVE_INGESTER_IMAGE).withTag(LIVE_INGESTER_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n")
                .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
                .withEnv("SPRING_ACTIVEMQ_BROKERURL", "nio://activemq:61616")
                .withEnv("ALFRESCO_TRANSFORM_SHARED-FILE-STORE_HOST", "http://shared-file-store")
                .withEnv("ALFRESCO_TRANSFORM_SHARED-FILE-STORE_PORT", "8099")
                .withExposedPorts(5007)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> liveIngester.withNetwork(n).withNetworkAliases(LIVE_INGESTER_ALIAS));

        return liveIngester;
    }

    public static GenericContainer<?> createPredictionApplierContainerWithin(Network network)
    {
        GenericContainer<?> predictionApplier = new GenericContainer<>(DockerImageName.parse(PREDICTION_APPLIER_IMAGE).withTag(PREDICTION_APPLIER_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5009,server=y,suspend=n")
                .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
                .withExposedPorts(5009)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> predictionApplier.withNetwork(n).withNetworkAliases(PREDICTION_APPLIER_ALIAS));

        return predictionApplier;
    }

    public static WireMockContainer createWireMockContainer()
    {
        return new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG))
                .withEnv("WIREMOCK_OPTIONS", "--verbose");
    }

    public static WireMockContainer createWireMockContainerWithin(Network network)
    {
        return createWireMockContainer()
                .withNetwork(network)
                .withEnv("WIREMOCK_OPTIONS", "--global-response-templating --verbose");
    }

    public static LocalStackContainer createLocalStackContainer()
    {
        return new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE).withTag(LOCALSTACK_TAG))
                .withExposedPorts(4566);
    }

    public static LocalStackContainer createLocalStackContainerWithin(Network network)
    {
        return createLocalStackContainer()
                .withNetwork(network)
                .withNetworkAliases(LOCALSTACK_ALIAS);
    }
}
