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

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Hard TCP reset (RST) on the broker -> live-ingester path: Toxiproxy's {@code reset_peer} toxic forces every connection through the proxy to be torn down with an RST instead of a FIN. Held for ~2 seconds, which is short enough that the broker is still considered up but long enough that any in-flight reads/writes from the consumer side blow up.
 *
 * <p>
 * The connector's reconnect machinery should observe the RST as a connection drop, mark the consumer as detached, and reconnect cleanly once the toxic is removed. The repository keeps producing events directly to ActiveMQ throughout (it bypasses Toxiproxy), and the durable subscription holds them until the live-ingester re-attaches.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Completeness</b> — events published before, during, and after the RST window all reach HX Insight; no in-flight event is silently lost when the connection is yanked.</li>
 * <li><b>Liveness</b> — a post-RST sentinel reaches HX Insight, proving the route reconnected on its own.</li>
 * <li><b>No silent drop</b> — DLQ depth stays at {@code 0}; a TCP RST is a transport event, not a message-level error.</li>
 * <li><b>Topic subscription preserved</b> — subscriber count remains {@code >= 1} after recovery.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqResetPeerReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqResetPeerReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    private static final Duration RESET_DURATION = Duration.ofSeconds(2);
    private static final long PUBLISH_GAP_MS = 500L;
    private static final long SETTLE_AFTER_RECOVERY_MS = 3_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldRecoverFromTcpResetWithoutLosingInFlightEvents() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-rst".getBytes());
        Node preRst = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-rst.txt", pre, "text/plain");
        log.info("[reliability] Pre-RST node {} — waiting for baseline ingestion event", preRst.id());
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preRst.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        ToxicPlanner<eu.rekawek.toxiproxy.Proxy> planner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.afterInitialDelayResetPeerForDelay(INITIAL_DELAY, RESET_DURATION));
        log.info("[reliability] Triggering reset_peer toxic in {} for {}", INITIAL_DELAY, RESET_DURATION);
        planner.start();

        Thread.sleep(INITIAL_DELAY.toMillis() + PUBLISH_GAP_MS);

        @Cleanup
        InputStream during = new ByteArrayInputStream("during-rst".getBytes());
        Node duringRst = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "during-rst.txt", during, "text/plain");
        log.info("[reliability] During-RST node {} published while consumer connection is being reset", duringRst.id());

        Thread.sleep(RESET_DURATION.toMillis() + SETTLE_AFTER_RECOVERY_MS);
        planner.stop();

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-rst".getBytes());
        Node postRst = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-rst.txt", post, "text/plain");
        log.info("[reliability] Post-RST node {} — waiting for liveness + completeness", postRst.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postRst.id()))
                    .as("post-RST sentinel must reach HX Insight — failure here means the route did not reconnect")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(duringRst.id()))
                    .as("during-RST node %s (published while connection was being reset) must still be delivered after recovery", duringRst.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("a TCP RST is a transport event and must not move messages to ActiveMQ.DLQ")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should be re-subscribed to %s after the RST", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
