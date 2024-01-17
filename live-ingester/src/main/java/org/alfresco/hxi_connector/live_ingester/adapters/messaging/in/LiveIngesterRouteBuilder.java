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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.in;

import static org.apache.camel.LoggingLevel.DEBUG;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.config.MessagingInputConfig;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.mapper.CamelEventMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class LiveIngesterRouteBuilder extends RouteBuilder
{
    private final MessagingInputConfig properties;
    private final EventProcessor eventProcessor;

    private final CamelEventMapper camelEventMapper;

    public LiveIngesterRouteBuilder(CamelContext context, MessagingInputConfig messagingInputConfig, EventProcessor eventProcessor, CamelEventMapper camelEventMapper)
    {
        super(context);
        this.properties = messagingInputConfig;
        this.camelEventMapper = camelEventMapper;
        this.eventProcessor = eventProcessor;
    }

    @Override
    public void configure()
    {
        from(properties.getEndpoint())
                .transacted()
                .routeId("ingester-events-consumer")
                .log(DEBUG, "Received repo event : ${header.JMSMessageID}")
                .process((exchange) -> eventProcessor.process(camelEventMapper.repoEventFrom(exchange)))
                .end();
    }
}
