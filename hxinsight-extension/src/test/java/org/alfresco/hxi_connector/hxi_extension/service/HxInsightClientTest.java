/*
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

package org.alfresco.hxi_connector.hxi_extension.service;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType.GOOD;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.hxi_connector.hxi_extension.service.model.Feedback;
import org.alfresco.hxi_connector.hxi_extension.service.model.ObjectReference;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;

@SuppressWarnings("PMD.FieldNamingConventions")
class HxInsightClientTest
{
    private static final String AGENT_ID = "agent-id";
    private static final Set<ObjectReference> OBJECT_REFERENCES = Set.of(new ObjectReference("dummy-node-id"));
    private static final String USER_AGENT_HEADER = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";
    private static final String SOURCE_ID = "alfresco-dummy-source-id-0a63de491876";
    private final HxInsightClientConfig config = new HxInsightClientConfig("http://hxinsight/integrations");
    private final AuthService authService = mock(AuthService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = mock(HttpClient.class);
    private final ApplicationInfoProvider applicationInfoProvider = mock(ApplicationInfoProvider.class);
    private final HxInsightClient hxInsightClient = new HxInsightClient(
            config,
            authService,
            objectMapper,
            httpClient,
            applicationInfoProvider);

    private ArgumentCaptor<HttpRequest> requestCaptor;

    @BeforeEach
    void setUp()
    {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        given(authService.getHxpAuthHeaders()).willReturn(Map.of("Authorization", "Bearer token"));
        given(applicationInfoProvider.getUserAgentData()).willReturn(USER_AGENT_HEADER);
        given(applicationInfoProvider.getSourceId()).willReturn(SOURCE_ID);
        requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    }

    @Test
    @SneakyThrows
    void shouldReturnQuestionId()
    {
        // given
        String expectedQuestionId = "bd4cfeff-f197-40b6-b205-9067cad3fca7";
        String responseBody = """
                {
                    "questionId": "%s"
                }
                """.formatted(expectedQuestionId);

        HttpResponse response = mock(HttpResponse.class);

        given(response.statusCode()).willReturn(202);
        given(response.body()).willReturn(responseBody);

        given(httpClient.send(any(), any())).willReturn(response);

        // when
        String actualQuestionId = hxInsightClient.askQuestion(AGENT_ID,
                new Question("Who won last year's Super Bowl?", OBJECT_REFERENCES));

        // then
        assertEquals(expectedQuestionId, actualQuestionId);
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldPassStatusCodeFromHxi_Question()
    {
        // given
        int expectedStatusCode = 418;

        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(expectedStatusCode);

        given(httpClient.send(any(), any())).willReturn(response);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, () -> hxInsightClient.askQuestion(
                AGENT_ID, new Question("Who won last year's Super Bowl?", OBJECT_REFERENCES)));
        assertEquals(expectedStatusCode, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldSet503StatusCodeOnCheckedException_Question()
    {
        // given
        given(httpClient.send(any(), any())).willThrow(IOException.class);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, () -> hxInsightClient.askQuestion(
                AGENT_ID, new Question("Who won last year's Super Bowl?", OBJECT_REFERENCES)));
        assertEquals(SC_SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @SneakyThrows
    @Test
    void shouldReturnAnswer()
    {
        // given
        String questionId = "dummy-id-1234";
        String answer = "The Kansas City Chiefs won last year's Super Bowl.";
        AnswerResponse expectedAnswerResponse = new AnswerResponse(questionId, "", "", "", answer, null);
        JSONObject responseBody = new JSONObject(expectedAnswerResponse);
        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(SC_OK);
        given(response.body()).willReturn(responseBody.toString());
        given(httpClient.send(any(), any())).willReturn(response);

        // when
        AnswerResponse answerResponse = hxInsightClient.getAnswer(questionId);

        // then
        assertEquals(answer, answerResponse.getAnswer());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldThrowOnNotExpectedStatusCodeWhenGettingAnswer()
    {
        // given
        String questionId = "dummy-id-1234";
        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(SC_BAD_REQUEST);
        given(httpClient.send(any(), any())).willReturn(response);

        // when + then
        assertThrows(WebScriptException.class, () -> hxInsightClient.getAnswer(questionId));
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldSet503StatusCodeOnIOException_Answer()
    {
        // given
        given(httpClient.send(any(), any())).willThrow(IOException.class);

        // when
        WebScriptException exception = assertThrows(WebScriptException.class, () -> hxInsightClient.getAnswer("dummy-id-1234"));

        // then
        assertEquals(SC_SERVICE_UNAVAILABLE, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldReturnAgents()
    {
        // given
        String responseBody = """
                [
                    {
                        "id": "1a3f4b3d-4b3d-4b3d-4b3d-4b3d4b3d4b3d",
                        "name": "Security Advisor",
                        "description": "I can help you to secure your systems."
                    },
                    {
                        "id": "1a3f1a3f-1a3f-1a3f-1a3f-1a3f1a3f1a3f",
                        "name": "Tax Advisor",
                        "description": "I can help you with your taxes and financial planning."
                    }
                ]
                """;
        List<Agent> expectedAgents = List.of(
                new Agent("1a3f4b3d-4b3d-4b3d-4b3d-4b3d4b3d4b3d", "Security Advisor", "I can help you to secure your systems."),
                new Agent("1a3f1a3f-1a3f-1a3f-1a3f-1a3f1a3f1a3f", "Tax Advisor", "I can help you with your taxes and financial planning."));

        HttpResponse response = mock(HttpResponse.class);

        given(response.statusCode()).willReturn(SC_OK);
        given(response.body()).willReturn(responseBody);

        given(httpClient.send(argThat(req -> req.uri().toString().equals("http://hxinsight/integrations/agents?sourceId=" + SOURCE_ID)), any())).willReturn(response);

        // when
        List<Agent> actualAgents = hxInsightClient.getAgents();

        // then
        assertEquals(expectedAgents, actualAgents);
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldPassStatusCodeFromHxi_Agents()
    {
        // given
        int expectedStatusCode = 418;

        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(expectedStatusCode);

        given(httpClient.send(any(), any())).willReturn(response);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, hxInsightClient::getAgents);
        assertEquals(expectedStatusCode, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldSet503StatusCodeOnCheckedException_Agents()
    {
        // given
        given(httpClient.send(any(), any())).willThrow(IOException.class);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, hxInsightClient::getAgents);
        assertEquals(SC_SERVICE_UNAVAILABLE, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void canSubmitFeedbackWithoutException()
    {
        // given
        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(200);

        given(httpClient.send(any(), any())).willReturn(response);

        // when
        hxInsightClient.submitFeedback("dummy-id-1234", new Feedback(GOOD, "This answer was amazing"));

        // then
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals("http://hxinsight/integrations/questions/dummy-id-1234/answer/feedback", requestCaptor.getValue().uri().toString());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldPassStatusCodeFromHxi_Feedback()
    {
        // given
        int expectedStatusCode = 418;

        HttpResponse response = mock(HttpResponse.class);
        given(response.statusCode()).willReturn(expectedStatusCode);

        given(httpClient.send(any(), any())).willReturn(response);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, () -> hxInsightClient.submitFeedback(
                "dummy-id-1234",
                new Feedback(GOOD, "This answer was amazing")));
        assertEquals(expectedStatusCode, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void shouldSet503StatusCodeOnCheckedException_Feedback()
    {
        // given
        given(httpClient.send(any(), any())).willThrow(IOException.class);

        // when, then
        WebScriptException exception = assertThrows(WebScriptException.class, () -> hxInsightClient.submitFeedback(
                "dummy-id-1234",
                new Feedback(GOOD, "This answer was amazing")));
        assertEquals(SC_SERVICE_UNAVAILABLE, exception.getStatus());
        then(httpClient).should().send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }

    @Test
    @SneakyThrows
    void canRetryQuestion()
    {
        // given
        String questionId = "dummy-id-1234";

        HttpResponse feedbackResponse = mock(HttpResponse.class);
        given(feedbackResponse.statusCode()).willReturn(SC_OK);
        ArgumentMatcher<? extends HttpRequest> feedbackMatcher = request -> request != null && request.uri().toString().equals("http://hxinsight/integrations/questions/dummy-id-1234/answer/feedback");
        given(httpClient.send(ArgumentMatchers.argThat(feedbackMatcher), any())).willReturn(feedbackResponse);

        HttpResponse questionResponse = mock(HttpResponse.class);
        given(questionResponse.statusCode()).willReturn(SC_ACCEPTED);
        given(questionResponse.body()).willReturn("""
                {
                    "questionId": "dummy-id-5678"
                }
                """);
        ArgumentMatcher<? extends HttpRequest> questionMatcher = request -> request != null && request.uri().toString().equals("http://hxinsight/integrations/agents/agent-id/questions");
        given(httpClient.send(ArgumentMatchers.argThat(questionMatcher), any())).willReturn(questionResponse);

        // when
        Question question = new Question("Create a sonnet about the Super Bowl", OBJECT_REFERENCES);
        String newQuestionId = hxInsightClient.retryQuestion(AGENT_ID, questionId, "The fourth line was not quite in iambic pentameter", question);

        // then
        assertEquals("dummy-id-5678", newQuestionId);
        then(httpClient).should(atLeast(1)).send(requestCaptor.capture(), any());
        assertEquals(USER_AGENT_HEADER, requestCaptor.getValue().headers().map().get(USER_AGENT).get(0));
    }
}
