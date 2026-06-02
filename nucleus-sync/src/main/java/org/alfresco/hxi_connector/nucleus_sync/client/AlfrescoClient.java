/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final boolean skipNotEnabled;
    private final MeterRegistry meterRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoClient.class);

    @Autowired
    public AlfrescoClient(
            AuthService authService,
            @Value("${alfresco.base-url}") String alfrescoBaseUrl,
            @Value("${http-client.timeout-minutes:5}") int timeoutInMins,
            @Value("${http-client.buffer-size-kilobytes:10240}") int bufferInKB,
            @Value("${alfresco.page-size:100}") int pageSize,
            @Value("${alfresco.user.skip-not-enabled:true}") boolean skipNotEnabled,
            MeterRegistry meterRegistry)
    {
        this(
                WebClient.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(bufferInKB * 1024))
                        .build(),
                new ObjectMapper(),
                authService,
                timeoutInMins,
                alfrescoBaseUrl,
                pageSize,
                skipNotEnabled,
                meterRegistry);
    }

    AlfrescoClient(
            WebClient webClient,
            ObjectMapper objectMapper,
            AuthService authService,
            int timeoutInMins,
            String alfrescoBaseUrl,
            int pageSize,
            boolean skipNotEnabled,
            MeterRegistry meterRegistry)
    {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.authService = authService;
        this.timeoutInMins = timeoutInMins;
        this.alfrescoBaseUrl = alfrescoBaseUrl;
        this.pageSize = pageSize;
        this.skipNotEnabled = skipNotEnabled;
        this.meterRegistry = meterRegistry;
    }

    public List<AlfrescoUser> getAllUsers()
    {
        List<AlfrescoUser> users = fetchAllPagedData(
                "/people",
                Map.of("fields", "id,email,enabled"),
                new TypeReference<AlfrescoPagedResponse<AlfrescoUser>>() {}, "getAllUsers");

        return this.skipNotEnabled
                ? users.stream()
                        .filter(AlfrescoUser::enabled)
                        .collect(Collectors.toList())
                : users;
    }

    public List<String> getUserGroups(String userId)
    {
        List<AlfrescoGroup> groups = fetchAllPagedData(
                "/people/" + userId + "/groups",
                Map.of("fields", "id"),
                new TypeReference<AlfrescoPagedResponse<AlfrescoGroup>>() {},
                "getUserGroups");
        return groups.stream().map(AlfrescoGroup::id).toList();
    }

    private <T> List<T> fetchAllPagedData(
            String basePath, Map<String, String> queryParams, TypeReference<AlfrescoPagedResponse<T>> typeRef, String operation)
    {
        try
        {
            List<T> results = new ArrayList<>();

            int skipCount = 0;
            boolean hasMoreItems = true;

            while (hasMoreItems)
            {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(basePath);
                queryParams.forEach(uriBuilder::queryParam);
                uriBuilder.queryParam("maxItems", pageSize);
                uriBuilder.queryParam("skipCount", skipCount);

                String response = makeAuthenticatedRequest(uriBuilder.toUriString())
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
                    .setMessage("Error fetching {} [op={}, method=GET]: {}")
                    .addArgument(operation)
                    .addArgument(operation)
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
            recordFailedRequest(operation, "GET", e);
            throw new ClientException("Failed to fetch " + operation, e);
        }
    }

    private void recordFailedRequest(String operation, String method, Throwable cause)
    {
        Counter.builder(NucleusSyncMetrices.AlfrescoMetrices.CONNECTION_ISSUE)
                .description(NucleusSyncMetrices.AlfrescoMetrices.CONNECTION_ISSUE_DESCRIPTION)
                .tag(NucleusSyncMetrices.Tags.OPERATION, operation)
                .tag(NucleusSyncMetrices.Tags.METHOD, method)
                .tag(NucleusSyncMetrices.Tags.ERROR_TYPE, ClientErrorClassifier.classify(cause))
                .register(meterRegistry)
                .increment();
    }

    @Retryable(retryFor = {EndpointServerErrorException.class,
                           WebClientResponseException.InternalServerError.class,
                           WebClientResponseException.ServiceUnavailable.class,
                           WebClientResponseException.GatewayTimeout.class,
                           WebClientRequestException.class},
            maxAttemptsExpression = "#{${http-client.retry.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.retry.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.retry.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.retry.max-delay-ms:10000}}"))
    public WebClient.ResponseSpec makeAuthenticatedRequest(String path)
    {
        Map<String, String> headers = authService.getAlfrescoAuthHeaders();

        return webClient
                .get()
                .uri(alfrescoBaseUrl + path)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve();
    }
}
