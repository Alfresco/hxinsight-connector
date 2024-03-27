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

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.LiveIngesterEventHandler.DENY_NODE;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.CamelEventMapper;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.EventUtils;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepoEventFilterHandler
{
    private final List<RepoEventFilterApplier> repoEventFilterAppliers;
    private final CamelEventMapper camelEventMapper;

    /**
     * Method handles event/node filtering. When node is denied by any of filters, a custom property "DENY_NODE: true" in Camel Exchange is created (used to filter out in main route). In case of update event: - when current version of the node is allowed but previous version was denied, then the type of the event is altered to 'Created'. - when current version of the node is denied but previous version was allowed, then the type of the event is altered to 'Deleted'.
     *
     * @param exchange
     *            Camel Exchange Object
     * @param filter
     *            Filter configuration
     */
    public void handle(Exchange exchange, Filter filter)
    {
        final FilteringResults filteringResults = calculateFilteringResults(exchange, filter);
        final boolean allowNode = filteringResults.allowed;
        if (!allowNode)
        {
            exchange.setProperty(DENY_NODE, true);
        }
        filteringResults.newEventType.ifPresent(type -> exchange.getIn().setBody(camelEventMapper.alterRepoEvent(exchange, type)));
    }

    private FilteringResults calculateFilteringResults(Exchange exchange, Filter filter)
    {
        final RepoEvent<DataAttributes<NodeResource>> repoEvent = exchange.getIn().getBody(RepoEvent.class);
        boolean allowCurrentNode = true;
        boolean allowPreviousNode = true;
        final boolean eventTypeUpdated = EventUtils.isEventTypeUpdated(repoEvent);
        for (RepoEventFilterApplier filterApplier : repoEventFilterAppliers)
        {
            log.atDebug().log("Applying filters {} to current repo event of id: {}", filter, repoEvent.getId());
            final boolean allow = filterApplier.allowNode(repoEvent.getData().getResource(), filter);
            allowCurrentNode = allowCurrentNode && allow;
            if (eventTypeUpdated)
            {
                log.atDebug().log("Applying filters {} to previous version of repo event of id: {}", filter, repoEvent.getId());
                final boolean allowPrevious = filterApplier.allowNodeBefore(repoEvent.getData().getResourceBefore(), filter).orElse(allow);
                allowPreviousNode = allowPreviousNode && allowPrevious;
            }
        }
        final Optional<String> newEventType = eventTypeUpdated ? resolveEventType(allowPreviousNode, allowCurrentNode) : Optional.empty();
        final boolean overallResult = allowCurrentNode || (allowPreviousNode && eventTypeUpdated);
        log.atDebug().log("Overall filtering results. Allow: {}, allow current: {}, allow previous: {}", overallResult, allowCurrentNode, allowPreviousNode);
        return new FilteringResults(overallResult, newEventType);
    }

    private Optional<String> resolveEventType(boolean resultBefore, boolean result)
    {
        if (resultBefore && !result)
        {
            return Optional.of(NODE_DELETED.getType());
        }
        if (!resultBefore && result)
        {
            return Optional.of(NODE_CREATED.getType());
        }
        return Optional.empty();
    }

    record FilteringResults(Boolean allowed, Optional<String> newEventType)
    {}
}
