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
package org.alfresco.hxi_connector.live_ingester.adapters.config.properties;

import static java.util.Objects.requireNonNullElseGet;

import java.util.Collections;
import java.util.Map;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record Transform(@NotNull Request request, @NotNull Response response, @NotNull SharedFileStore sharedFileStore, MimeType mimeType)
{

    @ConstructorBinding
    public Transform(@NotNull Request request, @NotNull Response response, @NotNull SharedFileStore sharedFileStore, MimeType mimeType)
    {
        this.request = request;
        this.response = response;
        this.sharedFileStore = sharedFileStore;
        this.mimeType = mimeType != null ? mimeType : new MimeType(Collections.emptyMap());
    }

    public record Request(@NotBlank String endpoint, @Positive @DefaultValue("20000") int timeout)
    {}

    @SuppressWarnings("PMD.UnusedAssignment")
    public record Response(
            @NotBlank String endpoint,
            @NotBlank String queueName,
            @NotNull @NestedConfigurationProperty Retry retryIngestion,
            @NotNull @NestedConfigurationProperty Retry retryTransformation)
    {
        public Response
        {
            retryIngestion = requireNonNullElseGet(retryTransformation, Retry::new);
            retryTransformation = requireNonNullElseGet(retryTransformation, Retry::new);
        }
    }

    @SuppressWarnings("PMD.UnusedAssignment")
    public record SharedFileStore(@NotBlank String host, @Positive int port, @NotNull @NestedConfigurationProperty Retry retry)
    {
        public SharedFileStore
        {
            retry = requireNonNullElseGet(retry, Retry::new);
        }
    }

    public record MimeType(Map<String, String> mapping)
    {}
}
