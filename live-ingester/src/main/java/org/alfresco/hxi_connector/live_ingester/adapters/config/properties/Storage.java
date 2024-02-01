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
package org.alfresco.hxi_connector.live_ingester.adapters.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@Getter(AccessLevel.NONE)
@NoArgsConstructor
@AllArgsConstructor
public class Storage
{
    @NotNull private Location location;
    @NotNull private Upload upload = new Upload();

    public Location location()
    {
        return location;
    }

    public Upload upload()
    {
        return upload;
    }

    @Data
    @Getter(AccessLevel.NONE)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location
    {
        @NotBlank
        private String endpoint;
        @NestedConfigurationProperty
        @NotNull private Retry retry = new Retry();

        public String endpoint()
        {
            return endpoint;
        }

        public Retry retry()
        {
            return retry;
        }
    }

    @Data
    @Getter(AccessLevel.NONE)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Upload
    {
        @NotNull @NestedConfigurationProperty
        private Retry retry = new Retry();

        public Retry retry()
        {
            return retry;
        }
    }
}
