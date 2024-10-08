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
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.COMMUNITY;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryExtension;
import org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DockerContainers
{
    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
    private static final String REPOSITORY_EXTENSION = DockerTags.getOrDefault("repository.extension", "alfresco-hxinsight-connector-hxinsight-extension");
    private static final String EXTENDED_REPOSITORY_LOCAL_NAME = "localhost/alfresco/alfresco-content-repository-hxinsight-extension";
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
    private static final String BULK_INGESTER_IMAGE = "quay.io/alfresco/alfresco-hxinsight-connector-bulk-ingester";
    private static final String LIVE_INGESTER_IMAGE = "quay.io/alfresco/alfresco-hxinsight-connector-live-ingester";
    private static final String HXI_CONNECTOR_TAG = DockerTags.getHxiConnectorTag();
    private static final String PREDICTION_APPLIER_IMAGE = "quay.io/alfresco/alfresco-hxinsight-connector-prediction-applier";
    private static final String DB_USER = "alfresco";
    private static final String DB_PASS = "alfresco";
    private static final String DB_NAME = "alfresco";
    private static final String REPOSITORY_ALIAS = "repository";
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String ACTIVE_MQ_ALIAS = "activemq";
    private static final String TRANSFORM_ROUTER_ALIAS = "transform-router";
    private static final String TRANSFORM_CORE_AIO_ALIAS = "transform-core-aio";
    private static final String SFS_ALIAS = "shared-file-store";
    private static final String BULK_INGESTER_ALIAS = "bulk-ingester";
    private static final String LIVE_INGESTER_ALIAS = "live-ingester";
    private static final String PREDICTION_APPLIER_ALIAS = "prediction-applier";
    private static final String LOCALSTACK_ALIAS = "aws-mock";
    public static final String MINIMAL_REPO_JAVA_OPTS = """
            -Ddb.driver=org.postgresql.Driver
            -Ddb.username=%s
            -Ddb.password=%s
            -Ddb.url=jdbc:postgresql://%s:5432/%s
            -Dcsrf.filter.enabled=false
            -Dmessaging.broker.url="failover:(nio://%s:61616)?timeout=3000&jms.useCompression=true"
            -Dalfresco.restApi.basicAuthScheme=true
            -Ddeployment.method=DOCKER_COMPOSE
            -Dalfresco.host=alfresco
            -Dalfresco.port=8080
            -Xms1500m -Xmx1500m
            -Dtransform.service.enabled=%s
            """;
    public static final String TRANSFORMS_REPO_JAVA_OPTS = """
            -Dtransform.service.url=http://transform-router:8095
            -Dsfs.url=http://shared-file-store:8099
            -DlocalTransform.core-aio.url=http://transform-core-aio:8090
            -Dalfresco-pdf-renderer.url=http://transform-core-aio:8090
            -Djodconverter.url=http://transform-core-aio:8090
            -Dimg.url=http://transform-core-aio:8090
            -Dtika.url=http://transform-core-aio:8090
            -Dtransform.misc.url=http://transform-core-aio:8090
            """;

    public static AlfrescoRepositoryContainer createExtendedRepositoryContainerWithin(Network network)
    {
        return createExtendedRepositoryContainerWithin(network, COMMUNITY);
    }

    public static AlfrescoRepositoryContainer createExtendedRepositoryContainerWithin(Network network, RepositoryType repositoryType)
    {
        pullRepositoryImage(repositoryType);
        AlfrescoRepositoryContainer repository = new AlfrescoRepositoryContainer(
                new AlfrescoRepositoryExtension(REPOSITORY_EXTENSION, EXTENDED_REPOSITORY_LOCAL_NAME, repositoryType))
                        .waitingFor(Wait
                                .forHttp("/alfresco")
                                .forPort(8080)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(AlfrescoRepositoryContainer.class.getSimpleName())));

        Optional.ofNullable(network).ifPresent(n -> repository.withNetwork(n).withNetworkAliases(REPOSITORY_ALIAS));

        return repository;
    }

    public static String concatJavaOpts(String... opts)
    {
        return String.join(" ", opts);
    }

    public static @NotNull String getMinimalRepoJavaOpts(PostgreSQLContainer<?> postgresContainer, GenericContainer<?> activemqContainer)
    {
        return MINIMAL_REPO_JAVA_OPTS.formatted(
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getNetworkAliases().stream().findFirst().get(),
                postgresContainer.getDatabaseName(),
                activemqContainer.getNetworkAliases().stream().findFirst().get(),
                "false")
                .replace("\n", " ");
    }

    public static @NotNull String getRepoJavaOptsWithTransforms(PostgreSQLContainer<?> postgresContainer, GenericContainer<?> activemqContainer)
    {
        return MINIMAL_REPO_JAVA_OPTS.formatted(
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                postgresContainer.getNetworkAliases().stream().findFirst().get(),
                postgresContainer.getDatabaseName(),
                activemqContainer.getNetworkAliases().stream().findFirst().get(),
                "true").replace("\n", " ") +
                TRANSFORMS_REPO_JAVA_OPTS.replace("\n", " ");
    }

    public static String getHxInsightRepoJavaOpts(WireMockContainer hxInsightMockContainer)
    {
        String hXIMockAlias = hxInsightMockContainer.getNetworkAliases().stream().findFirst().get();
        return """
                -Dhxi.discovery.agents-endpoint=http://%s:8080
                -Dhxi.discovery.questions-endpoint=http://%s:8080
                -Dhxi.auth.providers.hyland-experience.token-uri=http://%s:8080/token
                -Dhxi.question.max-context-size-for-question=10
                """
                .formatted(hXIMockAlias, hXIMockAlias, hXIMockAlias)
                .replace("\n", " ");
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
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5011,server=y,suspend=n")
                .withEnv("JAVA_OPTS", "-Xms256m -Xmx512m")
                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
                .withEnv("CORE_AIO_URL", "http://transform-core-aio:8090")
                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file")
                .withExposedPorts(8095)
                .waitingFor(Wait.forHttp("/")
                        .forPort(8095)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("TransformRouterContainer")));

        Optional.ofNullable(network).ifPresent(n -> transformRouter.withNetwork(n).withNetworkAliases(TRANSFORM_ROUTER_ALIAS));

        return transformRouter;
    }

    public static GenericContainer<?> createTransformCoreAioContainerWithin(Network network)
    {
        GenericContainer<?> transformCoreAio = new GenericContainer<>(DockerImageName.parse(TRANSFORM_CORE_AIO_IMAGE).withTag(TRANSFORM_CORE_AIO_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5010,server=y,suspend=n")
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1024m")
                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file")
                .withExposedPorts(8090)
                .waitingFor(Wait.forHttp("/")
                        .forPort(8090)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("TransformCoreContainer")));

        Optional.ofNullable(network).ifPresent(n -> transformCoreAio.withNetwork(n).withNetworkAliases(TRANSFORM_CORE_AIO_ALIAS));

        return transformCoreAio;
    }

    public static GenericContainer<?> createSfsContainerWithin(Network network)
    {
        GenericContainer<?> sfs = new GenericContainer<>(DockerImageName.parse(SFS_IMAGE).withTag(SFS_TAG))
                .withEnv("JAVA_OPTS", "-Xms256m -Xmx512m")
                .withEnv("SCHEDULER_CONTENT_AGE_MILLIS", "86400000")
                .withEnv("SCHEDULER_CLEANUP_INTERVAL", "86400000")
                .withExposedPorts(8099)
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> sfs.withNetwork(n).withNetworkAliases(SFS_ALIAS));

        return sfs;
    }

    public static GenericContainer<?> createBulkIngesterContainerWithin(PostgreSQLContainer<?> postgresContainer, Network network)
    {
        GenericContainer<?> bulkIngester = new GenericContainer<>(DockerImageName.parse(BULK_INGESTER_IMAGE).withTag(HXI_CONNECTOR_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5008,server=y,suspend=n")
                .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
                .withEnv("SPRING_ACTIVEMQ_BROKERURL", "nio://activemq:61616")
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:5432/%s".formatted(
                        postgresContainer.getNetworkAliases().stream().findFirst().get(),
                        postgresContainer.getDatabaseName()))
                .withEnv("SPRING_DATASOURCE_USERNAME", postgresContainer.getUsername())
                .withEnv("SPRING_DATASOURCE_PASSWORD", postgresContainer.getPassword())
                .withExposedPorts(5008)
                .withStartupTimeout(Duration.ofMinutes(2))
                .waitingFor(Wait.forLogMessage(".*Started BulkIngesterApplication.*", 1))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("BulkIngesterContainer")));

        Optional.ofNullable(network).ifPresent(n -> bulkIngester.withNetwork(n).withNetworkAliases(BULK_INGESTER_ALIAS));

        return bulkIngester;
    }

    public static GenericContainer<?> createLiveIngesterContainerWithin(Network network)
    {
        GenericContainer<?> liveIngester = new GenericContainer<>(DockerImageName.parse(LIVE_INGESTER_IMAGE).withTag(HXI_CONNECTOR_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n")
                .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
                .withEnv("SPRING_ACTIVEMQ_BROKERURL", "nio://activemq:61616")
                .withEnv("ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL", "http://shared-file-store:8099")
                .withEnv("ALFRESCO_REPOSITORY_HEALTH_PROBE_INTERVAL_SECONDS", "1")
                .withExposedPorts(8080, 5007)
                .waitingFor(Wait.forHttp("/actuator/health/readiness")
                        .forPort(8080)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("LiveIngesterContainer")));

        Optional.ofNullable(network).ifPresent(n -> liveIngester.withNetwork(n).withNetworkAliases(LIVE_INGESTER_ALIAS));

        return liveIngester;
    }

    public static GenericContainer<?> createLiveIngesterContainerForWireMock(WireMockContainer hxInsightMockContainer, Network network)
    {
        return createLiveIngesterContainerWithin(network)
                .withEnv("HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL",
                        "http://%s:8080".formatted(hxInsightMockContainer.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI",
                        "http://%s:8080/token".formatted(hxInsightMockContainer.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLANDEXPERIENCE_CLIENTID", "dummy-client-key")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_USERNAME", "admin")
                .withEnv("AUTH_PROVIDERS_ALFRESCO_PASSWORD", "admin");
    }

    public static GenericContainer<?> createLiveIngesterContainerForWireMock(WireMockContainer hxInsightMockContainer, AlfrescoRepositoryContainer acsContainer, Network network)
    {
        return createLiveIngesterContainerForWireMock(hxInsightMockContainer, network)
                .withEnv("ALFRESCO_REPOSITORY_BASE_URL", "http://%s:8080/alfresco".formatted(acsContainer.getNetworkAliases().stream().findFirst().get()));
    }

    public static GenericContainer<?> createPredictionApplierContainerWithin(Network network)
    {
        GenericContainer<?> predictionApplier = new GenericContainer<>(DockerImageName.parse(PREDICTION_APPLIER_IMAGE).withTag(HXI_CONNECTOR_TAG))
                .withEnv("JAVA_TOOL_OPTIONS", "-agentlib:jdwp=transport=dt_socket,address=*:5009,server=y,suspend=n")
                .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
                .withExposedPorts(8080, 5009)
                .waitingFor(Wait.forHttp("/actuator/health/readiness")
                        .forPort(8080)
                        .withStartupTimeout(Duration.ofMinutes(2)))
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("PredictionApplierContainer")));

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
                .withExposedPorts(4566)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("LocalStackContainer")));
    }

    public static LocalStackContainer createLocalStackContainerWithin(Network network)
    {
        return createLocalStackContainer()
                .withNetwork(network)
                .withNetworkAliases(LOCALSTACK_ALIAS);
    }

    public static @NotNull String getAppInfoRegex()
    {
        // Since ACS 23.2.1 release was a super quick fix to 23.2.0 it shows as 23.2.0 in the discovery endpoint.
        // Hence, we cannot use DockerTags.getRepositoryTag() without additional magic here
        // When we move past 23.2.1 ACS version we can modify this method to return more accurate regex.
        String pattern = "ACS HXI Connector\\/%s ACS\\/.*";
        String appVersion = escapeSpecialChars(DockerTags.getHxiConnectorTag());
        return pattern.formatted(appVersion);
    }

    private static String escapeSpecialChars(String string)
    {
        return SPECIAL_REGEX_CHARS.matcher(string).replaceAll("\\\\$0");
    }
}
