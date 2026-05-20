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
package org.alfresco.hxi_connector.e2e_test.reliability.hxi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Brief network partition (~{@value #PARTITION_SECONDS} s) between live-ingester and the HX Insight WireMock. Mirrors {@link AcsShortPartitionReliabilityIT} on the HXI side: the {@code toxic-hxi} proxy is fully disabled so connect attempts to {@code /presigned-urls}, {@code /ingestion-events}, and the auth {@code /token} endpoint are refused outright.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Bounded failure visibility</b> — an ingestion attempt that lands inside the partition window exhausts {@code @Retryable} attempts plus JMS redelivery and parks on {@code ActiveMQ.DLQ}; nothing is silently dropped.</li>
 * <li><b>Liveness</b> — once the proxy is re-enabled, a sentinel publish flows end-to-end. The route is not stuck.</li>
 * <li><b>Topic subscription preserved</b> — the live-ingester is still subscribed to {@code alfresco.repo.event2} after the partition (HXI chaos must not knock out the JMS subscription).</li>
 * </ul>
 *
 * <p>
 * Carve-out from the matrix's "30 s" wording: same reasoning as {@link AcsShortPartitionReliabilityIT}. Connector-side HXI publish budget is {@code INGESTER_RETRY_ATTEMPTS=2 × ~200 ms} plus {@code STORAGE_LOCATION_RETRY_ATTEMPTS=2 × ~200 ms} plus one JMS redelivery — total {@code < 1 s} before the message DLQs. A 30 s partition is indistinguishable from "HXI is down" at the connector's per-event time scale; bounded-failure visibility is what a {@value #PARTITION_SECONDS} s partition pins. The longer-outage shape is covered process-side by {@link HxiRestartReliabilityIT}.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=HxiShortPartitionReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class HxiShortPartitionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final int PARTITION_SECONDS = 2;
    private static final long SETTLE_AFTER_RECOVERY_MS = 1_000L;
    /**
     * Per-attempt step for the post-partition convergence check. Sized so the default 15-attempt cap on {@link RetryUtils#retryWithBackoff} comfortably covers the bounded-failure path: HXI publish budget plus JMS redelivery plus the partition window plus headroom.
     */
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldDeadLetterMidPartitionEventAndKeepRouteAlive() throws IOException, InterruptedException
    {
        Node victim;
        log.info("[reliability] Disabling toxic-hxi proxy for ~{} s", PARTITION_SECONDS);
        environment().hxiProxy().disable();
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("mid-partition victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "hxi-partition-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-partition node {} published — HXI publish path expected to bounded-fail and DLQ", victim.id());

            Thread.sleep(Duration.ofSeconds(PARTITION_SECONDS).toMillis());
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-hxi proxy");
            environment().hxiProxy().enable();
        }

        Thread.sleep(SETTLE_AFTER_RECOVERY_MS);

        @Cleanup
        InputStream postContent = new ByteArrayInputStream("post-partition sentinel".getBytes());
        Node sentinel = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "hxi-partition-sentinel.txt", postContent, "text/plain");
        log.info("[reliability] Post-partition sentinel {} — verifying liveness + DLQ visibility", sentinel.id());

        final Node finalVictim = victim;
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                    .as("liveness: post-partition sentinel must reach HX Insight — failure here means the route stopped after the HXI partition")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("mid-partition event for objectId=%s must surface on the DLQ — a zero here means the failure was silent (route stuck or message dropped)",
                            finalVictim.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("HXI partition must not knock out the JMS subscription — live-ingester should still be subscribed to %s", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
