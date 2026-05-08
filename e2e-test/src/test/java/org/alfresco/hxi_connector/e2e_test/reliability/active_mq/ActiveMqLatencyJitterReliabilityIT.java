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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Latency + jitter on the broker -> live-ingester path: Toxiproxy adds ~300 ms downstream latency with ±200 ms jitter for the duration of a sustained ~20-event publish burst. The connection stays open and bytes keep flowing, but message delivery is irregular and round-trips are stretched.
 *
 * <p>
 * The threat model here is reordering, throughput collapse, or a redelivery loop driven by a heartbeat-near-timeout boundary. The connector should keep flushing messages at the throttled rate without losing or unboundedly duplicating events; once latency is removed, throughput should resume normal speed.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Completeness</b> — every one of the ~20 events published during the latency window reaches HX Insight after recovery.</li>
 * <li><b>Bounded resources</b> — the test wall-time stays bounded by the chaos window plus the convergence budget; no thread or connection leak that would drag this past the {@link RetryUtils} budget.</li>
 * <li><b>No silent drop</b> — DLQ depth stays at {@code 0}; latency is not an error event.</li>
 * <li><b>Topic subscription preserved</b> — subscriber count remains {@code >= 1} after recovery.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqLatencyJitterReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqLatencyJitterReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final Duration LATENCY_DURATION = Duration.ofSeconds(15);
    private static final int EVENTS_DURING_LATENCY = 20;
    private static final long PUBLISH_GAP_MS = 600L;
    private static final long SETTLE_AFTER_RECOVERY_MS = 3_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDeliverAllEventsThroughLatencyAndJitter() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-jitter".getBytes());
        Node preJitter = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-jitter.txt", pre, "text/plain");
        log.info("[reliability] Pre-jitter node {} — waiting for baseline ingestion event", preJitter.id());
        RetryUtils.retryWithBackoff(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preJitter.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        ToxicPlanner<eu.rekawek.toxiproxy.Proxy> planner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.afterInitialDelaySetLatencyAndJitterForDelay(Duration.ZERO, LATENCY_DURATION));
        log.info("[reliability] Adding latency+jitter for {}", LATENCY_DURATION);
        planner.start();

        List<Node> duringJitter = new ArrayList<>(EVENTS_DURING_LATENCY);
        for (int i = 1; i <= EVENTS_DURING_LATENCY; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream(("during-jitter-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "during-jitter-" + i + ".txt", content, "text/plain");
            duringJitter.add(node);
            if (i % 5 == 0)
            {
                log.info("[reliability] During-jitter publish progress: {}/{}", i, EVENTS_DURING_LATENCY);
            }
            Thread.sleep(PUBLISH_GAP_MS);
        }

        Thread.sleep(SETTLE_AFTER_RECOVERY_MS);
        planner.stop();

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-jitter".getBytes());
        Node postJitter = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-jitter.txt", post, "text/plain");
        log.info("[reliability] Post-jitter node {} — waiting for completeness across {} events", postJitter.id(), EVENTS_DURING_LATENCY);

        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postJitter.id()))
                    .as("post-jitter sentinel must reach HX Insight after latency removed")
                    .isGreaterThanOrEqualTo(1);
            for (Node node : duringJitter)
            {
                assertThat(WiremockCounts.ingestionEventsFor(node.id()))
                        .as("during-jitter node %s must reach HX Insight; latency must not drop events", node.id())
                        .isGreaterThanOrEqualTo(1);
            }
            assertThat(environment().jolokia().dlqDepth())
                    .as("latency is not an error event and must not move messages to ActiveMQ.DLQ")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should still be subscribed to %s after recovery", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
