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
import static org.alfresco.hxi_connector.common.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.util.ErrorUtils;

@RequiredArgsConstructor
@Slf4j
public class DefaultAuthenticationClient implements AuthenticationClient
{
    public static final int EXPECTED_STATUS_CODE = 200;

    protected final AuthProperties authProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(providerId);
        ensureNonNull(authProvider, "Auth Provider not found for authorization provider id: " + providerId);

        log.atDebug().log("Authentication :: sending token request for {} authorization provider", providerId);

        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authProvider.getTokenUri()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(createEncodedBody(authProvider)))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            throwExceptionOnUnexpectedStatusCode(response.statusCode(), EXPECTED_STATUS_CODE);

            return objectMapper.readValue(response.body(), AuthenticationResult.class);
        }
        catch (IOException | InterruptedException e)
        {
            Set<Class<? extends Throwable>> retryReasons = authProperties.getRetry().reasons();

            throw ErrorUtils.wrapErrorIfNecessary(e, retryReasons);
        }
    }

    private String createEncodedBody(AuthProperties.AuthProvider authProvider)
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
