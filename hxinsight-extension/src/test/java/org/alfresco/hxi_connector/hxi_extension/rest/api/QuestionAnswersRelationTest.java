/*-
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

import static java.util.Collections.emptySet;

import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.AnswerModel;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@ExtendWith(MockitoExtension.class)
class QuestionAnswersRelationTest
{

    @Mock
    private QuestionService questionService;
    @Mock
    private Parameters mockParameters;

    @InjectMocks
    private QuestionAnswersRelation objectUnderTest;

    @ParameterizedTest
    @CsvSource(value = {"COMPLETE,true", "INCOMPLETE,false", "SUBMITTED,false"})
    void shouldCallHxInsightClientGetAnswer(String responseCompleteness, boolean isComplete)
    {
        // given
        String question = "Some question";
        AnswerResponse hXAnswer = AnswerResponse.builder()
                .question(question)
                .responseCompleteness(responseCompleteness)
                .answer("Some answer")
                .build();
        String questionId = "questionId";
        given(questionService.getAnswer(questionId)).willReturn(hXAnswer);

        // when
        CollectionWithPagingInfo<AnswerModel> answerResponse = objectUnderTest.readAll(questionId, mockParameters);

        // then
        then(questionService).should().getAnswer(questionId);
        Collection<AnswerModel> answerResponseEntries = answerResponse.getCollection();
        assertEquals(1, answerResponseEntries.size());
        AnswerModel answer = answerResponseEntries.iterator().next();
        AnswerModel expectedAnswer = new AnswerModel(hXAnswer.getAnswer(), hXAnswer.getQuestion(), isComplete, emptySet());
        assertEquals(expectedAnswer, answer);
    }

    @Test
    void shouldFailWhenHxClientThrowsException()
    {
        // given
        String questionId = "questionId";
        given(questionService.getAnswer(questionId)).willThrow(new WebScriptException(SC_SERVICE_UNAVAILABLE, "Some error message"));

        // when + then
        assertThrows(WebScriptException.class, () -> objectUnderTest.readAll(questionId, mockParameters));
    }
}
