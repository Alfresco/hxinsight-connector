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

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNonNull;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.DisposableBean;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.util.ErrorUtils;

@RequiredArgsConstructor
@Slf4j
public class DefaultAuthenticationClient implements AuthenticationClient, DisposableBean
{
    public static final int EXPECTED_STATUS_CODE = 200;

    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(providerId);
        ensureNonNull(authProvider, "Auth Provider not found for authorization provider id: " + providerId);

        log.atDebug().log("Authentication :: sending token request for {} authorization provider", providerId);

        try
        {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(createEncodedBody(authProvider));
            HttpPost httpPost = new HttpPost(authProvider.getTokenUri());
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(entity);

            return httpClient.execute(httpPost, response -> {
                ErrorUtils.throwExceptionOnUnexpectedStatusCode(response.getStatusLine().getStatusCode(), EXPECTED_STATUS_CODE);

                return objectMapper.readValue(EntityUtils.toString(response.getEntity()), AuthenticationResult.class);
            });
        }
        catch (Exception e)
        {
            Set<Class<? extends Throwable>> retryReasons = authProperties.getRetry().reasons();

            throw ErrorUtils.wrapErrorIfNecessary(e, retryReasons);
        }
    }

    private List<NameValuePair> createEncodedBody(AuthProperties.AuthProvider authProvider)
    {
        return TokenRequest.builder()
                .clientId(authProvider.getClientId())
                .grantType(authProvider.getGrantType())
                .clientSecret(authProvider.getClientSecret())
                .scope(authProvider.getScope())
                .username(authProvider.getUsername())
                .password(authProvider.getPassword())
                .build()
                .getTokenRequestBody();
    }

    @Override
    public void destroy()
    {
        try
        {
            log.trace("Closing the HTTP client");
            httpClient.close();
        }
        catch (IOException e)
        {
            log.error("Failed to close the HTTP client", e);
        }
    }
}
