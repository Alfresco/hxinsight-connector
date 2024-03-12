/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypeFilterApplier implements NodeFilterApplier
{
    @Override
    public boolean applyFilter(RepoEvent<DataAttributes<NodeResource>> repoEvent, Filter filter)
    {
        final String nodeType = repoEvent.getData().getResource().getNodeType();
        final List<String> allowed = filter.type().allow();
        final List<String> denied = filter.type().deny();
        log.atDebug().log("Applying type filters on repo event of id: {}. Event node type: {}. Allowed types: {}. Denied types: {}", repoEvent.getId(), nodeType, allowed, denied);
        final boolean allow = isAllowed(nodeType, allowed);
        final boolean deny = isDenied(nodeType, denied);
        return allow && !deny;
    }

    private boolean isAllowed(String nodeType, List<String> allowed)
    {
        return CollectionUtils.isEmpty(allowed) || allowed.contains(nodeType);
    }

    private boolean isDenied(String nodeType, List<String> denied)
    {
        return denied.contains(nodeType);
    }
}
