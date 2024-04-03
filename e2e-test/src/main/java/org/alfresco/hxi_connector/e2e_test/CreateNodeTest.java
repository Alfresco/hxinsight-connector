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

import static io.restassured.RestAssured.given;

import java.io.File;

import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.util.DockerContainers;

@Slf4j
@Testcontainers
public class CreateNodeTest
{

    private static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final Network network = Network.newNetwork();

    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final GenericContainer<?> sfs = DockerContainers.createSfsContainerWithin(network);

    @Container
    private static final GenericContainer<?> transform_core_aio = DockerContainers.createTransformCoreAioContainerWithin(network)
            .dependsOn(activemq)
            .dependsOn(sfs);
    @Container
    private static final GenericContainer<?> transform_router = DockerContainers.createTransformRouterContainerWithin(network)
            .dependsOn(activemq)
            .dependsOn(transform_core_aio)
            .dependsOn(sfs);
    @Container
    private static final GenericContainer<?> repository = createRepositoryContainer()
            .dependsOn(postgres)
            .dependsOn(activemq)
            .dependsOn(transform_router)
            .dependsOn(transform_core_aio)
            .dependsOn(sfs);
    @Container
    private static final WireMockContainer hxAuthServer = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("./src/main/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final GenericContainer<?> live_ingester = createLiveIngesterContainer()
            .dependsOn(activemq)
            .dependsOn(sfs)
            .dependsOn(repository);
    @Container
    private static final LocalStackContainer localStackServer = DockerContainers.createLocalStackContainerWithin(network);

    @BeforeAll
    @SneakyThrows
    public static void beforeAll()
    {
        localStackServer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
    }

    @Test
    void testCreateFile()
    {

        Response acsResponse = given().auth().basic("admin", "admin")
                .contentType("multipart/form-data")
                .multiPart("filedata", new File("src/main/resources/test-files/Alfresco Content Services 7.4.docx"))
                .when()
                .post("http://" + repository.getHost() + ":" + repository.getFirstMappedPort() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/-my-/children")
                .then()
                .extract().response();

        Assertions.assertEquals(201, acsResponse.statusCode());
        Assertions.assertNotNull(acsResponse.jsonPath().get("entry.id"));

        Response s3Response = given()
                .contentType("application/xml")
                .when()
                .get("http://localhost:" + localStackServer.getFirstMappedPort() + "/test-hxinsight-bucket/")
                .then()
                .extract().response();

        Assertions.assertEquals(200, s3Response.statusCode());
        // String stringS3response = s3Response.asString();
        // Assertions.assertTrue(stringS3response.contains("Contents"));

    }

    private static GenericContainer<?> createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network, true)
                .withEnv("JAVA_OPTS", """
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
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerWithin(network)
                .withEnv("HYLAND-EXPERIENCE_INSIGHT_BASE-URL", "http://%s:8080"
                        .formatted(
                                hxAuthServer.getNetworkAliases().stream().findFirst().get()))

                .withEnv("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_HYLAND-EXPERIENCE-AUTH_TOKEN-URI", "http://%s:8080/token"
                        .formatted(
                                hxAuthServer.getNetworkAliases().stream().findFirst().get()));
    }

}
