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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.GroupMapping;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncLargeIngestionIT;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class GroupMappingDuringNucleusOutageTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT {
    /**
     * Outer wait for the sync future to complete (covers post-recovery retry back-off plus the
     * synchronous controller round-trip plus the time to actually create all groups). Sized far
     * below any per-attempt WebClient timeout so a misconfigured retry path surfaces as a
     * timeout rather than a silent green pass.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 240L;

    /**
     * Delay between triggering sync and opening the partition. Small but non-zero — lets the
     * sync thread leave the controller, complete user discovery + mapping, and start hitting
     * Nucleus group-creation endpoints so the outage genuinely overlaps the group-mapping work.
     */
    private static final long DELAY_BEFORE_OUTAGE_MS = 10000L;

    /**
     * Window during which the {@code nucleusProxy} is disabled. Sized to burn 2–3 retry attempts
     * (≈200 ms, 600 ms, 1400 ms in the standard envelope) without exhausting the full 3 s retry
     * budget, so the next attempt after re-enable still lands inside the budget and the sync
     * recovers.
     */
    private static final long OUTAGE_DURATION_MS = 2_000L;

    @Test
    void shouldMapAllGroupsWhenNucleusOutageHealsBeforeRetryBudgetExhausts() throws Exception
    {
        installAllStubs();
        long startNanos = System.nanoTime();

        // 1. Trigger sync on a background thread — startSynchronization() blocks on the controller round-trip.
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        // 2. Let the sync make first contact with Nucleus and start creating groups, then open the partition.
        Thread.sleep(DELAY_BEFORE_OUTAGE_MS);
        log.info("[outage-test] Disabling nucleusProxy — nucleus-sync ↔ Nucleus partition opens mid-group-sync");
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

        // 4. Verify all group creations were eventually persisted — extract group IDs from POST bodies.
        List<LoggedRequest> groupPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH)));
        Set<String> createdGroupIds = extractGroupIds(groupPosts);

        log.info("[outage-test] Total POST /groups requests: {}, unique groups created: {}",
                groupPosts.size(), createdGroupIds.size());

        // 5. Every group must be created — the outage healed in time, so no groups should be dropped.
        assertThat(createdGroupIds.size())
                .as("Expected all %d groups to be mapped after Nucleus outage healed, but only %d were mapped "
                                + "(%d dropped). Either the retry budget was exhausted (shorten OUTAGE_DURATION_MS) "
                                + "or the recovery path is broken.",
                        TOTAL_GROUPS_COUNT, createdGroupIds.size(), TOTAL_GROUPS_COUNT - createdGroupIds.size())
                .isEqualTo(TOTAL_GROUPS_COUNT);

        // Spot-check specific groups to ensure correctness, not just count.
        assertThat(createdGroupIds)
                .as("First group (group0) should be mapped")
                .contains("group0");
        assertThat(createdGroupIds)
                .as("Last group (group%d) should be mapped", TOTAL_GROUPS_COUNT - 1)
                .contains("group" + (TOTAL_GROUPS_COUNT - 1));
        assertThat(createdGroupIds)
                .as("Middle group (group%d) should be mapped", TOTAL_GROUPS_COUNT / 2)
                .contains("group" + (TOTAL_GROUPS_COUNT / 2));

        log.info("[outage-test] ✓ All {} groups successfully mapped despite Nucleus outage — no mappings dropped!",
                TOTAL_GROUPS_COUNT);
    }

    private void installAllStubs()
    {
        installNucleusAuthStub();
        installSingleAcsUserStub();
        installNucleusSingleUserStub(); // Only Single user is present
        installRepositoryGroupStubs(TOTAL_GROUPS_COUNT);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }

}
