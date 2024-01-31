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
package org.alfresco.hxi_connector.live_ingester.adapters.config;

import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Ingester;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Repository;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Storage;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;

@Component
@EnableConfigurationProperties({IntegrationProperties.Alfresco.class, IntegrationProperties.HylandExperience.class})
@Validated
@Data
@Accessors(fluent = true)
public class IntegrationProperties
{

    @NotNull private final Alfresco alfresco;
    @NotNull private final HylandExperience hylandExperience;

    @ConfigurationProperties("alfresco")
    public record Alfresco(@NotNull Repository repository, @NotNull Transform transform)
    {}

    @ConfigurationProperties("hyland-experience")
    public record HylandExperience(@NotNull Storage storage, @NotNull Ingester ingester)
    {}
}
