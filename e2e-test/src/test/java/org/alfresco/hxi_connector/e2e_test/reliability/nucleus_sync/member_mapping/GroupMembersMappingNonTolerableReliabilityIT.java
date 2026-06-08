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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncLargeIngestionIT;

/**
 * Non-tolerable group-member mapping outage — Nucleus POST /group-members returns 503 permanently. Closes the mapping-flow failure-path matrix (User + Group already covered; this completes Member).
 *
 * <h2>Why WireMock fault, not Toxiproxy disable?</h2> Both ACS and Nucleus traffic share the {@code toxic-nucleus} listener when {@code withStubbedAcs()} is on. A proxy disable would kill ACS reads too. A targeted 503 on the membership-mutation endpoint leaves user/group reads + earlier mutation phases untouched.
 *
 * <h2>What this pins</h2>
 * <ol>
 * <li>User-mapping + group-creation phases succeed (the 503 only affects /group-members POST).</li>
 * <li>The first membership POST hits the 503 → ClientException propagates → sync future fails.</li>
 * <li>Fewer than {@link #TOTAL_USERS} memberships landed — proves the membership phase aborted partway through (or immediately) rather than completing despite the fault.</li>
 * </ol>
 */
@Slf4j
public class GroupMembersMappingNonTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    private static final int TOTAL_USERS = 100;
    private static final String SHARED_GROUP_ID = "groupShared";

    /**
     * Outer wait for the sync future to terminate (with an exception). The membership phase runs after user-mapping + group-creation, so the failure surfaces a few seconds in. 60 s is generous — a timeout here would mask a "sync hung instead of failing" regression.
     */
    private static final long SYNC_FAILURE_TIMEOUT_S = 60L;

    @Test
    void shouldFailWhenNucleusGroupMembersEndpointReturns503() throws Exception
    {
        installAllStubs();

        // Inject the 503 BEFORE triggering sync so it's guaranteed to be active by the time the
        // membership phase starts. Earlier phases (user mappings, groups) succeed normally.
        log.info("[outage-test] Injecting permanent 503 fault on POST {} before triggering sync",
                GROUP_MEMBERS_PATH);
        injectGroupMembersFault();

        long startNanos = System.nanoTime();

        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        assertThatThrownBy(() -> syncCall.get(SYNC_FAILURE_TIMEOUT_S, TimeUnit.SECONDS))
                .as("Sync was expected to fail on 503 from POST /group-members, but it completed "
                        + "cleanly. Either the fault wasn't applied (check priority) or the error "
                        + "was silently swallowed.")
                .isInstanceOf(ExecutionException.class);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[outage-test] Sync failed (as expected) after {} ms", elapsedMs);

        // Verify the membership phase aborted: fewer than TOTAL_USERS memberships were posted.
        List<LoggedRequest> memberPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH)));
        Set<String> createdMemberships = extractMemberships(memberPosts);

        log.info("[outage-test] POST /group-members requests landed: {} (unique memberships: {} / {})",
                memberPosts.size(), createdMemberships.size(), TOTAL_USERS);

        assertThat(createdMemberships.size())
                .as("Expected fewer than %d memberships because the 503 fault should have aborted "
                        + "the sync, but %d landed — fault was ineffective.",
                        TOTAL_USERS, createdMemberships.size())
                .isLessThan(TOTAL_USERS);

        log.info("[outage-test] ✓ Non-tolerable outage correctly aborted sync — {} of {} memberships landed",
                createdMemberships.size(), TOTAL_USERS);
    }

    /**
     * Replace the 200-OK POST /group-members stub with a 503 at higher priority. Other Nucleus mutation endpoints (POST /user-mappings, POST /groups) keep the normal 200 stub so the earlier phases of sync complete normally and the membership phase is where the cut lands.
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
