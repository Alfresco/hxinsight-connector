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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.member_mapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncLargeIngestionIT;

/**
 * Non-tolerable group-member mapping outage — Nucleus POST /group-members returns 503 permanently.
 *
 * <h2>Behaviour under test</h2>
 * {@code NucleusClient.assignGroupMembers()} throws {@code ClientException} when the POST fails
 * after retries exhaust. The sync controller catches this and returns an error HTTP status (500)
 * from {@code POST /sync/trigger}. The test verifies this via the response status code.
 *
 * <h2>Why extractMemberships() doesn't work for this assertion</h2>
 * WireMock's journal records the full request body of every attempt — including ones that got 503.
 * If all memberships are sent in a single batch POST, the request body contains all N memberships
 * even though the server rejected them. Counting memberships from request bodies does NOT tell you
 * how many were successfully persisted.
 *
 * <h2>What this pins</h2>
 * <ol>
 *   <li>User-mapping + group-creation phases succeed (503 only affects /group-members POST).</li>
 *   <li>The sync returns a non-200 status — {@code ClientException} propagated to the controller.</li>
 *   <li>At least one POST /group-members attempt was made — proves the phase was reached.</li>
 * </ol>
 *
 * <h2>Why WireMock fault, not Toxiproxy disable?</h2>
 * Both ACS and Nucleus traffic share the {@code toxic-nucleus} listener when {@code withStubbedAcs()}
 * is on. A proxy disable would kill ACS reads too. A targeted 503 on the membership-mutation endpoint
 * leaves user/group reads + earlier mutation phases untouched.
 */
@Slf4j
public class GroupMembersMappingNonTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    private static final int TOTAL_USERS = 1000;
    private static final String SHARED_GROUP_ID = "groupShared";

    /**
     * Outer wait for the sync future to terminate. The membership phase runs after user-mapping +
     * group-creation, so the failure surfaces a few seconds in. 120 s is generous — a timeout here
     * would mask a "sync hung" regression.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 120L;

    @Test
    void shouldFailSyncWhenGroupMembersEndpointPermanentlyReturns503() throws Exception
    {
        installAllStubs();

        // Inject the 503 BEFORE triggering sync so it's guaranteed to be active by the time the
        // membership phase starts. Earlier phases (user mappings, groups) succeed normally.
        log.info("[outage-test] Injecting permanent 503 fault on POST {} before triggering sync",
                GROUP_MEMBERS_PATH);
        injectGroupMembersFault();

        long startNanos = System.nanoTime();

        // startSynchronization() returns the HTTP status code from /sync/trigger.
        // It does NOT throw — even if the sync fails internally, the HTTP call succeeds.
        CompletableFuture<Integer> syncCall = CompletableFuture.supplyAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        int statusCode = syncCall.get(SYNC_COMPLETION_TIMEOUT_S, TimeUnit.SECONDS);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[outage-test] Sync returned HTTP {} after {} ms", statusCode, elapsedMs);

        // Primary assertion: sync must return a non-200 status because assignGroupMembers()
        // throws ClientException which the controller should surface as 500.
        assertThat(statusCode)
                .as("Expected /sync/trigger to return a non-200 status because POST /group-members "
                        + "permanently returns 503 and NucleusClient.assignGroupMembers() throws "
                        + "ClientException. Got HTTP %d — either the exception was swallowed in "
                        + "the orchestration layer, or the controller maps it to 200 (bug).", statusCode)
                .isGreaterThanOrEqualTo(400);

        // Secondary assertion: at least one POST /group-members attempt was made
        // (proves the membership phase was reached before the failure).
        List<LoggedRequest> memberPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH)));
        log.info("[outage-test] POST /group-members attempts before failure: {}", memberPosts.size());

        assertThat(memberPosts)
                .as("Expected at least one POST /group-members attempt — the membership phase "
                        + "should have been reached after user-mapping and group-creation succeeded")
                .isNotEmpty();

        // Tertiary assertion: user-mappings and groups succeeded (earlier phases were unaffected)
        List<LoggedRequest> userMappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        assertThat(userMappingPosts)
                .as("User-mapping phase should have completed before the membership fault kicked in")
                .isNotEmpty();

        List<LoggedRequest> groupPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH)));
        assertThat(groupPosts)
                .as("Group-creation phase should have completed before the membership fault kicked in")
                .isNotEmpty();

        log.info("[outage-test] ✓ Sync correctly failed with HTTP {} — {} membership attempts made, "
                        + "user-mappings and groups succeeded beforehand",
                statusCode, memberPosts.size());
    }

    /**
     * Replace the 200-OK POST /group-members stub with a 503 at higher priority. Other Nucleus
     * mutation endpoints (POST /user-mappings, POST /groups) keep the normal 200 stub so the
     * earlier phases of sync complete normally and the membership phase is where the fault lands.
     */
    private void injectGroupMembersFault()
    {
        StubMapping fault = nucleus().register(
                post(urlEqualTo(GROUP_MEMBERS_PATH))
                        .atPriority(SCENARIO_STUB_PRIORITY - 1) // priority 0 > priority 1
                        .willReturn(aResponse()
                                .withStatus(503)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\":\"Service Unavailable — simulated membership outage\"}")));
        registeredStubs.add(fault);
    }

    private void installAllStubs()
    {
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USERS);
        installAcsUserGroupStubWithGroupId(SHARED_GROUP_ID);
        installNucleusIamUsersStubs(TOTAL_USERS);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }
}
