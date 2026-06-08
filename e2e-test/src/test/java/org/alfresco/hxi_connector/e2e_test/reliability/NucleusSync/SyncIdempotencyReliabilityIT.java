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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Idempotency property of full sync — running sync twice against an unchanged source state must NOT
 * produce duplicate writes on the second run. This is a fundamental correctness invariant of any
 * reconciler: re-running it should converge, not amplify.
 *
 * <h2>What this catches</h2>
 * Real regressions this test would surface:
 * <ul>
 *   <li>Diff logic that compares the wrong identifier (e.g. uses {@code userName} instead of
 *       {@code externalUserId}) → second run sees "no current mappings" → re-POSTs everything.</li>
 *   <li>{@code getCurrentUserMappings} response not being consumed → diff always thinks Nucleus
 *       is empty → every sync re-creates all mappings.</li>
 *   <li>Stale in-memory caches in nucleus-sync that hold the first run's "to-create" list and
 *       re-emit it on the second trigger.</li>
 *   <li>{@code DELETE} thrash — second sync sees current mappings as "extra" and deletes them.</li>
 * </ul>
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>First sync runs against empty current state → N user-mapping POSTs land on Nucleus.</li>
 *   <li>We capture the posted mapping bodies and re-stub {@code GET /user-mappings} to return them
 *       — simulating "Nucleus now has these mappings persisted".</li>
 *   <li>We reset the WireMock request journal so the second run's counts are isolated.</li>
 *   <li>Second sync runs against the same ACS users + Nucleus IAM users → diff is empty → there
 *       should be ZERO new POSTs and ZERO DELETEs.</li>
 * </ol>
 *
 * <h2>Test sizing</h2>
 * Small ({@value #TOTAL_USERS}) — idempotency is a property, not a scale test. We don't need 1M
 * users to expose a "re-creates every mapping" bug; 50 is plenty and keeps the test fast.
 */
@Slf4j
public class SyncIdempotencyReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    private static final int TOTAL_USERS = 50;

    @Test
    void shouldNotRepeatMutationsWhenSyncReplaysAgainstUnchangedState()
    {
        // ── Run #1: cold sync, empty current state → expect TOTAL_USERS creates ─────────────────
        installColdStartStubs();
        log.info("[idempotency] Triggering first sync (cold start, no existing mappings)");
        environment.nucleusSyncClient().startSynchronization();

        int run1Creates = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))).size();
        log.info("[idempotency] Run #1 created {} user-mapping POSTs", run1Creates);
        assertThat(run1Creates)
                .as("First sync should create at least one user-mapping POST; check that "
                        + "installAcsPeopleStubs + installNucleusIamUsersStubs produced matching emails")
                .isGreaterThan(0);

        // ── Re-stub current state so run #2 sees the mappings as already-persisted ─────────────
        log.info("[idempotency] Re-stubbing GET /user-mappings to reflect post-run-#1 state");
        replaceCurrentMappingsStubWithRun1Result();

        // Reset request journal so run #2's counts aren't polluted by run #1.
        nucleus().resetRequests();

        // ── Run #2: warm sync, same source state → expect ZERO new mutations ───────────────────
        log.info("[idempotency] Triggering second sync (warm, current state matches source)");
        environment.nucleusSyncClient().startSynchronization();

        List<LoggedRequest> run2Creates = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        List<LoggedRequest> run2Deletes = nucleus().find(deleteRequestedFor(
                urlPathMatching(USER_MAPPINGS_PATH + "/.*")));

        log.info("[idempotency] Run #2 produced: {} POSTs, {} DELETEs", run2Creates.size(), run2Deletes.size());

        assertThat(run2Creates)
                .as("Second sync should NOT re-create user mappings already present on Nucleus, but "
                        + "%d POST /user-mappings requests landed. Likely causes: diff logic uses "
                        + "the wrong identifier, getCurrentUserMappings response is ignored, or "
                        + "nucleus-sync has stale 'to-create' state from run #1.",
                        run2Creates.size())
                .isEmpty();

        assertThat(run2Deletes)
                .as("Second sync should NOT delete user mappings — source state is unchanged. "
                        + "%d DELETE /user-mappings/{id} requests landed, suggesting the diff "
                        + "treats persisted mappings as 'extras' to remove.",
                        run2Deletes.size())
                .isEmpty();

        log.info("[idempotency] ✓ Sync is idempotent — second run produced no mutations");
    }

    private void installColdStartStubs()
    {
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USERS);
        installAcsUserGroupsStub();
        installNucleusIamUsersStubs(TOTAL_USERS);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }

    /**
     * Replace the "empty current mappings" stub with one that returns the mappings nucleus-sync
     * just POSTed. WireMock priority makes this stub win over the empty one installed earlier.
     * <p>
     * The current-mappings response shape is the same paged-list envelope used elsewhere:
     * {@code {"items":[{"userId":"iam-N","externalUserId":"userN"}, ...]}}.
     */
    private void replaceCurrentMappingsStubWithRun1Result()
    {
        String itemsJson = IntStream.range(0, TOTAL_USERS)
                .mapToObj(i -> "{\"userId\":\"iam-" + i + "\",\"externalUserId\":\"user" + i + "\"}")
                .collect(Collectors.joining(","));
        String body = "{\"items\":[" + itemsJson + "]}";

        StubMapping currentMappings = nucleus().register(
                get(urlPathEqualTo(USER_MAPPINGS_PATH))
                        .atPriority(SCENARIO_STUB_PRIORITY - 1) // priority 0 > priority 1
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)));
        registeredStubs.add(currentMappings);
    }
}
