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

import static java.lang.String.format;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.AnswerModel;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@Slf4j
@RequiredArgsConstructor
@RelationshipResource(name = "answers", title = "Answers to questions about documents", entityResource = QuestionsEntityResource.class)
public class QuestionAnswersRelation implements RelationshipResourceAction.ReadById<AnswerModel>
{

    private final QuestionService questionService;

    @Override
    @WebApiDescription(title = "Get answers to a question")
    public AnswerModel readById(String questionId, String id, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        ensureThat(id.equals("-default-"), () -> new EntityNotFoundException(format("%s (you should use id '-default-')", id)));

        AnswerResponse hxInsightAnswer = questionService.getAnswer(questionId);
        return AnswerModel.fromServiceModel(hxInsightAnswer);
    }
}
