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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.AgentModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@Slf4j
@AllArgsConstructor
@EntityResource(name = "agents", title = "AI Agents")
public class AgentsEntityResource implements EntityResourceAction.Read<AgentModel>
{
    private HxInsightClient hxInsightClient;

    @Override
    public CollectionWithPagingInfo<AgentModel> readAll(Parameters params)
    {
        List<AgentModel> agents = hxInsightClient.getAgents().stream().map(AgentModel::fromServiceModel).toList();
        return CollectionWithPagingInfo.asPaged(params.getPaging(), agents);
    }
}
