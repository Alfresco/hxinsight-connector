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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUsersOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupListOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipListOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusPagedResponse;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingListOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;

@Component
public class NucleusClient
{
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final String systemId;
    private final String nucleusBaseUrl;
    private final String idpBaseUrl;
    private final int pageSize;
    private final int timeoutInMin;
    private final int deleteBatchSize;
    private final MeterRegistry meterRegistry;

    private static final Logger LOGGER = LoggerFactory.getLogger(NucleusClient.class);

    @Autowired
    public NucleusClient(
            AuthService authService,
            @Value("${nucleus.system-id}") String systemId,
            @Value("${nucleus.base-url}") String nucleusBaseUrl,
            @Value("${nucleus.idp-base-url}") String idpBaseUrl,
            @Value("${nucleus.page-size:1000}") int pageSize,
            @Value("${nucleus.delete-group-member-batch-size:100}") int deleteBatchSize,
            @Value("${http-client.timeout-minutes:5}") int timeoutInMins,
            @Value("${http-client.buffer-size-kilobytes:10240}") int bufferInKB,
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
                systemId,
                nucleusBaseUrl,
                idpBaseUrl,
                pageSize,
                deleteBatchSize,
                timeoutInMins,
                meterRegistry);
    }

    NucleusClient(
            WebClient webClient,
            ObjectMapper objectMapper,
            AuthService authService,
            String systemId,
            String nucleusBaseUrl,
            String idpBaseUrl,
            int pageSize,
            int deleteBatchSize,
            int timeoutInMin,
            MeterRegistry meterRegistry)
    {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.authService = authService;
        this.systemId = systemId;
        this.nucleusBaseUrl = nucleusBaseUrl;
        this.idpBaseUrl = idpBaseUrl;
        this.pageSize = pageSize;
        this.timeoutInMin = timeoutInMin;
        this.deleteBatchSize = deleteBatchSize;
        this.meterRegistry = meterRegistry;
    }

    public List<IamUser> getAllIamUsers()
    {
        String op = "getAllIamUsers";
        String initialPath = "/api/users";

        try
        {
            return fetchAllPages(idpBaseUrl, initialPath, "users", IamUsersOutput.class);
        }
        catch (Exception e)
        {
            logAndRecord(op, "GET", "Error in retrieving iam users", e);
            throw new ClientException("Failed to retrieve iam users", e);
        }
    }

    public List<NucleusGroupOutput> getAllExternalGroups()
    {
        String op = "getAllExternalGroups";
        String initialPath = "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            return fetchAllPages(nucleusBaseUrl, initialPath, "groups", NucleusGroupListOutput.class);
        }
        catch (Exception e)
        {
            logAndRecord(op, "GET", "Error in retrieving groups", e);
            throw new ClientException("Failed to retrieve groups", e);
        }
    }

    public void createGroups(List<NucleusGroupInput> groups)
    {
        String op = "createGroups";
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(groups);

            executePostRequest(url, jsonBody);
        }
        catch (Exception e)
        {
            logAndRecord(op, "POST", "Error in creating groups in nucleus", e);
            throw new ClientException("Failed to create groups in nucleus", e);
        }
    }

    public List<NucleusUserMappingOutput> getCurrentUserMappings()
    {
        String op = "getCurrentUserMappings";
        String initialPath = "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            return fetchAllPages(nucleusBaseUrl, initialPath, "user mappings", NucleusUserMappingListOutput.class);
        }
        catch (Exception e)
        {
            logAndRecord(op, "GET", "Error in retrieving user mappings", e);
            throw new ClientException("Failed to retrieve user mappings", e);
        }
    }

    public void createUserMappings(List<NucleusUserMappingInput> userMappings)
    {
        String op = "createUserMappings";
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(userMappings);

            executePostRequest(url, jsonBody);
        }
        catch (Exception e)
        {
            logAndRecord(op, "POST", "Error in creating user mappings", e);
            throw new ClientException("Failed to create user mappings", e);
        }
    }

    public List<NucleusGroupMembershipOutput> getCurrentGroupMemberships()
    {
        String op = "getCurrentGroupMemberships";
        String initalPath = "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            return fetchAllPages(
                    nucleusBaseUrl,
                    initalPath,
                    "group memberships",
                    NucleusGroupMembershipListOutput.class);
        }
        catch (Exception e)
        {
            logAndRecord(op, "GET", "Error in retrieving group memberships", e);
            throw new ClientException("Failed to retrieve group memberships", e);
        }
    }

    public void assignGroupMembers(List<NucleusGroupMemberAssignmentInput> assignments)
    {
        String op = "assignGroupMembers";
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(assignments);

            executePostRequest(url, jsonBody);
        }
        catch (Exception e)
        {
            logAndRecord(op, "POST", "Error in creating member assignments to group", e);
            throw new ClientException("Error in creating member assignments to group", e);
        }
    }

    public void deleteGroup(String externalGroupId)
    {
        String op = "deleteGroup";
        try
        {
            String fullUrl = nucleusBaseUrl
                    + "/system-integrations/systems/"
                    + systemId
                    + "/groups/"
                    + externalGroupId;

            executeDeleteRequest(fullUrl);
        }
        catch (Exception e)
        {
            logAndRecord(op, "DELETE", "Error in deleting group", e);
            throw new ClientException("Failed to delete group", e);
        }
    }

    public void deleteUserMapping(String externalUserId)
    {
        String op = "deleteUserMapping";
        try
        {
            String fullUrl = nucleusBaseUrl
                    + "/system-integrations/systems/"
                    + systemId
                    + "/user-mappings/"
                    + externalUserId;

            executeDeleteRequest(fullUrl);
        }
        catch (Exception e)
        {
            logAndRecord(op, "DELETE", "Error in deleting user mapping", e);
            throw new ClientException("Failed to delete user mappings", e);
        }
    }

    public void removeGroupMembers(
            String parentExternalGroupId, List<String> memberExternalUserIds)
    {
        String op = "removeGroupMembers";
        try
        {
            for (int i = 0; i < memberExternalUserIds.size(); i += deleteBatchSize)
            {
                List<String> batch = memberExternalUserIds.subList(
                        i, Math.min(i + deleteBatchSize, memberExternalUserIds.size()));
                removeGroupMembersBatch(parentExternalGroupId, batch);
            }
        }
        catch (Exception e)
        {
            logAndRecord(op, "DELETE", "Error in removing group members", e);
            throw new ClientException("Failed to remove group members", e);
        }
    }

    private void removeGroupMembersBatch(String parentExternalGroupId, List<String> batch)
    {
        StringBuilder urlBuilder = new StringBuilder(nucleusBaseUrl)
                .append("/system-integrations/systems/")
                .append(systemId)
                .append("/group-members")
                .append("?parentExternalGroupId=")
                .append(parentExternalGroupId);

        // Add user IDs as query parameters
        for (String userId : batch)
        {
            urlBuilder.append("&memberExternalUserIds=")
                    .append(userId);
        }
        // Let exceptions propagate to removeGroupMembers() so that the failure
        // is logged & metered exactly once (no double-count, no double-wrap).
        executeDeleteRequest(urlBuilder.toString());
    }

    private <T, R extends NucleusPagedResponse<T>> List<T> fetchAllPages(
            String baseUrl,
            String initialPath,
            String context,
            Class<R> responseType) throws JsonProcessingException
    {
        List<T> allItems = new ArrayList<>();
        String nextPath = UriComponentsBuilder
                .fromUriString(initialPath)
                .queryParam("limit", pageSize)
                .toUriString();

        int pageCount = 0;

        while (nextPath != null)
        {
            LOGGER.atTrace()
                    .setMessage("Fetching page {} of nucleus {}")
                    .addArgument(++pageCount)
                    .addArgument(context)
                    .log();

            String fullUrl = baseUrl + nextPath;

            String response = executeGetRequest(fullUrl);
            R page = objectMapper.readValue(response, responseType);

            if (page.items() != null && !page.items().isEmpty())
            {
                allItems.addAll(page.items());
            }

            nextPath = page.next();
        }

        LOGGER.atDebug()
                .setMessage("Retrieved {} total {} across {} pages")
                .addArgument(allItems.size())
                .addArgument(context)
                .addArgument(pageCount)
                .log();

        return allItems;
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
    private String executeGetRequest(String fullUrl)
    {
        Map<String, String> headers = authService.getHxpAuthHeaders();

        return webClient
                .get()
                .uri(fullUrl)
                .headers(
                        httpHeaders -> {
                            headers.forEach(httpHeaders::set);
                            httpHeaders.set("Content-Type", "application/json");
                        })
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMin));
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
    private void executePostRequest(String fullUrl, String jsonBody)
    {
        Map<String, String> headers = authService.getHxpAuthHeaders();

        webClient
                .post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMin));
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
    private void executeDeleteRequest(String fullUrl)
    {
        Map<String, String> headers = authService.getHxpAuthHeaders();

        webClient
                .delete()
                .uri(fullUrl)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMin));
    }

    private void logAndRecord(String operation, String method, String message, Throwable e)
    {
        LOGGER.atError()
                .setMessage("{} [op={}, method={}]: {}")
                .addArgument(message)
                .addArgument(operation)
                .addArgument(method)
                .addArgument(e.getMessage())
                .setCause(e)
                .log();
        recordFailedRequest(operation, method, e);
    }

    private void recordFailedRequest(String operation, String method, Throwable cause)
    {
        Counter.builder(NucleusSyncMetrices.NucleusClientMetrics.CONNECTION_ISSUE)
                .description(NucleusSyncMetrices.NucleusClientMetrics.CONNECTION_ISSUE_DESCRIPTION)
                .tag(NucleusSyncMetrices.Tags.OPERATION, operation)
                .tag(NucleusSyncMetrices.Tags.METHOD, method)
                .tag(NucleusSyncMetrices.Tags.ERROR_TYPE, ClientErrorClassifier.classify(cause))
                .register(meterRegistry)
                .increment();
    }
}
