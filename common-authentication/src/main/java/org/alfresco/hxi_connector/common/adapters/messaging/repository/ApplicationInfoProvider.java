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
package org.alfresco.hxi_connector.common.adapters.messaging.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.alfresco.hxi_connector.common.config.properties.Application;

@Slf4j
@RequiredArgsConstructor
public class ApplicationInfoProvider
{

    public static final String APP_INFO_PATTERN = "ACS HXI Connector/%s ACS/%s (%s)";
    public static final String USER_AGENT_DATA = "user-agent-data";
    public static final String USER_AGENT_PARAM = String.format("&userAgent=${exchangeProperty.%s}", USER_AGENT_DATA);
    private final RepositoryInformation repositoryInformation;
    private final Application applicationProperties;

    private String applicationInfo;

    public String getUserAgentData()
    {
        if (StringUtils.isBlank(applicationInfo))
        {
            log.debug("Calculating user agent data.");
            applicationInfo = calculateUserAgentData();
        }
        log.debug("User agent data: {}", applicationInfo);
        return applicationInfo;
    }

    public String getSourceId()
    {
        return applicationProperties.getSourceId();
    }

    private String calculateUserAgentData()
    {
        String applicationVersion = applicationProperties.getVersion();
        String repositoryVersion = repositoryInformation.getRepositoryVersion();
        String osVersion = System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
        return String.format(APP_INFO_PATTERN, applicationVersion, repositoryVersion, osVersion);
    }
}
