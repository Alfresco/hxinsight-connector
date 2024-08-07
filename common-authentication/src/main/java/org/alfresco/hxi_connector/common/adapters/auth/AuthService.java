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

import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.AUTHORIZATION;

import java.util.Base64;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;

@Slf4j
@RequiredArgsConstructor
public class AuthService
{
    public static final String HXP_ENVIRONMENT_HEADER = "hxp-environment";
    public static final String HXP_APP_HEADER = "hxp-app";
    private static final String HXP_APP_VALUE = "hxai-discovery";
    public static final String HXP_AUTH_PROVIDER = "hyland-experience";
    public static final String ALFRESCO_AUTH_PROVIDER = "alfresco";
    static final String BASIC = "Basic ";
    static final String BEARER = "Bearer ";

    private final AuthProperties authProperties;
    private final AccessTokenProvider accessTokenProvider;

    public void setAlfrescoAuthorizationHeaders(Exchange exchange)
    {
        String authType = authProperties.getProviders().get(ALFRESCO_AUTH_PROVIDER).getType();
        clearAuthHeaders(exchange);
        getAlfrescoAuthHeaders().forEach((key, value) -> exchange.getIn().setHeader(key, value));
        log.debug("Authorization :: {} {} authorization header added", ALFRESCO_AUTH_PROVIDER, authType);
    }

    public void setHxIAuthorizationHeaders(Exchange exchange)
    {
        clearAuthHeaders(exchange);
        getHxpAuthHeaders().forEach((key, value) -> exchange.getIn().setHeader(key, value));
        log.debug("Authorization :: {} authorization header added", HXP_AUTH_PROVIDER);
    }

    public Map<String, String> getHxpAuthHeaders()
    {
        return Map.ofEntries(getAuthHeader(HXP_AUTH_PROVIDER), getHxpEnvironmentHeader(), getHxpAppHeader());
    }

    public Map<String, String> getAlfrescoAuthHeaders()
    {
        return Map.ofEntries(getAuthHeader(ALFRESCO_AUTH_PROVIDER));
    }

    public Map.Entry<String, String> getAuthHeader(String providerId)
    {
        return Map.entry(AUTHORIZATION, getAuthHeaderValue(providerId));
    }

    protected Map.Entry<String, String> getHxpEnvironmentHeader()
    {
        return Map.entry(HXP_ENVIRONMENT_HEADER, ofNullable(authProperties.getProviders().get(HXP_AUTH_PROVIDER))
                .map(AuthProperties.AuthProvider::getEnvironmentKey)
                .orElse(EMPTY));
    }

    protected Map.Entry<String, String> getHxpAppHeader()
    {
        return Map.entry(HXP_APP_HEADER, HXP_APP_VALUE);
    }

    private String getAuthHeaderValue(String providerId)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(ALFRESCO_AUTH_PROVIDER);
        return providerId.equals(HXP_AUTH_PROVIDER) ? getBearerAuthHeader(HXP_AUTH_PROVIDER) : getAlfrescoAuthHeader(authProvider);
    }

    private String getAlfrescoAuthHeader(AuthProperties.AuthProvider authProvider)
    {
        if (BASIC.trim().equalsIgnoreCase(authProvider.getType()))
        {
            return BASIC + getBasicAuthHeader(authProvider);
        }
        else
        {
            return getBearerAuthHeader(ALFRESCO_AUTH_PROVIDER);
        }
    }

    private String getBearerAuthHeader(String providerId)
    {
        return BEARER + accessTokenProvider.getAccessToken(providerId);
    }

    private void clearAuthHeaders(Exchange exchange)
    {
        exchange.getIn().removeHeader(AUTHORIZATION);
        exchange.getIn().removeHeader(getHxpEnvironmentHeader().getKey());
        exchange.getIn().removeHeader(getHxpAppHeader().getKey());
    }

    private static String getBasicAuthHeader(AuthProperties.AuthProvider authProvider)
    {
        String valueToEncode = authProvider.getUsername() + ":" + authProvider.getPassword();
        return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
