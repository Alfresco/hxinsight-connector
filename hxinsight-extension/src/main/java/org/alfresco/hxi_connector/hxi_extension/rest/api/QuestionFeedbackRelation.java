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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.FeedbackModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@RequiredArgsConstructor
@RelationshipResource(name = "feedback", title = "Feedback about answers given to questions", entityResource = QuestionsEntityResource.class)
public class QuestionFeedbackRelation implements RelationshipResourceAction.Create<FeedbackModel>
{
    private final HxInsightClient hxInsightClient;

    @Override
    public List<FeedbackModel> create(String questionId, List<FeedbackModel> feedbackEntries, Parameters parameters)
    {
        ensureThat(feedbackEntries.size() == 1, () -> new WebScriptException(Status.STATUS_BAD_REQUEST, "Exactly one feedback entry must be provided."));

        hxInsightClient.submitFeedback(questionId, feedbackEntries.get(0).toServiceModel());

        return feedbackEntries;
    }
}
