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

package org.alfresco.hxi_connector.live_ingester.messaging.in;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.exception.LiveIngesterRuntimeException;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventProcessor
{

    private final ObjectMapper mapper;

    public void process(Exchange exchange)
    {
        RepoEvent<?> event = eventFrom(exchange);

        if (NODE_CREATED.getType().equals(event.getType()))
        {
            log.info("Received event of type CREATE {}", event);
        } else if (NODE_UPDATED.getType().equals(event.getType()))
        {
            log.info("Received event of type UPDATE {}", event);
        }
    }

    private RepoEvent<?> eventFrom(Exchange exchange)
    {
        try
        {
            return mapper.readValue(exchange.getIn().getBody(String.class), RepoEvent.class);
        } catch (JsonProcessingException e)
        {
            throw new LiveIngesterRuntimeException("Event deserialization failed", e);
        }
    }
}
