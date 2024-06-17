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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester;

import static org.apache.camel.LoggingLevel.DEBUG;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Component
@RequiredArgsConstructor
public class IngestEventListener extends RouteBuilder
{
    private static final String ROUTE_ID = "bulk-ingester-events-consumer";

    private final ObjectMapper objectMapper;
    private final IngestEventProcessor eventProcessor;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        from(integrationProperties.alfresco().bulkIngester().endpoint())
                .transacted()
                .routeId(ROUTE_ID)
                .log(DEBUG, "Received bulk ingester event : ${header.JMSMessageID}")
                .process(exchange -> eventProcessor.process(mapToIngestEvent(exchange)))
                .end();
    }

    private IngestEvent mapToIngestEvent(Exchange exchange)
    {
        try
        {
            return objectMapper.readValue(exchange.getIn().getBody(String.class), new TypeReference<>() {});
        }
        catch (JsonProcessingException e)
        {
            throw new LiveIngesterRuntimeException("Event deserialization failed", e);
        }
    }
}
