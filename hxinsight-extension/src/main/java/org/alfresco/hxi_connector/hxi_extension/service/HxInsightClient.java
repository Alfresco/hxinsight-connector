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

import static java.lang.String.format;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.CONTENT_TYPE;
import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType.RETRY;
import static org.alfresco.hxi_connector.hxi_extension.service.util.HttpUtils.ensureCorrectHttpStatusReturned;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.hxi_connector.hxi_extension.service.model.Feedback;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.hxi_connector.hxi_extension.service.model.QuestionResponse;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.util.TempFileProvider;

@Slf4j
@RequiredArgsConstructor
public class HxInsightClient
{
    private final HxInsightClientConfig config;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final HttpClient client;
    private final ApplicationInfoProvider applicationInfoProvider;

    public List<Agent> getAgents()
    {
        try
        {
            String url = String.format("%s?source=%s", config.getAgentUrl(), applicationInfoProvider.getSourceId());

            HttpRequest request = requestWithRequiredHeaders()
                    .uri(URI.create(url))
                    .header(CONTENT_TYPE, "application/json")
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

            ensureCorrectHttpStatusReturned(SC_OK, httpResponse);

            return objectMapper.readValue(httpResponse.body(), new TypeReference<>() {});
        }
        catch (IOException | InterruptedException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, "Failed to ask question", e);
        }
    }

    public String askQuestion(Question question)
    {
        try
        {
            String body = objectMapper.writeValueAsString(question);

            HttpRequest request = requestWithRequiredHeaders()
                    .uri(URI.create(config.getQuestionUrl()))
                    .header(CONTENT_TYPE, "application/json")
                    .POST(BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

            ensureCorrectHttpStatusReturned(SC_ACCEPTED, httpResponse);

            return objectMapper.readValue(httpResponse.body(), QuestionResponse.class)
                    .getQuestionId();
        }
        catch (IOException | InterruptedException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, "Failed to ask question", e);
        }
    }

    public AnswerResponse getAnswer(String questionId)
    {
        try
        {
            HttpRequest request = requestWithRequiredHeaders()
                    .uri(URI.create(format(config.getAnswerUrl(), questionId)))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());
            log.atDebug().log("Question with id {} received a following answer {}", questionId, httpResponse.body());

            ensureCorrectHttpStatusReturned(SC_OK, httpResponse);

            return objectMapper.readValue(httpResponse.body(), AnswerResponse.class);
        }
        catch (IOException | InterruptedException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, format("Failed to get answer to question with id %s", questionId), e);
        }
    }

    public void submitFeedback(String questionId, Feedback feedback)
    {
        try
        {
            String body = objectMapper.writeValueAsString(feedback);

            HttpRequest request = requestWithRequiredHeaders()
                    .uri(URI.create(format(config.getFeedbackUrl(), questionId)))
                    .header(CONTENT_TYPE, "application/json")
                    .POST(BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

            ensureCorrectHttpStatusReturned(SC_OK, httpResponse);
        }
        catch (IOException | InterruptedException e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, format("Failed to submit feedback for question with id %s", questionId), e);
        }
    }

    public String retryQuestion(String questionId, String comments, Question question)
    {
        submitFeedback(questionId, Feedback.builder()
                .feedbackType(RETRY)
                .comments(comments)
                .build());
        return askQuestion(question);
    }

    public BinaryResource getAvatar(String agentId)
    {
        try
        {
            HttpRequest request = requestWithRequiredHeaders()
                    .uri(URI.create(format(config.getAvatarUrl(), agentId)))
                    .GET()
                    .build();

            HttpResponse<InputStream> httpResponse = client.send(request, BodyHandlers.ofInputStream());

            ensureCorrectHttpStatusReturned(SC_OK, httpResponse);
            log.atDebug().log("Successfully retrieved avatar for Agent with id: {}", agentId);

            String filePrefix = format("avatar-%s-%s", agentId, UUID.randomUUID());
            File tempImageFile = TempFileProvider.createTempFile(httpResponse.body(), filePrefix, "png");
            return new FileBinaryResource(tempImageFile);
        }
        catch (Exception e)
        {
            throw new WebScriptException(SC_SERVICE_UNAVAILABLE, format("Failed to get avatar for agent with id %s", agentId), e);
        }
    }

    private HttpRequest.Builder requestWithRequiredHeaders()
    {
        return HttpRequest.newBuilder()
                .header(USER_AGENT, applicationInfoProvider.getUserAgentData())
                .headers(getAuthHeaders());
    }

    private String[] getAuthHeaders()
    {
        return authService.getHxpAuthHeaders().entrySet().stream()
                .flatMap(header -> Stream.of(header.getKey(), header.getValue()))
                .toArray(String[]::new);
    }
}
