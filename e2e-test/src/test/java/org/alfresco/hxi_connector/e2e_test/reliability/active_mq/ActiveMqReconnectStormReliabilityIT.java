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
package org.alfresco.hxi_connector.e2e_test.reliability.active_mq;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ActuatorMetricsProbe;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;

/**
 * Reconnect-storm leak guard. Flaps the AMQ proxy 100 times with no traffic and asserts that JVM threads, heap, and loaded-class counts settle back near their pre-storm baseline. Catches per-reconnect leaks (JMS objects, executor threads, regenerated proxies) that scale with the reconnect count.
 *
 * <p>
 * Sibling to {@link ActiveMqFlappingReliabilityIT}: that test asserts liveness under random flap with load; this one is deterministic, no-load, and measures resource deltas directly.
 *
 * <p>
 * Shape: warm-up cycles → baseline snapshot → flap loop → settle → post-storm snapshot → assert bounds on threads, heap, classes, subscriber count, DLQ depth.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqReconnectStormReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    /** Warm-up cycles run before the baseline snapshot. The first reconnect loads ~70 cold classes that subsequent cycles reuse; running warm-up first stops those one-time loads counting as a leak. */
    private static final int WARMUP_CYCLES = 5;
    private static final int FLAP_CYCLES = 100;
    private static final long FLAP_DISABLED_MS = 100L;
    private static final long FLAP_ENABLED_MS = 100L;
    private static final long SETTLE_AFTER_WARMUP_MS = 2_000L;
    private static final long SETTLE_AFTER_FLAP_MS = 5_000L;

    private static final int CONVERGENCE_TOTAL_MS = 5_000;
    private static final int CONVERGENCE_DELAY_MS = 250;
    private static final int CONVERGENCE_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS;

    /** Tolerance for {@code jvm.threads.live}. Covers normal JVM thread churn (GC workers, ForkJoin pool). A per-reconnect thread leak would be ~100, blowing this bound by an order of magnitude. */
    private static final int THREAD_GROWTH_TOLERANCE = 15;
    /** Tolerance for {@code jvm.memory.used{area=heap}}. Heap is noisy (JIT compile, lazy init, caches) so this is a coarse leak detector. */
    private static final double HEAP_GROWTH_FACTOR = 2.0;
    /** Tolerance for {@code jvm.classes.loaded}. Healthy connectors load classes once on boot; per-reconnect proxy regeneration would scale with flap count. */
    private static final int CLASS_GROWTH_TOLERANCE = 50;

    @Test
    void shouldNotLeakResourcesAcrossOneHundredFlapCyclesWithoutLoad() throws IOException, InterruptedException
    {
        ActuatorMetricsProbe metrics = environment().actuatorMetrics();

        log.info("[reliability] Warming reconnect path: {} flap cycles before baseline snapshot", WARMUP_CYCLES);
        flap(WARMUP_CYCLES, 0);
        log.info("[reliability] Warm-up complete — settling {} ms before baseline snapshot", SETTLE_AFTER_WARMUP_MS);
        Thread.sleep(SETTLE_AFTER_WARMUP_MS);

        ResourceSnapshot baseline = snapshot(metrics, "baseline");

        log.info("[reliability] Starting reconnect storm: {} flap cycles ({} ms disabled / {} ms enabled per cycle)",
                FLAP_CYCLES, FLAP_DISABLED_MS, FLAP_ENABLED_MS);
        long stormStartNanos = System.nanoTime();
        flap(FLAP_CYCLES, 25);
        long stormDurationMs = (System.nanoTime() - stormStartNanos) / 1_000_000L;
        log.info("[reliability] Reconnect storm complete in {} ms — settling for {} ms before post-snapshot",
                stormDurationMs, SETTLE_AFTER_FLAP_MS);
        Thread.sleep(SETTLE_AFTER_FLAP_MS);

        RetryUtils.assertWithRetry(() -> {
            ResourceSnapshot post = snapshot(metrics, "post-storm");

            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("after %d flap cycles the connector must be subscribed exactly once on %s — zero means the durable subscription was lost mid-storm; >1 means a previous subscription leaked across a reconnect",
                            FLAP_CYCLES, REPO_EVENT_TOPIC)
                    .isEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no traffic was submitted during the storm; a non-zero DLQ here means the reconnect storm itself produced a poison-pill")
                    .isZero();

            assertThat((int) post.threads)
                    .as("live thread count must settle within +%d of baseline (baseline=%.0f, post=%.0f) — a growth proportional to %d flap cycles points to leaked JMS / transport threads",
                            THREAD_GROWTH_TOLERANCE, baseline.threads, post.threads, FLAP_CYCLES)
                    .isLessThanOrEqualTo((int) baseline.threads + THREAD_GROWTH_TOLERANCE);
            assertThat(post.heapBytes)
                    .as("heap usage must settle within %.1f× of baseline (baseline=%.0f bytes, post=%.0f bytes) — a per-reconnect heap leak would blow this bound by orders of magnitude across %d cycles",
                            HEAP_GROWTH_FACTOR, baseline.heapBytes, post.heapBytes, FLAP_CYCLES)
                    .isLessThanOrEqualTo(baseline.heapBytes * HEAP_GROWTH_FACTOR);
            assertThat((int) post.loadedClasses)
                    .as("loaded class count must settle within +%d of baseline (baseline=%.0f, post=%.0f) — a per-reconnect classloader leak (CGLib / JDK proxy regeneration) would scale linearly with %d flap cycles",
                            CLASS_GROWTH_TOLERANCE, baseline.loadedClasses, post.loadedClasses, FLAP_CYCLES)
                    .isLessThanOrEqualTo((int) baseline.loadedClasses + CLASS_GROWTH_TOLERANCE);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);
    }

    private void flap(int cycles, int progressInterval) throws IOException, InterruptedException
    {
        for (int i = 0; i < cycles; i++)
        {
            environment().activemqProxy().disable();
            Thread.sleep(FLAP_DISABLED_MS);
            environment().activemqProxy().enable();
            Thread.sleep(FLAP_ENABLED_MS);
            if (progressInterval > 0 && (i + 1) % progressInterval == 0)
            {
                log.info("[reliability] Reconnect storm progress: {} / {} cycles", i + 1, cycles);
            }
        }
    }

    private static ResourceSnapshot snapshot(ActuatorMetricsProbe metrics, String label)
    {
        double threads = metrics.gaugeValue("jvm.threads.live");
        double heapBytes = metrics.gaugeValue("jvm.memory.used", "area", "heap");
        double loadedClasses = metrics.gaugeValue("jvm.classes.loaded");
        log.info("[reliability] {} snapshot: threads={}, heap={} bytes, classes={}",
                label, (int) threads, (long) heapBytes, (int) loadedClasses);
        return new ResourceSnapshot(threads, heapBytes, loadedClasses);
    }

    private record ResourceSnapshot(double threads, double heapBytes, double loadedClasses)
    {}
}
