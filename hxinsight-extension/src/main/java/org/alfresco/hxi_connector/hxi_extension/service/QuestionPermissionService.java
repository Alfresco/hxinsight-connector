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

package org.alfresco.hxi_connector.hxi_extension.service;

import static org.alfresco.hxi_connector.hxi_extension.rest.api.util.NodesUtils.validateOrLookupNode;
import static org.alfresco.service.cmr.security.PermissionService.READ;

import lombok.RequiredArgsConstructor;

import org.alfresco.hxi_connector.hxi_extension.service.model.Question;
import org.alfresco.rest.api.Nodes;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

@RequiredArgsConstructor
public class QuestionPermissionService
{
    private final Nodes nodes;
    private final PermissionService permissionService;

    public boolean hasPermissionToAskAboutDocuments(Question question)
    {
        return question.getContextObjectIds()
                .stream()
                .map(nodeId -> validateOrLookupNode(nodes, nodeId))
                .map(nodeRef -> permissionService.hasPermission(nodeRef, READ))
                .allMatch(AccessStatus.ALLOWED::equals);
    }
}
