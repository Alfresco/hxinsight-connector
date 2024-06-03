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
package org.alfresco.hxi_connector.common.adapters.auth;

import java.util.Base64;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.http.HttpHeaders;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AuthSupport
{
    static final String ENVIRONMENT_KEY_HEADER = "hxai-environment";
    public static final String HXI_AUTH_PROVIDER = "hyland-experience";
    public static final String ALFRESCO_AUTH_PROVIDER = "alfresco";
    static final String BASIC = "Basic ";
    static final String BEARER = "Bearer ";

    public static void setAlfrescoAuthorizationHeaders(Exchange exchange, AccessTokenProvider accessTokenProvider, AuthProperties authProperties)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(ALFRESCO_AUTH_PROVIDER);
        String authType = authProvider.getType();
        String authHeaderValue = getAlfrescoAuthHeaderValue(accessTokenProvider, authProvider);
        clearAuthHeaders(exchange);
        exchange.getIn().setHeader(HttpHeaders.AUTHORIZATION, authHeaderValue);
        log.debug("Authorization :: {} {} authorization header added", ALFRESCO_AUTH_PROVIDER, authType);
    }

    public static void setHxIAuthorizationHeaders(Exchange exchange, AccessTokenProvider accessTokenProvider, AuthProperties authProperties)
    {
        final String token = accessTokenProvider.getAccessToken(HXI_AUTH_PROVIDER);
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(HXI_AUTH_PROVIDER);
        String authHeaderValue = BEARER + token;
        clearAuthHeaders(exchange);
        exchange.getIn().setHeader(HttpHeaders.AUTHORIZATION, authHeaderValue);
        exchange.getIn().setHeader(ENVIRONMENT_KEY_HEADER, authProvider.getEnvironmentKey());
        log.debug("Authorization :: {} authorization header added", HXI_AUTH_PROVIDER);
    }

    private static String getAlfrescoAuthHeaderValue(AccessTokenProvider accessTokenProvider, AuthProperties.AuthProvider authProvider)
    {
        if (BASIC.trim().equalsIgnoreCase(authProvider.getType()))
        {
            return BASIC + getBasicAuthenticationHeader(authProvider);
        }
        else
        {
            return BEARER + accessTokenProvider.getAccessToken(ALFRESCO_AUTH_PROVIDER);
        }
    }

    private static String getBasicAuthenticationHeader(AuthProperties.AuthProvider authProvider)
    {
        String valueToEncode = authProvider.getUsername() + ":" + authProvider.getPassword();
        return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private static void clearAuthHeaders(Exchange exchange)
    {
        exchange.getIn().removeHeader(HttpHeaders.AUTHORIZATION);
        exchange.getIn().removeHeader(ENVIRONMENT_KEY_HEADER);
    }
}
