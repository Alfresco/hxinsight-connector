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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.hxi_connector.hxi_extension.service.model.QuestionResponse;
import org.alfresco.hxi_connector.hxi_extension.service.util.AuthService;
import org.springframework.extensions.webscripts.WebScriptException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;
import static org.alfresco.hxi_connector.common.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode;
import static org.apache.http.HttpStatus.SC_OK;

@Slf4j
@RequiredArgsConstructor
public class HxInsightClient
{
    private static final int EXPECTED_STATUS_CODE = 202;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private final HxInsightClientConfig config;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final HttpClient client;

    @SneakyThrows
    public List<Agent> getAgents()
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getAgentUrl()))
                .header("Content-Type", "application/json")
                .headers(authService.getAuthHeaders())
                .GET()
                .build();

        HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

        throwExceptionOnUnexpectedStatusCode(httpResponse.statusCode(), SC_OK);

        return objectMapper.readValue(httpResponse.body(), new TypeReference<>() {});
    }

    @SneakyThrows
    public String askQuestion(Question question)
    {
        try
        {
            String body = objectMapper.writeValueAsString(question);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getQuestionUrl()))
                    .header("Content-Type", "application/json")
                    .headers(authService.getAuthHeaders())
                    .POST(BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> httpResponse = client.send(request, BodyHandlers.ofString());

            ensureThat(httpResponse.statusCode() == EXPECTED_STATUS_CODE,
                    () -> new WebScriptException(httpResponse.statusCode(), "Request to hxi failed"));

            return objectMapper.readValue(httpResponse.body(), QuestionResponse.class)
                    .questionId();
        }
        catch (IOException | InterruptedException e)
        {
            throw new WebScriptException(INTERNAL_SERVER_ERROR, "Failed to ask question", e);
        }
    }
}
