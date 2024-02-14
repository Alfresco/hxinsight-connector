/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property.PropertyMappingHelper.CONTENT_PROPERTY_KEY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta.updated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.UploadContentRenditionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.RemoteContentLocation;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformResponseHandler extends RouteBuilder
{

    private final IngestContentCommandHandler ingestContentCommandHandler;
    private final IngestMetadataCommandHandler ingestMetadataCommandHandler;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        from(integrationProperties.alfresco().transform().response().endpoint())
                .routeId("transform-events-consumer")
                .log(DEBUG, "Received transform completed event : ${body}")
                .unmarshal()
                .json(JsonLibrary.Jackson, TransformResponse.class)
                .process(this::uploadContentRendition)
                .process(this::updateContentLocation)
                .end();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void uploadContentRendition(Exchange exchange)
    {
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);

        UploadContentRenditionCommand command = new UploadContentRenditionCommand(transformResponse.targetReference(), transformResponse.clientData().nodeRef());

        RemoteContentLocation remoteContentLocation = ingestContentCommandHandler.handle(command);
        exchange.getIn().setBody(remoteContentLocation);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void updateContentLocation(Exchange exchange)
    {
        RemoteContentLocation remoteContentLocation = exchange.getIn().getBody(RemoteContentLocation.class);

        Set<CustomPropertyDelta<?>> properties = Set.of(updated(CONTENT_PROPERTY_KEY, remoteContentLocation.url()));
        IngestMetadataCommand command = new IngestMetadataCommand(remoteContentLocation.nodeId(), UPDATE, properties);
        ingestMetadataCommandHandler.handle(command);
    }
}
