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
package org.alfresco.hxi_connector.live_ingester.messaging.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.EventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.event.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.messaging.out.config.MessagingOutputConfig;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerRouteBuilder extends RouteBuilder implements EventPublisher
{
    private static final String LOCAL_ENDPOINT = "direct:start";

    private final CamelContext context;

    private final MessagingOutputConfig config;

    @Override
    public void configure()
    {
        from(LOCAL_ENDPOINT)
            .marshal()
            .json()
            .log("Sending event ${body}")
            .to(config.getEndpoint())
            .end();
    }

    @Override
    public void publishMessage(UpdateNodeMetadataEvent event)
    {
        context.createProducerTemplate()
            .sendBody(LOCAL_ENDPOINT, event);
    }
}
