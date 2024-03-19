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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.model.ATSTransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformRequester extends RouteBuilder implements TransformRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + ATSTransformRequester.class.getSimpleName();
    private static final String ROUTE_ID = "transform-request-publisher";
    private static final String WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private static final String TIMEOUT_KEY = "timeout";

    private final CamelContext camelContext;
    private final ObjectMapper objectMapper;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        from(LOCAL_ENDPOINT)
                .routeId(ROUTE_ID)
                .marshal()
                .json()
                .to(integrationProperties.alfresco().transform().request().endpoint());
    }

    @Override
    public void requestTransform(TransformRequest transformRequest)
    {
        ATSTransformRequest atsTransformRequest = atsTransformRequestFrom(transformRequest);
        log.info("Sending request to ATS: {}", atsTransformRequest);
        camelContext.createProducerTemplate()
                .sendBody(LOCAL_ENDPOINT, atsTransformRequest);
    }

    private ATSTransformRequest atsTransformRequestFrom(TransformRequest transformRequest)
    {
        String clientDataString = makeClientDataString(transformRequest);
        return ATSTransformRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .nodeRef(WORKSPACE_SPACES_STORE + transformRequest.nodeRef())
                .targetMediaType(transformRequest.targetMimeType())
                .replyQueue(integrationProperties.alfresco().transform().response().queueName())
                .transformOptions(getTransformRequestOptions(integrationProperties.alfresco().transform()))
                .clientData(clientDataString)
                .build();
    }

    private Map<String, String> getTransformRequestOptions(Transform transformProperties)
    {
        return Map.of(TIMEOUT_KEY, String.valueOf(transformProperties.request().timeout()));
    }

    private String makeClientDataString(TransformRequest transformRequest)
    {
        ClientData clientData = new ClientData(transformRequest.nodeRef(), transformRequest.targetMimeType());
        try
        {
            return objectMapper.writeValueAsString(clientData);
        }
        catch (JsonProcessingException e)
        {
            throw new LiveIngesterRuntimeException("Failed to construct client data string for Transform Service request.", e);
        }
    }
}
