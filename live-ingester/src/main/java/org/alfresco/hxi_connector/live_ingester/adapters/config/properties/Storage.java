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

import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

@SuppressWarnings("PMD.UnusedAssignment")
public record Storage(@NotNull Location location, @NotNull Upload upload)
{

    public Storage
    {
        upload = requireNonNullElse(upload, new Upload(new Retry()));
    }

    public record Location(@NotBlank String endpoint, @NestedConfigurationProperty @NotNull Retry retry)
    {
        public Location
        {
            retry = requireNonNullElseGet(retry, Retry::new);
        }
    }

    public record Upload(@NotNull @NestedConfigurationProperty Retry retry)
    {
        public Upload
        {
            retry = requireNonNullElseGet(retry, Retry::new);
        }
    }
}
