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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response;

import static org.apache.camel.LoggingLevel.DEBUG;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.TransformationFailedException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformResponseHandler extends RouteBuilder
{
    private static final String ROUTE_ID = "transform-events-consumer";

    private final IngestContentCommandHandler ingestContentCommandHandler;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        // @formatter:off
        SecurityContext securityContext = SecurityContextHolder.getContext();
        from(integrationProperties.alfresco().transform().response().endpoint())
            .routeId(ROUTE_ID)
            .log(DEBUG, log, "Transform :: Received completed event : ${body}")
            .unmarshal()
            .json(JsonLibrary.Jackson, TransformResponse.class)
            .process(exchange -> SecurityContextHolder.setContext(securityContext))
            .doTry()
                .process(this::ingestContent)
            .doCatch(TransformationFailedException.class)
                .log(DEBUG, log, "Ingestion :: Content update failed: ${exception.message}")
            .end();
        // @formatter:on
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void ingestContent(Exchange exchange)
    {
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);
        IngestContentCommand command = new IngestContentCommand(transformResponse.status(), transformResponse.targetReference(), transformResponse.clientData().nodeRef());

        ingestContentCommandHandler.handle(command);
    }

}
