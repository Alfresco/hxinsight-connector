/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.nucleus_sync.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoGroup;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoPagedResponse;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;

@Component
public class AlfrescoClient
{
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final String alfrescoBaseUrl;
    private final int timeoutInMins;
    private final int pageSize;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoClient.class);

    @Autowired
    public AlfrescoClient(
            AuthService authService,
            @Value("${alfresco.base-url}") String alfrescoBaseUrl,
            @Value("${http-client.timeout-minutes:5}") int timeoutInMins,
            @Value("${alfresco.page-size:100}") int pageSize)
    {
        this(WebClient.builder().build(),
                new ObjectMapper(),
                authService,
                timeoutInMins,
                alfrescoBaseUrl,
                pageSize);
    }

    AlfrescoClient(
            WebClient webClient,
            ObjectMapper objectMapper,
            AuthService authService,
            int timeoutInMins,
            String alfrescoBaseUrl,
            int pageSize)
    {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.authService = authService;
        this.timeoutInMins = timeoutInMins;
        this.alfrescoBaseUrl = alfrescoBaseUrl;
        this.pageSize = pageSize;
    }

    public List<AlfrescoUser> getAllUsers()
    {
        return fetchAllPagedData(
                "/people", new TypeReference<AlfrescoPagedResponse<AlfrescoUser>>() {}, "users");
    }

    public List<AlfrescoGroup> getAllGroups()
    {
        return fetchAllPagedData(
                "/groups", new TypeReference<AlfrescoPagedResponse<AlfrescoGroup>>() {}, "groups");
    }

    public List<String> getUserGroups(String userId)
    {
        List<AlfrescoGroup> groups = fetchAllPagedData(
                "/people/" + userId + "/groups",
                new TypeReference<AlfrescoPagedResponse<AlfrescoGroup>>() {},
                "groups for user " + userId);
        return groups.stream().map(AlfrescoGroup::id).toList();
    }

    private <T> List<T> fetchAllPagedData(
            String basePath, TypeReference<AlfrescoPagedResponse<T>> typeRef, String errorContext)
    {
        try
        {
            List<T> results = new ArrayList<>();

            int skipCount = 0;
            boolean hasMoreItems = true;

            while (hasMoreItems)
            {
                String response = makeAuthenticatedRequest(
                        basePath + "?maxItems=" + pageSize + "&skipCount=" + skipCount)
                                .bodyToMono(String.class)
                                .block(Duration.ofMinutes(timeoutInMins));

                AlfrescoPagedResponse<T> pagedResponse = objectMapper.readValue(response, typeRef);

                if (pagedResponse.getList() != null
                        && pagedResponse.getList().entries() != null)
                {
                    pagedResponse.getList().entries().stream()
                            .map(AlfrescoPagedResponse.EntryWrapper::entry)
                            .filter(entry -> entry != null)
                            .forEach(results::add);
                }

                AlfrescoPagedResponse.Pagination pagination = pagedResponse.getList().pagination();

                if (pagination != null)
                {
                    hasMoreItems = pagination.hasMoreItems();
                    skipCount += pagination.count();
                }
                else
                {
                    hasMoreItems = false;
                }
            }

            return results;
        }
        catch (Exception e)
        {
            LOGGER.atError()
                    .setMessage("Error fetching {}: {}")
                    .addArgument(errorContext)
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();

            throw new ClientException("Failed to fetch " + errorContext, e);
        }
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{${http-client.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.max-delay-ms:10000}}"))
    private WebClient.ResponseSpec makeAuthenticatedRequest(String path)
    {
        Map<String, String> headers = authService.getAlfrescoAuthHeaders();

        return webClient
                .get()
                .uri(alfrescoBaseUrl + path)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve();
    }
}
