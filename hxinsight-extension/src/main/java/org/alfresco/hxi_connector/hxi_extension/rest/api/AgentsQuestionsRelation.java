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

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.config.QuestionsApiConfig;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.QuestionModel;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.RetryModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionPermissionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;

@Slf4j
@AllArgsConstructor
@RelationshipResource(name = "questions", title = "Questions about documents", entityResource = AgentsEntityResource.class)
public class AgentsQuestionsRelation implements RelationshipResourceAction.Create<QuestionModel>
{
    private final HxInsightClient hxInsightClient;
    private final QuestionsApiConfig questionConfig;
    private final QuestionPermissionService questionPermissionService;

    @Override
    @WebApiDescription(title = "Ask question", successStatus = Status.STATUS_OK)
    public List<QuestionModel> create(String agentId, List<QuestionModel> questions, Parameters parameters)
    {
        ensureThat(questions.size() == 1, () -> new WebScriptException(Status.STATUS_BAD_REQUEST, "You can only ask one question at a time."));

        QuestionModel questionModel = questions.get(0);
        Question question = validateQuestion(questionModel);

        String questionId = hxInsightClient.askQuestion(agentId, question);

        return List.of(questionModel.withId(questionId));
    }

    @Operation("retry")
    @WebApiDescription(title = "Retry question", description = "Resubmit a question to try to get a better answer.")
    public RetryModel retry(String agentId, String questionId, RetryModel retry, Parameters parameters, WithResponse withResponse)
    {
        Question question = validateQuestion(retry.getOriginalQuestion());

        String newQuestionId = hxInsightClient.retryQuestion(agentId, questionId, retry.getComments(), question);
        return retry.withId(newQuestionId);
    }

    private Question validateQuestion(QuestionModel questionModel)
    {
        Question question = questionModel.toQuestion();

        log.info("Received question: {}", question);

        ensureThat(questionModel.getRestrictionQuery().getNodesIds().size() <= questionConfig.getMaxContextSizeForQuestion(),
                () -> new WebScriptException(Status.STATUS_BAD_REQUEST, String.format("You can only ask about up to %d nodes at a time.", questionConfig.getMaxContextSizeForQuestion())));
        ensureThat(questionPermissionService.hasPermissionToAskAboutDocuments(question),
                () -> new WebScriptException(Status.STATUS_FORBIDDEN, "You don't have permission to ask about some nodes"));
        return question;
    }

}
