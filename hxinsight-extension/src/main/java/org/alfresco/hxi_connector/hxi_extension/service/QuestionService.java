/*-
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

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyland.sdk.cic.agent.AgentService;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.qna.QnaService;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;

@Slf4j
@RequiredArgsConstructor
public class QuestionService
{
    private final AgentService agentService;
    private final QnaService qnaService;
    private final AuthenticationService authenticationService;
    private final PersonService personService;
    private final ApplicationInfoProvider applicationInfoProvider;
    private final QuestionMapper questionMapper;

    public List<Agent> listAgents()
    {
        try
        {
            return agentService.integrations().listAgents(applicationInfoProvider.getSourceId()).stream()
                    .map(questionMapper::toAgent)
                    .collect(Collectors.toList());
        }
        catch (CICServiceException e)
        {
            throw new WebScriptException(e.statusCode(),
                    format("Request to hxi failed, expected status 200, received %s", e.statusCode()), e);
        }
        catch (CICSdkException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, "Failed to list agents", e);
        }
    }

    public String askQuestion(String agentId, Question question)
    {
        question.setUserId(getUserId());
        return submitQuestion(agentId, question);
    }

    public AnswerResponse getAnswer(String questionId)
    {
        try
        {
            Answer answer = qnaService.integrations().question(questionId).getAnswer(getUserId());
            return questionMapper.toAnswerResponse(answer);
        }
        catch (CICServiceException e)
        {
            throw new WebScriptException(e.statusCode(),
                    format("Request to hxi failed, expected status 200, received %s", e.statusCode()), e);
        }
        catch (CICSdkException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE,
                    format("Failed to get answer to question with id %s", questionId), e);
        }
    }

    public void submitFeedback(String questionId, org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType serviceFeedbackType)
    {
        try
        {
            FeedbackType feedbackType = FeedbackType.valueOf(serviceFeedbackType.name());
            qnaService.question(questionId).submitFeedback(feedbackType);
        }
        catch (CICServiceException e)
        {
            throw new WebScriptException(e.statusCode(),
                    format("Request to hxi failed, expected status 200, received %s", e.statusCode()), e);
        }
        catch (CICSdkException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE,
                    format("Failed to submit feedback for question with id %s", questionId), e);
        }
    }

    public String retryQuestion(String agentId, String questionId, String comments, Question question)
    {
        question.setUserId(getUserId());
        try
        {
            qnaService.question(questionId).submitFeedback(FeedbackType.RETRY);
            return submitQuestion(agentId, question);
        }
        catch (CICServiceException e)
        {
            throw new WebScriptException(e.statusCode(),
                    format("Request to hxi failed, expected status 200, received %s", e.statusCode()), e);
        }
        catch (CICSdkException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE,
                    format("Failed to retry question with id %s", questionId), e);
        }
    }

    private String submitQuestion(String agentId, Question question)
    {
        log.atDebug().log("Sending question to agent {}: {}", agentId, question.getQuestion());

        IntegrationSubmitQuestionRequest request = IntegrationSubmitQuestionRequest.builder()
                .question(question.getQuestion())
                .userId(question.getUserId())
                .contextObjectIds(question.getContextObjectIds() != null ? List.copyOf(question.getContextObjectIds()) : null)
                .build();
        try
        {
            return agentService.integrations().agent(agentId).submitQuestion(request).questionId();
        }
        catch (CICServiceException e)
        {
            throw new WebScriptException(e.statusCode(),
                    format("Request to hxi failed, expected status 202, received %s", e.statusCode()), e);
        }
        catch (CICSdkException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, "Failed to ask question", e);
        }
    }

    private String getUserId()
    {
        NodeRef currentUserNodeRef = personService.getPersonOrNull(authenticationService.getCurrentUserName());
        return ofNullable(currentUserNodeRef).map(NodeRef::getId).orElse(null);
    }

}
