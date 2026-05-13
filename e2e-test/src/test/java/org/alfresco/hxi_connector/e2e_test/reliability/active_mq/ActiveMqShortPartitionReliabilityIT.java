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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Short network partition (~2 s) between live-ingester and ActiveMQ: the chaos window is shorter than the broker's heartbeat / reconnect detection budget, so the connector should ride it out without stopping the route or accumulating any DLQ traffic.
 *
 * <p>
 * Differs from {@link ActiveMqPartitionReliabilityIT}'s 30-second partition: that scenario forces a full disconnect/reconnect cycle and exercises the durable-subscription replay path; this one stresses the connector's tolerance of brief broker round-trip blips that, in production, would correspond to a transient network hiccup rather than a real outage.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Liveness</b> — a sentinel published right after the brief partition reaches HX Insight; the route did not stop, and the topic subscription is still alive.</li>
 * <li><b>Completeness</b> — the pre-partition baseline event also reaches HX Insight (proves topology was healthy before the chaos started; failure here means the test is wrong, not the system).</li>
 * <li><b>No silent drop</b> — DLQ depth stays at {@code 0}; brief network blips are not error events and must not be dead-lettered.</li>
 * <li><b>Topic subscription preserved</b> — subscriber count remains {@code >= 1} after the blip.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqShortPartitionReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqShortPartitionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final Duration PARTITION_DURATION = Duration.ofSeconds(2);
    private static final long SETTLE_AFTER_RECOVERY_MS = 2_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldRideOutShortPartitionWithoutStoppingRoute() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-blip".getBytes());
        Node preBlip = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-blip.txt", pre, "text/plain");
        log.info("[reliability] Pre-blip node {} — waiting for baseline ingestion event", preBlip.id());
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preBlip.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        ToxicPlanner<eu.rekawek.toxiproxy.Proxy> planner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.afterInitialDelayDisableProxyForDelay(Duration.ZERO, PARTITION_DURATION));
        log.info("[reliability] Triggering {}-second partition", PARTITION_DURATION.toSeconds());
        planner.start();
        Thread.sleep(PARTITION_DURATION.toMillis() + SETTLE_AFTER_RECOVERY_MS);
        planner.stop();

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-blip".getBytes());
        Node postBlip = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-blip.txt", post, "text/plain");
        log.info("[reliability] Post-blip node {} — waiting for liveness signal", postBlip.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postBlip.id()))
                    .as("liveness: post-blip sentinel must reach HX Insight — failure here means the route stopped after the partition")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("a transient blip must not move messages to ActiveMQ.DLQ")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should still be subscribed to %s after the blip", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
