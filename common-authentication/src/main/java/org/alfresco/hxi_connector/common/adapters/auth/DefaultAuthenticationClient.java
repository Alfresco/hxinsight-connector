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

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.web.client.RestTemplate;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;

@RequiredArgsConstructor
@Slf4j
public class DefaultAuthenticationClient implements AuthenticationClient
{
    public static final int EXPECTED_STATUS_CODE = 200;

    private final AuthProperties authProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(providerId);
        ensureNonNull(authProvider, "Auth Provider not found for authorization provider id: " + providerId);

        log.atDebug().log("Authentication :: sending token request for {} authorization provider", providerId);

        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPost httpPost = new HttpPost(authProvider.getTokenUri());

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            StringEntity entity = new UrlEncodedFormEntity(createEncodedBody(authProvider));
            httpPost.setEntity(entity);

            return httpClient.execute(httpPost, response -> {
                ErrorUtils.throwExceptionOnUnexpectedStatusCode(response.getCode(), EXPECTED_STATUS_CODE);

                return objectMapper.readValue(EntityUtils.toString(response.getEntity()), AuthenticationResult.class);
            });
        }
        catch (Exception e)
        {
            Set<Class<? extends Throwable>> retryReasons = authProperties.getRetry().reasons();

            ErrorUtils.wrapErrorIfNecessary(e, retryReasons);
            throw new EndpointServerErrorException(e);
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
}
