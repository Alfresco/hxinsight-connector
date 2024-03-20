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

import org.alfresco.hxi_connector.common.test.util.DockerContainers;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class CreateNodeTest  {

    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    static final GenericContainer<?> repository = createRepositoryContainer();

    @Container
    static final GenericContainer<?> transform_router = DockerContainers.createTransformRouterContainerWithin(network);
    @Container
    private static final GenericContainer<?> transform_core_aio = DockerContainers.createTransformCoreAioContainerWithin(network);
//    @Container
//    static final GenericContainer<?> transform_router = createTransformRouterContainer();
//    @Container
//    static final GenericContainer<?> transform_core_aio = createTransformCoreAioContainer();
    @Container
    static final GenericContainer<?> sfs = DockerContainers.createSfsContainer(network);

    @Test
    void testCreateFile() {

        given().auth().basic("admin","admin")
                .contentType("application/json")
                .body("{\"name\": \"testFile1.docx\", \"nodeType\": \"cm:content\"}")
                .when()
                .post("http://"+ repository.getHost() + ":"+ repository.getFirstMappedPort()+ "/alfresco/api/-default-/public/alfresco/versions/1/nodes/-my-/children")
                .then()
                .statusCode(201);
//                .body("list.entries", notNullValue());


    }

    private static GenericContainer<?> createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
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
//                                transform_router.getFirstMappedPort())
                        .replace("\n", " "));
    }

//    private static GenericContainer<?> createTransformRouterContainer()
//    {
//        return DockerContainers.createTransformRouterContainerWithin(network)
//                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
//                .withEnv("CORE_AIO_URL", "http://transform-core-aio:8090")
//                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file");
//    }
//
//    private static GenericContainer<?> createTransformCoreAioContainer()
//    {
//        return DockerContainers.createTransformCoreAioContainerWithin(network)
//                .withEnv("ACTIVEMQ_URL", "nio://activemq:61616")
//                .withEnv("FILE_STORE_URL", "http://shared-file-store:8099/alfresco/api/-default-/private/sfs/versions/1/file");
//    }

}
