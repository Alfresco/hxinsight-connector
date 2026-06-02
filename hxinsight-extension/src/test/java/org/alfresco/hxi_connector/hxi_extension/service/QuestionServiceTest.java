/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.hyland.sdk.cic.agent.AgentService;
import org.hyland.sdk.cic.agent.IntegrationAgentService;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.qna.IntegrationQnaService;
import org.hyland.sdk.cic.qna.QnaService;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;

@SuppressWarnings("PMD.FieldNamingConventions")
class QuestionServiceTest
{
    private static final String USER_NAME = "admin";
    private static final String USER_NODE_ID = "user-node-id";
    private static final String AGENT_ID = "agent-id";
    private static final String SOURCE_ID = "source-id";
    private static final String OBJECT_ID = "dummy-node-id";
    private static final Set<String> OBJECT_IDS = Set.of(OBJECT_ID);

    private final AgentService agentService = mock(AgentService.class);
    private final QnaService qnaService = mock(QnaService.class);
    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final PersonService personService = mock(PersonService.class);
    private final ApplicationInfoProvider applicationInfoProvider = mock(ApplicationInfoProvider.class);
    private final AgentAnswerMapper agentAnswerMapper = new AgentAnswerMapperImpl();

    private final IntegrationAgentService integrationAgentService = mock(IntegrationAgentService.class);
    private final IntegrationAgentService.IntegrationAgentResource integrationAgentResource = mock(IntegrationAgentService.IntegrationAgentResource.class);
    private final IntegrationQnaService integrationQnaService = mock(IntegrationQnaService.class);
    private final IntegrationQnaService.IntegrationQuestionResource integrationQuestionResource = mock(IntegrationQnaService.IntegrationQuestionResource.class);
    private final QnaService.QuestionResource questionResource = mock(QnaService.QuestionResource.class);

    private final QuestionService questionService = new QuestionService(
            agentService, qnaService, authenticationService, personService, applicationInfoProvider, agentAnswerMapper);

    @BeforeEach
    void setUp()
    {
        given(authenticationService.getCurrentUserName()).willReturn(USER_NAME);
        given(personService.getPersonOrNull(USER_NAME))
                .willReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, USER_NODE_ID));
        given(applicationInfoProvider.getSourceId()).willReturn(SOURCE_ID);
        given(agentService.integrations()).willReturn(integrationAgentService);
        given(integrationAgentService.agent(AGENT_ID)).willReturn(integrationAgentResource);
        given(qnaService.integrations()).willReturn(integrationQnaService);
    }

    @Test
    void shouldThrowOnSdkException_ListAgents()
    {
        // given
        given(integrationAgentService.listAgents(SOURCE_ID)).willThrow(new CICSdkException("Server error"));

        // when, then
        assertThrows(WebScriptException.class, questionService::listAgents);
    }

    @Test
    void shouldPreserveStatusCodeOnServiceException_ListAgents()
    {
        // given
        given(integrationAgentService.listAgents(SOURCE_ID)).willThrow(new CICServiceException("Not found", 404));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class, questionService::listAgents);
        assertEquals(404, ex.getStatus());
    }

    @Test
    void shouldReturnQuestionId()
    {
        // given
        String expectedQuestionId = "bd4cfeff-f197-40b6-b205-9067cad3fca7";
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willReturn(new QuestionResponse(expectedQuestionId));

        Question question = new Question("Who won last year's Super Bowl?", OBJECT_IDS);

        // when
        String actualQuestionId = questionService.askQuestion(AGENT_ID, question);

        // then
        assertEquals(expectedQuestionId, actualQuestionId);
    }

    @Test
    void shouldSendExpectedQuestionToSdk()
    {
        // given
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willReturn(new QuestionResponse("some-question-id"));

        Question question = new Question("What is the meaning of life?", OBJECT_IDS);

        // when
        questionService.askQuestion(AGENT_ID, question);

        // then
        ArgumentCaptor<IntegrationSubmitQuestionRequest> captor = ArgumentCaptor.forClass(IntegrationSubmitQuestionRequest.class);
        then(integrationAgentResource).should().submitQuestion(captor.capture());
        IntegrationSubmitQuestionRequest sentRequest = captor.getValue();
        assertEquals("What is the meaning of life?", sentRequest.question());
        assertEquals(USER_NODE_ID, sentRequest.userId());
        assertEquals(List.of(OBJECT_ID), sentRequest.contextObjectIds());
    }

    @Test
    void shouldThrowOnSdkException_Question()
    {
        // given
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willThrow(new CICSdkException("Server error"));

        Question question = new Question("Who won last year's Super Bowl?", OBJECT_IDS);

        // when, then
        assertThrows(WebScriptException.class, () -> questionService.askQuestion(AGENT_ID, question));
    }

    @Test
    void shouldPreserveStatusCodeOnServiceException_Question()
    {
        // given
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willThrow(new CICServiceException("Not found", 404));

        Question question = new Question("Who won last year's Super Bowl?", OBJECT_IDS);

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.askQuestion(AGENT_ID, question));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void shouldReturnAnswer()
    {
        // given
        String questionText = "Who won last year's Super Bowl?";
        String answerText = "The Kansas City Chiefs won last year's Super Bowl.";
        String questionId = "dummy-id-1234";

        Answer sdkAnswer = new Answer(answerText, AGENT_ID, 1, ResponseCompleteness.COMPLETE,
                List.of(), List.of(), questionText, null, null, null, null);

        given(integrationQnaService.question(questionId)).willReturn(integrationQuestionResource);
        given(integrationQuestionResource.getAnswer(USER_NODE_ID)).willReturn(sdkAnswer);

        // when
        AnswerResponse answerResponse = questionService.getAnswer(questionId);

        // then
        assertEquals(answerText, answerResponse.getAnswer());
        assertEquals(questionText, answerResponse.getQuestion());
        assertEquals("Complete", answerResponse.getResponseCompleteness());
        assertEquals(AGENT_ID, answerResponse.getAgentId());
        assertEquals("1", answerResponse.getAgentVersion());
    }

    @Test
    void shouldPreserveStatusCodeOnServiceException_Answer()
    {
        // given
        String questionId = "dummy-id-1234";
        given(integrationQnaService.question(questionId)).willReturn(integrationQuestionResource);
        given(integrationQuestionResource.getAnswer(USER_NODE_ID))
                .willThrow(new CICServiceException("Not found", 404));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.getAnswer(questionId));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void shouldThrowOnSdkException_Answer()
    {
        // given
        String questionId = "dummy-id-1234";
        given(integrationQnaService.question(questionId)).willReturn(integrationQuestionResource);
        given(integrationQuestionResource.getAnswer(USER_NODE_ID))
                .willThrow(new CICSdkException("Server error"));

        // when, then
        assertThrows(WebScriptException.class, () -> questionService.getAnswer(questionId));
    }

    @Test
    void canSubmitFeedback()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);

        // when
        questionService.submitFeedback(questionId, FeedbackType.GOOD);

        // then
        then(questionResource).should().submitFeedback(org.hyland.sdk.cic.qna.object.FeedbackType.GOOD);
    }

    @Test
    void shouldPreserveStatusCodeOnServiceException_Feedback()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        org.mockito.BDDMockito.willThrow(new CICServiceException("Not found", 404))
                .given(questionResource).submitFeedback(any(org.hyland.sdk.cic.qna.object.FeedbackType.class));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.submitFeedback(questionId, FeedbackType.GOOD));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void canRetryQuestion()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willReturn(new QuestionResponse("dummy-id-5678"));

        Question question = new Question("Create a sonnet about the Super Bowl", OBJECT_IDS);

        // when
        String newQuestionId = questionService.retryQuestion(AGENT_ID, questionId, question);

        // then
        assertEquals("dummy-id-5678", newQuestionId);
        then(questionResource).should().submitFeedback(org.hyland.sdk.cic.qna.object.FeedbackType.RETRY);
        then(integrationAgentResource).should().submitQuestion(any(IntegrationSubmitQuestionRequest.class));
    }

    @Test
    void shouldReturnAgentList()
    {
        // given
        AgentSummary summary = new AgentSummary("agent-1", "My Agent", "Desc", "gpt-4", null, "https://avatar.url",
                null, List.of(SOURCE_ID), List.of(), 1, true, null, null, null, null);
        given(integrationAgentService.listAgents(SOURCE_ID)).willReturn(List.of(summary));

        // when
        var agents = questionService.listAgents();

        // then
        assertEquals(1, agents.size());
        assertEquals("agent-1", agents.get(0).getId());
        assertEquals("My Agent", agents.get(0).getName());
        assertEquals("Desc", agents.get(0).getDescription());
        assertEquals("https://avatar.url", agents.get(0).getAvatarUrl());
    }

    @Test
    void shouldReturnEmptyAgentList()
    {
        // given
        given(integrationAgentService.listAgents(SOURCE_ID)).willReturn(List.of());

        // when
        var agents = questionService.listAgents();

        // then
        assertEquals(0, agents.size());
    }

    @Test
    void shouldReturnServiceUnavailableOnSdkException_ListAgents()
    {
        // given
        given(integrationAgentService.listAgents(SOURCE_ID)).willThrow(new CICSdkException("timeout"));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class, questionService::listAgents);
        assertEquals(SC_SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void shouldSendEmptyContextObjectIdsWhenQuestionHasNone()
    {
        // given
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willReturn(new QuestionResponse("some-id"));

        Question question = new Question("What is 2+2?", null);

        // when
        questionService.askQuestion(AGENT_ID, question);

        // then
        ArgumentCaptor<IntegrationSubmitQuestionRequest> captor = ArgumentCaptor.forClass(IntegrationSubmitQuestionRequest.class);
        then(integrationAgentResource).should().submitQuestion(captor.capture());
        assertEquals(List.of(), captor.getValue().contextObjectIds());
    }

    @Test
    void shouldThrowWhenPersonNotFound()
    {
        // given - SDK requires userId != null; if the person lookup returns null the service throws
        given(personService.getPersonOrNull(USER_NAME)).willReturn(null);

        Question question = new Question("What is 2+2?", OBJECT_IDS);

        // when, then
        assertThrows(NullPointerException.class,
                () -> questionService.askQuestion(AGENT_ID, question));
    }

    @Test
    void shouldReturnServiceUnavailableOnSdkException_AskQuestion()
    {
        // given
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willThrow(new CICSdkException("timeout"));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.askQuestion(AGENT_ID, new Question("Any?", OBJECT_IDS)));
        assertEquals(SC_SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void shouldReturnServiceUnavailableOnSdkException_GetAnswer()
    {
        // given
        String questionId = "dummy-id-1234";
        given(integrationQnaService.question(questionId)).willReturn(integrationQuestionResource);
        given(integrationQuestionResource.getAnswer(USER_NODE_ID)).willThrow(new CICSdkException("timeout"));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.getAnswer(questionId));
        assertEquals(SC_SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void shouldSubmitBadFeedback()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);

        // when
        questionService.submitFeedback(questionId, FeedbackType.BAD);

        // then
        then(questionResource).should().submitFeedback(org.hyland.sdk.cic.qna.object.FeedbackType.BAD);
    }

    @Test
    void shouldReturnServiceUnavailableOnSdkException_Feedback()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        willThrow(new CICSdkException("timeout"))
                .given(questionResource).submitFeedback(any(org.hyland.sdk.cic.qna.object.FeedbackType.class));

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.submitFeedback(questionId, FeedbackType.GOOD));
        assertEquals(SC_SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void shouldPreserveStatusCodeOnServiceException_RetryQuestion()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        willThrow(new CICServiceException("Not found", 404))
                .given(questionResource).submitFeedback(org.hyland.sdk.cic.qna.object.FeedbackType.RETRY);

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.retryQuestion(AGENT_ID, questionId,
                        new Question("Redo this", OBJECT_IDS)));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void shouldReturnServiceUnavailableOnSdkException_RetryQuestion()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        willThrow(new CICSdkException("timeout"))
                .given(questionResource).submitFeedback(org.hyland.sdk.cic.qna.object.FeedbackType.RETRY);

        // when, then
        WebScriptException ex = assertThrows(WebScriptException.class,
                () -> questionService.retryQuestion(AGENT_ID, questionId,
                        new Question("Redo this", OBJECT_IDS)));
        assertEquals(SC_SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void shouldPropagateUserIdInRetryQuestion()
    {
        // given
        String questionId = "dummy-id-1234";
        given(qnaService.question(questionId)).willReturn(questionResource);
        given(integrationAgentResource.submitQuestion(any(IntegrationSubmitQuestionRequest.class)))
                .willReturn(new QuestionResponse("new-id"));

        Question question = new Question("Redo this", OBJECT_IDS);

        // when
        questionService.retryQuestion(AGENT_ID, questionId, question);

        // then
        ArgumentCaptor<IntegrationSubmitQuestionRequest> captor = ArgumentCaptor.forClass(IntegrationSubmitQuestionRequest.class);
        then(integrationAgentResource).should().submitQuestion(captor.capture());
        assertEquals(USER_NODE_ID, captor.getValue().userId());
    }
}
