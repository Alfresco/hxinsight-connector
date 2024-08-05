/*-
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
package org.alfresco.hxi_connector.common.adapters.messaging.repository.api;

import static org.apache.hc.core5.http.HttpStatus.SC_OK;

import static org.alfresco.hxi_connector.common.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.util.ErrorUtils;

@RequiredArgsConstructor
@Slf4j
public class DiscoveryApiClient
{

    private final String discoveryEndpoint;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newHttpClient();

    public String getRepositoryVersion()
    {
        log.atDebug().log("Sending repository discovery API request to: {}", discoveryEndpoint);
        try
        {
            return getDiscoverApiResponse().getFullVersion();
        }
        catch (IOException | InterruptedException e)
        {
            Set<Class<? extends Throwable>> retryReasons = Set.of(IOException.class, InterruptedException.class);
            throw ErrorUtils.wrapErrorIfNecessary(e, retryReasons);
        }
    }

    private DiscoveryApiResponse getDiscoverApiResponse() throws IOException, InterruptedException
    {
        Map.Entry<String, String> authHeader = authService.getAuthHeader(AuthService.ALFRESCO_AUTH_PROVIDER);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(discoveryEndpoint))
                .header(authHeader.getKey(), authHeader.getValue())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        throwExceptionOnUnexpectedStatusCode(response.statusCode(), SC_OK);

        DiscoveryApiResponse discoveryApiResponse = objectMapper.readValue(response.body(), DiscoveryApiResponse.class);
        log.atTrace().log("Discovery API response: {}", discoveryApiResponse);
        return discoveryApiResponse;
    }
}
