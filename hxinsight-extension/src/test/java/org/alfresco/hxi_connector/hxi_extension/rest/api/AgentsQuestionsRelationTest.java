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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.extensions.webscripts.Status.STATUS_BAD_REQUEST;
import static org.springframework.extensions.webscripts.Status.STATUS_FORBIDDEN;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.config.QuestionsApiConfig;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionPermissionService;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.RestrictionQuery;

public class AgentsQuestionsRelationTest
{
    private static final String AGENT_ID = "agent-id";

    private final QuestionService questionService = mock(QuestionService.class);
    private final QuestionsApiConfig questionConfig = new QuestionsApiConfig(3);
    private final QuestionPermissionService questionPermissionService = mock(QuestionPermissionService.class);
    private final AgentsQuestionsRelation agentsQuestionsRelation = new AgentsQuestionsRelation(questionService, questionConfig, questionPermissionService);

    @BeforeEach
    public void setUp()
    {
        given(questionPermissionService.hasPermissionToAskAboutDocuments(any())).willReturn(true);
    }

    @Test
    public void shouldFailIfAskedMultipleQuestions()
    {
        // given
        List<QuestionModel> questions = List.of(mock(QuestionModel.class), mock(QuestionModel.class));

        // when
        WebScriptException webScriptException = assertThrows(WebScriptException.class, () -> agentsQuestionsRelation.create(AGENT_ID, questions, null));

        assertTrue(webScriptException.getMessage().contains("You can only ask one question at a time."));
        assertEquals(STATUS_BAD_REQUEST, webScriptException.getStatus());
    }

    @Test
    public void shouldFailIfAskedAboutTooManyDocuments()
    {
        // given
        QuestionModel question = new QuestionModel(
                null,
                "What is the capital of France?",
                new RestrictionQuery(Set.of("node-id-1", "node-id-2", "node-id-3", "node-id-4")));

        // when
        WebScriptException webScriptException = assertThrows(WebScriptException.class, () -> agentsQuestionsRelation.create(AGENT_ID, List.of(question), null));

        assertTrue(webScriptException.getMessage().contains("You can only ask about up to 3 nodes at a time"));
        assertEquals(STATUS_BAD_REQUEST, webScriptException.getStatus());
    }

    @Test
    public void shouldFailIfHasNoPermissionsToViewDocuments()
    {
        // given
        QuestionModel question = new QuestionModel(
                null,
                "What is the capital of France?",
                new RestrictionQuery(Set.of("node-id-1")));

        given(questionPermissionService.hasPermissionToAskAboutDocuments(any())).willReturn(false);

        // when
        WebScriptException webScriptException = assertThrows(WebScriptException.class, () -> agentsQuestionsRelation.create(AGENT_ID, List.of(question), null));

        assertTrue(webScriptException.getMessage().contains("You don't have permission to ask about some nodes"));
        assertEquals(STATUS_FORBIDDEN, webScriptException.getStatus());
    }

    @Test
    public void shouldReturnQuestionWithId()
    {
        // given
        QuestionModel question = new QuestionModel(
                null,
                "What is the capital of France?",
                new RestrictionQuery(Set.of("node-id")));

        String questionId = "a13c4b3d-4b3d-4b3d-4b3d-4b3d4b3d4b3d";
        given(questionService.askQuestion(eq(AGENT_ID), any())).willReturn(questionId);

        // when
        List<QuestionModel> questionIds = agentsQuestionsRelation.create(AGENT_ID, List.of(question), null);

        // then
        assertEquals(1, questionIds.size());
        assertEquals(questionId, questionIds.get(0).getQuestionId());
    }
}
