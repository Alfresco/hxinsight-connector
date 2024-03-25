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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ExchangeEnricher
{

    public static final String UPDATED_EVENT_TYPE_PROP = "updated-event-type";
    private static final String ENRICHED_EXCHANGE_MESSAGE = "Enriched exchange after filtering. Set exchange property {} to {}";

    /**
     * This method will enrich Camel exchange with a property indicating that original event type should be changed to the value of that property. It is called during node filtering as most of the needed logic is already there.
     *
     * @param exchange
     *            Camel exchange object
     * @param resultBefore
     *            Filtering result for node resource before
     * @param result
     *            Filtering result for current node resource
     */
    public static void enrichExchangeAfterFiltering(Exchange exchange, boolean resultBefore, boolean result)
    {
        if (resultBefore && !result)
        {
            log.atDebug().log(ENRICHED_EXCHANGE_MESSAGE, UPDATED_EVENT_TYPE_PROP, NODE_DELETED.getType());
            exchange.setProperty(UPDATED_EVENT_TYPE_PROP, NODE_DELETED.getType());
        }
        if (!resultBefore && result)
        {
            log.atDebug().log(ENRICHED_EXCHANGE_MESSAGE, UPDATED_EVENT_TYPE_PROP, NODE_CREATED.getType());
            exchange.setProperty(UPDATED_EVENT_TYPE_PROP, NODE_CREATED.getType());
        }
    }
}
