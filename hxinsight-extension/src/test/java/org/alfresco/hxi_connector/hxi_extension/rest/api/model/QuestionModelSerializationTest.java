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

package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class QuestionModelSerializationTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrowWhenQuestionIdSpecified()
    {
        // given
        String questionSerialized = """
                {
                    "_questionId": "86553f9b-e382-4a1b-b07d-52adae3e96e8",
                    "question": "What is the capital of France?",
                    "agentId": "agent-id",
                    "restrictionQuery": {
                        "nodesIds": ["node1", "node2"]
                    }
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(questionSerialized, QuestionModel.class));
    }

    @Test
    void shouldThrowWhenQuestionIdSpecified2()
    {
        // given
        String questionSerialized = """
                {
                    "questionId": "86553f9b-e382-4a1b-b07d-52adae3e96e8",
                    "question": "What is the capital of France?",
                    "agentId": "agent-id",
                    "restrictionQuery": {
                        "nodesIds": ["node1", "node2"]
                    }
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(questionSerialized, QuestionModel.class));
    }

    @Test
    @SneakyThrows
    void shouldDeserializeQuestion()
    {
        // given
        String questionSerialized = """
                {
                    "question": "What is the capital of France?",
                    "agentId": "agent-id",
                    "restrictionQuery": {
                        "nodesIds": ["node1", "node2"]
                    }
                }
                """;

        // when
        QuestionModel question = objectMapper.readValue(questionSerialized, QuestionModel.class);

        // then
        assertEquals("What is the capital of France?", question.getQuestion());
        assertEquals("agent-id", question.getAgentId());
        assertEquals(Set.of("node1", "node2"), question.getRestrictionQuery().getNodesIds());
    }
}
