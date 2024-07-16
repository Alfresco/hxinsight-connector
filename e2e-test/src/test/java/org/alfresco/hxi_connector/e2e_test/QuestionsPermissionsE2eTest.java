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
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.concatJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getHxInsightRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;
import static org.alfresco.hxi_connector.e2e_test.util.client.model.Visibility.PRIVATE;
import static org.alfresco.hxi_connector.e2e_test.util.client.model.Visibility.PUBLIC;

import java.io.ByteArrayInputStream;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.response.Response;
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
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class QuestionsPermissionsE2eTest
{
    private static final String QUESTIONS_URL = "/alfresco/api/-default-/private/hxi/versions/1/questions";
    private static final String SAMPLE_QUESTION = """
            {
                "question": "What is the meaning of life?",
                "agentId": "agent-id",
                "restrictionQuery": {
                    "nodesIds": ["%s"]
                }
            }
            """;
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
    private static final User regularUser = new User("test", "test");
    private static String publicDocumentId;
    private static String privateDocumentId;

    @BeforeAll
    public static void beforeAll()
    {
        RepositoryClient repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());

        repositoryClient.createUser(regularUser);

        String publicSiteId = repositoryClient.createSite("Test Public Project", PUBLIC);
        String publicSiteRootDirectory = repositoryClient.getSiteDocumentLibraryId(publicSiteId);
        publicDocumentId = repositoryClient.createNodeWithContent(
                publicSiteRootDirectory,
                "test public file",
                new ByteArrayInputStream("test file content".getBytes()),
                "text/plain").id();

        String privateSiteId = repositoryClient.createSite("Test Private Project", PRIVATE);
        String privateSiteRootDirectory = repositoryClient.getSiteDocumentLibraryId(privateSiteId);
        privateDocumentId = repositoryClient.createNodeWithContent(
                privateSiteRootDirectory,
                "test private file",
                new ByteArrayInputStream("test file content".getBytes()),
                "text/plain").id();
    }

    @Test
    void adminShouldBeAbleToAskQuestionAboutPublicDocument()
    {
        // when
        Response response = given().auth().preemptive().basic(ADMIN_USER.username(), ADMIN_USER.password())
                .contentType("application/json")
                .body(SAMPLE_QUESTION.formatted(publicDocumentId))
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
    }

    @Test
    void adminShouldBeAbleToAskQuestionAboutPrivateDocument()
    {
        // given

        // when
        Response response = given().auth().preemptive().basic(ADMIN_USER.username(), ADMIN_USER.password())
                .contentType("application/json")
                .body(SAMPLE_QUESTION.formatted(privateDocumentId))
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
    }

    @Test
    void regularUserShouldBeAbleToAskQuestionAboutPublicDocument()
    {
        // when
        Response response = given().auth().preemptive().basic(regularUser.username(), regularUser.password())
                .contentType("application/json")
                .body(SAMPLE_QUESTION.formatted(publicDocumentId))
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
    }

    @Test
    void regularUserShouldNotBeAbleToAskQuestionAboutPrivateDocument()
    {
        // when
        Response response = given().auth().preemptive().basic(regularUser.username(), regularUser.password())
                .contentType("application/json")
                .body(SAMPLE_QUESTION.formatted(privateDocumentId))
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
                .then().extract().response();

        // then
        assertEquals(SC_FORBIDDEN, response.statusCode());
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        String javaOpts = concatJavaOpts(getMinimalRepoJavaOpts(postgres, activemq), getHxInsightRepoJavaOpts(hxInsightMock));

        return DockerContainers.createExtendedRepositoryContainerWithin(network)
                .withJavaOpts(javaOpts);
    }
}
