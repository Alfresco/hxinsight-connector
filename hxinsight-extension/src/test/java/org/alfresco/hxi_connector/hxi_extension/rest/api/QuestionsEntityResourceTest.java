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

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.extensions.webscripts.Status.STATUS_BAD_REQUEST;

public class QuestionsEntityResourceTest
{
    private final HxInsightClient hxInsightClient = mock();
    private final QuestionsEntityResource questionsEntityResource = new QuestionsEntityResource(hxInsightClient);

    @Test
    public void shouldFailIfAskedMultipleQuestions()
    {
        // given
        List<QuestionModel> questions = List.of(mock(QuestionModel.class), mock(QuestionModel.class));

        // when
        WebScriptException webScriptException = assertThrows(WebScriptException.class, () -> questionsEntityResource.create(questions, null));

        assertTrue(webScriptException.getMessage().contains("You can only ask one question at a time."));
        assertEquals(STATUS_BAD_REQUEST, webScriptException.getStatus());
    }

    @Test
    public void shouldReturnQuestionWithId()
    {
        // given
        QuestionModel question = new QuestionModel(
                null,
                "What is the capital of France?",
                ""
        );

        String questionId = "a13c4b3d-4b3d-4b3d-4b3d-4b3d4b3d4b3d";
        given(hxInsightClient.askQuestion(any())).willReturn(questionId);


        // when
        List<QuestionModel> questionIds = questionsEntityResource.create(List.of(question), null);

        // then
        assertEquals(1, questionIds.size());
        assertEquals(questionId, questionIds.get(0).getQuestionId());
    }
}
