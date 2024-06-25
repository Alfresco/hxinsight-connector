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

package org.alfresco.hxi_connector.prediction_applier.service;

import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;

import static org.alfresco.hxi_connector.common.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode;

import java.io.IOException;
import java.util.Map;
import jakarta.annotation.PreDestroy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.alfresco.hxi_connector.prediction_applier.service.config.QuestionServiceConfig;
import org.alfresco.hxi_connector.prediction_applier.service.model.Question;

@Slf4j
@RequiredArgsConstructor
public class QuestionService
{
    private final QuestionServiceConfig config;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient client = HttpClients.createDefault();
    private final String QUESTION_ID_ENTRY = "questionId";
    private final int EXPECTED_STATUS_CODE = 202;

    @SneakyThrows
    public String askQuestion(Question question)
    {
        try (HttpEntity body = new StringEntity(objectMapper.writeValueAsString(question), APPLICATION_JSON))
        {
            HttpPost httpPost = new HttpPost(config.askQuestionUrl());
            httpPost.setEntity(body);

            return client.execute(httpPost, (response) -> {
                throwExceptionOnUnexpectedStatusCode(response.getCode(), EXPECTED_STATUS_CODE);

                return objectMapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, String>>() {}).get(QUESTION_ID_ENTRY);
            });
        }
    }

    @PreDestroy
    public void close()
    {
        try
        {
            log.info("Closing the HTTP client");
            client.close();
        }
        catch (IOException e)
        {
            log.error("Failed to close the HTTP client", e);
        }
    }
}
