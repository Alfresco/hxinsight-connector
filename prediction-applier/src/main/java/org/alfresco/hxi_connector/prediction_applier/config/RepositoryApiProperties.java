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
package org.alfresco.hxi_connector.prediction_applier.config;

import jakarta.validation.constraints.Positive;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.alfresco.hxi_connector.common.config.properties.Retry;

@ConfigurationProperties("alfresco.repository")
public record RepositoryApiProperties(String baseUrl, String discoveryEndpoint, Retry retry, HealthProbe healthProbe)
{

    public RepositoryApiProperties
    {
        if (StringUtils.isNotBlank(discoveryEndpoint) && StringUtils.isBlank(healthProbe.endpoint()))
        {
            throw new IllegalStateException("Property %s must be set in the Prediction Applier configuration when property %s is set."
                    .formatted("alfresco.repository.health-probe.endpoint", "alfresco.repository.discovery-endpoint"));
        }
    }

    @Validated
    public record HealthProbe(String endpoint, @Positive @DefaultValue("1800") int timeoutSeconds, @Positive @DefaultValue("30") int intervalSeconds)
    {}
}
