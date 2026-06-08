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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;

// This is the Base Class for Large Scale Ingestion Mapping Tolerance Tests
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseNucleusSyncLargeIngestionIT
{

    /** Must match NUCLEUS_SYSTEM_ID env var on the nucleus-sync container. */
    protected static final String SYSTEM_ID = "-dummy-system-id";
    protected static final String USER_MAPPINGS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    protected static final String GROUPS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    protected static final String GROUP_MEMBERS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/group-members";
    protected static final String ALL_USERS_PATH = "/api/users";

    /**
     * Page size for ACS /people API (must match nucleus-sync's {@code alfresco.page-size} config, default 100).
     */
    protected static final int ACS_PAGE_SIZE = 100;

    /**
     * Page size Nucleus IAM uses (must match nucleus-sync's {@code nucleus.page-size} config, default 1000).
     */
    protected static final int NUCLEUS_PAGE_SIZE = 1000;

    protected static final int SCENARIO_STUB_PRIORITY = 1;

    /**
     * Total number of users to simulate on BOTH ACS and Nucleus sides. Tune this single knob to scale the test — currently {@code 100} for fast local runs; production-grade scale check is {@code 1_000_000}. Note the install cost: each ACS page is one stub, so 1M users at {@link #ACS_PAGE_SIZE}=100 means 10 000 stubs registered before the sync even starts.
     */
    protected static final int TOTAL_USER_COUNT = 1000000;

    // Total User Groups
    protected static final int TOTAL_GROUPS_COUNT = 1000000;

    /** Track stubs we register so we can clean them up in @AfterEach. */
    protected final List<StubMapping> registeredStubs = new ArrayList<>();

    /** Environment built with withStubbedAcs() to route ACS calls to nucleus WireMock. */
    protected ReliabilityEnvironment environment;

    protected WireMock nucleus()
    {
        return environment.nucleusWireMock();
    }

    @BeforeAll
    void startEnvironment() throws IOException, InterruptedException
    {
        log.info("[scale-test] Starting environment with stubbed ACS for large-scale user mapping test");
        environment = ReliabilityEnvironment.builder()
                .withStubbedAcs()
                .build();
        environment.start();
        log.info("[scale-test] Environment ready for 1M user test");
    }

    @AfterAll
    void stopEnvironment()
    {
        if (environment != null)
        {
            environment.close();
        }
    }

    @AfterEach
    void cleanupStubs()
    {
        for (StubMapping stub : registeredStubs)
        {
            try
            {
                nucleus().removeStubMapping(stub);
            }
            catch (Exception e)
            {
                log.warn("[scale-test] Failed to remove stub {}", stub.getName(), e);
            }
        }
        registeredStubs.clear();
    }

    // All Stub Helpers
    protected void installNucleusAuthStub()
    {
        StubMapping stub = nucleus().register(post(urlEqualTo("/token"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "stub-access-token",
                                  "expires_in": 3600,
                                  "token_type": "Bearer",
                                  "scope": "iam.user-data.account system-integrations-config system-integrations-runtime environment_authorization"
                                }
                                """)));
        registeredStubs.add(stub);
    }

    /**
     * Stub ACS per-user {@code /people/{userId}/groups} endpoint. nucleus-sync calls this for every user during the mapping flow (see {@code AlfrescoClient#getUserGroups}); without this stub each request returns 404 and the sync fails. Returns an empty group list for every user via a regex URL match.
     */
    protected void installSingleAcsUserStub()
    {
        StubMapping stub = nucleus().register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                        .atPriority(SCENARIO_STUB_PRIORITY)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                          "list": {
                                            "pagination": {
                                              "count": 1,
                                              "hasMoreItems": false,
                                              "totalItems": 1,
                                              "skipCount": 0,
                                              "maxItems": 100
                                            },
                                            "entries": [
                                              {
                                                "entry": {
                                                  "id": "user1",
                                                  "email": "abcd@hyland.com",
                                                  "enabled": true
                                                }
                                              }
                                            ]
                                          }
                                        }
                                        """)));
        registeredStubs.add(stub);
    }

    /**
     * Stub ACS /people endpoint with paginated responses for the given number of users. Uses skipCount-based pagination matching ACS's API.
     */
    protected void installAcsPeopleStubs(int totalUserCount)
    {
        int pageCount = (totalUserCount + ACS_PAGE_SIZE - 1) / ACS_PAGE_SIZE;
        log.info("[scale-test] Installing {} ACS /people page stubs for {} users (page size {})",
                pageCount, totalUserCount, ACS_PAGE_SIZE);

        for (int page = 0; page < pageCount; page++)
        {
            int skipCount = page * ACS_PAGE_SIZE;
            int remaining = totalUserCount - skipCount;
            int count = Math.min(ACS_PAGE_SIZE, remaining);
            boolean hasMoreItems = skipCount + count < totalUserCount;

            String responseBody = buildAcsPeoplePageJson(skipCount, count, hasMoreItems, totalUserCount);

            StubMapping stub = nucleus().register(
                    get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                            .withQueryParam("skipCount", equalTo(String.valueOf(skipCount)))
                            .atPriority(SCENARIO_STUB_PRIORITY)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(responseBody)));
            registeredStubs.add(stub);

            if (page % 1000 == 0)
            {
                log.debug("[scale-test] Installed ACS /people stub for page {} (skipCount={})", page, skipCount);
            }
        }
        log.info("[scale-test] Finished installing {} ACS /people stubs", pageCount);
    }

    /**
     * Build ACS /people paginated response JSON matching the Alfresco REST API format.
     */
    protected String buildAcsPeoplePageJson(int skipCount, int count, boolean hasMoreItems, int totalItems)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"list\":{")
                .append("\"pagination\":{")
                .append("\"count\":").append(count).append(",")
                .append("\"hasMoreItems\":").append(hasMoreItems).append(",")
                .append("\"totalItems\":").append(totalItems).append(",")
                .append("\"skipCount\":").append(skipCount).append(",")
                .append("\"maxItems\":").append(ACS_PAGE_SIZE)
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
                    .append("\"email\":\"user").append(userId).append("@hyland.com\",")
                    .append("\"enabled\":true")
                    .append("}}");
        }

        sb.append("]}}");
        return sb.toString();
    }

    /**
     * Stub Nucleus IAM /api/users to return a large set of synthetic users. Uses offset-based pagination with the {@code next} link in the response.
     */
    protected void installNucleusIamUsersStubs(int totalUserCount)
    {
        int pageCount = (totalUserCount + NUCLEUS_PAGE_SIZE - 1) / NUCLEUS_PAGE_SIZE;
        log.info("[scale-test] Installing {} Nucleus /api/users page stubs for {} users (page size {})",
                pageCount, totalUserCount, NUCLEUS_PAGE_SIZE);

        // First page (no offset param or offset=0)
        int firstPageSize = Math.min(NUCLEUS_PAGE_SIZE, totalUserCount);
        boolean hasNext = totalUserCount > NUCLEUS_PAGE_SIZE;
        String firstPageUsers = buildNucleusUsersJson(0, firstPageSize);
        String firstPageNext = hasNext
                ? ",\"next\":\"/api/users?limit=" + NUCLEUS_PAGE_SIZE + "&offset=" + NUCLEUS_PAGE_SIZE + "\""
                : "";

        StubMapping firstStub = nucleus().register(
                get(urlPathEqualTo("/api/users"))
                        // Lock first page to "no offset" so it cannot collide with the offset-paged stubs
                        // below. nucleus-sync's initial request is GET /api/users?limit=1000 (no offset);
                        // every follow-up comes from the `next` link which always carries an offset param.
                        .withQueryParam("offset", absent())
                        .atPriority(SCENARIO_STUB_PRIORITY)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"users\":[" + firstPageUsers + "]" + firstPageNext + "}")));
        registeredStubs.add(firstStub);

        // Subsequent pages with offset parameter
        for (int page = 1; page < pageCount; page++)
        {
            int offset = page * NUCLEUS_PAGE_SIZE;
            int remaining = totalUserCount - offset;
            int count = Math.min(NUCLEUS_PAGE_SIZE, remaining);
            boolean hasMore = offset + count < totalUserCount;

            String pageUsers = buildNucleusUsersJson(offset, count);
            String nextLink = hasMore
                    ? ",\"next\":\"/api/users?limit=" + NUCLEUS_PAGE_SIZE + "&offset=" + (offset + NUCLEUS_PAGE_SIZE) + "\""
                    : "";

            StubMapping stub = nucleus().register(
                    get(urlPathEqualTo("/api/users"))
                            .withQueryParam("offset", equalTo(String.valueOf(offset)))
                            .atPriority(SCENARIO_STUB_PRIORITY)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{\"users\":[" + pageUsers + "]" + nextLink + "}")));
            registeredStubs.add(stub);

            if (page % 100 == 0)
            {
                log.debug("[scale-test] Installed Nucleus /api/users stub for page {} (offset={})", page, offset);
            }
        }
        log.info("[scale-test] Finished installing {} Nucleus /api/users stubs", pageCount);
    }

    protected String buildNucleusUsersJson(int offset, int count)
    {
        return IntStream.range(offset, offset + count)
                .mapToObj(i -> String.format(
                        "{\"userName\":\"user%d\",\"userId\":\"iam-%d\",\"email\":\"user%d@hyland.com\"}", i, i, i))
                .collect(Collectors.joining(","));
    }

    /**
     * Stub ACS per-user {@code /people/{userId}/groups} endpoint. nucleus-sync calls this for every user during the mapping flow (see {@code AlfrescoClient#getUserGroups}); without this stub each request returns 404 and the sync fails. Returns an empty group list for every user via a regex URL match.
     */
    protected void installAcsUserGroupsStub()
    {
        StubMapping stub = nucleus().register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people/[^/]+/groups"))
                        .atPriority(SCENARIO_STUB_PRIORITY)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"list\":{\"pagination\":{\"count\":0,\"hasMoreItems\":false,\"totalItems\":0,\"skipCount\":0,\"maxItems\":100},\"entries\":[]}}")));
        registeredStubs.add(stub);
    }

    // Install ACS User Group Stub with shared Group
    protected void installAcsUserGroupStubWithGroupId(String SHARED_GROUP_ID)
    {
        String body = "{\"list\":{"
                + "\"pagination\":{\"count\":1,\"hasMoreItems\":false,\"totalItems\":1,\"skipCount\":0,\"maxItems\":100},"
                + "\"entries\":[{\"entry\":{\"id\":\"" + SHARED_GROUP_ID + "\"}}]"
                + "}}";
        StubMapping stub = nucleus().register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people/user[0-9]+/groups"))
                        .atPriority(SCENARIO_STUB_PRIORITY)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)));
        registeredStubs.add(stub);
    }

    protected void installEmptyMappingsStub()
    {
        StubMapping stub = nucleus().register(get(urlPathEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    protected void installEmptyGroupsStub()
    {
        StubMapping stub = nucleus().register(get(urlPathEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    protected void installEmptyGroupMembersStub()
    {
        StubMapping stub = nucleus().register(get(urlPathEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    protected void installMutationEndpointsWithTracking()
    {
        StubMapping mappings = nucleus().register(post(urlEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(mappings);

        StubMapping groups = nucleus().register(post(urlEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(groups);

        StubMapping members = nucleus().register(post(urlEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(members);
    }

    /**
     * Extract the Alfresco-side user IDs that were mapped, by parsing the POST bodies sent to {@code /user-mappings}. The body is a JSON array of {@code NucleusUserMappingInput} records: {@code [{"userId":"iam-0","externalUserId":"user0"}, ...]}. The Alfresco id lives in {@code externalUserId} (see {@code UserMappingSyncProcessor#syncUserMappings} where the input is constructed as {@code new NucleusUserMappingInput(nucleusUserId, alfrescoUserId)}).
     */
    protected Set<String> extractMappedUserIds(List<LoggedRequest> requests)
    {
        Set<String> userIds = new HashSet<>();
        Pattern userIdPattern = Pattern.compile("\"externalUserId\"\\s*:\\s*\"([^\"]+)\"");

        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = userIdPattern.matcher(body);
            while (matcher.find())
            {
                userIds.add(matcher.group(1));
            }
        }
        return userIds;
    }

    // Install Nucleus Empty Users
    protected void installNucleusSingleUserStub()
    {
        StubMapping users = nucleus().register(get(urlPathEqualTo(ALL_USERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "users": [
                                    {
                                      "userName": "asmith",
                                      "userId": "iam-1234",
                                      "email": "abcd@hyland.com"
                                    }
                                  ]
                                }
                                """)));
        registeredStubs.add(users);
    }

    /**
     * Stub ACS /group endpoint with paginated responses for the given number of users. Uses skipCount-based pagination matching ACS's API.
     */
    protected void installRepositoryGroupStubs(int totalGroupsCount)
    {
        int pageCount = (totalGroupsCount + ACS_PAGE_SIZE - 1) / ACS_PAGE_SIZE;
        log.info("[scale-test] Installing {} ACS /people page stubs for {} users (page size {})",
                pageCount, totalGroupsCount, ACS_PAGE_SIZE);

        for (int page = 0; page < pageCount; page++)
        {
            int skipCount = page * ACS_PAGE_SIZE;
            int remaining = totalGroupsCount - skipCount;
            int count = Math.min(ACS_PAGE_SIZE, remaining);
            boolean hasMoreItems = skipCount + count < totalGroupsCount;

            String responseBody = buildAcsGroupPageJson(skipCount, count, hasMoreItems, totalGroupsCount);

            StubMapping stub = nucleus().register(
                    get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people/user1/groups"))
                            .withQueryParam("skipCount", equalTo(String.valueOf(skipCount)))
                            .atPriority(SCENARIO_STUB_PRIORITY)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(responseBody)));
            registeredStubs.add(stub);

            if (page % 1000 == 0)
            {
                log.debug("[scale-test] Installed ACS /people stub for page {} (skipCount={})", page, skipCount);
            }
        }
        log.info("[scale-test] Finished installing {} ACS /people stubs", pageCount);
    }

    /**
     * Build ACS /people paginated response JSON matching the Alfresco REST API format.
     */
    private String buildAcsGroupPageJson(int skipCount, int count, boolean hasMoreItems, int totalItems)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"list\":{")
                .append("\"pagination\":{")
                .append("\"count\":").append(count).append(",")
                .append("\"hasMoreItems\":").append(hasMoreItems).append(",")
                .append("\"totalItems\":").append(totalItems).append(",")
                .append("\"skipCount\":").append(skipCount).append(",")
                .append("\"maxItems\":").append(ACS_PAGE_SIZE)
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
                    .append("\"id\":\"group").append(userId).append("\"")
                    .append("}}");
        }

        sb.append("]}}");
        return sb.toString();
    }

    /**
     * Extract the Alfresco-side group IDs that were posted, by parsing the POST bodies sent to {@code /groups}. The body is a JSON array of {@code NucleusGroupInput} records where the Alfresco id lives in {@code externalGroupId}.
     */
    protected Set<String> extractGroupIds(List<LoggedRequest> requests)
    {
        Set<String> groupIds = new HashSet<>();
        Pattern idPattern = Pattern.compile("\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"");

        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = idPattern.matcher(body);
            while (matcher.find())
            {
                groupIds.add(matcher.group(1));
            }
        }
        return groupIds;
    }

    /**
     * Extract every {@code (externalGroupId, memberExternalUserId)} pair captured in POST {@code /group-members} bodies — these are the {@code NucleusGroupMemberAssignmentInput} records. The DTO is an array per request so we walk the body left-to-right pairing the fields in document order.
     */
    protected Set<String> extractMemberships(List<LoggedRequest> requests)
    {
        Set<String> pairs = new HashSet<>();
        Pattern pairPattern = Pattern.compile(
                "\\{[^}]*?\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"memberExternalUserId\"\\s*:\\s*\"([^\"]+)\"[^}]*?}"
                        + "|"
                        + "\\{[^}]*?\"memberExternalUserId\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"[^}]*?}");

        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = pairPattern.matcher(body);
            while (matcher.find())
            {
                String groupId = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
                String userId = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                pairs.add(membershipKey(groupId, userId));
            }
        }
        return pairs;
    }

    protected static String membershipKey(String groupId, String userId)
    {
        return groupId + "::" + userId;
    }

}
