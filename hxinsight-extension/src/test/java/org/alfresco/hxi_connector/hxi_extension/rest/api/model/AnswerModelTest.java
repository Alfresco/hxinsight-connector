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
package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;

class AnswerModelTest
{

    @Test
    void testFromServiceModel()
    {
        AnswerResponse answer = new AnswerResponse();
        String answerText = "answer";
        answer.setAnswer(answerText);
        String questionId = "questionId";
        answer.setQuestionId(questionId);
        AnswerResponse.Reference reference = new AnswerResponse.Reference();
        String referenceId = "referenceId";
        reference.setReferenceId(referenceId);
        String referenceText = "referenceText";
        reference.setTextReference(referenceText);
        answer.setReferences(Set.of(reference));
        AnswerModel.ReferenceModel referenceModel = new AnswerModel.ReferenceModel(referenceId, referenceText);
        AnswerModel expected = new AnswerModel(answerText, questionId, Set.of(referenceModel));
        assertEquals(expected, AnswerModel.fromServiceModel(answer));
    }

    @Test
    void testFromServiceModelNullReferences()
    {
        AnswerResponse answer = new AnswerResponse();
        String answerText = "answer";
        answer.setAnswer(answerText);
        String questionId = "questionId";
        answer.setQuestionId(questionId);
        answer.setReferences(null);
        AnswerModel expected = new AnswerModel(answerText, questionId, Set.of());
        assertEquals(expected, AnswerModel.fromServiceModel(answer));
    }

    @Test
    void testFromServiceModelReference()
    {
        AnswerResponse.Reference reference = new AnswerResponse.Reference();
        String referenceId = "referenceId";
        reference.setReferenceId(referenceId);
        String referenceText = "referenceText";
        reference.setTextReference(referenceText);
        AnswerModel.ReferenceModel expected = new AnswerModel.ReferenceModel(referenceId, referenceText);
        assertEquals(expected, AnswerModel.ReferenceModel.fromServiceModel(reference));
    }

}
