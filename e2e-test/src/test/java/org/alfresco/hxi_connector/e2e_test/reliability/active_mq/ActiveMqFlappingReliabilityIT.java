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
 * Flapping proxy: Toxiproxy is randomly disabled and re-enabled in a tight loop for ~20 seconds, simulating an unstable broker hop where the connection drops and recovers many times in quick succession. The threat model is a reconnect storm — if the connector reacts to every disable with a hard re-init, it could exhaust connection pools, leak threads, or lose its durable subscription registration.
 *
 * <p>
 * The repository's own connection bypasses Toxiproxy and keeps publishing throughout. The durable subscription should preserve any messages that land while the consumer is in one of its disabled phases.
 *
 * <p>
 * Asserts:
 * <ul>
 * <li><b>Liveness</b> — the post-flap sentinel reaches HX Insight, proving the route survived the storm.</li>
 * <li><b>Completeness (pre-flap)</b> — the pre-flap baseline reaches HX Insight before the chaos starts.</li>
 * <li><b>No silent drop</b> — DLQ depth stays at {@code 0}; flapping is a transport-level event, not a message-level error.</li>
 * <li><b>Topic subscription preserved</b> — subscriber count remains {@code >= 1} after the storm settles. A zero here would mean the connector dropped its durable subscription registration during the storm and must be manually re-registered.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqFlappingReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqFlappingReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final Duration FLAPPING_DURATION = Duration.ofSeconds(20);
    private static final long SETTLE_AFTER_FLAPPING_MS = 5_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldSurviveFlappingProxyWithoutReconnectStorm() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-flap".getBytes());
        Node preFlap = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-flap.txt", pre, "text/plain");
        log.info("[reliability] Pre-flap sentinel {} — waiting for baseline ingestion event", preFlap.id());
        RetryUtils.retryWithBackoff(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preFlap.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        ToxicPlanner<eu.rekawek.toxiproxy.Proxy> planner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.disableAndEnableProxyContinuously());
        log.info("[reliability] Starting flapping proxy for ~{}", FLAPPING_DURATION);
        planner.start();
        try
        {
            Thread.sleep(FLAPPING_DURATION.toMillis());
        }
        finally
        {
            planner.stop();
        }
        log.info("[reliability] Flapping stopped — waiting {}ms for connector to settle", SETTLE_AFTER_FLAPPING_MS);
        Thread.sleep(SETTLE_AFTER_FLAPPING_MS);

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-flap".getBytes());
        Node postFlap = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-flap.txt", post, "text/plain");
        log.info("[reliability] Post-flap sentinel {} — waiting for liveness signal", postFlap.id());

        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postFlap.id()))
                    .as("liveness: post-flap sentinel must reach HX Insight — failure here means the storm killed the route")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("flapping is a transport-level event and must not move messages to ActiveMQ.DLQ")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should still be subscribed to %s after the storm settles — a zero here would mean the durable subscription registration was lost mid-storm",
                            REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
