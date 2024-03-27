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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.filter;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.repository.filter.FieldFilter;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.NodeResource;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypeFilterApplier implements RepoEventFilterApplier
{
    @Override
    public boolean allowNode(NodeResource nodeResource, Filter filter)
    {
        final String nodeType = nodeResource.getNodeType();
        final List<String> allowed = filter.type().allow();
        final List<String> denied = filter.type().deny();
        log.atDebug().log("Applying type filters on node id: {}", nodeResource.getId());
        return FieldFilter.filter(nodeType, allowed, denied);
    }

    @Override
    public Optional<Boolean> allowNodeBefore(NodeResource nodeResourceBefore, Filter filter)
    {
        log.atDebug().log("Applying type filters on previous version of repo node id: {}", nodeResourceBefore.getId());
        final String nodeType = nodeResourceBefore.getNodeType();
        if (nodeType == null)
        {
            return Optional.empty();
        }
        return Optional.of(allowNode(nodeResourceBefore, filter));
    }
}
