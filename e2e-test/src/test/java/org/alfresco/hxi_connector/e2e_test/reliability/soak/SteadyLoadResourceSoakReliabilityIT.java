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
package org.alfresco.hxi_connector.e2e_test.reliability.soak;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ActuatorMetricsProbe;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Bounded-resources soak guard: submits {@value #SOAK_NODE_COUNT} content events end-to-end at the natural ACS REST rate (~10 ev/s) and asserts that the live-ingester's bounded resources (live threads, loaded classes) settle back to within tolerance of their pre-load baseline once the load has drained, with the durable subscription preserved and the DLQ empty. Catches per-message resource leaks that scale with work volume — leaked HTTP-client thread workers, classloader-retaining caches, lingering subscribers — and would not surface in short single-shot ITs.
 *
 * <p>
 * Shape: warm-up ({@value #WARMUP_NODE_COUNT} events to flatten the cold-class tail) → baseline snapshot → soak ({@value #SOAK_NODE_COUNT} events end-to-end) → drain → settle → post snapshot. Same pattern as {@code ActiveMqReconnectStormReliabilityIT}; the warm-up phase is what stops cold-cache class loading from masquerading as a leak.
 *
 * <p>
 * Heap is intentionally not asserted. Container-default JVM heap settings + G1's IHOP threshold mean mixed-GC cycles only fire above a few hundred MB of allocation, so neither {@code jvm.memory.used} (conflates retained data with uncollected young-gen garbage) nor {@code jvm.gc.live.data.size} (zero until a mixed GC fires) gives a stable signal at this event count. Heap leaks of the magnitudes that matter (per-event collection growth, classloader retention) co-vary with thread or class growth, which we do assert; the post-soak heap is included in the snapshot log line for diagnostic triage only.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class SteadyLoadResourceSoakReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    /** Pre-baseline events to flatten the cold-class tail (Camel HTTP, Spring Retry, JSON binding, S3 SDK) so {@link #CLASS_GROWTH_TOLERANCE} measures real leaks, not warm-up. */
    private static final int WARMUP_NODE_COUNT = 50;
    private static final int SOAK_NODE_COUNT = 1_000;
    /** Metadata POST + content POST per node — same baseline as {@code MultiEventNoLossReliabilityIT}. */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;

    private static final long SETTLE_AFTER_WARMUP_MS = 2_000L;
    private static final long SETTLE_AFTER_SOAK_MS = 5_000L;

    /** Drain SLA — ~3× the natural ~10 ev/s rate for {@value #SOAK_NODE_COUNT} events to absorb CI variance. */
    private static final int CONVERGENCE_TOTAL_MS = 5 * 60_000;
    private static final int CONVERGENCE_DELAY_MS = 2_000;
    private static final int CONVERGENCE_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS;
    private static final int RESOURCE_CONVERGENCE_TOTAL_MS = 5_000;
    private static final int RESOURCE_CONVERGENCE_DELAY_MS = 250;
    private static final int RESOURCE_CONVERGENCE_MAX_ATTEMPTS = RESOURCE_CONVERGENCE_TOTAL_MS / RESOURCE_CONVERGENCE_DELAY_MS;

    /** Covers normal JVM thread fluctuation (GC workers, ForkJoin commonPool, JIT compile). */
    private static final int THREAD_GROWTH_TOLERANCE = 25;
    /** Covers truly rare exception-class and JIT-emitted method classes the warm-up did not touch. */
    private static final int CLASS_GROWTH_TOLERANCE = 100;

    @Test
    void shouldNotLeakResourcesUnderSteadyLoad() throws IOException
    {
        ActuatorMetricsProbe metrics = environment().actuatorMetrics();

        log.info("[reliability] Warming the full content pipeline with {} events before baseline snapshot", WARMUP_NODE_COUNT);
        long warmupStartNanos = System.nanoTime();
        submitContentEvents(WARMUP_NODE_COUNT, "warmup");
        awaitDrain(WARMUP_NODE_COUNT, WARMUP_NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE);
        long warmupMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - warmupStartNanos);
        log.info("[reliability] Warm-up complete: {} events in {} ms — settling {} ms before baseline snapshot",
                WARMUP_NODE_COUNT, warmupMs, SETTLE_AFTER_WARMUP_MS);
        sleepMs(SETTLE_AFTER_WARMUP_MS, "settling after warm-up");

        ResourceSnapshot baseline = snapshot(metrics, "baseline");

        log.info("[reliability] Starting soak phase: {} events at ACS REST natural rate (~10 ev/s; ~{} s expected)",
                SOAK_NODE_COUNT, SOAK_NODE_COUNT / 10);
        long soakStartNanos = System.nanoTime();
        submitContentEvents(SOAK_NODE_COUNT, "soak");
        long soakSubmitMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - soakStartNanos);
        log.info("[reliability] Soak submit complete: {} events in {} ms ({} ev/s observed at the test JVM)",
                SOAK_NODE_COUNT, soakSubmitMs, SOAK_NODE_COUNT * 1_000L / Math.max(soakSubmitMs, 1));

        int expectedTotalIngestionEvents = WARMUP_NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE + SOAK_NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
        int expectedTotalPresignedUrls = WARMUP_NODE_COUNT + SOAK_NODE_COUNT;
        long drainStartNanos = System.nanoTime();
        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            int totalPresignedUrls = WiremockCounts.presignedUrlRequests();
            log.info("[reliability] Drain progress: {} / {} ingestion-events, {} / {} presigned-urls",
                    totalIngestionEvents, expectedTotalIngestionEvents, totalPresignedUrls, expectedTotalPresignedUrls);
            assertThat(totalIngestionEvents)
                    .as("expected ≥ %d ingestion-event POSTs (≥ %d per node) — below = events dropped under sustained load",
                            expectedTotalIngestionEvents, MIN_INGESTION_EVENTS_PER_NODE)
                    .isGreaterThanOrEqualTo(expectedTotalIngestionEvents);
            assertThat(totalPresignedUrls)
                    .as("expected ≥ %d presigned-URL POSTs (1 per node) — below = upload leg skipped for some nodes",
                            expectedTotalPresignedUrls)
                    .isGreaterThanOrEqualTo(expectedTotalPresignedUrls);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);
        long drainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);
        log.info("[reliability] Drain converged in {} ms — settling {} ms before post-load snapshot",
                drainMs, SETTLE_AFTER_SOAK_MS);

        sleepMs(SETTLE_AFTER_SOAK_MS, "settling after soak drain");

        RetryUtils.assertWithRetry(() -> {
            ResourceSnapshot post = snapshot(metrics, "post-soak");

            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("durable subscription on %s must be 1 — zero = lost mid-soak; >1 = leaked", REPO_EVENT_TOPIC)
                    .isEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no chaos injected — non-zero DLQ means the soak volume itself triggered an exception-classifier mismatch")
                    .isZero();

            assertThat((int) post.threads)
                    .as("threads: baseline=%.0f, post=%.0f, tolerance=%d", baseline.threads, post.threads, THREAD_GROWTH_TOLERANCE)
                    .isLessThanOrEqualTo((int) baseline.threads + THREAD_GROWTH_TOLERANCE);
            assertThat((int) post.loadedClasses)
                    .as("classes: baseline=%.0f, post=%.0f, tolerance=%d", baseline.loadedClasses, post.loadedClasses, CLASS_GROWTH_TOLERANCE)
                    .isLessThanOrEqualTo((int) baseline.loadedClasses + CLASS_GROWTH_TOLERANCE);
        }, RESOURCE_CONVERGENCE_MAX_ATTEMPTS, RESOURCE_CONVERGENCE_DELAY_MS);
    }

    private void submitContentEvents(int count, String label) throws IOException
    {
        for (int i = 0; i < count; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream((label + "-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "soak-" + label + "-" + i + ".txt", content, "text/plain");
            if ((i + 1) % 100 == 0)
            {
                log.info("[reliability] {} submit progress: {} / {} (last id: {})", label, i + 1, count, node.id());
            }
        }
    }

    private void awaitDrain(int submittedCount, int expectedIngestionEvents)
    {
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEvents())
                        .as("warm-up drain: ≥ %d ingestion-event POSTs after %d events", expectedIngestionEvents, submittedCount)
                        .isGreaterThanOrEqualTo(expectedIngestionEvents),
                CONVERGENCE_MAX_ATTEMPTS,
                CONVERGENCE_DELAY_MS);
    }

    private static ResourceSnapshot snapshot(ActuatorMetricsProbe metrics, String label)
    {
        double threads = metrics.gaugeValue("jvm.threads.live");
        double loadedClasses = metrics.gaugeValue("jvm.classes.loaded");
        double usedHeapBytes = metrics.gaugeValue("jvm.memory.used", "area", "heap");
        log.info("[reliability] {} snapshot: threads={}, classes={}, used-heap={} bytes (diagnostic only)",
                label, (int) threads, (int) loadedClasses, (long) usedHeapBytes);
        return new ResourceSnapshot(threads, loadedClasses);
    }

    private static void sleepMs(long millis, String reason)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[reliability] Interrupted while " + reason, e);
        }
    }

    private record ResourceSnapshot(double threads, double loadedClasses)
    {}
}
