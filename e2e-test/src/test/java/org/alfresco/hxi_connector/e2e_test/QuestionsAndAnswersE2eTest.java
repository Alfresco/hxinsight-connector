/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.concatJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getHxInsightRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class QuestionsAndAnswersE2eTest
{
    private static final String PREEXISTING_DOCUMENT_ID = "1a0b110f-1e09-4ca2-b367-fe25e4964a4e";
    private static final String QUESTIONS_URL = "/alfresco/api/-default-/private/hxi/versions/1/agents/agent-id/questions";
    private static final String SUBMIT_QUESTION_SCENARIO = "Submit-question";
    private static final String NEXT_QUESTION_STATE = "Next-question";

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

    private final RepositoryClient repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @AfterEach
    public void resetWiremock()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
    }

    @Test
    void shouldReturnQuestionId()
    {
        // given
        String questions = """
                {
                    "question": "What is the meaning of life?",
                    "restrictionQuery": {
                        "nodesIds": ["%s"]
                    }
                }
                """.formatted(PREEXISTING_DOCUMENT_ID);

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/agents/agent-id/questions")
                .then().extract().response();

        // then
        assertThat(response.statusCode()).isEqualTo(SC_OK);
        assertThat(response.jsonPath().<String> get("entry.questionId")).isEqualTo("5fca2c77-cdc0-4118-9373-e75f53177ff8");
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> loggedRequests = WireMock.findAll(postRequestedFor(urlPathTemplate("/agents/{agentId}/questions"))
                    .withPathParam("agentId", equalTo("agent-id"))
                    .withRequestBody(containing("userId")));
            assertThat(loggedRequests)
                    .hasSize(1)
                    .first()
                    .extracting(this::extractUserIdFromBody)
                    .extracting(this::getUsernameByNodeId)
                    .isEqualTo(ADMIN_USER.username());
        });
    }

    @Test
    void shouldReturn400IfAskedTooManyQuestions()
    {
        // given
        String questions = """
                [
                    {
                        "question": "What is the meaning of life?",
                        "restrictionQuery": {
                            "nodesIds": ["%s"]
                        }
                    },
                    {
                        "question": "Who is the president of the United States?",
                        "restrictionQuery": {
                            "nodesIds": ["%s"]
                        }
                    }
                ]
                """.formatted(PREEXISTING_DOCUMENT_ID, PREEXISTING_DOCUMENT_ID);

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
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
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    void shouldReturn400IfAskedAboutTooManyNodes()
    {
        // given
        String questions = """
                {
                    "question": "What is the meaning of life?",
                    "restrictionQuery": {
                        "nodesIds": ["node1", "node2", "node3", "node4", "node5", "node6", "node7", "node8", "node9", "node10", "node11"]
                    }
                }
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(questions)
                .when().post(repository.getBaseUrl() + QUESTIONS_URL)
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
                .when().get(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/answers/-default-".formatted(questionId))
                .then().extract().response();

        // then
        assertThat(response.statusCode()).isEqualTo(SC_OK);
        assertThat(response.jsonPath()).satisfies(jsonPath -> {
            assertThat(jsonPath.<String> get("entry.question")).isEqualTo("This is some question");
            assertThat(jsonPath.<String> get("entry.answer")).isEqualTo("This is the answer to the question");
            assertThat(jsonPath.<List<Map<String, Object>>> get("entry.objectReferences")).satisfies(refs -> {
                assertThat(refs).hasSize(1);
                Map<String, Object> firstRef = refs.get(0);
                assertThat(firstRef.get("objectId")).isEqualTo("276718b0-c3ab-4e11-81d5-96dbbb540269");
                List<Map<String, Object>> references = (List<Map<String, Object>>) firstRef.get("references");
                assertThat(references).hasSize(1);
                assertThat(references.get(0))
                        .containsEntry("referenceId", "276718b0-c3ab-4e11-81d5-96dbbb540269")
                        .containsKey("rankScore")
                        .containsKey("rank");
            });
        });

        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> loggedRequests = WireMock.findAll(getRequestedFor(urlPathTemplate("/questions/{questionId}/answer"))
                    .withPathParam("questionId", equalTo(questionId))
                    .withQueryParam("userId", matching("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
            assertThat(loggedRequests)
                    .hasSize(1)
                    .first()
                    .extracting(this::extractUserIdFromQueryParam)
                    .extracting(this::getUsernameByNodeId)
                    .isEqualTo(ADMIN_USER.username());
        });
    }

    @Test
    void shouldNotGetAnswerWhenHxIReturnsUnexpectedStatus()
    {
        // given
        String questionId = "non-existing-question-id";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .when().get(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/answers/-default-".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_NOT_FOUND, response.statusCode());
        assertTrue(((String) response.jsonPath().get("error.briefSummary")).contains("Request to hxi failed, expected status 200, received 404"));
    }

    @Test
    void shouldSubmitFeedback()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";
        String feedback = """
                [
                    {
                        "feedbackType": "LIKE",
                        "comments": "The response was very helpful and detailed. Good bot."
                    }
                ]
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(feedback)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/feedback".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_CREATED, response.statusCode());
        assertEquals("LIKE", response.jsonPath().get("entry.feedbackType"));
        assertEquals("The response was very helpful and detailed. Good bot.", response.jsonPath().get("entry.comments"));
        RetryUtils.retryWithBackoff(() -> WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/questions/%s/answer/feedback".formatted(questionId)))
                .withRequestBody(containing("GOOD"))));
    }

    @Test
    void shouldNotSubmitFeedbackWhenHxIReturnsUnexpectedStatus()
    {
        // given
        String questionId = "non-existing-question-id";
        String feedback = """
                [
                    {
                        "feedbackType": "LIKE",
                        "comments": "The response was very helpful and detailed. Good bot."
                    }
                ]
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(feedback)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/feedback".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_NOT_FOUND, response.statusCode());
        assertTrue(((String) response.jsonPath().get("error.briefSummary")).contains("Request to hxi failed, expected status 200, received 404"));
    }

    @Test
    void shouldReturn400IfNoFeedbackSubmitted()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";
        String feedback = "[]";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(feedback)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/feedback".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    void shouldReturn400IfSubmittedTooMuchFeedback()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";
        String feedback = """
                [
                    {
                        "feedbackType": "LIKE",
                        "comments": "The response was very helpful and detailed. Good bot."
                    },
                    {
                        "feedbackType": "LIKE",
                        "comments": "The response was not bad."
                    }
                ]
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(feedback)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/questions/%s/feedback".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
    }

    @Test
    void shouldRetryQuestion()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";
        String retry = """
                {
                    "comments": "I need more details about the answer.",
                    "originalQuestion": {
                        "question": "What is the meaning of life?",
                        "restrictionQuery": {
                            "nodesIds": ["%s"]
                        }
                    }
                }
                """.formatted(PREEXISTING_DOCUMENT_ID);
        WireMock.setScenarioState(SUBMIT_QUESTION_SCENARIO, NEXT_QUESTION_STATE);

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(retry)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/agents/agent-id/questions/%s/retry".formatted(questionId))
                .then().extract().response();

        // then
        assertThat(response.statusCode()).isEqualTo(SC_CREATED);
        assertThat(response.jsonPath()).satisfies(jsonPath -> {
            assertThat(jsonPath.<String> get("entry.questionId")).isEqualTo("a1eae985-6984-4346-9e08-d430fa8404b2");
            assertThat(jsonPath.<String> get("entry.comments")).isEqualTo("I need more details about the answer.");
        });
        RetryUtils.retryWithBackoff(() -> {
            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/questions/%s/answer/feedback".formatted(questionId)))
                    .withRequestBody(containing("RETRY")));
            List<LoggedRequest> loggedRequests = WireMock.findAll(postRequestedFor(urlPathTemplate("/agents/{agentId}/questions"))
                    .withPathParam("agentId", equalTo("agent-id"))
                    .withRequestBody(containing("userId")));
            assertThat(loggedRequests)
                    .hasSize(1)
                    .first()
                    .extracting(this::extractUserIdFromBody)
                    .extracting(this::getUsernameByNodeId)
                    .isEqualTo(ADMIN_USER.username());
        });
    }

    @Test
    void shouldReturn400IfRetriedQuestionWithTooManyNodes()
    {
        // given
        String questionId = "5fca2c77-cdc0-4118-9373-e75f53177ff8";
        String retry = """
                {
                    "comments": "I need more details about the answer.",
                    "originalQuestion": {
                        "question": "What is the meaning of life?",
                        "restrictionQuery": {
                            "nodesIds": ["node1", "node2", "node3", "node4", "node5", "node6", "node7", "node8", "node9", "node10", "node11"]
                        }
                    }
                }
                """;

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .body(retry)
                .when().post(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/agents/agent-id/questions/%s/retry".formatted(questionId))
                .then().extract().response();

        // then
        assertEquals(SC_BAD_REQUEST, response.statusCode());
        WireMock.verify(exactly(0), postRequestedFor(urlEqualTo("/questions/%s/answer/feedback".formatted(questionId))));
        WireMock.verify(exactly(0), postRequestedFor(urlEqualTo("/agents/agent-id/questions")));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        String javaOpts = concatJavaOpts(getMinimalRepoJavaOpts(postgres, activemq), getHxInsightRepoJavaOpts(hxInsightMock));

        return DockerContainers.createExtendedRepositoryContainerWithin(network)
                .withJavaOpts(javaOpts);
    }

    @SneakyThrows
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String extractUserIdFromBody(LoggedRequest request)
    {
        return new ObjectMapper().readTree(request.getBodyAsString()).get("userId").asText();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String extractUserIdFromQueryParam(LoggedRequest request)
    {
        return request.queryParameter("userId").firstValue();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private String getUsernameByNodeId(String nodeId)
    {
        return repositoryClient.getNode(nodeId)
                .properties()
                .get("cm:userName")
                .toString();
    }
}
