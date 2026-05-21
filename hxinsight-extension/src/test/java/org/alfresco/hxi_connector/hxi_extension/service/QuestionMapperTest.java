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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.AnswerObjectReferences;
import org.hyland.sdk.cic.qna.object.ReferenceItem;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;

class QuestionMapperTest
{
    private static final String AGENT_ID = "agent-id";

    private final QuestionMapper questionMapper = new QuestionMapperImpl();

    @Test
    void shouldMapAgentSummaryToAgent()
    {
        // given
        AgentSummary agentSummary = new AgentSummary("agent-1", "Agent One", "Desc", "gpt-4",
                "http://avatar.url", null, null, List.of(), List.of(), 1, true, null, null, null, null);

        // when
        Agent agent = questionMapper.toAgent(agentSummary);

        // then
        assertEquals("agent-1", agent.getId());
        assertEquals("Agent One", agent.getName());
        assertEquals("Desc", agent.getDescription());
        assertEquals("http://avatar.url", agent.getAvatarUrl());
    }

    @Test
    void shouldPreferPresignedAvatarUrl()
    {
        // given
        AgentSummary agentSummary = new AgentSummary("agent-1", "Agent One", "Desc", "gpt-4",
                "http://static.url", "http://presigned.url", null, List.of(), List.of(), 1, true, null, null, null, null);

        // when
        Agent agent = questionMapper.toAgent(agentSummary);

        // then
        assertEquals("http://presigned.url", agent.getAvatarUrl());
    }

    @Test
    void shouldMapAnswerToAnswerResponse()
    {
        // given
        Answer sdkAnswer = new Answer("answer text", AGENT_ID, 1, ResponseCompleteness.COMPLETE,
                List.of(), List.of(), "question text", null, null, null, null);

        // when
        AnswerResponse answerResponse = questionMapper.toAnswerResponse(sdkAnswer);

        // then
        assertEquals("answer text", answerResponse.getAnswer());
        assertEquals("question text", answerResponse.getQuestion());
        assertEquals("Complete", answerResponse.getResponseCompleteness());
        assertEquals(AGENT_ID, answerResponse.getAgentId());
        assertEquals("1", answerResponse.getAgentVersion());
        assertEquals(Set.of(), answerResponse.getObjectReferences());
    }

    @Test
    void shouldMapObjectReferencesInAnswer()
    {
        // given
        ReferenceItem ref = new ReferenceItem("ref-1", 0.95, 1);
        AnswerObjectReferences objRef = new AnswerObjectReferences("obj-1", List.of(ref));
        Answer sdkAnswer = new Answer("answer", AGENT_ID, 2, ResponseCompleteness.COMPLETE,
                List.of(objRef), List.of(), "question", null, null, null, null);

        // when
        AnswerResponse answerResponse = questionMapper.toAnswerResponse(sdkAnswer);

        // then
        assertEquals(1, answerResponse.getObjectReferences().size());
        AnswerResponse.ObjectReference mappedObjRef = answerResponse.getObjectReferences().iterator().next();
        assertEquals("obj-1", mappedObjRef.getObjectId());
        assertEquals(1, mappedObjRef.getReferences().size());
        AnswerResponse.Reference mappedRef = mappedObjRef.getReferences().iterator().next();
        assertEquals("ref-1", mappedRef.getReferenceId());
        assertEquals(0.95, mappedRef.getRankScore());
        assertEquals(1, mappedRef.getRank());
    }

    @Test
    void shouldDefaultNullRankToZero()
    {
        // given
        ReferenceItem ref = new ReferenceItem("ref-1", 0.5, null);
        AnswerObjectReferences objRef = new AnswerObjectReferences("obj-1", List.of(ref));
        Answer sdkAnswer = new Answer("answer", AGENT_ID, 1, ResponseCompleteness.COMPLETE,
                List.of(objRef), List.of(), "question", null, null, null, null);

        // when
        AnswerResponse answerResponse = questionMapper.toAnswerResponse(sdkAnswer);

        // then
        AnswerResponse.Reference mappedRef = answerResponse.getObjectReferences().iterator().next()
                .getReferences().iterator().next();
        assertEquals(0, mappedRef.getRank());
    }

    @Test
    void shouldReturnEmptyObjectReferencesWhenAnswerHasNone()
    {
        // given
        Answer sdkAnswer = new Answer("answer", AGENT_ID, 1, ResponseCompleteness.COMPLETE,
                null, null, "question", null, null, null, null);

        // when
        AnswerResponse answerResponse = questionMapper.toAnswerResponse(sdkAnswer);

        // then
        assertEquals(Set.of(), answerResponse.getObjectReferences());
    }

}
