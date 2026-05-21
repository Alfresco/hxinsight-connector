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
 * Connector burst throughput baseline using a real backlog primitive — Option B in the throughput-baseline comparison alongside {@link MultiEventNoLossReliabilityIT} (the multi-event correctness guard) and {@code SyntheticEventThroughputReliabilityIT} (Option C — synthetic-event publishing). The three sit side-by-side in this package while we evaluate which baseline shape gives the most useful per-CI-run signal.
 *
 * <p>
 * The shape:
 * <ol>
 * <li><b>Sever the consumer.</b> {@link org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment#activemqProxy() activemqProxy()}.{@code disable()} cuts the {@code toxic-activemq} listener, severing the live-ingester ↔ broker path. ACS is configured against the broker's direct alias ({@code activemq:61616}), so its own publish path is unaffected — events keep landing on the {@code alfresco.repo.event2} topic where the broker buffers them under the durable subscription {@code LiveIngesterSubscription}.</li>
 * <li><b>Pre-stage the backlog.</b> Submit {@value #NODE_COUNT} content events through the ACS REST API. The submit phase is bounded by ACS REST throughput (~10 ev/s in the test profile) and represents wall-time we pay regardless of which throughput baseline we use; it is not part of the throughput measurement.</li>
 * <li><b>Restore the consumer; time the drain.</b> Re-enable the proxy and start the wall-clock at the {@code enable()} call. The drain duration is measured from "consumer reconnects" to "≥ {@value #MIN_TOTAL_INGESTION_EVENTS} ingestion-event POSTs and ≥ {@value #NODE_COUNT} presigned-URL POSTs observed at WireMock". This is the actual throughput baseline — the connector is consuming a real backlog through the full content pipeline (metadata POST + presigned-URL request + S3 PUT + content POST).</li>
 * </ol>
 *
 * <p>
 * What this measures that {@link MultiEventNoLossReliabilityIT} cannot: with sequential ACS REST submits the connector trivially keeps up with the trickle, so the "drain time" measurement is dominated by ACS REST submit rate and tells you nothing about the connector. Pre-staging events at the broker forces the connector to actually drain a backlog, exposing prefetch / consumer-thread / route-throughput regressions that would otherwise hide behind the ACS↔connector pacing.
 *
 * <p>
 * What this measures that Option C cannot: the full content pipeline. Synthetic metadata-only events skip the ACS download / S3 PUT / content-event-POST chain, so they only exercise the connector's publish-side throughput. Option B exercises everything end-to-end.
 *
 * <p>
 * Empirical sizing notes: observed connector drain rate against a real backlog through the full content pipeline is ~10 ev/s on a healthy local laptop run ({@value #NODE_COUNT} events drained in ~10 s). This is the per-event cost of (ACS download + presigned-URL request + S3 PUT + content-event POST) executed sequentially by a single JMS listener thread. Notably, this is the <i>same rate</i> as ACS REST submit (~10 ev/s observed in {@link MultiEventNoLossReliabilityIT}), and ~500× slower than the metadata-only path measured by {@link SyntheticEventThroughputReliabilityIT} (~5359 ev/s with the INFO-level logging baseline; see that class for the DEBUG-vs-INFO comparison). The takeaway: ACS REST submit and connector drain are paced by the same ACS-side work; the connector's throughput ceiling on the full pipeline is set by the content leg (ACS download + S3 PUT), not by JMS-listener / HTTP-publish overhead. The {@value #DRAIN_SLA_MS} ms cap gives ~6× headroom for CI cold-cache variance,
 * tightenable once we have a corpus of green CI runs.
 *
 * <p>
 * Convergence poll is sized to {@value #CONVERGENCE_DELAY_MS} ms — long enough that the WireMock journal scan inside {@link WiremockCounts#ingestionEvents()} (which is O(N) over captured requests) does not dominate per-attempt cost when N grows into the hundreds, but short enough that the first-success time is a meaningful upper bound on actual drain time.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=BacklogDrainReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class BacklogDrainReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /**
     * Backlog size. Sized to match {@link MultiEventNoLossReliabilityIT#NODE_COUNT NODE_COUNT} so the two tests are directly comparable: the only difference between them then is "trickle vs burst" feeding pattern, isolating the variable. At the observed connector rate of ~10 ev/s, {@value} events drain in ~10 s, which keeps the IT under 30 s wall-time including pre-stage.
     */
    private static final int NODE_COUNT = 100;
    /**
     * Lower bound on observed POSTs to {@code /ingestion-events} per node. Matches the natural-flow baseline pinned by {@code ActiveMqReliabilityIT#shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline}: every create yields ≥ 2 ingestion-event POSTs end-to-end (metadata + content).
     */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    private static final int MIN_TOTAL_INGESTION_EVENTS = NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
    /**
     * SLA for the drain phase only — wall-clock time from "consumer proxy re-enabled" to "convergence threshold met". Sized to ~6× the expected drain at the observed ~10 ev/s rate (~10 s for {@value #NODE_COUNT} events) for CI cold-cache variance; see class Javadoc.
     */
    private static final long DRAIN_SLA_MS = 60_000;
    /**
     * Per-attempt step for the drain convergence loop. Sized to absorb the WireMock journal-scan cost as N grows into the hundreds without making the convergence loop the dominant wall-time component, while still giving a meaningful upper bound on the drain time when convergence happens fast.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    private static final int CONVERGENCE_MAX_ATTEMPTS = (int) (DRAIN_SLA_MS / CONVERGENCE_DELAY_MS);

    @Test
    void shouldDrainBacklogAtConnectorThroughputAfterConsumerReconnect() throws IOException
    {
        log.info("[reliability] Severing toxic-activemq to isolate consumer (ACS continues publishing through activemq:61616 direct)");
        environment().activemqProxy().disable();

        long preStageStart = System.nanoTime();
        for (int i = 0; i < NODE_COUNT; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream(("backlog-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "backlog-drain-" + i + ".txt", content, "text/plain");
            if ((i + 1) % 25 == 0)
            {
                log.info("[reliability] Pre-stage progress: {} / {} (last id: {})", i + 1, NODE_COUNT, node.id());
            }
        }
        long preStageDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - preStageStart);
        log.info("[reliability] Pre-stage complete: {} events buffered at the broker in {} ms ({} ev/s ACS REST submit rate). Consumer still severed. Restoring now and starting drain timer.",
                NODE_COUNT, preStageDurationMs, NODE_COUNT * 1_000L / Math.max(preStageDurationMs, 1));

        long drainStartNanos = System.nanoTime();
        environment().activemqProxy().enable();

        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            int totalPresignedUrls = WiremockCounts.presignedUrlRequests();
            log.info("[reliability] Drain progress: {} / {} ingestion-events, {} / {} presigned-urls",
                    totalIngestionEvents, MIN_TOTAL_INGESTION_EVENTS, totalPresignedUrls, NODE_COUNT);
            assertThat(totalIngestionEvents)
                    .as("backlog drain: %d events pre-staged at the broker, expected ≥ %d ingestion-event POSTs at WireMock (≥ %d per node natural-flow baseline)",
                            NODE_COUNT, MIN_TOTAL_INGESTION_EVENTS, MIN_INGESTION_EVENTS_PER_NODE)
                    .isGreaterThanOrEqualTo(MIN_TOTAL_INGESTION_EVENTS);
            assertThat(totalPresignedUrls)
                    .as("backlog drain: %d events pre-staged, expected ≥ %d presigned-URL POSTs (1 per node)",
                            NODE_COUNT, NODE_COUNT)
                    .isGreaterThanOrEqualTo(NODE_COUNT);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);

        long drainDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);
        log.info("[reliability] Backlog drained: {} events in {} ms ({} ev/s connector drain rate). Pre-stage was {} ms (ACS-bound). End-to-end wall-time {} ms.",
                NODE_COUNT, drainDurationMs, NODE_COUNT * 1_000L / Math.max(drainDurationMs, 1),
                preStageDurationMs, preStageDurationMs + drainDurationMs);

        assertThat(environment().jolokia().dlqDepth())
                .as("backlog drain must produce zero DLQ entries — any depth here means the consumer reconnect produced a redelivery storm or the route's exception classifier regressed")
                .isZero();
    }
}
