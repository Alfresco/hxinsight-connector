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
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.concatJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getHxInsightRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;

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

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class QuestionsAndAnswersE2eTest
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
    void shouldReturnQuestionId()
    {
        // given
        String questions = """
                {
                    "question": "What is the meaning of life?"
                }
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions")
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
        assertEquals("5fca2c77-cdc0-4118-9373-e75f53177ff8", response.jsonPath().get("entry.questionId"));
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
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions")
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
        assertTrue(response.body().asString().contains("You can only ask one question at a time."));
    }

    @Test
    void shouldReturn400IfNoQuestionsAsked()
    {
        // given
        String questions = "[]";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions")
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    void shouldGetAnswer()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .when().get(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/answers".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
        assertEquals(questionId, response.jsonPath().get("list.entries.entry[0].questionId"));
        assertEquals("This is the answer to the question", response.jsonPath().get("list.entries.entry[0].answer"));
        assertEquals("276718b0-c3ab-4e11-81d5-96dbbb540269", response.jsonPath().get("list.entries.entry[0].references[0].referenceId"));
    }

    @Test
    void shouldNotGetAnswerWheHxIReturnsUnexpectedStatus()
    {
        // given
        String questionId = "non-existing-question-id";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .when().get(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/answers".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_INTERNAL_SERVER_ERROR, response.statusCode());
        assertEquals("Unexpected response status code - expecting: 200, received: 404", response.jsonPath().get("error.briefSummary"));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
            .withJavaOpts(concatJavaOpts(getMinimalRepoJavaOpts(postgres, activemq),
                        (getHxInsightRepoJavaOpts(hxInsightMock)))
                );
        // @formatter:on
    }
}
