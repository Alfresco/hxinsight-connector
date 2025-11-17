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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoGroup;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;

public class AlfrescoClientIntegrationTest
{
    private WireMockServer wireMockServer;
    private AlfrescoClient alfrescoClient;

    @BeforeEach
    void setUp()
    {
        // Start WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        // Create mock AuthService
        AuthService authService = new AuthService(null, null) {
            @Override
            public Map<String, String> getAlfrescoAuthHeaders()
            {
                return Map.of("Authorization", "Bearer test-token");
            }
        };

        // Create client with WireMock URL
        String baseUrl = "http://localhost:" + wireMockServer.port() + "/alfresco/api/-default-/public/alfresco/versions/1";
        alfrescoClient = new AlfrescoClient(
                WebClient.builder().build(),
                new ObjectMapper(),
                authService,
                5,
                baseUrl,
                100);
    }

    @AfterEach
    void tearDown()
    {
        wireMockServer.stop();
    }

    @Test
    public void testGetAllUsers_SinglePage_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "list": {
                    "pagination": {
                      "count": 2,
                      "hasMoreItems": false,
                      "totalItems": 2,
                      "skipCount": 0,
                      "maxItems": 100
                    },
                    "entries": [
                      {
                        "entry": {
                          "id": "jdoe",
                          "firstName": "John",
                          "lastName": "Doe",
                          "email": "john.doe@example.com",
                          "enabled": true
                        }
                      },
                      {
                        "entry": {
                          "id": "jsmith",
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "email": "jane.smith@example.com",
                          "enabled": true
                        }
                      }
                    ]
                  }
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0"))
                .withHeader("Authorization", equalTo("Bearer test-token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<AlfrescoUser> users = alfrescoClient.getAllUsers();

        // Assert
        assertThat(users)
                .hasSize(2)
                .extracting(AlfrescoUser::getId, AlfrescoUser::getFirstName, AlfrescoUser::getEmail)
                .containsExactly(
                        tuple("jdoe", "John", "john.doe@example.com"),
                        tuple("jsmith", "Jane", "jane.smith@example.com"));

        // Verify the request was made
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0")));
    }

    @Test
    void testGetAllGroups_Success()
    {
        // Arrange
        String responseBody = """
                {
                  "list": {
                    "pagination": {
                      "count": 2,
                      "hasMoreItems": false,
                      "totalItems": 2,
                      "skipCount": 0,
                      "maxItems": 100
                    },
                    "entries": [
                      {
                        "entry": {
                          "id": "GROUP_ADMINS",
                          "displayName": "Administrators",
                          "isRoot": true
                        }
                      },
                      {
                        "entry": {
                          "id": "GROUP_DEVS",
                          "displayName": "Developers",
                          "isRoot": false
                        }
                      }
                    ]
                  }
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/groups"))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<AlfrescoGroup> groups = alfrescoClient.getAllGroups();

        // Assert
        assertThat(groups)
                .hasSize(2)
                .extracting(AlfrescoGroup::getId, AlfrescoGroup::getDisplayName)
                .containsExactly(
                        tuple("GROUP_ADMINS", "Administrators"),
                        tuple("GROUP_DEVS", "Developers"));
    }

    @Test
    void testGetUserGroups_Success()
    {
        // Arrange
        String userId = "jdoe";
        String responseBody = """
                {
                  "list": {
                    "pagination": {
                      "count": 2,
                      "hasMoreItems": false,
                      "totalItems": 2,
                      "skipCount": 0,
                      "maxItems": 100
                    },
                    "entries": [
                      {
                        "entry": {
                          "id": "GROUP_ADMINS",
                          "displayName": "Administrators",
                          "isRoot": true
                        }
                      },
                      {
                        "entry": {
                          "id": "GROUP_DEVS",
                          "displayName": "Developers",
                          "isRoot": true
                        }
                      }
                    ]
                  }
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people/" + userId + "/groups"))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        // Act
        List<String> groupIds = alfrescoClient.getUserGroups(userId);

        // Assert
        assertThat(groupIds)
                .hasSize(2)
                .containsExactlyInAnyOrder("GROUP_ADMINS", "GROUP_DEVS");
    }

    @Test
    void testGetAllUsers_ApiError_ThrowsException()
    {
        // Arrange
        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Act & Assert
        assertThatThrownBy(alfrescoClient::getAllUsers)
                .isInstanceOf(ClientException.class)
                .hasMessageContaining("Failed to fetch users");
    }

    @Test
    void testGetAllUsers_LargePagination_HandlesCorrectly()
    {
        // Arrange - Simulate large dataset with realistic pagination
        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .withQueryParam("skipCount", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createPagedResponse(0, 100, true, 300))));

        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .withQueryParam("skipCount", equalTo("100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createPagedResponse(100, 100, true, 300))));

        wireMockServer.stubFor(get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                .withQueryParam("skipCount", equalTo("200"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(createPagedResponse(200, 100, false, 300))));

        // Act
        List<AlfrescoUser> users = alfrescoClient.getAllUsers();

        // Assert
        assertThat(users).hasSize(300);

        // Verify correct number of requests
        wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people")));
    }

    private String createPagedResponse(int skipCount, int count, boolean hasMoreItems, int totalItems)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"list\":{")
                .append("\"pagination\":{")
                .append("\"count\":").append(count).append(",")
                .append("\"hasMoreItems\":").append(hasMoreItems).append(",")
                .append("\"totalItems\":").append(totalItems).append(",")
                .append("\"skipCount\":").append(skipCount).append(",")
                .append("\"maxItems\":100")
                .append("},")
                .append("\"entries\":[");

        for (int i = 0; i < count; i++)
        {
            int userId = skipCount + i;
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append("{\"entry\":{")
                    .append("\"id\":\"user").append(userId).append("\",")
                    .append("\"firstName\":\"User\",")
                    .append("\"lastName\":\"").append(userId).append("\",")
                    .append("\"email\":\"user").append(userId).append("@example.com\",")
                    .append("\"enabled\":true")
                    .append("}}");
        }

        sb.append("]}}");
        return sb.toString();
    }
}
