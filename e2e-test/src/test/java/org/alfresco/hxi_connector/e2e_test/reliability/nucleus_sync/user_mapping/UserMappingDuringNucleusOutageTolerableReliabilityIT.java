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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.user_mapping;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncReliabilityIT;

@Slf4j
public class UserMappingDuringNucleusOutageTolerableReliabilityIT extends BaseNucleusSyncReliabilityIT
{
    /**
     * Outer wait for the sync to complete (covers the post-recovery retry back-off plus the synchronous controller round-trip plus the time to actually map {@value #TOTAL_USERS} users). Far below the per-attempt WebClient timeout so a misconfigured retry path surfaces as a future timeout rather than a silent green pass.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 120L;

    /**
     * Delay between triggering sync and opening the partition. Small but non-zero — gives the sync thread enough time to leave the controller and hit the first Nucleus call so the outage genuinely overlaps the mapping work rather than landing before it starts.
     */
    private static final long DELAY_BEFORE_OUTAGE_MS = 20 * 1000L;

    /**
     * Window during which the {@code nucleusProxy} is disabled. Sized to burn 2–3 retry attempts (≈200 ms, 600 ms, 1400 ms in the standard envelope) without exhausting the full 3 s retry budget, so the next attempt after re-enable still lands inside the budget and the sync recovers.
     */
    private static final long OUTAGE_DURATION_MS = 1_000L;

    @Test
    void shouldMapAllUsersWhenNucleusOutageHealsBeforeRetryBudgetExhausts() throws Exception
    {
        installAllStubsSpecific();
        long startNanos = System.nanoTime();

        // 1. Trigger sync on a background thread — startSynchronization() blocks on the controller round-trip.
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment().nucleusSyncClient().startSynchronization());

        // 2. Let the sync make first contact with Nucleus, then open the partition mid-mapping.
        Thread.sleep(DELAY_BEFORE_OUTAGE_MS);
        log.info("[reliability] Disabling nucleusProxy — nucleus-sync ↔ Nucleus partition opens mid-sync");
        environment().nucleusproxy().disable();

        try
        {
            Thread.sleep(OUTAGE_DURATION_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling nucleusProxy after {} ms outage window", OUTAGE_DURATION_MS);
            environment().nucleusproxy().enable();
        }

        // 3. If retries exhausted, this rethrows the ExecutionException — surface that as a clear failure.
        syncCall.get(SYNC_COMPLETION_TIMEOUT_S, TimeUnit.SECONDS);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[reliability] Sync completed in {} ms after surviving {} ms Nucleus outage", elapsedMs, OUTAGE_DURATION_MS);

        // 4. Verify all mappings were created — extract user IDs from POST requests
        List<LoggedRequest> mappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        Set<String> mappedUserIds = extractMappedUserIds(mappingPosts);

        log.info("[reliability] Total mapping POST requests: {}, unique users mapped: {}",
                mappingPosts.size(), mappedUserIds.size());

        // 5. Every user must be mapped — the outage healed in time, so no users should be dropped.
        assertThat(mappedUserIds.size())
                .as("Expected all %d users to be mapped after Nucleus outage healed, but only %d were mapped "
                        + "(%d dropped). Either the retry budget was exhausted (shorten OUTAGE_DURATION_MS) "
                        + "or the recovery path is broken.",
                        TOTAL_USERS, mappedUserIds.size(), TOTAL_USERS - mappedUserIds.size())
                .isEqualTo(TOTAL_USERS);

        // Spot-check specific users to ensure mapping correctness, not just count.
        assertThat(mappedUserIds)
                .as("First user (test-user-0) should be mapped")
                .contains("test-user-0");
        assertThat(mappedUserIds)
                .as("Last user (test-user-%d) should be mapped", TOTAL_USERS - 1)
                .contains("test-user-" + (TOTAL_USERS - 1));
        assertThat(mappedUserIds)
                .as("Middle user (test-user-%d) should be mapped", TOTAL_USERS / 2)
                .contains("test-user-" + (TOTAL_USERS / 2));

        log.info("[reliability] ✓ All {} users successfully mapped despite Nucleus outage — no mappings dropped!", TOTAL_USERS);
    }

    private void installAllStubsSpecific()
    {
        installAuthResponse();
        installReturnEmptyMapping();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        createUsersInRepository(TOTAL_USERS);

        // Now Need to create the stub also for nucleus side
        installNucleusIamUsersStubs(TOTAL_USERS);
        installMutationEndpointsWithTracking();
    }

}
