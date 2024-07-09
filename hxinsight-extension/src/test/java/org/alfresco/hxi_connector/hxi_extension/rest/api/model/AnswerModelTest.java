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
        String answerText = "answer";
        String questionId = "questionId";
        String referenceId = "referenceId";
        String referenceText = "referenceText";
        AnswerResponse.Reference reference = new AnswerResponse.Reference(referenceId, null, referenceText);
        AnswerResponse answer = AnswerResponse.builder().answer(answerText).questionId(questionId).references(Set.of(reference)).build();
        AnswerModel.ReferenceModel referenceModel = new AnswerModel.ReferenceModel(referenceId, referenceText);
        AnswerModel expected = new AnswerModel(answerText, questionId, Set.of(referenceModel));
        assertEquals(expected, AnswerModel.fromServiceModel(answer));
    }

    @Test
    void testFromServiceModelNullReferences()
    {
        String answerText = "answer";
        String questionId = "questionId";
        AnswerResponse answer = AnswerResponse.builder().answer(answerText).questionId(questionId).references(null).build();
        AnswerModel expected = new AnswerModel(answerText, questionId, Set.of());
        assertEquals(expected, AnswerModel.fromServiceModel(answer));
    }

    @Test
    void testFromServiceModelReference()
    {
        String referenceId = "referenceId";
        String referenceText = "referenceText";
        AnswerResponse.Reference reference = new AnswerResponse.Reference(referenceId, null, referenceText);
        AnswerModel.ReferenceModel expected = new AnswerModel.ReferenceModel(referenceId, referenceText);
        assertEquals(expected, AnswerModel.ReferenceModel.fromServiceModel(reference));
    }

}
