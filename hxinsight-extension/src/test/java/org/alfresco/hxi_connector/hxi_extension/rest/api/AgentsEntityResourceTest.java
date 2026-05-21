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
package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.AgentModel;
import org.alfresco.hxi_connector.hxi_extension.service.QuestionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.Agent;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@ExtendWith(MockitoExtension.class)
class AgentsEntityResourceTest
{
    @Mock
    private QuestionService questionService;
    @Mock
    private Parameters parameters;

    @InjectMocks
    private AgentsEntityResource objectUnderTest;

    @Test
    void shouldReturnAgentsFromService()
    {
        // given
        Paging paging = Paging.valueOf(0, 100);
        given(parameters.getPaging()).willReturn(paging);
        given(questionService.listAgents()).willReturn(List.of(
                new Agent("agent-1", "Agent One", "First agent", "http://avatar1.url"),
                new Agent("agent-2", "Agent Two", "Second agent", null)));

        // when
        CollectionWithPagingInfo<AgentModel> result = objectUnderTest.readAll(parameters);

        // then
        assertEquals(2, result.getCollection().size());
        List<AgentModel> agents = List.copyOf(result.getCollection());
        assertEquals(new AgentModel("agent-1", "Agent One", "First agent", "http://avatar1.url"), agents.get(0));
        assertEquals(new AgentModel("agent-2", "Agent Two", "Second agent", null), agents.get(1));
    }

    @Test
    void shouldReturnEmptyCollectionWhenNoAgents()
    {
        // given
        Paging paging = Paging.valueOf(0, 100);
        given(parameters.getPaging()).willReturn(paging);
        given(questionService.listAgents()).willReturn(List.of());

        // when
        CollectionWithPagingInfo<AgentModel> result = objectUnderTest.readAll(parameters);

        // then
        assertEquals(0, result.getCollection().size());
    }
}
