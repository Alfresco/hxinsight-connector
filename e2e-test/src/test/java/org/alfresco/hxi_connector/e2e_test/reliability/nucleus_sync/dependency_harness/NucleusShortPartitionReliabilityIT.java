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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.dependency_harness;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;

@Slf4j
public class NucleusShortPartitionReliabilityIT extends BaseNucleusSyncReliabilityIT
{
    /**
     * Window during which the {@code nucleusProxy} is disabled. Long enough to burn the first 2–3 retry attempts, short enough that attempt 4 lands after recovery.
     */
    private static final long PARTITION_DURATION_MS = 1_000L;

    /**
     * Outer wait for the sync to complete (covers the post-recovery retry back-off plus the synchronous controller round-trip). Far below the per-attempt WebClient timeout so a misconfigured retry path surfaces as a future timeout rather than a silent green pass.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 20L;

    @Test
    void shouldRecoverWhenNucleusBriefPartitionEndsBeforeRetryBudgetExhausts() throws Exception
    {
        String emailId = "abcd_%s@hyland.com".formatted(UUID.randomUUID());
        createTestUserWithTestEmail(emailId);
        installAllStubs(emailId);

        // Open the partition BEFORE triggering sync — otherwise the first outbound call to Nucleus could
        // land before disable() takes effect on the Toxiproxy listener.
        log.info("[reliability] Disabling nucleusProxy — nucleus-sync ↔ Nucleus partition opens");
        environment().nucleusproxy().disable();

        // Trigger sync on a background thread; startSynchronization() blocks on the controller round-trip.
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment().nucleusSyncClient().startSynchronization());

        try
        {
            Thread.sleep(PARTITION_DURATION_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling nucleusProxy after {} ms partition window", PARTITION_DURATION_MS);
            environment().nucleusproxy().enable();
        }

        // If retries exhausted, this rethrows the ExecutionException — surface that as a clear failure.
        syncCall.get(SYNC_COMPLETION_TIMEOUT_S, TimeUnit.SECONDS);

        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))))
                    .as("nucleus-sync didn't reach /api/users after partition recovery — either retries "
                            + "exhausted before re-enable (extend PARTITION_DURATION_MS up, or shorten the "
                            + "retry budget) or NUCLEUS_BASE_URL isn't routed via toxic-nucleus")
                    .isNotEmpty();
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus-sync didn't reach the user-mappings GET after partition recovery — sync "
                            + "orchestration stopped before the mapping phase")
                    .isNotEmpty();
        });
    }

}
