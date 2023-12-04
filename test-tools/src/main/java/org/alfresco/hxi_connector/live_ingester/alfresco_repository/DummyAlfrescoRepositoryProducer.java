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

package org.alfresco.hxi_connector.live_ingester.alfresco_repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnBean(DummyAlfrescoRepositoryConfig.class)
public class DummyAlfrescoRepositoryProducer extends RouteBuilder
{

    @Value("${alfresco.alfresco_repository.endpoint_uri}")
    private String targetEndpointUri;

    private final String localEndpointUri = "direct:start";

    private final CamelContext context;

    private final DummyAlfrescoRepositoryConfig config;

    @Override
    public void configure()
    {
        config.setupEnvironment();

        from(localEndpointUri)
            .to(targetEndpointUri)
            .log("Message with body ${body} sent")
            .end();
    }

    @PostMapping
    public void sendEvent(@RequestBody String eventBody)
    {
        log.info("Sending event {}", eventBody);
        publishMessage(eventBody);
    }

    private void publishMessage(String message)
    {
        context.createProducerTemplate().sendBody(localEndpointUri, message);
    }
}
