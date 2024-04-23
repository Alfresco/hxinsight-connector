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

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.model.ATSTransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformRequester extends RouteBuilder implements TransformRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + ATSTransformRequester.class.getSimpleName();
    private static final String ROUTE_ID = "transform-request-publisher";
    protected static final String TIMEOUT_KEY = "timeout";

    private final CamelContext camelContext;
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
        ATSTransformRequest atsTransformRequest = toTransformRequest(transformRequest, 0);
        log.info("Sending request to ATS: {}", atsTransformRequest);
        camelContext.createProducerTemplate()
                .sendBody(LOCAL_ENDPOINT, atsTransformRequest);
    }

    public void requestTransformRetry(TransformRequest transformRequest, int attempt)
    {
        ATSTransformRequest atsTransformRequest = toTransformRequest(transformRequest, attempt);
        log.info("Retrying transformation. request: {} attempt: {}", atsTransformRequest, attempt);
        camelContext.createProducerTemplate()
                .sendBody(LOCAL_ENDPOINT, atsTransformRequest);
    }

    protected ATSTransformRequest toTransformRequest(TransformRequest transformRequest, int attempt)
    {
        Transform transformProperties = integrationProperties.alfresco().transform();
        String targetMimeType = transformRequest.targetMimeType();
        Map<String, String> transformOptions = new HashMap<>();
        transformOptions.put(TIMEOUT_KEY, String.valueOf(transformProperties.request().timeout()));
        Map<String, Map<String, String>> configuredOptions = transformProperties.request().options();
        if (configuredOptions != null && configuredOptions.containsKey(targetMimeType))
        {
            transformOptions.putAll(configuredOptions.get(targetMimeType));
        }
        return new ATSTransformRequest(
                transformRequest.nodeRef(),
                targetMimeType,
                new ClientData(transformRequest.nodeRef(), targetMimeType, attempt),
                transformOptions,
                transformProperties.response().queueName());
    }

}
