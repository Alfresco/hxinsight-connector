/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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
import java.util.Set;

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
public class AspectFilterApplier implements NodeFilterApplier
{
    @Override
    public boolean applyFilter(RepoEvent<DataAttributes<NodeResource>> repoEvent, Filter filter)
    {
        final Set<String> aspectNames = repoEvent.getData().getResource().getAspectNames();
        final List<String> allowed = filter.aspect().allow();
        final List<String> denied = filter.aspect().deny();
        log.debug("Applying aspect filters on repo event of id: {}. Event aspects: {}. Allowed aspects: {}. Denied aspects: {}", repoEvent.getId(), aspectNames, allowed, denied);
        final boolean allow = filterAllowed(aspectNames, allowed);
        final boolean deny = filterDenied(aspectNames, denied);
        return allow && !deny;
    }

    private boolean filterAllowed(Set<String> aspectNames, List<String> allowed)
    {
        return CollectionUtils.isEmpty(aspectNames) ||
                (allowed.stream().anyMatch(aspectNames::contains) || CollectionUtils.isEmpty(allowed));
    }

    private boolean filterDenied(Set<String> aspectNames, List<String> denied)
    {
        return !CollectionUtils.isEmpty(aspectNames) &&
                (!CollectionUtils.isEmpty(denied) && denied.stream().anyMatch(aspectNames::contains));
    }
}
