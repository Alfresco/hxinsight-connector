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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Actions;
import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncReliabilityIT;

@Slf4j
public class AlfrescoLatencyJitterReliabilityIT extends BaseNucleusSyncReliabilityIT
{
    /**
     * Generous upper bound for the synchronous {@code /sync/trigger} call to return. Sized 4× the worst-case wall clock under jitter (~2 s observed) so a moderately slow CI runner doesn't flake the test, while still being far enough below the 5-minute per-attempt timeout that an accidental retry would manifest as a timeout rather than as a green pass.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 30L;
    /**
     * Lower bound for "the chaos was actually in effect". Without latency the round-trip for the people fetch is ~10 ms; with 300 ± 200 ms latency on each chunk it inflates by an order of magnitude. We assert the wall clock is at least this many ms over the baseline so a silently-bypassed toxic (e.g. nucleus-sync accidentally pointed at the real {@code repository} alias instead of {@code toxic-acs}) surfaces as a clear failure rather than a falsely-passing assertion.
     */
    private static final long EXPECTED_MIN_WALL_CLOCK_MS = 400L;

    @AfterEach
    void removeAcsLatencyToxic()
    {
        Actions.removeLatencyAndJitter(environment().acsProxy()).run();
    }

    @Test
    void shouldCompleteSyncWhenAlfrescoPathHasModerateLatencyAndJitter()
    {

        String emailIdForUserToCreate = "abcd@hyland_%s.com".formatted(UUID.randomUUID()); // This must be unique as Nucleus Sync Drops mapping for multiple users with same email ID
        createTestUserWithTestEmail(emailIdForUserToCreate);
        installStubs(emailIdForUserToCreate);

        log.info("[reliability] Adding latency+jitter toxic on acsProxy (300 ± 200 ms downstream)");
        Actions.addLatencyAndJitter(environment().acsProxy()).run();

        long startNanos = System.nanoTime();
        // Synchronous on purpose — under tolerable latency the sync must return cleanly, not blow up
        // into a retry storm. The wall-clock measurement around the call is part of the assertion below.
        environment().nucleusSyncClient().startSynchronization();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[reliability] Synchronisation under latency completed in {} ms", elapsedMs);

        // Belt-and-braces upper-bound check — if we somehow exceeded the test budget the future-style
        // get(timeout) would already have failed for the partition test; here we just assert post-hoc.
        assertThat(elapsedMs)
                .as("Synchronisation took %d ms — far over the budget of %d s. Either nucleus-sync retried "
                        + "(latency tripped a timeout we didn't account for) or the test wall clock is "
                        + "starved on this runner.", elapsedMs, SYNC_COMPLETION_TIMEOUT_S)
                .isLessThan(TimeUnit.SECONDS.toMillis(SYNC_COMPLETION_TIMEOUT_S));

        assertThat(elapsedMs)
                .as("Synchronisation completed in %d ms, which is below the expected minimum %d ms "
                        + "under latency injection. The latency toxic likely didn't apply — check that "
                        + "ALFRESCO_BASE_URL is routed via toxic-acs and that addLatencyAndJitter actually "
                        + "fired against environment().acsProxy()", elapsedMs, EXPECTED_MIN_WALL_CLOCK_MS)
                .isGreaterThanOrEqualTo(EXPECTED_MIN_WALL_CLOCK_MS);

        // End-to-end success contract: orchestration progressed past the latency-affected Alfresco hop
        // into the Nucleus phase. A single in-memory WireMock journal lookup is enough — if the GETs
        // hadn't landed, the sync would have errored on data shape, not silently succeeded.
        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))))
                    .as("nucleus-sync didn't reach /api/users under latency — sync likely failed mid-call")
                    .isNotEmpty();
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus-sync didn't reach the user-mappings GET under latency — orchestration "
                            + "stopped before the Nucleus phase, which means a slow Alfresco read was "
                            + "wrongly treated as an error rather than just slow")
                    .isNotEmpty();
        });
    }

    /**
     * Heavier latency than {@link #shouldCompleteSyncWhenAlfrescoPathHasModerateLatencyAndJitter} — still under the per-attempt WebClient block timeout (5 minutes default), so retries must not fire and the sync must complete. Uses a {@link Actions#addLatencyAndJitter}-style custom toxic because the shared helper is fixed at 300 ± 200 ms.
     *
     * <p>
     * 1.5 s ± 500 ms is "noticeably slow but legal" — covers the case where ACS is under load but not unreachable. If a future change tightens the per-attempt block timeout below this, this test will start failing with a {@code WebClientRequestException} chain and the assertion message above will point at exactly the right cause.
     */
    @Test
    void shouldCompleteSyncUnderHeavyButTolerableLatency() throws IOException
    {
        String emailIdForUserToCreate = "abcd@hyland_%s.com".formatted(UUID.randomUUID()); // This must be unique as Nucleus Sync Drops mapping for multiple users with same email ID
        createTestUserWithTestEmail(emailIdForUserToCreate);
        installStubs(emailIdForUserToCreate);

        // Custom toxic — we want a stronger latency profile than the shared 300 ms helper, but the
        // toxic name is the same so the @AfterEach hook removes it via the standard helper.
        log.info("[reliability] Adding heavy latency+jitter toxic on acsProxy (1500 ± 500 ms downstream)");
        environment().acsProxy().toxics()
                .latency("latency_and_jitter", ToxicDirection.DOWNSTREAM, 1500L)
                .setJitter(500L);

        long startNanos = System.nanoTime();
        environment().nucleusSyncClient().startSynchronization();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[reliability] Synchronisation under heavy latency completed in {} ms", elapsedMs);

        assertThat(elapsedMs)
                .as("Heavy-latency sync took %d ms, exceeding the %d s test budget. Likely cause: the "
                        + "WebClient per-attempt timeout fired and triggered a retry storm.",
                        elapsedMs, SYNC_COMPLETION_TIMEOUT_S)
                .isLessThan(TimeUnit.SECONDS.toMillis(SYNC_COMPLETION_TIMEOUT_S));

        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus-sync didn't reach the user-mappings GET under heavy latency — the "
                            + "Alfresco read likely timed out and the orchestration aborted")
                    .isNotEmpty();
        });
    }

    private void installStubs(String emailId)
    {
        installAuthResponse();
        installReturnUserWithSameMail(emailId);
        installReturnEmptyMapping();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        installMutationEndpoints();
    }

}
