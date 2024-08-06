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

package org.alfresco.hxi_connector.hxi_extension.service.util;

import java.util.stream.Stream;

import org.alfresco.hxi_connector.common.adapters.auth.AccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;

public class AuthService extends org.alfresco.hxi_connector.common.adapters.auth.AuthService
{

    public AuthService(AuthProperties authProperties, AccessTokenProvider accessTokenProvider)
    {
        super(authProperties, accessTokenProvider);
    }

    public String[] getAuthHeaders()
    {
        return getAuthHeaders(HXI_AUTH_PROVIDER).entrySet().stream()
                .flatMap(header -> Stream.of(header.getKey(), header.getValue()))
                .toArray(String[]::new);
    }
}
