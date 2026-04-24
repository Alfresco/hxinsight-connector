/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.AnswerObjectReferences;
import org.hyland.sdk.cic.qna.object.ReferenceItem;

import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;

public class QuestionMapperImpl implements QuestionMapper
{
    public Agent toAgent(AgentSummary agentSummary)
    {
        String avatarUrl = agentSummary.avatarPresignedUrl() != null ? agentSummary.avatarPresignedUrl() : agentSummary.avatarUrl();
        return new Agent(agentSummary.id(), agentSummary.name(), agentSummary.description(), avatarUrl);
    }

    public AnswerResponse toAnswerResponse(Answer answer)
    {
        Set<AnswerResponse.ObjectReference> objectReferences = Optional.ofNullable(answer.objectReferences())
                .map(refs -> refs.stream()
                        .map(this::toObjectReference)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        return AnswerResponse.builder()
                .question(answer.question())
                .responseCompleteness(answer.responseCompleteness() != null ? answer.responseCompleteness().value() : null)
                .agentId(answer.agentId())
                .agentVersion(String.valueOf(answer.agentVersion()))
                .answer(answer.answer())
                .objectReferences(objectReferences)
                .build();
    }

    private AnswerResponse.ObjectReference toObjectReference(AnswerObjectReferences ref)
    {
        Set<AnswerResponse.Reference> references = Optional.ofNullable(ref.references())
                .map(items -> items.stream()
                        .map(this::toReference)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        return AnswerResponse.ObjectReference.builder()
                .objectId(ref.objectId())
                .references(references)
                .build();
    }

    private AnswerResponse.Reference toReference(ReferenceItem item)
    {
        return AnswerResponse.Reference.builder()
                .referenceId(item.referenceId())
                .rankScore(item.rankScore())
                .rank(item.rank() != null ? item.rank() : 0)
                .build();
    }
}
