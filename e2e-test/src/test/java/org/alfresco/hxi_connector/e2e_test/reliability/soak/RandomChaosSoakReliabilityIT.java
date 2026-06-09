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

import static eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Soak: rotates the connector through five tolerable-chaos blocks (AMQ flap → ACS latency → HXI latency → AMQ disconnect → no-chaos recovery), submitting content events in each, and asserts the no-silent-loss contract holds across the whole sequence.
 *
 * <p>
 * "No silent loss" = every Created event either reaches HX Insight or is explicitly dead-lettered. The test asserts the broker fully drains, the completeness floor (POSTs + DLQ credit), a bounded DLQ ceiling, a 1-subscriber durable subscription, and broker health.
 *
 * <p>
 * Tuned via {@code -Dsoak.*} system properties (event counts and window timings). Standard run is ~3 min; production-reference scale (~10k events, longer windows) is ~30 min.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class RandomChaosSoakReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    private static final int DEFAULT_EVENTS_PER_CHAOS_BLOCK = 50;
    private static final int DEFAULT_EVENTS_FINAL_RECOVERY = 100;
    private static final int DEFAULT_CHAOS_WINDOW_MS = 15_000;
    private static final int DEFAULT_AMQ_DISCONNECT_WINDOW_MS = 10_000;
    /** Single-threaded consumer cost: ~70 ms / event end-to-end (4 sequential HTTP round-trips per event). */
    private static final int DRAIN_BUDGET_MS_PER_EVENT = 70;
    private static final int DRAIN_SAFETY_FACTOR = 2;
    private static final int DRAIN_SLA_FLOOR_MS = 30_000;

    private static final int EVENTS_PER_CHAOS_BLOCK = Integer.getInteger("soak.eventsPerChaosBlock", DEFAULT_EVENTS_PER_CHAOS_BLOCK);
    private static final int EVENTS_FINAL_RECOVERY_NODE_COUNT = Integer.getInteger("soak.eventsFinalRecovery", DEFAULT_EVENTS_FINAL_RECOVERY);
    private static final long CHAOS_WINDOW_MS = Integer.getInteger("soak.chaosWindowMs", DEFAULT_CHAOS_WINDOW_MS);
    private static final long AMQ_DISCONNECT_WINDOW_MS = Integer.getInteger("soak.amqDisconnectWindowMs", DEFAULT_AMQ_DISCONNECT_WINDOW_MS);

    private static final int TOTAL_NODE_COUNT = 4 * EVENTS_PER_CHAOS_BLOCK + EVENTS_FINAL_RECOVERY_NODE_COUNT;
    private static final int CONVERGENCE_TOTAL_MS = Integer.getInteger("soak.chaosDrainSlaMs",
            Math.max(DRAIN_SLA_FLOOR_MS, TOTAL_NODE_COUNT * DRAIN_BUDGET_MS_PER_EVENT * DRAIN_SAFETY_FACTOR));
    /** Metadata POST + content POST per node. */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    /** ~1% of total nodes: tight enough to catch multi-percent loss, loose enough to absorb legitimate redelivery coalescing. */
    private static final int PRESIGNED_URL_LOSS_TOLERANCE = Math.max(3, TOTAL_NODE_COUNT / 100);
    private static final int PRESIGNED_URL_DELIVERY_FLOOR = TOTAL_NODE_COUNT - PRESIGNED_URL_LOSS_TOLERANCE;
    /**
     * DLQ ceiling sized for the worst case where every AMQ-chaos-block event ends up dead-lettered. Anything above means an event was dead-lettered from a non-AMQ block — a real regression.
     */
    private static final int DLQ_TOLERANCE = Math.max(3, 2 * EVENTS_PER_CHAOS_BLOCK);

    private static final long AMQ_FLAP_HALFCYCLE_MS = 500L;
    /** Below the 3 s response timeout so attempts return late instead of timing out. */
    private static final int ACS_LATENCY_MS = 1_500;
    private static final String ACS_LATENCY_TOXIC_NAME = "soak_acs_latency";
    private static final int HXI_LATENCY_MS = 1_500;
    private static final String HXI_LATENCY_TOXIC_NAME = "soak_hxi_latency";
    /** Pause between chaos blocks so in-flight retries drain before the next block starts. */
    private static final long RECOVERY_BETWEEN_CHAOS_MS = 5_000L;

    private static final long TOTAL_CHAOS_BUDGET_MS = 3 * CHAOS_WINDOW_MS + AMQ_DISCONNECT_WINDOW_MS;

    private static final int CONVERGENCE_DELAY_MS = 2_000;
    private static final int CONVERGENCE_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS;

    @Test
    void shouldHoldAllInvariantsUnderRotatingChaos() throws IOException, InterruptedException
    {
        log.info("[reliability] Soak parameters: eventsPerChaosBlock={}, eventsFinalRecovery={}, chaosWindowMs={}, amqDisconnectWindowMs={}, drainSlaMs={}",
                EVENTS_PER_CHAOS_BLOCK, EVENTS_FINAL_RECOVERY_NODE_COUNT, CHAOS_WINDOW_MS, AMQ_DISCONNECT_WINDOW_MS, CONVERGENCE_TOTAL_MS);
        log.info("[reliability] Starting random-chaos soak: {} events across 5 blocks, {} ms total chaos budget", TOTAL_NODE_COUNT, TOTAL_CHAOS_BUDGET_MS);
        long soakStartNanos = System.nanoTime();

        runAmqFlapBlock();
        Thread.sleep(RECOVERY_BETWEEN_CHAOS_MS);

        runAcsLatencyBlock();
        Thread.sleep(RECOVERY_BETWEEN_CHAOS_MS);

        runHxiLatencyBlock();
        Thread.sleep(RECOVERY_BETWEEN_CHAOS_MS);

        runAmqDisconnectBlock();
        Thread.sleep(RECOVERY_BETWEEN_CHAOS_MS);

        runFinalRecoveryBlock();

        long soakSubmitMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - soakStartNanos);
        log.info("[reliability] Soak chaos sequence complete: {} events submitted in {} ms — entering drain convergence",
                TOTAL_NODE_COUNT, soakSubmitMs);

        long drainStartNanos = System.nanoTime();
        int expectedIngestionEvents = TOTAL_NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
        try
        {
            RetryUtils.assertWithRetry(() -> {
                int totalIngestionEvents = WiremockCounts.ingestionEvents();
                int totalPresignedUrls = WiremockCounts.presignedUrlRequests();
                int dlqDepth = environment().jolokia().dlqDepth();
                int topicInFlight = environment().jolokia().topicInFlightCount(REPO_EVENT_TOPIC);
                int topicEnqueued = environment().jolokia().topicEnqueueCount(REPO_EVENT_TOPIC);
                int topicDequeued = environment().jolokia().topicDequeueCount(REPO_EVENT_TOPIC);
                int ingestionAccountedFor = totalIngestionEvents + dlqDepth * MIN_INGESTION_EVENTS_PER_NODE;
                int presignedAccountedFor = totalPresignedUrls + dlqDepth;
                log.info("[reliability] Drain progress: ingestion={} (+dlq×{}={}/≥{}), presigned={} (+dlq={}/≥{}), dlq={}, broker[enq={}, deq={}, inflight={}], jvmThreads[live={}, blocked={}, waiting={}, timedWaiting={}, runnable={}]",
                        totalIngestionEvents, MIN_INGESTION_EVENTS_PER_NODE, ingestionAccountedFor, expectedIngestionEvents,
                        totalPresignedUrls, presignedAccountedFor, PRESIGNED_URL_DELIVERY_FLOOR,
                        dlqDepth, topicEnqueued, topicDequeued, topicInFlight,
                        (int) environment().actuatorMetrics().gaugeValue("jvm.threads.live"),
                        (int) environment().actuatorMetrics().gaugeValue("jvm.threads.states", "state", "blocked"),
                        (int) environment().actuatorMetrics().gaugeValue("jvm.threads.states", "state", "waiting"),
                        (int) environment().actuatorMetrics().gaugeValue("jvm.threads.states", "state", "timed-waiting"),
                        (int) environment().actuatorMetrics().gaugeValue("jvm.threads.states", "state", "runnable"));
                assertThat(topicInFlight)
                        .as("topic %s in-flight count must drain to 0 — non-zero = events dispatched but never ACK'd; broker is still working", REPO_EVENT_TOPIC)
                        .isZero();
                assertThat(ingestionAccountedFor)
                        .as("expected ≥ %d (ingestionEvents + dlq×%d) — below = silent loss not accounted for by DLQ escalation",
                                expectedIngestionEvents, MIN_INGESTION_EVENTS_PER_NODE)
                        .isGreaterThanOrEqualTo(expectedIngestionEvents);
                assertThat(presignedAccountedFor)
                        .as("expected ≥ %d (presignedUrls + dlq) — below = upload-leg events disappeared without DLQ trace",
                                PRESIGNED_URL_DELIVERY_FLOOR)
                        .isGreaterThanOrEqualTo(PRESIGNED_URL_DELIVERY_FLOOR);
            }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);
        }
        finally
        {
            long drainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);
            int finalIngestionEvents = WiremockCounts.ingestionEvents();
            int finalPresignedUrls = WiremockCounts.presignedUrlRequests();
            int finalDlqDepth = environment().jolokia().dlqDepth();
            int finalTopicInFlight = environment().jolokia().topicInFlightCount(REPO_EVENT_TOPIC);
            int finalTopicEnqueued = environment().jolokia().topicEnqueueCount(REPO_EVENT_TOPIC);
            int finalTopicDequeued = environment().jolokia().topicDequeueCount(REPO_EVENT_TOPIC);
            log.info("[reliability] Drain phase ended after {} ms — ingestion={} + dlq×{}={} (expected ≥ {}), presigned={} + dlq={} (expected ≥ {}), dlq={} (≤ {}), broker[enq={}, deq={}, inflight={}]",
                    drainMs, finalIngestionEvents, MIN_INGESTION_EVENTS_PER_NODE, finalIngestionEvents + finalDlqDepth * MIN_INGESTION_EVENTS_PER_NODE, expectedIngestionEvents,
                    finalPresignedUrls, finalPresignedUrls + finalDlqDepth, PRESIGNED_URL_DELIVERY_FLOOR,
                    finalDlqDepth, DLQ_TOLERANCE,
                    finalTopicEnqueued, finalTopicDequeued, finalTopicInFlight);
        }

        assertThat(environment().jolokia().dlqDepth())
                .as("DLQ depth bounded by %d — above = a chaos window exceeded its retry budget or cumulative chaos burned a budget no single block alone would", DLQ_TOLERANCE)
                .isLessThanOrEqualTo(DLQ_TOLERANCE);
        assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                .as("durable subscription on %s must be 1 — zero = lost during AMQ chaos; >1 = leaked across reconnect", REPO_EVENT_TOPIC)
                .isEqualTo(1);
        assertThat(environment().jolokia().brokerHealthy())
                .as("broker must be healthy at end of soak")
                .isTrue();
    }

    private void runAmqFlapBlock() throws IOException, InterruptedException
    {
        log.info("[reliability] Block 1/5: AMQ flap for {} ms ({} ms half-cycles)", CHAOS_WINDOW_MS, AMQ_FLAP_HALFCYCLE_MS);
        long deadline = System.currentTimeMillis() + CHAOS_WINDOW_MS;
        Thread submitter = startSubmitterThread(EVENTS_PER_CHAOS_BLOCK, "block1-amq-flap");
        try
        {
            while (System.currentTimeMillis() < deadline)
            {
                environment().activemqProxy().disable();
                Thread.sleep(AMQ_FLAP_HALFCYCLE_MS);
                environment().activemqProxy().enable();
                Thread.sleep(AMQ_FLAP_HALFCYCLE_MS);
            }
        }
        finally
        {
            environment().activemqProxy().enable();
            submitter.join();
        }
        log.info("[reliability] Block 1/5 complete");
    }

    private void runAcsLatencyBlock() throws IOException, InterruptedException
    {
        log.info("[reliability] Block 2/5: ACS tolerable latency ({} ms) for {} ms", ACS_LATENCY_MS, CHAOS_WINDOW_MS);
        environment().acsProxy().toxics().latency(ACS_LATENCY_TOXIC_NAME, DOWNSTREAM, ACS_LATENCY_MS);
        try
        {
            submitContentEvents(EVENTS_PER_CHAOS_BLOCK, "block2-acs-latency");
            long remaining = Math.max(0, CHAOS_WINDOW_MS - submitOverheadEstimateMs(EVENTS_PER_CHAOS_BLOCK));
            Thread.sleep(remaining);
        }
        finally
        {
            try
            {
                environment().acsProxy().toxics().get(ACS_LATENCY_TOXIC_NAME).remove();
            }
            catch (IOException e)
            {
                log.warn("[reliability] Could not remove ACS latency toxic at end of block 2 — BaseReliabilityIT reset will catch it next test", e);
            }
        }
        log.info("[reliability] Block 2/5 complete");
    }

    private void runHxiLatencyBlock() throws IOException, InterruptedException
    {
        log.info("[reliability] Block 3/5: HXI tolerable latency ({} ms) for {} ms", HXI_LATENCY_MS, CHAOS_WINDOW_MS);
        environment().hxiProxy().toxics().latency(HXI_LATENCY_TOXIC_NAME, DOWNSTREAM, HXI_LATENCY_MS);
        try
        {
            submitContentEvents(EVENTS_PER_CHAOS_BLOCK, "block3-hxi-latency");
            long remaining = Math.max(0, CHAOS_WINDOW_MS - submitOverheadEstimateMs(EVENTS_PER_CHAOS_BLOCK));
            Thread.sleep(remaining);
        }
        finally
        {
            try
            {
                environment().hxiProxy().toxics().get(HXI_LATENCY_TOXIC_NAME).remove();
            }
            catch (IOException e)
            {
                log.warn("[reliability] Could not remove HXI latency toxic at end of block 3 — BaseReliabilityIT reset will catch it next test", e);
            }
        }
        log.info("[reliability] Block 3/5 complete");
    }

    private void runAmqDisconnectBlock() throws IOException, InterruptedException
    {
        log.info("[reliability] Block 4/5: AMQ disconnect for {} ms", AMQ_DISCONNECT_WINDOW_MS);
        environment().activemqProxy().disable();
        try
        {
            submitContentEvents(EVENTS_PER_CHAOS_BLOCK, "block4-amq-disconnect");
            long remaining = Math.max(0, AMQ_DISCONNECT_WINDOW_MS - submitOverheadEstimateMs(EVENTS_PER_CHAOS_BLOCK));
            Thread.sleep(remaining);
        }
        finally
        {
            environment().activemqProxy().enable();
        }
        log.info("[reliability] Block 4/5 complete");
    }

    private void runFinalRecoveryBlock() throws IOException
    {
        log.info("[reliability] Block 5/5: Recovery (no chaos) — submitting {} events", EVENTS_FINAL_RECOVERY_NODE_COUNT);
        submitContentEvents(EVENTS_FINAL_RECOVERY_NODE_COUNT, "block5-recovery");
        log.info("[reliability] Block 5/5 complete");
    }

    private Thread startSubmitterThread(int count, String label)
    {
        Thread submitter = new Thread(() -> {
            try
            {
                submitContentEvents(count, label);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[reliability] Submitter thread failed for " + label, e);
            }
        }, "soak-submitter-" + label);
        submitter.setDaemon(true);
        submitter.start();
        return submitter;
    }

    private void submitContentEvents(int count, String label) throws IOException
    {
        for (int i = 0; i < count; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream((label + "-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "soak-" + label + "-" + i + ".txt", content, "text/plain");
            if ((i + 1) % 25 == 0)
            {
                log.info("[reliability] {} submit progress: {} / {} (last id: {})", label, i + 1, count, node.id());
            }
        }
    }

    /** Estimated submit wall-time at the ACS REST natural rate (~10 ev/s) so chaos windows bracket the events rather than racing the submit phase. */
    private static long submitOverheadEstimateMs(int count)
    {
        return count * 100L;
    }
}
