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
 * Multi-event correctness. {@value #NODE_COUNT} sequential creates must reach HX Insight with zero DLQ entries and the expected per-event POST ratios (1 presigned-URL and ≥ 2 ingestion-events per node).
 *
 * <p>
 * Not a throughput baseline — submission is paced by ACS REST. See {@link BacklogDrainReliabilityIT} and {@link SyntheticEventThroughputReliabilityIT} for throughput numbers.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class MultiEventNoLossReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final int NODE_COUNT = 100;
    /** Metadata POST + content POST per node. */
    private static final int MIN_INGESTION_EVENTS_PER_NODE = 2;
    private static final int MIN_TOTAL_INGESTION_EVENTS = NODE_COUNT * MIN_INGESTION_EVENTS_PER_NODE;
    private static final int CONVERGENCE_TOTAL_MS = 30_000;
    private static final int CONVERGENCE_DELAY_MS = 1_000;
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
