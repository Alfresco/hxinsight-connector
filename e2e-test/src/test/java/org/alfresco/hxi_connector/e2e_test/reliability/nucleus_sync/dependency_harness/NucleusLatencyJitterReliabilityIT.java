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
import java.util.concurrent.TimeUnit;

import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Actions;
import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncReliabilityIT;

@Slf4j
public class NucleusLatencyJitterReliabilityIT extends BaseNucleusSyncReliabilityIT
{
    /**
     * Generous upper bound for {@code /sync/trigger} to return. Sized far below the WebClient per-attempt timeout so an accidental retry surfaces as a wall-clock overshoot rather than a silent green pass.
     */
    private static final long SYNC_COMPLETION_TIMEOUT_S = 30L;

    /**
     * Lower bound for "the chaos was actually in effect". Without latency the round-trip is ~10 ms; with 300 ± 200 ms downstream latency it inflates by an order of magnitude. A silently-bypassed toxic (e.g. nucleus-sync pointed at the real {@code nucleus} alias instead of {@code toxic-nucleus}) would fall through this floor.
     */
    private static final long EXPECTED_MIN_WALL_CLOCK_MS = 400L;

    @AfterEach
    void removeAllNucleusToxics() throws IOException
    {
        // Enumeration-based cleanup — robust against custom toxic names used by individual tests.
        for (Toxic t : environment().nucleusproxy().toxics().getAll())
        {
            t.remove();
        }
    }

    @Test
    void shouldCompleteSyncWhenNucleusPathHasModerateLatencyAndJitter()
    {
        environment().repositoryClient().createUser(createUser("test1", "abcd@hyland.com"));
        installAllStubs();

        log.info("[reliability] Adding latency+jitter toxic on nucleusProxy (300 ± 200 ms downstream)");
        // .run() — the factory returns a Runnable; without invoking it no toxic is applied (the original
        // bug here silently passed the test with no chaos in effect).
        Actions.addLatencyAndJitter(environment().nucleusproxy()).run();

        long startNanos = System.nanoTime();
        environment().nucleusSyncClient().startSynchronization();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[reliability] Synchronisation under Nucleus latency completed in {} ms", elapsedMs);

        assertThat(elapsedMs)
                .as("Sync took %d ms — far over the %d s budget. Either a retry storm fired (per-attempt "
                        + "timeout tripped) or the wall clock is starved on this runner.",
                        elapsedMs, SYNC_COMPLETION_TIMEOUT_S)
                .isLessThan(TimeUnit.SECONDS.toMillis(SYNC_COMPLETION_TIMEOUT_S));

        assertThat(elapsedMs)
                .as("Sync completed in %d ms — below the expected minimum %d ms under latency injection. "
                        + "The latency toxic likely didn't apply: check NUCLEUS_BASE_URL routes via "
                        + "toxic-nucleus and that addLatencyAndJitter ran against environment().nucleusProxy()",
                        elapsedMs, EXPECTED_MIN_WALL_CLOCK_MS)
                .isGreaterThanOrEqualTo(EXPECTED_MIN_WALL_CLOCK_MS);

        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))))
                    .as("nucleus-sync didn't reach /api/users under latency — sync likely failed mid-call")
                    .isNotEmpty();
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus-sync didn't reach the user-mappings GET under latency — orchestration "
                            + "stopped before the mapping phase, which means slow Nucleus reads were "
                            + "wrongly treated as errors rather than just slow")
                    .isNotEmpty();
        });
    }

    /**
     * Heavier latency than the moderate case — still below the per-attempt WebClient block timeout, so retries must not fire and the sync must complete. Models "Nucleus is under load but reachable".
     */
    @Test
    void shouldCompleteSyncUnderHeavyButTolerableNucleusLatency() throws IOException
    {
        environment().repositoryClient().createUser(createUser("test2", "heavy@hyland.com"));
        installAllStubs();

        // Custom name on purpose — proves the @AfterEach enumeration-based cleanup catches arbitrary
        // toxic names, not just the one the shared helper knows about.
        log.info("[reliability] Adding heavy latency+jitter toxic on nucleusProxy (1500 ± 500 ms downstream)");
        environment().nucleusproxy().toxics()
                .latency("nucleus-heavy-latency", ToxicDirection.DOWNSTREAM, 1500L)
                .setJitter(500L);

        long startNanos = System.nanoTime();
        environment().nucleusSyncClient().startSynchronization();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[reliability] Synchronisation under heavy Nucleus latency completed in {} ms", elapsedMs);

        assertThat(elapsedMs)
                .as("Heavy-latency sync took %d ms, exceeding the %d s budget. Likely cause: WebClient "
                        + "per-attempt timeout fired and triggered a retry storm.",
                        elapsedMs, SYNC_COMPLETION_TIMEOUT_S)
                .isLessThan(TimeUnit.SECONDS.toMillis(SYNC_COMPLETION_TIMEOUT_S));

        RetryUtils.assertWithRetry(() -> assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                .as("nucleus-sync didn't reach the user-mappings GET under heavy latency — the Nucleus "
                        + "read likely timed out and the orchestration aborted")
                .isNotEmpty());
    }

}
