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

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@RelationshipResource(name = "avatars", title = "Avatars of agents", entityResource = AgentsEntityResource.class)
public class AvatarRelation implements RelationshipResourceAction.ReadById<BinaryResource>
{
    private HxInsightClient hxInsightClient;

    @Override
    @WebApiDescription(title = "Get Agent Avatar image")
    public BinaryResource readById(String agentId, String avatarId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        log.debug("Avatar requested for agent with id={}", agentId);

        if (!avatarId.equals("-default-"))
        {
            log.info("Avatar id is different than -default-");
            throw new NotFoundException(String.format("Avatar with id=%s not found", avatarId));
        }

        return hxInsightClient.getAvatar(agentId);
    }
}
