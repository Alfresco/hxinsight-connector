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

package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.AUTHORIZATION;

import java.net.http.HttpClient;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.util.AuthService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = QuestionsEntityResourceIntegrationTest.TestConfig.class)
@Testcontainers
public class QuestionsEntityResourceIntegrationTest
{
    private static final String AUTH_TOKEN = "auth_token";
    private static final String QUESTION_ENDPOINT = "/v1/questions";
    private static final String QUESTION_ID = "1ac7b3e7-0b3b-4b3b-8b3b-3b3b3b3b3b3b";
    private static final String QUESTION_RESPONSE_BODY = """
            {
                "questionId": "%s"
            }
            """.formatted(QUESTION_ID);

    @Container
    static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainer();

    @MockBean
    private HxInsightClientConfig hxInsightClientConfig;

    @MockBean
    private AuthService authService;

    @Autowired
    private QuestionsEntityResource questionsEntityResource;

    @BeforeAll
    protected static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @BeforeEach
    void setUp()
    {
        WireMock.reset();
        given(hxInsightClientConfig.getQuestionUrl()).willReturn(hxInsightMock.getBaseUrl() + QUESTION_ENDPOINT);
        given(authService.getAuthHeaders()).willReturn(new String[]{AUTHORIZATION, AUTH_TOKEN});
    }

    @Test
    public void shouldAskQuestion()
    {
        // given
        List<QuestionModel> questions = List.of(new QuestionModel("", "Is world flat?", ""));

        givenThat(post(QUESTION_ENDPOINT)
                .withHeader(AUTHORIZATION, WireMock.equalTo(AUTH_TOKEN))
                .withRequestBody(WireMock.equalToJson("""
                        {
                            "question": "Is world flat?",
                            "restrictionQuery": ""
                        }
                        """))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(QUESTION_RESPONSE_BODY)));

        // when
        List<QuestionModel> returnedQuestions = questionsEntityResource.create(questions, null);

        // then
        assertEquals(1, returnedQuestions.size());
        assertEquals(QUESTION_ID, returnedQuestions.get(0).getQuestionId());
    }

    @TestConfiguration
    public static class TestConfig
    {
        @Bean
        public ObjectMapper objectMapper()
        {
            return new ObjectMapper();
        }

        @Bean
        public HttpClient httpClient()
        {
            return HttpClient.newHttpClient();
        }

        @Bean
        public HxInsightClient hxInsightClient(
                HxInsightClientConfig config,
                AuthService authService,
                ObjectMapper objectMapper,
                HttpClient httpClient)
        {
            return new HxInsightClient(
                    config,
                    authService,
                    objectMapper,
                    httpClient);
        }

        @Bean
        public QuestionsEntityResource questionsEntityResource(HxInsightClient hxInsightClient)
        {
            return new QuestionsEntityResource(hxInsightClient);
        }
    }
}
