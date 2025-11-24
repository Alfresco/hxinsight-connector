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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final int timeoutInMin;

    private static final Logger LOGGER = LoggerFactory.getLogger(NucleusClient.class);

    @Autowired
    public NucleusClient(
            AuthService authService,
            @Value("${nucleus.system-id}") String systemId,
            @Value("${nucleus.base-url}") String nucleusBaseUrl,
            @Value("${nucleus.idp-base-url}") String idpBaseUrl,
            @Value("${http-client.timeout-minutes:5}") int timeoutInMins)
    {

        this(
                WebClient.builder().build(),
                new ObjectMapper(),
                authService,
                systemId,
                nucleusBaseUrl,
                idpBaseUrl,
                timeoutInMins);
    }

    NucleusClient(
            WebClient webClient,
            ObjectMapper objectMapper,
            AuthService authService,
            String systemId,
            String nucleusBaseUrl,
            String idpBaseUrl,
            int timeoutInMin)
    {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.authService = authService;
        this.systemId = systemId;
        this.nucleusBaseUrl = nucleusBaseUrl;
        this.idpBaseUrl = idpBaseUrl;
        this.timeoutInMin = timeoutInMin;
    }

    public List<IamUser> getAllIamUsers()
    {
        String url = idpBaseUrl + "/api/users";

        try
        {
            String response = executeGetRequest(url);

            IamUsersOutput iamUsersOutput = objectMapper.readValue(response, IamUsersOutput.class);

            return iamUsersOutput.getUsers();
        }
        catch (Exception e)
        {
            LOGGER.error("Error in retrieving iam users: {}", e.getMessage(), e);
            throw new ClientException("Failed to retrieve iam users", e);
        }
    }

    public List<NucleusGroupOutput> getAllExternalGroups()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            String response = executeGetRequest(url);

            NucleusGroupListOutput groupsOutput = objectMapper.readValue(response, NucleusGroupListOutput.class);

            return groupsOutput.getItems() != null ? groupsOutput.getItems() : List.of();
        }
        catch (Exception e)
        {
            LOGGER.error("Error in retrieving groups: {}", e.getMessage(), e);
            throw new ClientException("Failed to retrieve groups", e);
        }
    }

    public void createGroups(List<NucleusGroupInput> groups)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(groups);

            executePostRequest(url, jsonBody);

        }
        catch (Exception e)
        {
            LOGGER.error("Error in creating groups in nucleus: {}", e.getMessage(), e);
            throw new ClientException("Failed to create groups in nucleus", e);
        }
    }

    public List<NucleusUserMappingOutput> getCurrentUserMappings()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            String response = executeGetRequest(url);

            NucleusUserMappingListOutput userMappingsOutput = objectMapper.readValue(response, NucleusUserMappingListOutput.class);

            return userMappingsOutput.getItems() != null
                    ? userMappingsOutput.getItems()
                    : List.of();

        }
        catch (Exception e)
        {
            LOGGER.error("Error in retrieving user mappings: {}", e.getMessage(), e);
            throw new ClientException("Failed to retrieve user mappings", e);
        }
    }

    public void createUserMappings(List<NucleusUserMappingInput> userMappings)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(userMappings);

            executePostRequest(url, jsonBody);

        }
        catch (Exception e)
        {
            LOGGER.error("Error in creating user mappings: {}", e.getMessage(), e);
            throw new ClientException("Failed to create user mappings", e);
        }
    }

    public List<NucleusGroupMembershipOutput> getCurrentGroupMemberships()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            String response = executeGetRequest(url);

            NucleusGroupMembershipListOutput groupMembershipOutput = objectMapper.readValue(response, NucleusGroupMembershipListOutput.class);

            return groupMembershipOutput.getItems() != null
                    ? groupMembershipOutput.getItems()
                    : List.of();

        }
        catch (Exception e)
        {
            LOGGER.error("Error in retrieving group memberships: {}", e.getMessage(), e);
            throw new ClientException("Failed to retrieve group memberships", e);
        }
    }

    public void assignGroupMembers(List<NucleusGroupMemberAssignmentInput> assignments)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(assignments);

            executePostRequest(url, jsonBody);

        }
        catch (Exception e)
        {
            LOGGER.error("Error in creating member assignments to group: {}", e.getMessage(), e);
            throw new ClientException("Error in creating member assignments to group", e);
        }
    }

    public void deleteGroup(String externalGroupId)
    {
        try
        {
            String fullUrl = nucleusBaseUrl
                    + "/system-integrations/systems/"
                    + systemId
                    + "/groups/"
                    + URLEncoder.encode(externalGroupId, StandardCharsets.UTF_8);

            executeDeleteRequest(fullUrl);

        }
        catch (Exception e)
        {
            LOGGER.error("Error in deleting group: {}", e.getMessage(), e);
            throw new ClientException("Failed to delete group", e);
        }
    }

    public void deleteUserMapping(String externalUserId)
    {
        try
        {
            String fullUrl = nucleusBaseUrl
                    + "/system-integrations/systems/"
                    + systemId
                    + "/user-mappings/"
                    + URLEncoder.encode(externalUserId, StandardCharsets.UTF_8);

            executeDeleteRequest(fullUrl);
        }
        catch (Exception e)
        {
            LOGGER.error("Error in deleting user mappings: {}", e.getMessage(), e);
            throw new ClientException("Failed to delete user mappings", e);
        }
    }

    public void removeGroupMembers(
            String parentExternalGroupId, List<String> memberExternalUserIds)
    {
        try
        {
            StringBuilder urlBuilder = new StringBuilder(nucleusBaseUrl)
                    .append("/system-integrations/systems/")
                    .append(systemId)
                    .append("/group-members")
                    .append("?parentExternalGroupId=")
                    .append(URLEncoder.encode(parentExternalGroupId, StandardCharsets.UTF_8));

            // Add user IDs as query parameters
            for (String userId : memberExternalUserIds)
            {
                urlBuilder.append("&memberExternalUserIds=")
                        .append(URLEncoder.encode(userId, StandardCharsets.UTF_8));
            }

            executeDeleteRequest(urlBuilder.toString());

        }
        catch (Exception e)
        {
            LOGGER.error("Error removing group members: {}", e.getMessage(), e);
            throw new ClientException("Failed to remove group members", e);
        }
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{${http-client.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.max-delay-ms:10000}}"))
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

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{${http-client.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.max-delay-ms:10000}}"))
    private String executePostRequest(String fullUrl, String jsonBody)
    {
        Map<String, String> headers = authService.getHxpAuthHeaders();

        return webClient
                .post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMin));
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{${http-client.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.max-delay-ms:10000}}"))
    private String executeDeleteRequest(String fullUrl)
    {
        Map<String, String> headers = authService.getHxpAuthHeaders();

        return webClient
                .delete()
                .uri(fullUrl)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMin));
    }
}
