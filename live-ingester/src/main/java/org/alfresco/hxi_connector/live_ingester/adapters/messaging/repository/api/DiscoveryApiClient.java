/*-
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.api;

import static org.apache.camel.LoggingLevel.TRACE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscoveryApiClient extends RouteBuilder implements DiscoveryApi
{

    private static final String LOCAL_ENDPOINT = "direct:" + DiscoveryApiClient.class.getSimpleName();
    public static final String ROUTE_ID = "discovery-api-client";

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;
    private final AuthService authService;

    @Override
    public void configure()
    {
        // @formatter:off
        from(LOCAL_ENDPOINT)
            .routeId(ROUTE_ID)
            .process(authService::setAlfrescoAuthorizationHeaders)
            .log(TRACE, log,"Sending repository discovery API request to: %s".formatted(integrationProperties.alfresco().repository().discoveryEndpoint()))
            .to(integrationProperties.alfresco().repository().discoveryEndpoint())
            .log(TRACE, log,"Discovery API response: ${body}")
            .unmarshal()
            .json(JsonLibrary.Jackson, DiscoverApiResponse.class)
        .end();
        // @formatter:on
    }

    @Override
    public String getRepositoryVersion()
    {
        DiscoverApiResponse response = camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .request(DiscoverApiResponse.class);
        return response.getFullVersion();
    }
}
