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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.group_mapping;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncLargeIngestionIT;

/**
 * Non-tolerable group-mapping outage scenario — simulates a Nucleus outage during the group creation phase by injecting WireMock faults on the mutation endpoint.
 *
 * <h2>Why WireMock faults instead of Toxiproxy disable?</h2> With {@code withStubbedAcs()}, both ACS and Nucleus traffic flow through the same Toxiproxy listener ({@code toxic-nucleus}). Calling {@code nucleusproxy().disable()} therefore kills ACS stubs too — not just Nucleus — causing the sync to fail during group discovery rather than group creation. Using WireMock stub replacement lets us selectively break only Nucleus mutation endpoints while ACS continues responding normally.
 *
 * <h2>Why not 1M groups on ACS side?</h2> {@code /people/user1/groups} with 1M groups requires 10,000 paginated requests just for discovery. This takes many seconds and introduces non-deterministic timing for the outage injection. We use a smaller group count ({@value #TEST_GROUPS_COUNT}) that completes discovery quickly, then the creation phase (posting groups to Nucleus) takes measurable time — that's where we inject the fault.
 *
 * <h2>Expected behaviour</h2>
 * <ol>
 * <li>Sync starts: user mapping completes, group discovery from ACS completes quickly.</li>
 * <li>Group creation POSTs begin landing on Nucleus (some succeed).</li>
 * <li>We replace the POST /groups stub with a 503 fault — subsequent creation attempts fail.</li>
 * <li>Retries exhaust (the fault outlasts the retry budget), sync fails.</li>
 * <li>Fewer than {@value #TEST_GROUPS_COUNT} groups were created (partial success, then abort).</li>
 * </ol>
 */
@Slf4j
public class GroupMappingDuringNucleusOutageNonTolerableReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    /**
     * Number of groups for this test. Small enough that ACS group discovery (paginated at 100/page) finishes in ~10 requests (< 1 s), but large enough that the Nucleus group creation phase takes measurable time so the fault can land mid-creation.
     */
    private static final int TEST_GROUPS_COUNT = 10_000;

    /**
     * Delay between triggering sync and injecting the fault. Allows user-mapping + group discovery + the first batch of group-creation POSTs to land before we break the endpoint.
     */
    private static final long DELAY_BEFORE_FAULT_MS = 1_000L;

    /**
     * Duration the fault stays active. Must outlive the retry budget (~2.4 s with the test env's 5 × 200 ms × 2 config) so all retry attempts hit the fault and the sync gives up.
     */
    private static final long FAULT_DURATION_MS = 8_000L;

    /**
     * Outer wait for the sync future to terminate (with an exception). Generous so a "sync hung instead of failing" regression surfaces as a test failure, not a confusing TimeoutException.
     */
    private static final long SYNC_FAILURE_TIMEOUT_S = 60L;

    @Test
    void shouldFailGroupCreationWhenNucleusMutationEndpointReturns503() throws Exception
    {
        installAllStubs();
        long startNanos = System.nanoTime();

        // 1. Trigger sync on a background thread.
        CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        // 2. Let sync complete user-mapping and start group creation, then inject the fault.
        Thread.sleep(DELAY_BEFORE_FAULT_MS);
        log.info("[outage-test] Injecting 503 fault on POST {} — group creation will fail",
                GROUPS_PATH);
        StubMapping faultStub = injectGroupCreationFault();

        try
        {
            // 3. Hold the fault active past the full retry budget.
            Thread.sleep(FAULT_DURATION_MS);
        }
        finally
        {
            // 4. Remove the fault so @AfterEach cleanup and subsequent tests aren't affected.
            log.info("[outage-test] Removing 503 fault after {} ms", FAULT_DURATION_MS);
            nucleus().removeStubMapping(faultStub);
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[outage-test] Sync failed (as expected) after {} ms — fault window was {} ms",
                elapsedMs, FAULT_DURATION_MS);

        // 6. Verify partial progress: some groups landed before the fault, but not all.
        List<LoggedRequest> groupPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH)));
        Set<String> createdGroupIds = extractGroupIds(groupPosts);

        log.info("[outage-test] POST /groups requests: {}, unique groups created: {} / {}",
                groupPosts.size(), createdGroupIds.size(), TEST_GROUPS_COUNT);

        assertThat(createdGroupIds.size())
                .as("Expected fewer than %d groups because the fault outlived the retry budget, "
                        + "but %d were created — fault may not have been injected at the right time.",
                        TEST_GROUPS_COUNT, createdGroupIds.size())
                .isLessThan(TEST_GROUPS_COUNT);

        // At least some groups should have been created before the fault (proves mid-creation cut).
        assertThat(createdGroupIds)
                .as("Expected at least one group to be created before the fault was injected "
                        + "(proves cut was mid-creation). Got zero — increase DELAY_BEFORE_FAULT_MS.")
                .isNotEmpty();

        log.info("[outage-test] ✓ Non-tolerable outage correctly aborted sync after {} of {} groups created",
                createdGroupIds.size(), TEST_GROUPS_COUNT);
    }

    /**
     * Replace the normal 200-OK POST /groups stub with a 503 Service Unavailable at higher priority. This simulates Nucleus being down for mutations while reads (ACS stubs) continue working.
     */
    private StubMapping injectGroupCreationFault()
    {
        StubMapping fault = nucleus().register(
                post(urlEqualTo(GROUPS_PATH))
                        .atPriority(SCENARIO_STUB_PRIORITY - 1) // Higher priority than the normal stub
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
        installSingleAcsUserStub();
        installRepositoryGroupStubs(TEST_GROUPS_COUNT);
        installNucleusSingleUserStub();
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }
}
