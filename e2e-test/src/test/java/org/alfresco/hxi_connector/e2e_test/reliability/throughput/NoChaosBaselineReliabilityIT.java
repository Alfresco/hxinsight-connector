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
package org.alfresco.hxi_connector.e2e_test.reliability.throughput;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * No-chaos baseline that mirrors the submit shape of {@code RandomChaosSoakReliabilityIT}. Provides an apples-to-apples comparison point for that test and a happy-path regression check at production-reference event volume.
 *
 * <p>
 * Inherits {@code -Dsoak.*} parameters. Submit parallelism can be raised via {@code -Dsoak.submitParallelism=N} to lift the test JVM above the connector's drain rate and observe steady-state backlog.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class NoChaosBaselineReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    private static final int DEFAULT_EVENTS_PER_CHAOS_BLOCK = 50;
    private static final int DEFAULT_EVENTS_FINAL_RECOVERY = 100;
    private static final int DEFAULT_CHAOS_WINDOW_MS = 15_000;
    private static final int DEFAULT_AMQ_DISCONNECT_WINDOW_MS = 10_000;

    private static final int EVENTS_PER_CHAOS_BLOCK = Integer.getInteger("soak.eventsPerChaosBlock", DEFAULT_EVENTS_PER_CHAOS_BLOCK);
    private static final int EVENTS_FINAL_RECOVERY_NODE_COUNT = Integer.getInteger("soak.eventsFinalRecovery", DEFAULT_EVENTS_FINAL_RECOVERY);
    private static final long CHAOS_WINDOW_MS = Integer.getInteger("soak.chaosWindowMs", DEFAULT_CHAOS_WINDOW_MS);
    private static final long AMQ_DISCONNECT_WINDOW_MS = Integer.getInteger("soak.amqDisconnectWindowMs", DEFAULT_AMQ_DISCONNECT_WINDOW_MS);
    /** Submit-side concurrency. 1 = single-threaded ACS-paced shape; higher values fan submission out to push backlog past the connector's drain rate. */
    private static final int SUBMIT_PARALLELISM = Math.max(1, Integer.getInteger("soak.submitParallelism", 1));

    private static final int TOTAL_NODE_COUNT = 4 * EVENTS_PER_CHAOS_BLOCK + EVENTS_FINAL_RECOVERY_NODE_COUNT;
    /** Metadata POST + content POST per node. */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    private static final int EXPECTED_INGESTION_EVENTS = TOTAL_NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;

    private static final long RECOVERY_BETWEEN_BLOCKS_MS = 5_000L;
    /** Single-threaded consumer cost: ~70 ms / event (four sequential HTTP round-trips). */
    private static final int DRAIN_BUDGET_MS_PER_EVENT = 70;
    private static final int DRAIN_SAFETY_FACTOR = 2;
    private static final int DRAIN_SLA_FLOOR_MS = 30_000;
    private static final int CONVERGENCE_TOTAL_MS = Integer.getInteger("soak.chaosDrainSlaMs",
            Math.max(DRAIN_SLA_FLOOR_MS, TOTAL_NODE_COUNT * DRAIN_BUDGET_MS_PER_EVENT * DRAIN_SAFETY_FACTOR));
    private static final int DRAIN_DELAY_MS = 2_000;
    private static final int DRAIN_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / DRAIN_DELAY_MS;

    @Test
    void shouldHoldNoLossInvariantAndDrainBrokerWithoutChaos() throws IOException, InterruptedException
    {
        log.info("[reliability] No-chaos baseline parameters: eventsPerChaosBlock={}, eventsFinalRecovery={}, chaosWindowMs={}, amqDisconnectWindowMs={}, totalEvents={}, submitParallelism={}, drainSlaMs={}",
                EVENTS_PER_CHAOS_BLOCK, EVENTS_FINAL_RECOVERY_NODE_COUNT, CHAOS_WINDOW_MS, AMQ_DISCONNECT_WINDOW_MS, TOTAL_NODE_COUNT, SUBMIT_PARALLELISM, CONVERGENCE_TOTAL_MS);

        long submitStartNanos = System.nanoTime();
        runBaselineBlock(1, EVENTS_PER_CHAOS_BLOCK, "block1", CHAOS_WINDOW_MS);
        Thread.sleep(RECOVERY_BETWEEN_BLOCKS_MS);

        runBaselineBlock(2, EVENTS_PER_CHAOS_BLOCK, "block2", CHAOS_WINDOW_MS);
        Thread.sleep(RECOVERY_BETWEEN_BLOCKS_MS);

        runBaselineBlock(3, EVENTS_PER_CHAOS_BLOCK, "block3", CHAOS_WINDOW_MS);
        Thread.sleep(RECOVERY_BETWEEN_BLOCKS_MS);

        runBaselineBlock(4, EVENTS_PER_CHAOS_BLOCK, "block4", AMQ_DISCONNECT_WINDOW_MS);
        Thread.sleep(RECOVERY_BETWEEN_BLOCKS_MS);

        runBaselineBlock(5, EVENTS_FINAL_RECOVERY_NODE_COUNT, "block5-recovery", 0);

        long submitMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - submitStartNanos);
        long submitRateEvPerSec = TOTAL_NODE_COUNT * 1_000L / Math.max(submitMs, 1);
        log.info("[reliability] All {} events submitted in {} ms ({} ev/s observed at the test JVM, parallelism={}). Entering drain.",
                TOTAL_NODE_COUNT, submitMs, submitRateEvPerSec, SUBMIT_PARALLELISM);

        long drainStartNanos = System.nanoTime();
        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            int totalPresignedUrls = WiremockCounts.presignedUrlRequests();
            int topicInFlight = environment().jolokia().topicInFlightCount(REPO_EVENT_TOPIC);
            int topicEnqueued = environment().jolokia().topicEnqueueCount(REPO_EVENT_TOPIC);
            int topicDequeued = environment().jolokia().topicDequeueCount(REPO_EVENT_TOPIC);
            log.info("[reliability] Drain progress: ingestion={}/≥{}, presigned={}/={}, broker[enq={}, deq={}, inflight={}]",
                    totalIngestionEvents, EXPECTED_INGESTION_EVENTS,
                    totalPresignedUrls, TOTAL_NODE_COUNT,
                    topicEnqueued, topicDequeued, topicInFlight);
            assertThat(topicInFlight)
                    .as("topic %s in-flight count must drain to 0 — non-zero at no-chaos baseline = events dispatched but never ACK'd, which is a regression on the consumer", REPO_EVENT_TOPIC)
                    .isZero();
            assertThat(totalIngestionEvents)
                    .as("expected ≥ %d ingestion-event POSTs (%d per node × %d nodes) — below at no-chaos baseline = silent loss on the happy path",
                            EXPECTED_INGESTION_EVENTS, MIN_INGESTION_EVENTS_PER_NODE, TOTAL_NODE_COUNT)
                    .isGreaterThanOrEqualTo(EXPECTED_INGESTION_EVENTS);
            assertThat(totalPresignedUrls)
                    .as("expected = %d presigned-URL POSTs (1 per node, no chaos = no coalescing tolerance)", TOTAL_NODE_COUNT)
                    .isGreaterThanOrEqualTo(TOTAL_NODE_COUNT);
        }, DRAIN_MAX_ATTEMPTS, DRAIN_DELAY_MS);
        long drainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);

        long totalMs = submitMs + drainMs;
        long overallRateEvPerSec = TOTAL_NODE_COUNT * 1_000L / Math.max(totalMs, 1);
        log.info("[reliability] No-chaos baseline complete: submit={} ms ({} ev/s), drain={} ms, total={} ms ({} ev/s overall)",
                submitMs, submitRateEvPerSec, drainMs, totalMs, overallRateEvPerSec);

        assertThat(environment().jolokia().dlqDepth())
                .as("DLQ must be empty at no-chaos baseline — any depth here means the connector failed an event mid-flight despite no disruption, which is a regression on the route's exception classifier")
                .isZero();
        assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                .as("durable subscription on %s must be 1 — a no-chaos run cannot legitimately lose or duplicate the subscription", REPO_EVENT_TOPIC)
                .isEqualTo(1);
        assertThat(environment().jolokia().brokerHealthy())
                .as("broker must be healthy at end of no-chaos baseline")
                .isTrue();
    }

    private void runBaselineBlock(int blockNumber, int eventCount, String label, long windowMs) throws IOException, InterruptedException
    {
        log.info("[reliability] Block {}/5: submitting {} events, then sleeping until window ({} ms) closes", blockNumber, eventCount, windowMs);
        long blockStartNanos = System.nanoTime();
        submitContentEvents(eventCount, label);
        long submitMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - blockStartNanos);
        long blockSubmitRate = eventCount * 1_000L / Math.max(submitMs, 1);
        int topicInFlight = environment().jolokia().topicInFlightCount(REPO_EVENT_TOPIC);
        int topicEnqueued = environment().jolokia().topicEnqueueCount(REPO_EVENT_TOPIC);
        int topicDequeued = environment().jolokia().topicDequeueCount(REPO_EVENT_TOPIC);
        log.info("[reliability] Block {}/5 submit complete: {} events in {} ms ({} ev/s); broker[enq={}, deq={}, inflight={}]",
                blockNumber, eventCount, submitMs, blockSubmitRate, topicEnqueued, topicDequeued, topicInFlight);

        long remaining = Math.max(0, windowMs - submitMs);
        if (remaining > 0)
        {
            log.info("[reliability] Block {}/5: sleeping {} ms to match chaos-counterpart window timing", blockNumber, remaining);
            Thread.sleep(remaining);
        }
    }

    private void submitContentEvents(int count, String label) throws IOException, InterruptedException
    {
        if (SUBMIT_PARALLELISM == 1)
        {
            submitContentEventsSequentially(count, label);
        }
        else
        {
            submitContentEventsInParallel(count, label, SUBMIT_PARALLELISM);
        }
    }

    private void submitContentEventsSequentially(int count, String label) throws IOException
    {
        for (int i = 0; i < count; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream((label + "-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "baseline-" + label + "-" + i + ".txt", content, "text/plain");
            if ((i + 1) % 100 == 0)
            {
                log.info("[reliability] {} submit progress: {} / {} (last id: {})", label, i + 1, count, node.id());
            }
        }
    }

    private void submitContentEventsInParallel(int count, String label, int parallelism) throws InterruptedException
    {
        AtomicInteger nextThreadId = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(parallelism, runnable -> {
            Thread thread = new Thread(runnable, "baseline-submitter-" + label + "-" + nextThreadId.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        AtomicInteger completed = new AtomicInteger();
        try
        {
            List<Future<?>> futures = new ArrayList<>(count);
            for (int i = 0; i < count; i++)
            {
                int idx = i;
                futures.add(pool.submit(() -> {
                    try (InputStream content = new ByteArrayInputStream((label + "-" + idx).getBytes()))
                    {
                        Node node = environment().repositoryClient()
                                .createNodeWithContent(PARENT_ID, "baseline-" + label + "-" + idx + ".txt", content, "text/plain");
                        int done = completed.incrementAndGet();
                        if (done % 100 == 0)
                        {
                            log.info("[reliability] {} submit progress: {} / {} (last id: {})", label, done, count, node.id());
                        }
                    }
                    catch (IOException e)
                    {
                        throw new UncheckedIOException(e);
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures)
            {
                try
                {
                    future.get();
                }
                catch (ExecutionException e)
                {
                    throw new IllegalStateException("[reliability] Parallel submit failed for " + label + " (completed=" + completed.get() + " / " + count + ")", e.getCause());
                }
            }
        }
        finally
        {
            pool.shutdown();
            if (!pool.awaitTermination(30, TimeUnit.SECONDS))
            {
                pool.shutdownNow();
            }
        }
    }
}
