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

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.config.QuestionsApiConfig;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionPermissionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@Slf4j
@RequiredArgsConstructor
@EntityResource(name = "questions", title = "Questions about documents")
public class QuestionsEntityResource implements EntityResourceAction.Create<QuestionModel>
{
    private final HxInsightClient hxInsightClient;
    private final QuestionsApiConfig questionConfig;
    private final QuestionPermissionService questionPermissionService;

    @Override
    @WebApiDescription(title = "Ask question", successStatus = Status.STATUS_OK)
    public List<QuestionModel> create(List<QuestionModel> questions, Parameters parameters)
    {
        ensureThat(questions.size() == 1, () -> new WebScriptException(Status.STATUS_BAD_REQUEST, "You can only ask one question at a time."));

        QuestionModel questionModel = questions.get(0);
        Question question = questionModel.toQuestion();

        log.info("Received question: {}", question);

        ensureThat(question.getRestrictionQuery().nodesIds().size() <= questionConfig.maxContextSizeForQuestion(),
                () -> new WebScriptException(Status.STATUS_BAD_REQUEST, "You can only ask about up to %d nodes at a time.".formatted(questionConfig.maxContextSizeForQuestion())));
        ensureThat(questionPermissionService.hasPermissionToAskAboutDocuments(question),
                () -> new WebScriptException(Status.STATUS_FORBIDDEN, "You don't have permission to ask about some nodes"));

        String questionId = hxInsightClient.askQuestion(question);

        return List.of(questionModel.withId(questionId));
    }
}
