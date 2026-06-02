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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.DependencyHarness;


import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class AlfrescoShortPartitionReliabilityIT extends BaseNucleusSyncReliabilityIT {
    /**
     * How long the {@code acsProxy} stays disabled. Sized to lie inside the nucleus-sync HTTP retry budget
     *      * first attempt fails at t≈0,
     *      * retries fire at t≈200, 600, 1400 , 240, 3400
     *      So 3.6 s partition will exhaust all the retries
     */
    private static final long PARTITION_DURATION_MS = 2_600L;

    /**
     * Settle window after re-enabling the proxy, before assertions run. Covers the post-recovery retry
     * back-off plus the synchronous /sync/trigger response trip.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 20L;


    @Test
    void shouldRecoverWhenAlfrescoBriefPartitionEndsBeforeRetryBudgetExhausts() throws Exception
    {
        installAllStubs();
        // Pre-conditions: user in Alfresco + all Nucleus stubs ready so the only failure axis is the partition.
        environment().repositoryClient().createUser(new User("test", "test", "abcd@hyland.com"));

        // 1. Open the partition BEFORE triggering sync so the first outbound Alfresco call fails immediately
        //    (otherwise the call might land before the disable() takes effect on the Toxiproxy listener).
        log.info("[reliability] Disabling acsProxy — nucleus-sync ↔ ACS partition opens");
        environment().acsProxy().disable();

        // 2. Trigger sync on a background thread because startSynchronization() blocks until the Mono
        //    resolves (the controller doesn't return until performFullSync() completes).
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment().nucleusSyncClient().startSynchronization());

        try
        {
            // 3. Hold the partition for a window shorter than the retry budget.
            Thread.sleep(PARTITION_DURATION_MS);
        }
        finally
        {
            // 4. Heal the partition. The next in-flight retry should succeed against the now-restored path.
            log.info("[reliability] Re-enabling acsProxy after {} ms partition window", PARTITION_DURATION_MS);
            environment().acsProxy().enable();
        }

        // 5. Wait for the sync to complete (post-recovery retry + remaining downstream calls).
        //    If retries exhausted, startSynchronization() would have thrown IllegalStateException
        //    and the future completes exceptionally — surface that as a clear test failure.
        syncCall.get(SYNC_COMPLETION_TIMEOUT_S, TimeUnit.SECONDS);

        // 6. Assert the recovery contract:
        //    - At least one /api/users call landed on the Nucleus mock — proves the post-recovery sync
        //      progressed past the Alfresco-people fetch (the partitioned hop) into the Nucleus phase.
        //    - At least one user-mappings GET landed — proves the orchestration ran end-to-end after recovery.
        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))))
                    .as("nucleus-sync did  call /api/users after partition recovery — "
                            + "either retries didn't exhausted after re-enable (extend PARTITION_DURATION_MS upward, "
                            + "or shorten the retry budget) or ALFRESCO_BASE_URL is routed via toxic-acs")
                    .isNotEmpty();

            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus sync reached somehow to the alfresco instead of network partition "
                            + "sync orchestration was able before the Nucleus mapping phase")
                    .isNotEmpty();
        });
    }

    private void installAllStubs(){
        installMutationEndpointsWithTracking();
        installAuthResponse();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        installReturnEmptyMapping();
        installReturnUserWithSameMail();
    }

}
