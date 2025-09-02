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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
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
    private static final Logger logger = LoggerFactory.getLogger(NucleusClient.class);

    @Value("${nucleus.system-id}")
    private String systemId;

    @Value("${nucleus.base-url}")
    private String nucleusBaseUrl;

    @Value("${nucleus.idp-base-url}")
    private String idpBaseUrl;

    private int timeoutInMin = 5;

    public NucleusClient(AuthService authService)
    {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<IamUser> getAllIamUsers()
    {
        String url = idpBaseUrl + "/api/users";

        try
        {
            String response = makeAuthenticatedRequest("GET", url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

            IamUsersOutput iamUsersOutput = objectMapper.readValue(response, IamUsersOutput.class);

            return iamUsersOutput.getUsers();
        }
        catch (Exception e)
        {
            logger.error("Error in retrieving iam users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve iam users", e);
        }
    }

    public List<NucleusGroupOutput> getAllExternalGroups()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            String response = makeAuthenticatedRequest("GET", url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

            NucleusGroupListOutput groupsOutput = objectMapper.readValue(response, NucleusGroupListOutput.class);

            return groupsOutput.getItems() != null ? groupsOutput.getItems() : List.of();
        }
        catch (Exception e)
        {
            logger.error("Error in retrieving groups: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve groups", e);
        }
    }

    public void createGroups(List<NucleusGroupInput> groups)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/groups";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(groups);

            ((WebClient.RequestBodySpec) makeAuthenticatedRequest("POST", url))
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

        }
        catch (Exception e)
        {
            logger.error("Error in creating groups in nucleus: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create groups in nucleus", e);
        }
    }

    public List<NucleusUserMappingOutput> getCurrentUserMappings()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            String response = makeAuthenticatedRequest("GET", url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

            NucleusUserMappingListOutput userMappingsOutput = objectMapper.readValue(response, NucleusUserMappingListOutput.class);

            return userMappingsOutput.getItems() != null
                    ? userMappingsOutput.getItems()
                    : List.of();

        }
        catch (Exception e)
        {
            logger.error("Error in retrieving user mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user mappings", e);
        }
    }

    public void createUserMappings(List<NucleusUserMappingInput> userMappings)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/user-mappings";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(userMappings);

            ((WebClient.RequestBodySpec) makeAuthenticatedRequest("POST", url))
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

        }
        catch (Exception e)
        {
            logger.error("Error in creating user mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user mappings", e);
        }
    }

    public List<NucleusGroupMembershipOutput> getCurrentGroupMemberships()
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            String response = makeAuthenticatedRequest("GET", url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

            NucleusGroupMembershipListOutput groupMembershipOutput = objectMapper.readValue(response, NucleusGroupMembershipListOutput.class);

            return groupMembershipOutput.getItems() != null
                    ? groupMembershipOutput.getItems()
                    : List.of();

        }
        catch (Exception e)
        {
            logger.error("Error in retrieving group memberships: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve group memberships", e);
        }
    }

    public void assignGroupMembers(List<NucleusGroupMemberAssignmentInput> assignments)
    {
        String url = nucleusBaseUrl + "/system-integrations/systems/" + systemId + "/group-members";

        try
        {
            String jsonBody = objectMapper.writeValueAsString(assignments);

            ((WebClient.RequestBodySpec) makeAuthenticatedRequest("POST", url))
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

        }
        catch (Exception e)
        {
            logger.error("Error in creating member assignments to group: {}", e.getMessage(), e);
            throw new RuntimeException("Error in creating member assignments to group", e);
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
                    + externalGroupId;

            makeAuthenticatedRequest("DELETE", fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));
        }
        catch (Exception e)
        {
            logger.error("Error in deleting group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete group", e);
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
                    + externalUserId;

            makeAuthenticatedRequest("DELETE", fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));
        }
        catch (Exception e)
        {
            logger.error("Error in deleting user mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user mappings", e);
        }
    }

    public void removeGroupMembers(
            String parentExternalGroupId, List<String> memberExternalUserIds)
    {
        try
        {
            String url = nucleusBaseUrl
                    + "/system-integrations/systems/"
                    + systemId
                    + "/group-members"
                    + "?parentExternalGroupId="
                    + parentExternalGroupId;

            // Add user IDs as query parameters
            for (String userId : memberExternalUserIds)
            {
                url += "&memberExternalUserIds=" + userId;
            }

            makeAuthenticatedRequest("DELETE", url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofMinutes(timeoutInMin));

        }
        catch (Exception e)
        {
            logger.error("Error removing group members: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove group members", e);
        }
    }

    private WebClient.RequestHeadersSpec<?> makeAuthenticatedRequest(
            String method, String fullUrl)
    {

        Map<String, String> headers = authService.getHxpAuthHeaders();

        WebClient.RequestHeadersUriSpec<?> request;

        if ("POST".equalsIgnoreCase(method))
        {
            request = webClient.post();
        }
        else if ("PUT".equalsIgnoreCase(method))
        {
            request = webClient.put();
        }
        else if ("DELETE".equalsIgnoreCase(method))
        {
            request = webClient.delete();
        }
        else if ("GET".equalsIgnoreCase(method))
        {
            request = webClient.get();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return request.uri(fullUrl)
                .headers(
                        httpHeaders -> {
                            headers.forEach(httpHeaders::set);
                            httpHeaders.set("Content-Type", "application/json");
                        });
    }
}
