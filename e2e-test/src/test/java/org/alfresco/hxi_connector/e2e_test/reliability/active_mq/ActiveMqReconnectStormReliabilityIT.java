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
 * Reconnect-storm leak guard: cycles the {@code toxic-activemq} Toxiproxy listener {@value #FLAP_CYCLES} times with no concurrent traffic and asserts that the connector's bounded JVM resources (live threads, heap, loaded classes) settle back to within tolerance of their pre-storm baseline once the storm is over. Catches resource leaks that scale with the reconnect count — leaked JMS connection/session/consumer objects, leaked executor or scheduler threads, classloader churn from dynamically-generated proxies, file-descriptor leaks on the broker socket — none of which the existing flap test surfaces.
 *
 * <p>
 * Sibling to {@link ActiveMqFlappingReliabilityIT} and intentionally narrower:
 * <ul>
 * <li>{@code ActiveMqFlappingReliabilityIT} runs <i>random</i> concurrent flap toxics for ~20 s while load is in flight, asserts liveness (post-flap sentinel reaches HX Insight) and that the durable-subscription registration survives. Its Javadoc names the leak threats but does not actually <i>measure</i> them.</li>
 * <li>This test runs <i>deterministic</i> sequential flap cycles ({@value #FLAP_CYCLES}) with <i>no</i> traffic and measures the leak threats directly. The deterministic count means a regression that leaks {@code k} resources per reconnect lands as {@code k × FLAP_CYCLES} in the delta, which crosses the assertion thresholds cleanly.</li>
 * </ul>
 *
 * <p>
 * The shape:
 * <ol>
 * <li><b>Warm-up.</b> {@value #WARMUP_CYCLES} flap cycles before baseline is snapshotted. Reconnect paths touch a long tail of normally-cold classes (exception families: {@code EOFException}, {@code ConnectException}, {@code SocketException}; ActiveMQ failover state, JDK proxy regeneration, Spring's {@code DefaultJmsMessageListenerContainer} retry-loop bookkeeping). Without warm-up the first reconnect loads ~70 classes that the subsequent 99 do not, and the {@link #CLASS_GROWTH_TOLERANCE} bound triggers on cold-cache loading rather than a real leak. Warm-up evicts that one-time tail so the bound stays a true linear-leak detector.</li>
 * <li><b>Quiet baseline.</b> {@link BaseReliabilityIT#resetBetweenTests()} has already drained any in-flight retries, purged the DLQ, and verified the connector is subscribed to {@code alfresco.repo.event2} once. After the warm-up loop and a short settle, snapshot the live-ingester's {@code jvm.threads.live}, {@code jvm.memory.used{area=heap}}, and {@code jvm.classes.loaded} gauges via the Spring Boot Actuator probe. These are the upper bounds the post-storm snapshot must come back to.</li>
 * <li><b>Flap loop.</b> {@value #FLAP_CYCLES} sequential iterations of {@code disable() → sleep({@value #FLAP_DISABLED_MS} ms) → enable() → sleep({@value #FLAP_ENABLED_MS} ms)}. Sleep budgets are tuned so each cycle produces a real disconnect-detect → reconnect round trip on the connector side rather than collapsing into a no-op (the broker's NIO transport detects a TCP RST faster than the inactivity timer; {@value #FLAP_DISABLED_MS} ms is comfortably above that). No work is submitted — the broker stays idle on the publish side, the route stays idle on the consume side, so any heap/thread/class growth observed at the end is attributable to the reconnect storm itself.</li>
 * <li><b>Settle.</b> {@value #SETTLE_AFTER_FLAP_MS} ms wall-clock pause after the last {@code enable()} call. Long enough that the connector has completed its final reconnect, GC has a chance to clear short-lived allocations, and any straggler heartbeat threads have parked. Without this gap, the post-snapshot would catch transient state and the assertions would flake.</li>
 * <li><b>Compare.</b> Snapshot the same three gauges, plus the broker-side topic subscriber count via Jolokia and the DLQ depth, and assert the bounds described below.</li>
 * </ol>
 *
 * <p>
 * Assertion bounds (each one corresponds to one class of leak):
 * <ul>
 * <li><b>Live threads.</b> {@code post ≤ baseline + {@value #THREAD_GROWTH_TOLERANCE}}. ActiveMQ's transport thread, JMS session listener, and Camel JMS consumer thread are all expected to recycle on each reconnect; a regression that fails to terminate them on disconnect would land as {@code FLAP_CYCLES} extra threads — an order of magnitude above the tolerance. The tolerance covers normal JVM thread fluctuation (G1 GC workers, ForkJoin commonPool churn).</li>
 * <li><b>Heap.</b> {@code post ≤ baseline × {@value #HEAP_GROWTH_FACTOR}}. Heap is the noisiest gauge — JIT compilation, internal caches, and short-lived allocations between baseline and post can legitimately grow it. {@value #HEAP_GROWTH_FACTOR}× is a coarse leak detector: a reconnect-storm regression that retains a full {@code MessageConsumer} graph per reconnect would scale linearly with {@value #FLAP_CYCLES} and blow this bound by orders of magnitude; normal warmth would not.</li>
 * <li><b>Loaded classes.</b> {@code post ≤ baseline + {@value #CLASS_GROWTH_TOLERANCE}}. Classloader leaks (e.g. CGLib / JDK proxy classes regenerated per reconnect) scale linearly with the reconnect count and are the single most diagnostic gauge — a healthy connector loads its classes once on boot and the count stays flat thereafter. The tolerance covers any truly lazy classes touched for the first time during the storm (e.g. a rarely-exercised reconnect-error path).</li>
 * <li><b>Topic subscribers = 1.</b> The connector reconnected after the final {@code enable()} call and ended up with exactly one durable subscription on {@code alfresco.repo.event2}. Zero would mean the connector lost its subscription mid-storm; greater than one would mean a previous subscription leaked across a reconnect.</li>
 * <li><b>DLQ = 0.</b> No traffic was injected during the storm, so the DLQ must be empty. A non-zero value here would mean the reconnect storm itself produced a poison-pill — typically a half-built JMS message that the connector tried to process during a partial reconnect.</li>
 * </ul>
 *
 * <p>
 * Sizing notes: at {@value #FLAP_DISABLED_MS} ms + {@value #FLAP_ENABLED_MS} ms per cycle the storm phase lasts ~20 s; settle adds {@value #SETTLE_AFTER_FLAP_MS} ms; the assertion convergence loop adds at most {@value #CONVERGENCE_TOTAL_MS} ms. The whole test runs in well under a minute on a healthy laptop. Bumping {@value #FLAP_CYCLES} would tighten leak detection (more flap → bigger delta from a per-cycle leak) but proportionally increase wall-time; 100 was chosen as the smallest count that crosses the {@value #CLASS_GROWTH_TOLERANCE}-class tolerance for a hypothetical 1-class-per-reconnect leak.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqReconnectStormReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqReconnectStormReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    /**
     * Pre-baseline cycles that warm the reconnect path so the actual baseline snapshot is taken against a connector that has already loaded the long tail of normally-cold classes (exception types, ActiveMQ failover internals, JDK / CGLib proxy regeneration). Empirically, the first reconnect loads ~70 classes that subsequent reconnects reuse — without this warm-up, those 70 classes show up as a "leak" against {@link #CLASS_GROWTH_TOLERANCE} even though the steady-state cost per cycle is essentially zero. Five cycles is plenty: the cold tail collapses to zero new classes by the third or fourth cycle on every machine observed.
     */
    private static final int WARMUP_CYCLES = 5;
    private static final int FLAP_CYCLES = 100;
    private static final long FLAP_DISABLED_MS = 100L;
    private static final long FLAP_ENABLED_MS = 100L;
    /**
     * Wall-clock settle after the warm-up loop, before snapshotting baseline. Lets the connector finish its last warm-up reconnect, JIT-compile the freshly-loaded reconnect classes, and let any short-lived helper threads park. Smaller than {@link #SETTLE_AFTER_FLAP_MS} because warm-up has 20× fewer cycles to settle from.
     */
    private static final long SETTLE_AFTER_WARMUP_MS = 2_000L;
    private static final long SETTLE_AFTER_FLAP_MS = 5_000L;

    /**
     * Convergence cap for the post-storm assertion block. Sized large enough that a slow GC pause or a delayed reconnect does not flake the bounds, but small enough that a genuine regression fails fast rather than burning the whole reliability-suite budget.
     */
    private static final int CONVERGENCE_TOTAL_MS = 5_000;
    /**
     * Per-attempt step for the convergence loop. Short enough that a healthy run reports a tight time-to-convergence; long enough that we are not hammering Actuator / Jolokia.
     */
    private static final int CONVERGENCE_DELAY_MS = 250;
    private static final int CONVERGENCE_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS;

    /**
     * Allowed absolute increase in {@code jvm.threads.live} between baseline and post-storm. Empirically the connector grows by ~7 threads under {@link #FLAP_CYCLES}=100 cycles on a healthy laptop (a one-time pool expansion in Spring's {@code DefaultJmsMessageListenerContainer} and ActiveMQ's NIO transport, not a per-flap leak — verified by the count <i>stabilising</i> at the post-storm value rather than continuing to climb). The tolerance is sized at {@code 15} to cover that pool growth plus CI-side variance (G1 GC workers, ForkJoin commonPool, JIT compile threads). A per-reconnect thread leak would scale linearly with the flap count (e.g. 100 leaked threads at 100 cycles) and blow this bound by an order of magnitude.
     */
    private static final int THREAD_GROWTH_TOLERANCE = 15;
    /**
     * Allowed multiplicative increase in {@code jvm.memory.used{area=heap}} between baseline and post-storm. Heap is intrinsically noisy (JIT compile, lazy initialisation, internal caches) so this is a coarse leak detector rather than a memory-budget regression test.
     */
    private static final double HEAP_GROWTH_FACTOR = 2.0;
    /**
     * Allowed absolute increase in {@code jvm.classes.loaded} between baseline and post-storm. Healthy connectors load their classes once on boot; a reconnect-storm regression that regenerates JDK / CGLib proxies per cycle would scale linearly with {@link #FLAP_CYCLES} and blow this bound. The tolerance covers truly lazy classes touched for the first time during the reconnect path itself.
     */
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
