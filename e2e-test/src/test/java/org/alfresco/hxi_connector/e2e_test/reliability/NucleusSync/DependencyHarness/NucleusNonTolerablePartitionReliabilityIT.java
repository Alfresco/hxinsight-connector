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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class NucleusNonTolerablePartitionReliabilityIT extends BaseNucleusSyncReliabilityIT {
    /**
     * Window during which the {@code nucleusProxy} stays disabled. Sized to outlive the entire retry
     * budget: attempts at t≈0, 200, 600, 1400, 2400 ms → 3 600 ms guarantees every attempt fails before
     * we re-enable.
     */
    private static final long PARTITION_DURATION_MS = 3_600L;

    /**
     * Outer wait for the background sync future to terminate (in this test, with an exception). Must
     * exceed PARTITION_DURATION + the controller's failure-propagation tail. 20 s is generous on purpose
     * — a future timeout here would mask a real "sync hung instead of failing" regression behind a
     * confusing TimeoutException.
     */
    private static final long SYNC_FAILURE_TIMEOUT_S = 20L;


    @Test
    void shouldFailFastWhenNucleusPartitionOutlastsRetryBudget() throws Exception
    {
        environment().repositoryClient().createUser(new User("test", "test", "abcd@hyland.com"));
        installAllStubs();

        // 1. Open the partition BEFORE triggering sync, so the first Nucleus call fails immediately.
        log.info("[reliability] Disabling nucleusProxy — partition opens (will outlast the retry budget)");
        environment().nucleusproxy().disable();

        // 2. Trigger sync on a background thread; the controller blocks until performFullSync() resolves
        //    (here: with an error, after retries exhaust).
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment().nucleusSyncClient().startSynchronization());

        try
        {
            // 3. Hold the partition past the full retry budget so every attempt hits a closed listener.
            Thread.sleep(PARTITION_DURATION_MS);
        }
        finally
        {
            // 4. Restore the network even though we expect failure — otherwise the @BeforeEach reset in
            //    the next test could itself race against a still-disabled proxy.
            log.info("[reliability] Re-enabling nucleusProxy after {} ms partition window", PARTITION_DURATION_MS);
            environment().nucleusproxy().enable();
        }


        // 6. No mutations should have landed on Nucleus during the outage. The retried calls all failed
        //    at TCP (listener closed), so the WireMock journal must be empty for the mutation endpoints.
        //    A non-empty result here would mean either (a) the partition healed mid-test and orchestration
        //    re-attempted past it, or (b) the proxy wasn't actually controlling that traffic.
        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("Expected zero user-mapping mutations on Nucleus during a total outage, but some "
                            + "POSTs landed — partition likely healed mid-test or proxy routing is wrong")
                    .isEmpty();
            assertThat(nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH))))
                    .as("Expected zero group mutations during a total outage")
                    .isEmpty();
            assertThat(nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH))))
                    .as("Expected zero group-member mutations during a total outage")
                    .isEmpty();
            // /api/users read also must not have succeeded — every attempt was sunk in retries.
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("Expected zero user-mappings GETs to have succeeded during a total outage")
                    .isEmpty();
        });
    }

    private void installAllStubs()
    {
        installAuthResponse();
        installReturnUserWithSameMail();
        installReturnEmptyMapping();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        installMutationEndpoints();
    }

}
