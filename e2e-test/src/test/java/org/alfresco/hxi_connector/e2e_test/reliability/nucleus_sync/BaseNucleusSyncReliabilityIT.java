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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

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

import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;

/**
 * This Test is meant to Have The common Components Needed for any Nucleus Sync Tests
 */
@Slf4j
public abstract class BaseNucleusSyncReliabilityIT extends BaseReliabilityIT
{
    /** Must match NUCLEUS_SYSTEM_ID env var on the nucleus-sync container (DockerContainers#createNucleusSyncContainerForWireMock). */
    protected static final String SYSTEM_ID = "-dummy-system-id";
    protected static final String USER_MAPPINGS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    protected static final String GROUPS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    protected static final String GROUP_MEMBERS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/group-members";

    /**
     * Page size Nucleus IAM uses (must match nucleus-sync's {@code nucleus.page-size} config, default 1000).
     */
    protected static final int NUCLEUS_PAGE_SIZE = 1000;

    /**
     * Outer wait for the sync to complete (covers the post-recovery retry back-off plus the synchronous controller round-trip plus the time to actually map {@value #TOTAL_USERS} users). Far below the per-attempt WebClient timeout so a misconfigured retry path surfaces as a future timeout rather than a silent green pass.
     */
    protected static final long SYNC_COMPLETION_TIMEOUT_S = 60L;

    protected static final int SCENARIO_STUB_PRIORITY = 1;

    protected static final int TOTAL_USERS = 100;

    /**
     * Delay between triggering sync and opening the partition. Small but non-zero — gives the sync thread enough time to leave the controller and hit the first Nucleus call so the outage genuinely overlaps the mapping work rather than landing before it starts.
     */
    protected static final long DELAY_BEFORE_OUTAGE_MS = 50L;

    /**
     * Window during which the {@code nucleusProxy} is disabled. Sized to burn 2–3 retry attempts (≈200 ms, 600 ms, 1400 ms in the standard envelope) without exhausting the full 3 s retry budget, so the next attempt after re-enable still lands inside the budget and the sync recovers.
     */
    protected static final long OUTAGE_DURATION_MS = 1_000L;

    protected final List<StubMapping> registeredStubs = new java.util.ArrayList<>();

    protected WireMock nucleus()
    {
        return environment().nucleusWireMock();
    }

    /**
     * A stubbed response for the "get users" endpoint that returns a user with the same email as the test user.
     */
    protected void installReturnUserWithSameMail()
    {
        nucleus().register(get(urlPathEqualTo("/api/users"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
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
    }

    protected static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body)
    {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }

    protected User createTestUserWithMail(String email)
    {
        return new User("test", "test", email);
    }

    // Dummy Token Response From Nucleus Side
    protected void installAuthResponse()
    {
        // OAuth2-shaped token response — Spring's OAuth2 client expects access_token / expires_in / token_type.
        // Returning {"token":"..."} (the previous shape) silently fails deserialization → no downstream calls.
        nucleus().register(post(urlEqualTo("/token"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        {
                          "access_token": "stub-access-token",
                          "expires_in": 3600,
                          "token_type": "Bearer",
                          "scope": "iam.user-data.account system-integrations-config system-integrations-runtime environment_authorization"
                        }
                        """)));
    }

    // Return Any Empty Mapping
    protected void installReturnEmptyMapping()
    {
        nucleus().register(get(urlPathEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        { "items": [] }
                        """)));
    }

    protected void installReturnEmptyGroups()
    {
        nucleus().register(get(urlPathEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        { "items": [] }
                        """)));
    }

    protected void installReturnEmptyGroupMembers()
    {
        nucleus().register(get(urlPathEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        { "items": [] }
                        """)));
    }

    // Install Mutual Endpoints
    protected void installMutationEndpoints()
    {
        // Accept any POST so nucleus-sync's "create mapping for new user" call succeeds rather than 404.
        nucleus().register(post(urlEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{}")));
        nucleus().register(post(urlEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{}")));
        nucleus().register(post(urlEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{}")));
    }

    protected User createUser(String name, String email)
    {
        return new User(name, "test", email);
    }

    protected void createUsersInRepository(int count)
    {
        for (int i = 0; i < count; i++)
        {
            User user = createUser("test-user-%d".formatted(i), "user%d@hyland.com".formatted(i));
            environment().repositoryClient().createUser(user);
        }
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

    private String buildNucleusUsersJson(int offset, int count)
    {
        return IntStream.range(offset, offset + count)
                .mapToObj(i -> String.format(
                        "{\"userName\":\"user%d\",\"userId\":\"iam-%d\",\"email\":\"user%d@hyland.com\"}", i, i, i))
                .collect(Collectors.joining(","));
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
     * Installs the standard set of stubs needed for a happy-path sync: auth, single Nucleus user matching a single ACS user by email, empty current mappings/groups/members, and mutation endpoints. Subclasses can override specific stubs at higher priority for chaos injection.
     */
    protected void installAllStubs()
    {
        installAuthResponse();
        installReturnUserWithSameMail();
        installReturnEmptyMapping();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        installMutationEndpoints();
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

}
