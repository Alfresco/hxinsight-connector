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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.apache.camel.LoggingLevel.DEBUG;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.CamelEventMapper;

@Component
@RequiredArgsConstructor
public class LiveIngesterEventHandler extends RouteBuilder
{
    private static final String ROUTE_ID = "repo-events-consumer";
    public static final String DENY_NODE = "DENY_NODE";

    private final EventProcessor eventProcessor;
    private final IntegrationProperties integrationProperties;
    private final CamelEventMapper camelEventMapper;

    @Override
    public void configure()
    {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        from(integrationProperties.alfresco().repository().endpoint())
                .transacted()
                .routeId(ROUTE_ID)
                .log(DEBUG, "Received repo event : ${header.JMSMessageID}")
                .setBody(camelEventMapper::repoEventFrom)
                .process(exchange -> SecurityContextHolder.setContext(securityContext))
                .process(eventProcessor::process)
                .end();
    }
}
