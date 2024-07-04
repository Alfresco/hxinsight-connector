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

import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.common.mapper.TypeRef;
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
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class AskQuestionE2eTest
{
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

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @Test
    void shouldReturn400IfAskedTooManyQuestions()
    {
        // given
        String questions = """
                [
                    {
                        "question": "What is the meaning of life?"
                    },
                    {
                        "question": "Who is the president of the United States?"
                    }
                ]
                """;

        // when
        List<QuestionModel> xx = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions")
                .then().extract().response()
                .as(new TypeRef<List<QuestionModel>>() {});

        // then
        System.out.println(xx);
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
            .withJavaOpts("""
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
            -Dhxi.client.baseUrl=%s
            -Dhxi.auth.providers.hyland-experience.token-uri=%s/token
            """.formatted(
                postgres.getUsername(),
                postgres.getPassword(),
                postgres.getNetworkAliases().stream().findFirst().get(),
                postgres.getDatabaseName(),
                activemq.getNetworkAliases().stream().findFirst().get(),
                hxInsightMock.getBaseUrl(),
                hxInsightMock.getBaseUrl())
            .replace("\n", " "));
        // @formatter:on
    }
}
