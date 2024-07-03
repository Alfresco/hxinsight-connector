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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.hxi_connector.hxi_extension.service.util.AuthService;

class HxInsightClientTest
{

    private final HxInsightClientConfig config = mock(HxInsightClientConfig.class);
    private final AuthService authService = mock(AuthService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = mock(HttpClient.class);

    private final HxInsightClient hxInsightClient = new HxInsightClient(
            config,
            authService,
            objectMapper,
            httpClient);

    @BeforeEach
    void setUp()
    {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        given(config.getQuestionUrl()).willReturn("http://hxinsight/question");

        given(authService.getAuthHeaders()).willReturn(new String[]{"Authorization", "Bearer token"});
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
        String actualQuestionId = hxInsightClient.askQuestion(
                new Question("Who won last year's Super Bowl?", ""));

        // then
        assertEquals(expectedQuestionId, actualQuestionId);
    }

    @Test
    @SneakyThrows
    void shouldThrowOnNotExpectedStatusCode()
    {
        // given
        HttpResponse response = mock(HttpResponse.class);

        given(response.statusCode()).willReturn(400);

        given(httpClient.send(any(), any())).willReturn(response);

        // when
        assertThrows(WebScriptException.class, () -> hxInsightClient.askQuestion(
                new Question("Who won last year's Super Bowl?", "")));
    }

}
