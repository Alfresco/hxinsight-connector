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
package org.alfresco.hxi_connector.hxi_extension.service;

import static java.util.Optional.ofNullable;

import lombok.RequiredArgsConstructor;

import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;
import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;

@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService
{
    private final HxInsightClient hxInsightClient;
    private final AuthenticationService authenticationService;
    private final PersonService personService;

    @Override
    public String askQuestion(String agentId, Question question)
    {
        NodeRef currentUserNodeRef = personService.getPersonOrNull(authenticationService.getCurrentUserName());
        question.setUserId(ofNullable(currentUserNodeRef).map(NodeRef::getId).orElse(null));
        return hxInsightClient.askQuestion(agentId, question);
    }

    @Override
    public AnswerResponse getAnswer(String questionId)
    {

        NodeRef currentUserNodeRef = personService.getPersonOrNull(authenticationService.getCurrentUserName());
        String userId = ofNullable(currentUserNodeRef).map(NodeRef::getId).orElse(null);
        return hxInsightClient.getAnswer(questionId, userId);
    }

    @Override
    public String retryQuestion(String agentId, String questionId, String comments, Question question)
    {
        NodeRef currentUserNodeRef = personService.getPersonOrNull(authenticationService.getCurrentUserName());
        question.setUserId(ofNullable(currentUserNodeRef).map(NodeRef::getId).orElse(null));
        return hxInsightClient.retryQuestion(agentId, questionId, comments, question);
    }
}
