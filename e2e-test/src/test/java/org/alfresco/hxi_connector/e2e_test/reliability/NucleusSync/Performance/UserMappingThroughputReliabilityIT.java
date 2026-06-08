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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.Performance;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncLargeIngestionIT;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Measures end-to-end throughput of the user-mapping flow at the 1M-user scale.
 * Renamed with the {@code *IT} suffix so Failsafe picks it up under the
 * {@code reliability-tests} profile (the {@code *Tests} suffix was being filtered out).
 */
@Slf4j
public class UserMappingThroughputReliabilityIT extends BaseNucleusSyncLargeIngestionIT {

    private static final long SAMPLE_WINDOW_MS = 5000L;
    private static final long SYNC_HARD_TIMEOUT_MIN = 20L;
    /**
     * Minimum acceptable aggregate throughput, measured as POST /user-mappings
     * REQUESTS per second (not users — each request carries a batch of mappings).
     * Calibrate after first stable runs.
     */
    private static final double MIN_AGGREGATE_THROUGHPUT_REQUESTS_PER_SEC = 1.0;


    @Test
    public void shouldMaintainExpectedThroughput() throws Exception {
        installAllStubs();
        long startNanos = System.nanoTime();
        CompletableFuture<Void> syncTask = CompletableFuture.runAsync(() -> {
            environment.nucleusSyncClient().startSynchronization();
        });

        long previousCount = 0;
        long previousWindowEndNanos = startNanos;
        List<Double> perWindowRequestsPerSec = new ArrayList<>();

        while (!syncTask.isDone())
        {
            Thread.sleep(SAMPLE_WINDOW_MS);

            long now = System.nanoTime();
            long currentCount = countMappingPostRequests();
            long deltaCount = currentCount - previousCount;
            double windowSec = TimeUnit.NANOSECONDS.toMillis(now - previousWindowEndNanos) / 1_000.0;
            double requestsPerSec = windowSec > 0 ? deltaCount / windowSec : 0;

            perWindowRequestsPerSec.add(requestsPerSec);

            log.info("[throughput] window=+{}s, requests-so-far={}, window-rate={} req/sec",
                    (long) windowSec, currentCount, (long) requestsPerSec);

            previousCount = currentCount;
            previousWindowEndNanos = now;
        }

        // Re-raise any sync failure with its real cause. The 20-min timeout is a no-op on
        // the happy path because the busy-wait above already exited on isDone() — kept here
        // purely to surface ExecutionException for failures and as a hard-stop safety net.
        syncTask.get(SYNC_HARD_TIMEOUT_MIN, TimeUnit.MINUTES);
        long totalElapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        long finalRequestCount = countMappingPostRequests();
        double aggregateRequestsPerSec = finalRequestCount * 1_000.0 / Math.max(1, totalElapsedMs);

        // One-shot parse of the final journal — pay the body-parsing cost ONCE here, instead
        // of on every sample. This gives us the true number of mapped users for the
        // correctness gate.
        List<LoggedRequest> mappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        Set<String> mappedUserIds = extractMappedUserIds(mappingPosts);
        double aggregateUsersPerSec = mappedUserIds.size() * 1_000.0 / Math.max(1, totalElapsedMs);

        // Sort samples to compute percentiles. p50 is the typical window; p95 absorbs the
        // tail (GC, stub-registration thread contention etc.).
        Collections.sort(perWindowRequestsPerSec);

        double p50 = percentile(perWindowRequestsPerSec, 50);
        double p95 = percentile(perWindowRequestsPerSec, 95);

        log.info("[throughput] === RESULTS ===");
        log.info("[throughput] Total mapping POSTs : {}", finalRequestCount);
        log.info("[throughput] Total users mapped  : {}", mappedUserIds.size());
        log.info("[throughput] Total elapsed       : {} ms ({} s)", totalElapsedMs, totalElapsedMs / 1000);
        log.info("[throughput] Aggregate rate      : {} req/sec  (= {} users/sec)",
                String.format("%.2f", aggregateRequestsPerSec), String.format("%.2f", aggregateUsersPerSec));
        log.info("[throughput] Per-window p50      : {} req/sec", String.format("%.2f", p50));
        log.info("[throughput] Per-window p95      : {} req/sec", String.format("%.2f", p95));
        log.info("[throughput] Per-window samples  : {}", perWindowRequestsPerSec.size());

        // Correctness gate — without this, a "fast but wrong" run could pass.
        assertThat(mappedUserIds)
                .as("Expected all %d users to be mapped, but only %d distinct externalUserIds "
                                + "appeared in POST bodies (%d dropped).",
                        TOTAL_USER_COUNT, mappedUserIds.size(), TOTAL_USER_COUNT - mappedUserIds.size())
                .hasSize(TOTAL_USER_COUNT);

        // Regression gate — calibrate the threshold after first stable runs.
        assertThat(aggregateRequestsPerSec)
                .as("Aggregate throughput %.2f req/sec is below the minimum acceptable "
                                + "threshold (%s). Either a regression in nucleus-sync, or noisy CI host.",
                        aggregateRequestsPerSec, MIN_AGGREGATE_THROUGHPUT_REQUESTS_PER_SEC)
                .isGreaterThanOrEqualTo(MIN_AGGREGATE_THROUGHPUT_REQUESTS_PER_SEC);
    }

    private double percentile(List<Double> sortedSamples, int p)
    {
        if (sortedSamples.isEmpty()) return 0;
        int idx = Math.min(sortedSamples.size() - 1, (int) Math.ceil((p / 100.0) * sortedSamples.size()) - 1);
        return sortedSamples.get(Math.max(0, idx));
    }

    /**
     * Cheap-ish sample of how many POST /user-mappings requests WireMock has journaled
     * so far. Each request carries a BATCH of mappings, so this is "requests landed",
     * not "users mapped" — see the one-shot extractMappedUserIds() call after the sync
     * completes for the user-level count.
     * <p>
     * Note: the WireMock client API has no count-only endpoint exposed; {@code find()}
     * is the only option and materialises every {@link LoggedRequest}. At 1M users
     * batched at typical sizes (~100/POST) that's ~10k objects per sample — acceptable.
     */
    private long countMappingPostRequests()
    {
        return nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))).size();
    }


    protected void installAllStubs() {
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