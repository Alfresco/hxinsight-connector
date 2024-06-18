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

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.util.ErrorUtils;

@RequiredArgsConstructor
@Slf4j
public class DefaultAuthenticationClient implements AuthenticationClient
{
    public static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;
    private final AuthProperties authProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(providerId);
        ensureNonNull(authProvider, "Auth Provider not found for authorization provider id: " + providerId);

        log.atDebug().log("Authentication :: sending token request for {} authorization provider", providerId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(
                createEncodedBody(authProvider),
                headers);

        ResponseEntity<AuthenticationResult> response = restTemplate.exchange(
                authProvider.getTokenUri(),
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<AuthenticationResult>() {});

        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response.getStatusCode().value(), EXPECTED_STATUS_CODE);

        return response.getBody();
    }

    private MultiValueMap<String, String> createEncodedBody(AuthProperties.AuthProvider authProvider)
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
