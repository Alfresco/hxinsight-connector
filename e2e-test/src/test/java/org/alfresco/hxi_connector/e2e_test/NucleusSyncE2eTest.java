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
package org.alfresco.hxi_connector.e2e_test;

import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;

@Testcontainers
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.JUnitTestsShouldIncludeAssert"})
class NucleusSyncE2eTest
{
    private static final Network network = Network.newNetwork();
    private static final String SYSTEM_ID = "sync-system";
    private static final String PEOPLE_ENDPOINT = "/alfresco/api/-default-/public/alfresco/versions/1/people";
    private static final String BASIC_ADMIN = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
    private static final String ACCESS_TOKEN = "stub-access-token";
    private static final String PRIMARY_SCOPE = "iam.user-data.account";
    private static final String SECONDARY_SCOPE = "system-integrations-config";
    private static final String NUCLEUS_ALIAS = "nucleus-mock";
    private static final String ALFRESCO_ALIAS = "alfresco-mock";
    private static final int SYNC_TRIGGER_MAX_ATTEMPTS = 240;
    private static final int SYNC_TRIGGER_DELAY_MS = 1000;

    @Container
    private static final WireMockContainer nucleusMock = DockerContainers.createWireMockContainerWithin(network)
            .withNetworkAliases(NUCLEUS_ALIAS);

    @Container
    private static final WireMockContainer alfrescoMock = DockerContainers.createWireMockContainerWithin(network)
            .withNetworkAliases(ALFRESCO_ALIAS);

    @Container
    private static final GenericContainer<?> nucleusSync = DockerContainers.createNucleusSyncContainerWithin(network)
            .withEnv("SYNC_ENABLED", "false")
            .withEnv("SYNC_CRON_EXPRESSION", "0 0 0 * * ?")
            .withEnv("AUTH_ALFRESCO_TYPE", "basic")
            .withEnv("ALFRESCO_USER_NAME", "admin")
            .withEnv("ALFRESCO_PASSWORD", "admin")
            .withEnv("AUTH_HX_TYPE", "oauth2")
            .withEnv("HX_CLIENT_ID", "client-id")
            .withEnv("HX_CLIENT_SECRET", "client-secret")
            .withEnv("HX_TOKEN_URI", "http://" + NUCLEUS_ALIAS + ":8080/token")
            .withEnv("SPRING_APPLICATION_JSON", multiScopeConfig())
            .withEnv("ALFRESCO_BASE_URL", "http://" + ALFRESCO_ALIAS + ":8080/alfresco/api/-default-/public/alfresco/versions/1")
            .withEnv("NUCLEUS_IDP_BASE_URL", "http://" + NUCLEUS_ALIAS + ":8080")
            .withEnv("NUCLEUS_BASE_URL", "http://" + NUCLEUS_ALIAS + ":8080")
            .withEnv("NUCLEUS_SYSTEM_ID", SYSTEM_ID)
            .withEnv("LOGGING_LEVEL_ORG_ALFRESCO", "DEBUG")
            .withEnv("SPRING_THREADS_VIRTUAL_ENABLED", "true")
            .dependsOn(alfrescoMock, nucleusMock)
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forPort(8081)
                    .forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(5));

    private static WireMock nucleusWireMock;
    private static WireMock alfrescoWireMock;

    @BeforeAll
    static void initMocks()
    {
        nucleusWireMock = new WireMock(nucleusMock.getHost(), nucleusMock.getPort());
        alfrescoWireMock = new WireMock(alfrescoMock.getHost(), alfrescoMock.getPort());
        WireMock.configureFor(nucleusMock.getHost(), nucleusMock.getPort());
    }

    @BeforeEach
    void resetMocks()
    {
        resetWireMock(nucleusWireMock);
        resetWireMock(alfrescoWireMock);
    }

    private void resetWireMock(WireMock wireMock)
    {
        wireMock.resetRequests();
        wireMock.resetMappings();
        wireMock.resetScenarios();
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

        waitUntilServiceIsReadyForSync();
        assertDoesNotThrow(this::triggerSync, "Sync trigger should complete successfully");

        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(postRequestedFor(urlEqualTo("/token"))
                .withRequestBody(matching(encodedScopesPattern()))));

        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(postRequestedFor(urlEqualTo(userMappingsPath()))
                .withRequestBody(matchingJsonPath("$[?(@.userId == 'iam-bob' && @.externalUserId == 'bjones')]"))));
        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(deleteRequestedFor(urlEqualTo(userMappingsPath() + "/legacy"))));

        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(postRequestedFor(urlEqualTo(groupsPath()))
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_ENGINEERING')]"))));
        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(deleteRequestedFor(urlEqualTo(groupsPath() + "/GROUP_ORPHAN"))));

        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(postRequestedFor(urlEqualTo(groupMembersPath()))
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_ENGINEERING' && @.memberExternalUserId == 'asmith')]"))
                .withRequestBody(matchingJsonPath("$[?(@.externalGroupId == 'GROUP_HR' && @.memberExternalUserId == 'bjones')]"))));

        RetryUtils.retryWithBackoff(() -> nucleusWireMock.verify(deleteRequestedFor(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("parentExternalGroupId", equalTo("GROUP_ORPHAN"))
                .withQueryParam("memberExternalUserIds", equalTo("ghost"))));
    }

    private void waitUntilServiceIsReadyForSync()
    {
        RetryUtils.retryWithBackoff(() -> {
            try
            {
                HttpRequest healthRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + nucleusSync.getHost() + ":" + nucleusSync.getMappedPort(8081) + "/actuator/health"))
                        .GET()
                        .build();
                HttpResponse<String> healthResp = HttpClient.newHttpClient().send(healthRequest, HttpResponse.BodyHandlers.ofString());
                if (healthResp.statusCode() != 200)
                {
                    throw new AssertionError("nucleus-sync health not OK yet: " + healthResp.statusCode());
                }

                HttpRequest alfrescoRequest = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + alfrescoMock.getHost() + ":" + alfrescoMock.getPort()
                                + PEOPLE_ENDPOINT + "?maxItems=1000&skipCount=0"))
                        .header("Authorization", "Basic " + BASIC_ADMIN)
                        .GET()
                        .build();
                HttpResponse<String> alfrescoResp = HttpClient.newHttpClient().send(alfrescoRequest, HttpResponse.BodyHandlers.ofString());
                if (alfrescoResp.statusCode() != 200)
                {
                    throw new AssertionError("alfresco mock not responding yet: " + alfrescoResp.statusCode());
                }

                // Additional delay to ensure Docker network DNS resolution is stable from nucleus-sync's perspective
                Thread.sleep(2000);
            }
            catch (AssertionError ae)
            {
                throw ae;
            }
            catch (Exception e)
            {
                throw new AssertionError("Service readiness check failed: " + e.getMessage(), e);
            }
        }, SYNC_TRIGGER_MAX_ATTEMPTS, SYNC_TRIGGER_DELAY_MS);
    }

    private void triggerSync()
    {
        RetryUtils.retryWithBackoff(() -> {
            try
            {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + nucleusSync.getHost() + ":" + nucleusSync.getMappedPort(8081) + "/sync/trigger"))
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();

                if (statusCode == 503)
                {
                    throw new AssertionError("Sync trigger not ready yet (503): " + getSafeResponseBody(response));
                }

                if (statusCode != 200)
                {
                    throw new AssertionError("Sync trigger endpoint returned HTTP " + statusCode
                            + " with response body: " + getSafeResponseBody(response)
                            + "\n--- nucleus-sync container logs (last 2000 chars) ---\n"
                            + getSafeContainerLogs());
                }
            }
            catch (AssertionError ae)
            {
                throw ae;
            }
            catch (Exception exception)
            {
                String containerLogs = getSafeContainerLogs();
                throw new AssertionError(
                        "Sync trigger endpoint not ready: " + exception.getClass().getName() + ": " + exception.getMessage()
                                + "\n--- nucleus-sync container logs (last 2000 chars) ---\n"
                                + containerLogs,
                        exception);
            }
        }, SYNC_TRIGGER_MAX_ATTEMPTS, SYNC_TRIGGER_DELAY_MS);
    }

    private String getSafeResponseBody(HttpResponse<String> response)
    {
        try
        {
            if (response == null || response.body() == null)
            {
                return "(no response body)";
            }
            String body = response.body();
            if (body == null || body.isBlank())
            {
                return "(empty response body)";
            }
            return body.substring(Math.max(0, body.length() - 1000));
        }
        catch (Exception bodyException)
        {
            return "(failed to read response body: " + bodyException.getClass().getName() + ": " + bodyException.getMessage() + ")";
        }
    }

    private String getSafeContainerLogs()
    {
        try
        {
            String logs = nucleusSync.getLogs();
            if (logs == null || logs.isBlank())
            {
                return "(no logs)";
            }
            return logs.substring(Math.max(0, logs.length() - 2000));
        }
        catch (Exception logException)
        {
            return "(failed to read container logs: " + logException.getClass().getName() + ": " + logException.getMessage() + ")";
        }
    }

    private static String multiScopeConfig()
    {
        return "{\"auth\":{\"providers\":{\"hyland-experience\":{\"scope\":[\""
                + PRIMARY_SCOPE + "\",\"" + SECONDARY_SCOPE + "\"]}}}}";
    }

    private static String encodedScopesPattern()
    {
        String primaryScope = PRIMARY_SCOPE.replace(".", "\\.");
        String secondaryScope = SECONDARY_SCOPE.replace(".", "\\.");
        return ".*scope=(" + primaryScope + "\\+" + secondaryScope + "|" + secondaryScope + "\\+" + primaryScope + ").*";
    }

    private void stubTokenEndpoint()
    {
        nucleusWireMock.register(post(urlEqualTo("/token"))
                .willReturn(jsonResponse("""
                        {
                          \"access_token\": \"%s\",
                          \"expires_in\": 3600,
                          \"token_type\": \"Bearer\",
                          \"scope\": \"%s %s\"
                        }
                        """.formatted(ACCESS_TOKEN, PRIMARY_SCOPE, SECONDARY_SCOPE))));
    }

    private void stubAlfrescoUsers()
    {
        alfrescoWireMock.register(get(urlPathEqualTo(PEOPLE_ENDPOINT))
                .withQueryParam("maxItems", equalTo("1000"))
                .withQueryParam("skipCount", equalTo("0"))
                .withHeader("Authorization", equalTo("Basic " + BASIC_ADMIN))
                .willReturn(jsonResponse("""
                        {
                          \"list\": {
                            \"pagination\": {
                              \"count\": 2,
                              \"hasMoreItems\": false,
                              \"totalItems\": 2,
                              \"skipCount\": 0,
                              \"maxItems\": 1000
                            },
                            \"entries\": [
                              {
                                \"entry\": {
                                  \"id\": \"asmith\",
                                  \"firstName\": \"Alice\",
                                  \"lastName\": \"Smith\",
                                  \"email\": \"alice@example.com\",
                                  \"enabled\": true,
                                  \"displayName\": \"Alice Smith\"
                                }
                              },
                              {
                                \"entry\": {
                                  \"id\": \"bjones\",
                                  \"firstName\": \"Bob\",
                                  \"lastName\": \"Jones\",
                                  \"email\": \"bob@example.com\",
                                  \"enabled\": true,
                                  \"displayName\": \"Bob Jones\"
                                }
                              }
                            ]
                          }
                        }
                        """)));
    }

    private void stubGroupsFor(String userId, String response)
    {
        alfrescoWireMock.register(get(urlPathEqualTo(PEOPLE_ENDPOINT + "/" + userId + "/groups"))
                .withQueryParam("maxItems", equalTo("1000"))
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
                      \"entry\": {
                        \"id\": \"%s\",
                        \"displayName\": \"%s\",
                        \"isRoot\": true
                      }
                    }
                    """.formatted(groupIds[i], groupIds[i]));
        }
        return """
                {
                  \"list\": {
                    \"pagination\": {
                      \"count\": %d,
                      \"hasMoreItems\": false,
                      \"totalItems\": %d,
                      \"skipCount\": 0,
                      \"maxItems\": 1000
                    },
                    \"entries\": [%s]
                  }
                }
                """.formatted(groupIds.length, groupIds.length, entries);
    }

    private void stubIamUsers()
    {
        nucleusWireMock.register(get(urlPathEqualTo("/api/users"))
                .withQueryParam("limit", equalTo("1000"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          \"users\": [
                            {
                              \"userName\": \"asmith\",
                              \"userId\": \"iam-alice\",
                              \"email\": \"alice@example.com\",
                              \"preferredLanguage\": \"en\"
                            },
                            {
                              \"userName\": \"bjones\",
                              \"userId\": \"iam-bob\",
                              \"email\": \"bob@example.com\",
                              \"preferredLanguage\": \"en\"
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentUserMappings()
    {
        nucleusWireMock.register(get(urlPathEqualTo(userMappingsPath()))
                .withQueryParam("limit", equalTo("1000"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          \"items\": [
                            {
                              \"userId\": \"iam-alice\",
                              \"externalUserId\": \"asmith\",
                              \"attributes\": []
                            },
                            {
                              \"userId\": \"iam-stale\",
                              \"externalUserId\": \"legacy\",
                              \"attributes\": []
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentGroups()
    {
        nucleusWireMock.register(get(urlPathEqualTo(groupsPath()))
                .withQueryParam("limit", equalTo("1000"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          \"items\": [
                            {
                              \"externalGroupId\": \"GROUP_HR\",
                              \"attributes\": []
                            },
                            {
                              \"externalGroupId\": \"GROUP_ORPHAN\",
                              \"attributes\": []
                            }
                          ]
                        }
                        """)));
    }

    private void stubCurrentMemberships()
    {
        nucleusWireMock.register(get(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("limit", equalTo("1000"))
                .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
                .willReturn(jsonResponse("""
                        {
                          \"items\": [
                            {
                              \"externalGroupId\": \"GROUP_HR\",
                              \"memberExternalUserId\": \"asmith\"
                            },
                            {
                              \"externalGroupId\": \"GROUP_ORPHAN\",
                              \"memberExternalUserId\": \"ghost\"
                            }
                          ]
                        }
                        """)));
    }

    private void stubMutationEndpoints()
    {
        nucleusWireMock.register(post(urlEqualTo(userMappingsPath()))
                .willReturn(jsonResponse("{}")));
        nucleusWireMock.register(post(urlEqualTo(groupsPath()))
                .willReturn(jsonResponse("{}")));
        nucleusWireMock.register(post(urlEqualTo(groupMembersPath()))
                .willReturn(jsonResponse("{}")));
        nucleusWireMock.register(delete(urlPathMatching(userMappingsPath() + "/.*"))
                .willReturn(jsonResponse("{}")));
        nucleusWireMock.register(delete(urlPathMatching(groupsPath() + "/.*"))
                .willReturn(jsonResponse("{}")));
        nucleusWireMock.register(delete(urlPathEqualTo(groupMembersPath()))
                .withQueryParam("parentExternalGroupId", equalTo("GROUP_ORPHAN"))
                .withQueryParam("memberExternalUserIds", equalTo("ghost"))
                .willReturn(jsonResponse("{}")));
    }

    private static ResponseDefinitionBuilder jsonResponse(String body)
    {
        return jsonResponse(body, 200);
    }

    private static ResponseDefinitionBuilder jsonResponse(String body, int status)
    {
        return com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
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
