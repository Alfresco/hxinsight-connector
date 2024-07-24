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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import java.util.Optional;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Repository;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.api.DiscoveryApiClient;

@Slf4j
@RequiredArgsConstructor
@Component
public class ApplicationInfoProvider
{

    public static final String APP_INFO_PATTERN = "ACS HXI Connector/%s ACS/%s (%s)";
    public static final String USER_AGENT_DATA = "user-agent-data";
    public static final String USER_AGENT_PARAM = "&userAgent=${exchangeProperty.%s}".formatted(USER_AGENT_DATA);
    private final DiscoveryApiClient discoveryApiClient;
    private final IntegrationProperties integrationProperties;

    private String applicationInfo;

    public String getUserAgentData()
    {
        if (Strings.isBlank(applicationInfo))
        {
            applicationInfo = calculateUserAgentData();
        }
        return applicationInfo;
    }

    private String calculateUserAgentData()
    {
        String applicationVersion = integrationProperties.application().version();
        String repositoryVersion = discoveryApiClient.getRepositoryVersion()
                .orElseGet(() -> Optional.of(integrationProperties)
                        .map(IntegrationProperties::alfresco)
                        .map(IntegrationProperties.Alfresco::repository)
                        .map(Repository::version)
                        .filter(Predicate.not(String::isBlank))
                        .orElseThrow(() -> new IllegalStateException("The repository version cannot be fetched from the Discovery API and the Live Ingester configuration.")));

        String osVersion = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
        return APP_INFO_PATTERN.formatted(applicationVersion, repositoryVersion, osVersion);
    }
}
