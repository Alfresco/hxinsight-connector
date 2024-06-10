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
package org.alfresco.hxi_connector.common.adapters.auth.config.properties;

import java.util.Map;
import java.util.Set;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import org.alfresco.hxi_connector.common.config.properties.Retry;

@Data
public class AuthProperties
{

    private Map<String, AuthProvider> providers;
    private Retry retry;

    @Data
    @Validated
    public static class AuthProvider
    {
        private @NotNull String type;
        private @NotNull String clientId;
        private String clientSecret;
        private String tokenUri;
        private String grantType;
        private Set<String> scope;
        private String clientName;
        private String username;
        private String password;
        private String environmentKey;
    }
}
