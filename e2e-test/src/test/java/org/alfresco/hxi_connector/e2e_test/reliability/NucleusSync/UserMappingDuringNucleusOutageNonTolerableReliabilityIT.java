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

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Non-tolerable user-mapping outage scenario — inject a Nucleus mutation fault mid-sync so
 * retries exhaust and the sync aborts with a partial set of users mapped.
 *
 * <h2>Why WireMock faults instead of Toxiproxy disable?</h2>
 * With {@code withStubbedAcs()}, both ACS and Nucleus traffic flow through the same Toxiproxy
 * listener ({@code toxic-nucleus:8083}). Calling {@code nucleusproxy().disable()} kills ACS
 * stubs too — the sync would fail during user <em>discovery</em> (read phase), not during
 * user-mapping <em>creation</em> (write phase). WireMock stub replacement selectively breaks
 * only the Nucleus POST /user-mappings endpoint while ACS reads continue.
 *
 * <h2>Expected behaviour</h2>
 * <ol>
 *   <li>Sync starts: reads ACS /people (1M stubbed users) + Nucleus /api/users (1M).</li>
 *   <li>User-mapping POSTs begin — some succeed.</li>
 *   <li>We replace POST /user-mappings with a 503 fault at higher priority.</li>
 *   <li>Retries exhaust against the 503, sync fails.</li>
 *   <li>Fewer than {@link #TOTAL_USER_COUNT} mappings were created.</li>
 * </ol>
 */
@Slf4j
public class UserMappingDuringNucleusOutageNonTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    /**
     * Delay between triggering sync and injecting the fault. Must be long enough for user
     * discovery (reading 1M users from ACS + Nucleus stubs) to complete AND the first batch of
     * user-mapping POSTs to land. 10 s is generous for paginated stub reads.
     */
    private static final long DELAY_BEFORE_FAULT_MS = 5 * 60_000L + 5000;

    /**
     * Duration the fault stays active. Must outlive the retry budget (~2.4 s with the test env's
     * 5 × 200 ms × 2 config) so all retry attempts hit the 503.
     */
    private static final long FAULT_DURATION_MS = 3 * 60_000L;

    /**
     * Outer wait for the sync future to terminate (with an exception). Generous so a "sync hung
     * instead of failing" regression surfaces as a test failure.
     */
    private static final long SYNC_FAILURE_TIMEOUT_S = 20L;

    @Test
    void shouldFailMidSyncWhenNucleusMappingEndpointReturns503() throws Exception
    {
        installAllStubs();
        long startNanos = System.nanoTime();

        // 1. Trigger sync on a background thread.
        CompletableFuture<Void> syncCall = CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        // 2. Let sync read users from both sides and begin POSTing mappings, then inject fault.
        Thread.sleep(DELAY_BEFORE_FAULT_MS);
        log.info("[outage-test] Injecting 503 fault on POST {} — user-mapping creation will fail",
                USER_MAPPINGS_PATH);
        StubMapping faultStub = injectUserMappingFault();

        try
        {
            // 3. Hold the fault active past the retry budget.
            Thread.sleep(FAULT_DURATION_MS);
        }
        finally
        {
            // 4. Remove the fault for clean teardown.
            log.info("[outage-test] Removing 503 fault after {} ms", FAULT_DURATION_MS);
            nucleus().removeStubMapping(faultStub);
        }

        syncCall.get(SYNC_FAILURE_TIMEOUT_S,TimeUnit.MINUTES);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[outage-test] Sync failed (as expected) after {} ms — fault window was {} ms",
                elapsedMs, FAULT_DURATION_MS);

        // 6. Verify the cut happened MID-sync: some mappings landed before the fault, but not all.
        List<LoggedRequest> mappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        Set<String> mappedUserIds = extractMappedUserIds(mappingPosts);

        log.info("[outage-test] user-mapping POSTs landed before/during fault: {} (unique users: {} / {})",
                mappingPosts.size(), mappedUserIds.size(), TOTAL_USER_COUNT);

        assertThat(mappedUserIds)
                .as("Expected at least one user-mapping POST to land before the fault (proves the "
                        + "cut was mid-sync, not pre-sync). Got zero — increase DELAY_BEFORE_FAULT_MS.")
                .isNotEmpty();

        assertThat(mappedUserIds.size())
                .as("Expected fewer than %d mappings to land because the fault outlived the retry "
                        + "budget, but %d did — either FAULT_DURATION_MS is too short or the fault "
                        + "wasn't applied correctly.",
                        TOTAL_USER_COUNT, mappedUserIds.size())
                .isLessThan(TOTAL_USER_COUNT);

        log.info("[outage-test] ✓ Non-tolerable outage correctly aborted sync after {} of {} users mapped",
                mappedUserIds.size(), TOTAL_USER_COUNT);
    }

    /**
     * Replace the normal 200-OK POST /user-mappings stub with a 503 Service Unavailable at
     * higher priority. ACS stubs are unaffected since they use different URL paths.
     */
    private StubMapping injectUserMappingFault()
    {
        StubMapping fault = nucleus().register(
                post(urlEqualTo(USER_MAPPINGS_PATH))
                        .atPriority(SCENARIO_STUB_PRIORITY - 1) // higher priority than the normal stub
                        .willReturn(aResponse()
                                .withStatus(503)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\":\"Service Unavailable — simulated outage\"}")));
        registeredStubs.add(fault);
        return fault;
    }

    private void installAllStubs()
    {
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USER_COUNT);
        installAcsUserGroupsStub();
        installNucleusIamUsersStubs(TOTAL_USER_COUNT);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }
}
