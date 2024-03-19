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
package org.alfresco.hxi_connector.common.test.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

public class DockerContainers
{
    private static final String REPOSITORY_IMAGE = "alfresco/alfresco-content-repository-community";
    private static final String REPOSITORY_TAG = DockerTags.getRepositoryTag();
    private static final String POSTGRES_IMAGE = "postgres";
    private static final String POSTGRES_TAG = DockerTags.getPostgresTag();
    private static final String ACTIVE_MQ_IMAGE = "quay.io/alfresco/alfresco-activemq";
    private static final String ACTIVE_MQ_TAG = DockerTags.getActiveMqTag();
    private static final String DB_USER = "alfresco";
    private static final String DB_PASS = "alfresco";
    private static final String DB_NAME = "alfresco";
    private static final String REPOSITORY_ALIAS = "repository";
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String ACTIVE_MQ_ALIAS = "activemq";

    public static GenericContainer<?> createExtendedRepositoryContainerWithin(Network network)
    {
        Path jarFile = findTargetJar();
        GenericContainer<?> repository = new GenericContainer<>(new ImageFromDockerfile("localhost/alfresco/alfresco-content-repository-prediction-applier-extension")
                .withFileFromPath(jarFile.toString(), jarFile)
                .withDockerfileFromBuilder(builder -> builder
                        .from(DockerImageName.parse(REPOSITORY_IMAGE).withTag(REPOSITORY_TAG).toString())
                        .user("root")
                        .copy(jarFile.toString(), "/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/")
                        .run("chown -R -h alfresco /usr/local/tomcat")
                        .user("alfresco")
                        .build()))
                                .withEnv("CATALINA_OPTS", "-agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n")
                                .withEnv("JAVA_TOOL_OPTIONS", """
                                        -Dencryption.keystore.type=JCEKS
                                        -Dencryption.cipherAlgorithm=DESede/CBC/PKCS5Padding
                                        -Dencryption.keyAlgorithm=DESede
                                        -Dencryption.keystore.location=/usr/local/tomcat/shared/classes/alfresco/extension/keystore/keystore
                                        -Dmetadata-keystore.password=mp6yc0UD9e
                                        -Dmetadata-keystore.aliases=metadata
                                        -Dmetadata-keystore.metadata.password=oKIWzVdEdA
                                        -Dmetadata-keystore.metadata.algorithm=DESede
                                        """.replace("\n", " "))
                                .withExposedPorts(8080, 8000)
                                .withReuse(true);

        Optional.ofNullable(network).ifPresent(n -> repository.withNetwork(n).withNetworkAliases(REPOSITORY_ALIAS));

        return repository;
    }

    public static PostgreSQLContainer<?> createPostgresContainer()
    {
        return createPostgresContainerWithin(null);
    }

    public static PostgreSQLContainer<?> createPostgresContainerWithin(Network network)
    {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE).withTag(POSTGRES_TAG))
                .withUsername(DB_USER)
                .withPassword(DB_PASS)
                .withDatabaseName(DB_NAME)
                .withExposedPorts(5432)
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> postgres.withNetwork(n).withNetworkAliases(POSTGRES_ALIAS));

        return postgres;
    }

    public static GenericContainer<?> createActiveMqContainer()
    {
        return createActiveMqContainerWithin(null);
    }

    public static GenericContainer<?> createActiveMqContainerWithin(Network network)
    {
        GenericContainer<?> activeMq = new GenericContainer<>(DockerImageName.parse(ACTIVE_MQ_IMAGE).withTag(ACTIVE_MQ_TAG))
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1g")
                .withExposedPorts(61616, 8161, 5672, 61613)
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2));

        Optional.ofNullable(network).ifPresent(n -> activeMq.withNetwork(n).withNetworkAliases(ACTIVE_MQ_ALIAS));

        return activeMq;
    }

    @SneakyThrows
    private static Path findTargetJar()
    {
        @Cleanup
        Stream<Path> files = Files.list(Paths.get("target"));

        return files.filter(new MatchExtensionPredicate("jar"))
                .filter(f -> !f.getFileName().toString().contains("-tests"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("JAR file not found in target/ directory"));
    }

    /* public static GenericContainer<?> createAlfrescoSearchServiceContainer(Network network) { return new GenericContainer<>(DockerImageName.parse("alfresco/alfresco-search-services").withTag("2.0.3")) .withNetwork(network) .withNetworkAliases("search-service") .withEnv("SOLR_ALFRESCO_HOST", "repository") .withEnv("SOLR_ALFRESCO_PORT", "8080") .withEnv("SOLR_SOLR_HOST", "search-service") .withEnv("SOLR_SOLR_PORT", "8983") .withEnv("SOLR_CREATE_ALFRESCO_DEFAULTS", "alfresco,archive") .withEnv("ALFRESCO_SECURE_COMMS", "secret") .withEnv("JAVA_TOOL_OPTIONS", "-Dalfresco.secureComms.secret=secret") .withExposedPorts(8983); } */
}
