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
package org.alfresco.hxi_connector.nucleus_sync.services.orchestration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserSyncFlowIntegrationTest
{
    private static final WireMockServer NUCLEUS_MOCK = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final WireMockServer ALFRESCO_MOCK = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    private static final String SYSTEM_ID = "sync-system";
    private static final String PEOPLE_ENDPOINT = "/alfresco/api/-default-/public/alfresco/versions/1/people";
    private static final String BASIC_ADMIN = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
    private static final String ACCESS_TOKEN = "stub-access-token";

    static
    {
        NUCLEUS_MOCK.start();
        ALFRESCO_MOCK.start();
    }

    @Autowired
    private SyncOrchestrationService syncOrchestrationService;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry)
    {
        registry.add("sync.enabled", () -> "false");

        registry.add("alfresco.base-url", UserSyncFlowIntegrationTest::alfrescoBaseUrl);
        registry.add("alfresco.page-size", () -> 100);
        registry.add("alfresco.sync-batch-size", () -> 100);
        registry.add("alfresco.user-group.fetch-timeout", () -> "PT5S");

        registry.add("nucleus.base-url", UserSyncFlowIntegrationTest::nucleusBaseUrl);
        registry.add("nucleus.idp-base-url", UserSyncFlowIntegrationTest::nucleusBaseUrl);
        registry.add("nucleus.system-id", () -> SYSTEM_ID);
        registry.add("nucleus.page-size", () -> 100);
        registry.add("nucleus.delete-group-member-batch-size", () -> 25);

        registry.add("auth.providers.alfresco.type", () -> "Basic");
        registry.add("auth.providers.alfresco.username", () -> "admin");
        registry.add("auth.providers.alfresco.password", () -> "admin");

        registry.add("auth.providers.hyland-experience.client-id", () -> "client-id");
        registry.add("auth.providers.hyland-experience.client-secret", () -> "client-secret");
        registry.add("auth.providers.hyland-experience.client-name", () -> "hx-test");
        registry.add("auth.providers.hyland-experience.environment-key", () -> "test-env");
        registry.add("auth.providers.hyland-experience.type", () -> "oauth2");
        registry.add("auth.providers.hyland-experience.grant-type", () -> "client_credentials");
        registry.add("auth.providers.hyland-experience.scope[0]", () -> "iam.user-data.account");
        registry.add("auth.providers.hyland-experience.scope[1]", () -> "system-integrations-config");
        registry.add("auth.providers.hyland-experience.scope[2]", () -> "system-integrations-runtime");
        registry.add("auth.providers.hyland-experience.scope[3]", () -> "environment_authorization");
        registry.add("auth.providers.hyland-experience.token-uri", () -> nucleusBaseUrl() + "/token");

        registry.add("auth.retry.attempts", () -> 1);
        registry.add("auth.retry.initial-delay", () -> 0);
        registry.add("auth.retry.delay-multiplier", () -> 1.0);
    }

    @BeforeEach
    void resetServers()
    {
      NUCLEUS_MOCK.resetAll();
      ALFRESCO_MOCK.resetAll();
    }

    @AfterAll
    static void stopServers()
    {
        NUCLEUS_MOCK.stop();
        ALFRESCO_MOCK.stop();
    }

    @Test
    void shouldSyncUsersGroupsAndMemberships()
    {
        stubTokenEndpoint();
        stubAlfrescoUsers();
        stubGroupsFor("asmith", groupResponse("GROUP_HR", "GROUP_ENGINEERING"));
        stubGroupsFor("bjones", groupResponse("GROUP_HR"));
        stubIamUsers();
        stubCurrentUserMappings();
        stubCurrentGroups();
        stubCurrentMemberships();
        stubMutationEndpoints();

        String result = syncOrchestrationService.performFullSync();

        assertThat(result).isEqualTo("Sync completed successfully");

        NUCLEUS_MOCK.verify(postRequestedFor(urlEqualTo(userMappingsPath()))
                .withRequestBody(matchingJsonPath("$[?(@.userId == 'iam-bob' && @.externalUserId == 'bjones')]") ));
        NUCLEUS_MOCK.verify(deleteRequestedFor(urlEqualTo(userMappingsPath() + "/legacy")));

        NUCLEUS_MOCK.verify(postRequestedFor(urlEqualTo(groupsPath()))
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_ENGINEERING')]") ));
        NUCLEUS_MOCK.verify(deleteRequestedFor(urlEqualTo(groupsPath() + "/GROUP_ORPHAN")));

        NUCLEUS_MOCK.verify(postRequestedFor(urlEqualTo(groupMembersPath()))
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_ENGINEERING' && @.memberExternalUserId == 'asmith')]") )
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_HR' && @.memberExternalUserId == 'bjones')]") ));

        NUCLEUS_MOCK.verify(deleteRequestedFor(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("parentExternalGroupId", equalTo("GROUP_ORPHAN"))
                .withQueryParam("memberExternalUserIds", equalTo("ghost")));
    }

    private void stubTokenEndpoint()
    {
        NUCLEUS_MOCK.stubFor(post(urlEqualTo("/token"))
                .willReturn(jsonResponse("""
                        {
                          "access_token": "%s",
                          "expires_in": 3600,
                          "token_type": "Bearer",
                          "scope": "iam.user-data.account"
                        }
                        """.formatted(ACCESS_TOKEN))));
    }

    private void stubAlfrescoUsers()
    {
        ALFRESCO_MOCK.stubFor(get(urlPathEqualTo(PEOPLE_ENDPOINT))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0"))
                .withHeader("Authorization", equalTo("Basic " + BASIC_ADMIN))
                .willReturn(jsonResponse("""
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
                                  "id": "asmith",
                                  "firstName": "Alice",
                                  "lastName": "Smith",
                                  "email": "alice@example.com",
                                  "enabled": true,
                                  "displayName": "Alice Smith"
                                }
                              },
                              {
                                "entry": {
                                  "id": "bjones",
                                  "firstName": "Bob",
                                  "lastName": "Jones",
                                  "email": "bob@example.com",
                                  "enabled": true,
                                  "displayName": "Bob Jones"
                                }
                              }
                            ]
                          }
                        }
                        """)));
    }

    private void stubGroupsFor(String userId, String response)
    {
        ALFRESCO_MOCK.stubFor(get(urlPathEqualTo(PEOPLE_ENDPOINT + "/" + userId + "/groups"))
                .withQueryParam("maxItems", equalTo("100"))
                .withQueryParam("skipCount", equalTo("0"))
                .withHeader("Authorization", equalTo("Basic " + BASIC_ADMIN))
                .willReturn(jsonResponse(response)));
    }

    private static String groupResponse(String... groupIds)
    {
        StringBuilder entries = new StringBuilder();
        for (int i = 0; i < groupIds.length; i++)
        {
            if (i > 0)
            {
                entries.append(",");
            }
            entries.append("""
                    {
                      "entry": {
                        "id": "%s",
                        "displayName": "%s",
                        "isRoot": true
                      }
                    }
                    """.formatted(groupIds[i], groupIds[i]));
        }
        return """
                {
                  "list": {
                    "pagination": {
                      "count": %d,
                      "hasMoreItems": false,
                      "totalItems": %d,
                      "skipCount": 0,
                      "maxItems": 100
                    },
                    "entries": [%s]
                  }
                }
                """.formatted(groupIds.length, groupIds.length, entries);
    }

    private void stubIamUsers()
    {
        NUCLEUS_MOCK.stubFor(get(urlPathEqualTo("/api/users"))
                .withQueryParam("limit", equalTo("100"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          "users": [
                            {
                              "userName": "asmith",
                              "userId": "iam-alice",
                              "email": "alice@example.com",
                              "preferredLanguage": "en"
                            },
                            {
                              "userName": "bjones",
                              "userId": "iam-bob",
                              "email": "bob@example.com",
                              "preferredLanguage": "en"
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentUserMappings()
    {
        NUCLEUS_MOCK.stubFor(get(urlPathEqualTo(userMappingsPath()))
                .withQueryParam("limit", equalTo("100"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          "items": [
                            {
                              "userId": "iam-alice",
                              "externalUserId": "asmith",
                              "attributes": []
                            },
                            {
                              "userId": "iam-stale",
                              "externalUserId": "legacy",
                              "attributes": []
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentGroups()
    {
        NUCLEUS_MOCK.stubFor(get(urlPathEqualTo(groupsPath()))
                .withQueryParam("limit", equalTo("100"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          "items": [
                            {
                              "externalGroupId": "GROUP_HR",
                              "attributes": []
                            },
                            {
                              "externalGroupId": "GROUP_ORPHAN",
                              "attributes": []
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentMemberships()
    {
        NUCLEUS_MOCK.stubFor(get(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("limit", equalTo("100"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          "items": [
                            {
                              "externalGroupId": "GROUP_HR",
                              "memberExternalUserId": "asmith"
                            },
                            {
                              "externalGroupId": "GROUP_ORPHAN",
                              "memberExternalUserId": "ghost"
                            }
                          ]
                        }
                        """)));
    }

    private void stubMutationEndpoints()
    {
        NUCLEUS_MOCK.stubFor(post(urlEqualTo(userMappingsPath()))
                .willReturn(jsonResponse("{}")));
        NUCLEUS_MOCK.stubFor(delete(urlPathMatching(userMappingsPath() + "/.*"))
                .willReturn(jsonResponse("{}")));

        NUCLEUS_MOCK.stubFor(post(urlEqualTo(groupsPath()))
                .willReturn(jsonResponse("{}")));
        NUCLEUS_MOCK.stubFor(delete(urlPathMatching(groupsPath() + "/.*"))
                .willReturn(jsonResponse("{}")));

        NUCLEUS_MOCK.stubFor(post(urlEqualTo(groupMembersPath()))
                .willReturn(jsonResponse("{}")));
        NUCLEUS_MOCK.stubFor(delete(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("parentExternalGroupId", equalTo("GROUP_ORPHAN"))
                .withQueryParam("memberExternalUserIds", equalTo("ghost"))
                .willReturn(jsonResponse("{}")));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body)
    {
        return jsonResponse(body, 200);
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body, int status)
    {
        return aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }

    private static String nucleusBaseUrl()
    {
        return NUCLEUS_MOCK.baseUrl();
    }

    private static String alfrescoBaseUrl()
    {
        return ALFRESCO_MOCK.baseUrl() + PEOPLE_ENDPOINT.replace("/people", "");
    }

    private static String userMappingsPath()
    {
        return "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    }

    private static String groupsPath()
    {
        return "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    }

    private static String groupMembersPath()
    {
        return "/system-integrations/systems/" + SYSTEM_ID + "/group-members";
    }
}
