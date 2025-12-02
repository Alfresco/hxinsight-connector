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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;

public class NucleusClientIntegrationTest
{
    private WireMockServer wireMockServer;
    private NucleusClient nucleusClient;

    private static final String SYSTEM_ID = "test-system-id";

    @BeforeEach
    void setUp()
    {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        // Create mock AuthService
        AuthService authService = new AuthService(null, null) {
            @Override
            public Map<String, String> getHxpAuthHeaders()
            {
                return Map.of("Authorization", "Bearer nucleus-token");
            }
        };

        // Create client with WireMock URL
        String nucleusBaseUrl = "http://localhost:" + wireMockServer.port();
        String idpBaseUrl = "http://localhost:" + wireMockServer.port();

        nucleusClient = new NucleusClient(
                WebClient.builder().build(),
                new ObjectMapper(),
                authService,
                SYSTEM_ID,
                nucleusBaseUrl,
                idpBaseUrl,
                50,
                5);
    }

    @AfterEach
    void tearDown()
    {
        wireMockServer.stop();
    }

    @Test
    void testGetAllIamUsers_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "users": [
                    {
                      "userName": "Jane.Doe+cin@hyland.com",
                      "userId": "714e1cbd-d88a-4fd1-a491-be438f7a2233",
                      "email": "Jane.Doe+cin@hyland.com",
                      "preferredLanguage": "en-US"
                    },
                    {
                      "userName": "moliver",
                      "userId": "34f8e319-6a8f-4359-a5b1-d26a04a4abc0",
                      "email": "Michale.Oliver+cin@hyland.com",
                      "preferredLanguage": "en-US"
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlEqualTo("/api/users"))
                .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<IamUser> users = nucleusClient.getAllIamUsers();

        // Assert
        assertThat(users).containsExactlyInAnyOrder(
                new IamUser("Jane.Doe+cin@hyland.com", "714e1cbd-d88a-4fd1-a491-be438f7a2233",
                        "Jane.Doe+cin@hyland.com"),
                new IamUser("moliver", "34f8e319-6a8f-4359-a5b1-d26a04a4abc0",
                        "Michale.Oliver+cin@hyland.com"));
    }

    @Test
    void testGetAllExternalGroups_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "items": [
                    {
                      "externalGroupId": "GROUP_HR",
                      "attributes": []
                    },
                    {
                      "externalGroupId": "GROUP_MARKETING",
                      "attributes": []
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlEqualTo("/system-integrations/systems/" + SYSTEM_ID + "/groups"))
                .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<NucleusGroupOutput> groups = nucleusClient.getAllExternalGroups();

        // Assert
        assertThat(groups).containsExactlyInAnyOrder(
                new NucleusGroupOutput("GROUP_HR"),
                new NucleusGroupOutput("GROUP_MARKETING"));
    }

    @Test
    void testGetCurrentUserMappings_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "items": [
                    {
                      "userId": "18a17e9d-dbb1-4643-a2e7-3e1859961f5b",
                      "externalUserId": "jdoe",
                      "attributes": []
                    },
                    {
                      "userId": "24bd547d-58b3-4722-889a-84e68c41615b",
                      "externalUserId": "moliver",
                      "attributes": []
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlEqualTo("/system-integrations/systems/" + SYSTEM_ID + "/user-mappings"))
                .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<NucleusUserMappingOutput> mappings = nucleusClient.getCurrentUserMappings();

        // Assert
        assertThat(mappings).containsExactlyInAnyOrder(
                new NucleusUserMappingOutput("18a17e9d-dbb1-4643-a2e7-3e1859961f5b", "jdoe"),
                new NucleusUserMappingOutput("24bd547d-58b3-4722-889a-84e68c41615b", "moliver"));
    }

    @Test
    void testGetCurrentGroupMemberships_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "items": [
                    {
                      "externalGroupId": "GROUP_Frontend_Team",
                      "memberExternalUserId": "jdoe"
                    },
                    {
                      "externalGroupId": "GROUP_Frontend_Team",
                      "memberExternalUserId": "moliver"
                    },
                    {
                      "externalGroupId": "GROUP_Backend_Team",
                      "memberExternalUserId": "aturing"
                    }
                  ]
                }
                """;

        wireMockServer.stubFor(get(urlEqualTo("/system-integrations/systems/" + SYSTEM_ID + "/group-members"))
                .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<NucleusGroupMembershipOutput> memberships = nucleusClient.getCurrentGroupMemberships();

        // Assert
        assertThat(memberships).containsExactlyInAnyOrder(
                new NucleusGroupMembershipOutput("GROUP_Frontend_Team", "jdoe"),
                new NucleusGroupMembershipOutput("GROUP_Frontend_Team", "moliver"),
                new NucleusGroupMembershipOutput("GROUP_Backend_Team", "aturing"));
    }

    @Test
    void testDeleteGroup_Success()
    {
        // Arrange
        String externalGroupId = "GROUP_ADMINS";

        wireMockServer.stubFor(delete(
                urlEqualTo("/system-integrations/systems/" + SYSTEM_ID + "/groups/" + externalGroupId))
                        .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                        .willReturn(aResponse()
                                .withStatus(204)));

        // Act
        assertDoesNotThrow(() -> nucleusClient.deleteGroup(externalGroupId));

        // Assert
        wireMockServer.verify(1, deleteRequestedFor(urlEqualTo(
                "/system-integrations/systems/" + SYSTEM_ID + "/groups/" + externalGroupId)));
    }

    @Test
    void testDeleteUserMapping_Success()
    {
        // Arrange
        String externalUserId = "aturing";

        wireMockServer.stubFor(delete(urlEqualTo(
                "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings/" + externalUserId))
                        .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                        .willReturn(aResponse()
                                .withStatus(204)));

        // Act
        assertDoesNotThrow(() -> nucleusClient.deleteUserMapping(externalUserId));

        // Assert
        wireMockServer.verify(1, deleteRequestedFor(urlEqualTo(
                "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings/" + externalUserId)));
    }

    @Test
    void testDeleteUserMapping_WithSpecialCharacters_EncodesUrl()
    {
        // Arrange
        String externalUserId = "user+cin@example.com";

        wireMockServer.stubFor(
                delete(urlMatching("/system-integrations/systems/" + SYSTEM_ID + "/user-mappings/.*"))
                        .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                        .willReturn(aResponse()
                                .withStatus(204)));

        // Act
        assertDoesNotThrow(() -> nucleusClient.deleteUserMapping(externalUserId));

        // Assert
        wireMockServer.verify(1, deleteRequestedFor(urlPathEqualTo("/system-integrations/systems/" + SYSTEM_ID
                + "/user-mappings/user%252Bcin%2540example.com")));
    }

    @Test
    void testRemoveGroupMembers_MultipleUsers_Success()
    {
        // Arrange
        String parentGroupId = "GROUP_HR";
        List<String> memberUserIds = List.of("jdoe", "moliver", "aturing");

        wireMockServer.stubFor(
                delete(urlMatching("/system-integrations/systems/" + SYSTEM_ID + "/group-members\\?.*"))
                        .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                        .willReturn(aResponse()
                                .withStatus(204)));

        // Act
        assertDoesNotThrow(() -> nucleusClient.removeGroupMembers(parentGroupId, memberUserIds));

        // Assert
        wireMockServer.verify(1,
                deleteRequestedFor(urlPathMatching(
                        "/system-integrations/systems/" + SYSTEM_ID + "/group-members"))
                                .withQueryParam("parentExternalGroupId", equalTo("GROUP_HR"))
                                .withQueryParam("memberExternalUserIds", equalTo("jdoe"))
                                .withQueryParam("memberExternalUserIds", equalTo("moliver"))
                                .withQueryParam("memberExternalUserIds", equalTo("aturing")));
    }

    @Test
    void testRemoveGroupMembers_ExceedsBatchSize_MakesMultipleRequests()
    {
        // Arrange
        String parentGroupId = "GROUP_HR";
        // Since deleteBatchSize = 50, create 75 users to trigger 2 batches
        List<String> memberUserIds = IntStream.range(0, 75)
                .mapToObj(i -> "user" + i)
                .collect(Collectors.toList());

        wireMockServer.stubFor(
                delete(urlMatching("/system-integrations/systems/" + SYSTEM_ID + "/group-members\\?.*"))
                        .withHeader("Authorization", equalTo("Bearer nucleus-token"))
                        .willReturn(aResponse()
                                .withStatus(204)));

        // Act
        assertDoesNotThrow(() -> nucleusClient.removeGroupMembers(parentGroupId, memberUserIds));

        // Assert - Should make 2 requests (50 + 25)
        wireMockServer.verify(2,
                deleteRequestedFor(urlPathMatching(
                        "/system-integrations/systems/" + SYSTEM_ID + "/group-members")));
    }

    @Test
    void testCreateGroups_Error_ThrowsException()
    {
        // Arrange
        NucleusGroupInput group = new NucleusGroupInput("GROUP_HR");
        List<NucleusGroupInput> groups = List.of(group);

        wireMockServer.stubFor(post(urlEqualTo("/system-integrations/systems/" + SYSTEM_ID + "/groups"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Bad Request")));

        // Act & Assert
        assertThatThrownBy(() -> nucleusClient.createGroups(groups))
                .isInstanceOf(ClientException.class)
                .hasMessageContaining("Failed to create groups in nucleus");
    }
}
