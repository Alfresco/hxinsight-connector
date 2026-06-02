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

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class GroupMembersMappingTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT{
    /**
     * Delay between triggering sync and opening the partition. Larger than the user-mapping
     * variant because group-member sync runs last — user discovery + mapping + group discovery
     * + group creation must all complete first so the outage genuinely overlaps the membership
     * POST phase rather than earlier stages.
     */
    private static final long DELAY_BEFORE_OUTAGE_MS = 300L;

    /**
     * Window during which the {@code nucleusProxy} is disabled. Sized to burn 2–3 retry attempts
     * without exhausting the full 3 s retry budget, so the next attempt after re-enable still
     * lands inside the budget and the sync recovers.
     */
    private static final long OUTAGE_DURATION_MS = 1_000L;

    /** Total users → also total memberships expected against the shared group. */
    private static final int TOTAL_USERS = 100;

    private static final String SHARED_GROUP_ID = "groupShared";

    /** Outer wait for the sync future to complete. */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 60L;


    @Test
    void shouldMapAllGroupMembershipsWhenNucleusOutageHealsBeforeRetryBudgetExhausts() throws Exception
    {
        installAllStubs();
        long startNanos = System.nanoTime();

        // 1. Trigger sync on a background thread — startSynchronization() blocks on the controller round-trip.
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        // 2. Let user-mapping + group-creation phases complete so the outage lands during membership POSTs.
        Thread.sleep(DELAY_BEFORE_OUTAGE_MS);
        log.info("[outage-test] Disabling nucleusProxy — nucleus-sync ↔ Nucleus partition opens mid-membership-sync");
        environment.nucleusproxy().disable();

        try
        {
            Thread.sleep(OUTAGE_DURATION_MS);
        }
        finally
        {
            log.info("[outage-test] Re-enabling nucleusProxy after {} ms outage window", OUTAGE_DURATION_MS);
            environment.nucleusproxy().enable();
        }

        // 3. If retries exhausted, this rethrows the ExecutionException — surface that as a clear failure.
        syncCall.get(SYNC_COMPLETION_TIMEOUT_S, TimeUnit.SECONDS);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[outage-test] Sync completed in {} ms after surviving {} ms Nucleus outage",
                elapsedMs, OUTAGE_DURATION_MS);

        // 4. Verify every (group, user) pair was eventually POSTed.
        List<LoggedRequest> memberPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH)));
        Set<String> createdMemberships = extractMemberships(memberPosts);

        log.info("[outage-test] Total POST /group-members requests: {}, unique memberships created: {}",
                memberPosts.size(), createdMemberships.size());

        assertThat(createdMemberships.size())
                .as("Expected all %d memberships against group '%s' to be mapped after Nucleus outage healed, "
                                + "but only %d were mapped (%d dropped). Either the retry budget was exhausted "
                                + "(shorten OUTAGE_DURATION_MS) or the recovery path is broken.",
                        TOTAL_USERS, SHARED_GROUP_ID, createdMemberships.size(),
                        TOTAL_USERS - createdMemberships.size())
                .isEqualTo(TOTAL_USERS);

        // Spot-check specific memberships to ensure correctness, not just count.
        assertThat(createdMemberships)
                .as("First user (user0) should be a member of %s", SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user0"));
        assertThat(createdMemberships)
                .as("Last user (user%d) should be a member of %s", TOTAL_USERS - 1, SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user" + (TOTAL_USERS - 1)));
        assertThat(createdMemberships)
                .as("Middle user (user%d) should be a member of %s", TOTAL_USERS / 2, SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user" + (TOTAL_USERS / 2)));

        log.info("[outage-test] ✓ All {} memberships against {} successfully mapped despite Nucleus outage!",
                TOTAL_USERS, SHARED_GROUP_ID);
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
