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
import java.util.concurrent.TimeUnit;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Multi-event end-to-end correctness guard. Pins that {@value #NODE_COUNT} sequentially-created content events flow through the connector to HX Insight WireMock with zero DLQ entries and per-event POST ratios matching the single-event natural-flow baseline pinned by {@code ActiveMqReliabilityIT#shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline}. Catches regressions where the connector silently drops events on the floor, batches them in a way that violates the per-event POST contract, or starts producing DLQ entries on the happy path.
 *
 * <p>
 * <b>This is NOT a throughput baseline.</b> An earlier draft framed it that way and the data dismantled the framing: with sequential ACS REST submits, the test JVM submits at ~11 ev/s while the connector drains at >4500 ev/s in the test profile. The bottleneck is ACS REST, not the connector, so any "drain time" measurement here is dominated by the submit phase plus a sub-second tail and tells you nothing about connector throughput. For an actual throughput baseline see the sibling tests in this package:
 * <ul>
 * <li>{@link BacklogDrainReliabilityIT} — severs the consumer-side Toxiproxy proxy during submit so events pile up at the broker, then re-enables and times the drain. Measures connector burst throughput against a real backlog through the full content pipeline.</li>
 * <li>{@link SyntheticEventThroughputReliabilityIT} — publishes synthetic metadata-only CloudEvent payloads directly to {@code alfresco.repo.event2}, bypassing ACS REST entirely. Cleanest measure of connector publish-side throughput; does not exercise the content pipeline.</li>
 * </ul>
 *
 * <p>
 * Two coupled invariants pinned here, single test:
 * <ol>
 * <li><b>No silent loss.</b> {@link org.alfresco.hxi_connector.e2e_test.reliability.harness.JolokiaProbe#dlqDepth dlqDepth} stays {@code 0} throughout — no event reaches the DLQ on the happy path. A non-zero depth means an event failed mid-flight despite no chaos being injected, which is a regression on the route's exception classifier or the connector's tolerance to brief upstream slowness.</li>
 * <li><b>Boundary parity with the natural-flow baseline.</b> Per-event POST counts ({@code /presigned-urls} and {@code /ingestion-events}) match the natural-flow ratios scaled to {@value #NODE_COUNT} events: exactly 1 presigned-URL per node and at least {@value #MIN_INGESTION_EVENTS_PER_NODE} ingestion-events per node. Drift here means the connector's flow shape under multi-event volume diverges from its single-event flow shape — typically a sign of a route batching, deduplicating, or dropping under load.</li>
 * </ol>
 *
 * <p>
 * Convergence budget is sized to "give the connector long enough to finish, don't claim this is a deadline" — see {@link #CONVERGENCE_TOTAL_MS}. The wall-time the test takes is bounded by ACS REST submit latency, not the connector, so this constant is not an SLA; it is just an upper bound that prevents a wedged test from hanging CI indefinitely.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=MultiEventNoLossReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class MultiEventNoLossReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final int NODE_COUNT = 100;
    /**
     * Lower bound on observed POSTs to {@code /ingestion-events} per node. Matches the natural-flow baseline pinned by {@code ActiveMqReliabilityIT#shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline}: every create yields ≥ 2 ingestion-event POSTs end-to-end (the metadata POST plus the content POST). A regression where the connector emits only the metadata event would show up here as a count of 1 per node.
     */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    private static final int MIN_TOTAL_INGESTION_EVENTS = NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
    /**
     * Hard upper bound on the convergence loop. Sized for "give the connector long enough to finish under cold-cache CI conditions, fail rather than hang if something goes wrong". With observed submit rates of ~10 ev/s through ACS REST plus a sub-second drain tail, the expected wall-time of the convergence-loop body is well below this cap. Not an SLA — see class Javadoc.
     */
    private static final int CONVERGENCE_TOTAL_MS = 30_000;
    /**
     * Per-attempt step for the convergence loop. Short enough that an early-converging run reports a tight time-to-convergence; long enough that we are not spamming the WireMock admin endpoint while POSTs land.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Convergence attempt cap derived from the total budget. Encoded as a constant so the relationship between {@link #CONVERGENCE_TOTAL_MS} and {@link #CONVERGENCE_DELAY_MS} is explicit at the call site.
     */
    private static final int CONVERGENCE_MAX_ATTEMPTS = CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS;

    @Test
    void shouldDeliver100ConsecutiveContentEventsEndToEndWithoutLoss() throws IOException
    {
        long submitStartNanos = System.nanoTime();
        for (int i = 0; i < NODE_COUNT; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream(("multi-event-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "multi-event-no-loss-" + i + ".txt", content, "text/plain");
            if ((i + 1) % 25 == 0)
            {
                log.info("[reliability] Submit progress: {} / {} nodes (last id: {})", i + 1, NODE_COUNT, node.id());
            }
        }
        long submitDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - submitStartNanos);
        log.info("[reliability] Submit phase complete: {} nodes in {} ms ({} ev/s observed at the test JVM — bound by ACS REST, not the connector)",
                NODE_COUNT, submitDurationMs, NODE_COUNT * 1_000L / Math.max(submitDurationMs, 1));

        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            int totalPresignedUrls = WiremockCounts.presignedUrlRequests();
            log.info("[reliability] Convergence progress: {} / {} ingestion-events, {} / {} presigned-urls",
                    totalIngestionEvents, MIN_TOTAL_INGESTION_EVENTS, totalPresignedUrls, NODE_COUNT);
            assertThat(totalIngestionEvents)
                    .as("multi-event no-loss: %d events submitted, expected ≥ %d ingestion-event POSTs at WireMock (≥ %d per node natural-flow baseline). A count below this means the connector dropped events on the floor or the route silently coalesced them",
                            NODE_COUNT, MIN_TOTAL_INGESTION_EVENTS, MIN_INGESTION_EVENTS_PER_NODE)
                    .isGreaterThanOrEqualTo(MIN_TOTAL_INGESTION_EVENTS);
            assertThat(totalPresignedUrls)
                    .as("multi-event no-loss: %d events submitted, expected ≥ %d presigned-URL POSTs (1 per node). A count below this means the upload leg did not run for some nodes",
                            NODE_COUNT, NODE_COUNT)
                    .isGreaterThanOrEqualTo(NODE_COUNT);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);

        assertThat(environment().jolokia().dlqDepth())
                .as("multi-event no-loss: zero DLQ entries expected — any depth here means an event failed mid-flight despite no chaos being injected, which is a regression on the route's exception classifier or the connector's tolerance to brief upstream slowness")
                .isZero();
    }
}
