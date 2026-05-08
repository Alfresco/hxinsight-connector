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
 * Bandwidth-cut on the broker -> live-ingester path: {@code 0 KB/s} downstream throttle for ~5 seconds, applied via Toxiproxy. The connection stays open from the kernel's perspective, but bytes stop flowing — Camel/Spring JMS may either ride it out or treat it as a heartbeat-timeout disconnect; either path must converge to "all events delivered, with bounded duplication" once the throttle is removed.
 *
 * <p>
 * The repository's own ActiveMQ connection bypasses Toxiproxy ({@code activemq:61616} direct), so events keep landing on the topic during the throttle window — the threat is purely on the consumer's side.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Completeness</b> — every event published during and around the throttle window reaches HX Insight after recovery.</li>
 * <li><b>Bounded duplication</b> — at-least-once delivery is acceptable, but a stuck consumer should not produce an unbounded number of redeliveries; cap at {@code MAX_DUPLICATES_PER_NODE} per node.</li>
 * <li><b>No silent drop</b> — DLQ depth stays at {@code 0}; a stalled consumer is not an error event.</li>
 * <li><b>Topic subscription preserved</b> — subscriber count remains {@code >= 1} after recovery.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqBandwidthCutReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqBandwidthCutReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    private static final Duration BANDWIDTH_CUT_DURATION = Duration.ofSeconds(5);
    private static final int EVENTS_DURING_CUT = 3;
    private static final long PUBLISH_GAP_MS = 800L;
    private static final long SETTLE_AFTER_RECOVERY_MS = 2_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * At-least-once is allowed; uncontrolled redelivery is not. The connector's reconnect path can produce a small number of duplicates after the broker decides the consumer is gone, but tens of duplicates would indicate a redelivery loop.
     */
    private static final int MAX_DUPLICATES_PER_NODE = 5;

    @Test
    void shouldDeliverAllEventsWithBoundedDuplicationAfterBandwidthRecovery() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-cut".getBytes());
        Node preCut = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-cut.txt", pre, "text/plain");
        log.info("[reliability] Pre-cut node {} — waiting for baseline ingestion event", preCut.id());
        RetryUtils.retryWithBackoff(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preCut.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        ToxicPlanner<eu.rekawek.toxiproxy.Proxy> planner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.afterInitialDelayCutBandwidthForDelay(INITIAL_DELAY, BANDWIDTH_CUT_DURATION));
        log.info("[reliability] Cutting downstream bandwidth in {} for {}", INITIAL_DELAY, BANDWIDTH_CUT_DURATION);
        planner.start();

        Thread.sleep(INITIAL_DELAY.toMillis());

        List<Node> duringCut = new ArrayList<>(EVENTS_DURING_CUT);
        for (int i = 1; i <= EVENTS_DURING_CUT; i++)
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream(("during-cut-" + i).getBytes());
            Node node = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "during-cut-" + i + ".txt", content, "text/plain");
            duringCut.add(node);
            log.info("[reliability] During-cut node {} ({}) published while bandwidth=0", i, node.id());
            Thread.sleep(PUBLISH_GAP_MS);
        }

        Thread.sleep(BANDWIDTH_CUT_DURATION.toMillis() + SETTLE_AFTER_RECOVERY_MS);
        planner.stop();

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-cut".getBytes());
        Node postCut = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-cut.txt", post, "text/plain");
        log.info("[reliability] Post-cut node {} — waiting for recovery + completeness", postCut.id());

        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postCut.id()))
                    .as("post-cut sentinel must reach HX Insight after bandwidth recovery")
                    .isGreaterThanOrEqualTo(1);
            for (Node node : duringCut)
            {
                int count = WiremockCounts.ingestionEventsFor(node.id());
                assertThat(count)
                        .as("during-cut node %s (published while bandwidth=0) must reach HX Insight after recovery", node.id())
                        .isGreaterThanOrEqualTo(1);
                assertThat(count)
                        .as("during-cut node %s must not be redelivered more than %d times — bounded duplication is acceptable, redelivery storm is not",
                                node.id(), MAX_DUPLICATES_PER_NODE)
                        .isLessThanOrEqualTo(MAX_DUPLICATES_PER_NODE);
            }
            assertThat(environment().jolokia().dlqDepth())
                    .as("a stalled connection must not move messages to ActiveMQ.DLQ")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should still be subscribed to %s after recovery", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
