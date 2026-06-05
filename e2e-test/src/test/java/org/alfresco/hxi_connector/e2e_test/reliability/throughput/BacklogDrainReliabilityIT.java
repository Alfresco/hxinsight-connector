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
 * Connector burst throughput baseline against a real backlog. Detaches the consumer via Toxiproxy, pre-stages events through ACS, then re-enables the proxy and times the drain through the full content pipeline.
 *
 * <p>
 * Pair with {@link MultiEventNoLossReliabilityIT} (correctness on a trickle) and {@link SyntheticEventThroughputReliabilityIT} (publish-side baseline, no content path).
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class BacklogDrainReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /** Matches {@code MultiEventNoLossReliabilityIT.NODE_COUNT} so the two are directly comparable (trickle vs burst). */
    private static final int NODE_COUNT = 100;
    /** Metadata + content POST per node. */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    private static final int MIN_TOTAL_INGESTION_EVENTS = NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
    private static final long DRAIN_SLA_MS = 60_000;
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
