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

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Mid-stream network partition between live-ingester and ActiveMQ: the broker is reachable from the repository (which keeps publishing repo events) but unreachable from the live-ingester for the duration of the partition. Verifies that no event is silently dropped while the consumer is detached and that the route resumes on its own once connectivity heals.
 *
 * <p>
 * Shape (three publishes around one disable/enable):
 * <ol>
 * <li><b>Pre-outage</b> — baseline create on a healthy path; must reach HX Insight before the partition starts.</li>
 * <li><b>Partition</b> — disable the Toxiproxy for ~{@value #PARTITION_SECONDS} seconds. While the proxy is disabled, create a <i>mid-outage</i> node. The repository still reaches ActiveMQ directly (it connects to {@code activemq:61616}, not through Toxiproxy), so the repo-event publish lands on the topic even though the live-ingester's subscriber is disconnected. Whether that message is retained and replayed on reconnect depends on the topic subscription being durable.</li>
 * <li><b>Post-outage</b> — a third create after re-enabling the proxy; must reach HX Insight after the partition heals.</li>
 * </ol>
 *
 * <p>
 * Asserts all four reliability invariants exercised by this scenario:
 * <ul>
 * <li><b>Completeness</b> — every committed ACS create (including the mid-outage one) produces at least one ingestion event downstream.</li>
 * <li><b>Backlog drain</b> — an event published while the subscriber is disconnected is still delivered after recovery. A failure here would mean the subscription is non-durable and the broker dropped the message — a silent-drop regression.</li>
 * <li><b>Liveness</b> — once the partition heals, downstream traffic resumes without manual intervention.</li>
 * <li><b>No silent drop</b> — DLQ depth is observable and remains 0 (any drop would land here, not on the floor).</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqPartitionReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqPartitionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final int PARTITION_SECONDS = 30;
    private static final long SETTLE_BEFORE_MID_OUTAGE_MS = 2_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldSurviveActiveMqPartitionMidStream() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-outage".getBytes());
        Node preOutage = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-outage.txt", pre, "text/plain");

        log.info("[reliability] Pre-outage node {} — waiting for baseline ingestion event", preOutage.id());
        RetryUtils.retryWithBackoff(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preOutage.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        log.info("[reliability] Disabling Toxiproxy in front of ActiveMQ for {}s", PARTITION_SECONDS);
        environment().activemqProxy().disable();
        Node midOutage;
        try
        {
            Thread.sleep(SETTLE_BEFORE_MID_OUTAGE_MS);

            @Cleanup
            InputStream mid = new ByteArrayInputStream("mid-outage".getBytes());
            midOutage = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "mid-outage.txt", mid, "text/plain");
            log.info("[reliability] Mid-outage node {} published while subscriber is disconnected — expecting delivery after recovery",
                    midOutage.id());

            Thread.sleep(PARTITION_SECONDS * 1_000L - SETTLE_BEFORE_MID_OUTAGE_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling Toxiproxy");
            environment().activemqProxy().enable();
        }

        @Cleanup
        InputStream post = new ByteArrayInputStream("post-outage".getBytes());
        Node postOutage = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-outage.txt", post, "text/plain");

        log.info("[reliability] Post-outage node {} — waiting for recovery + mid-outage backlog drain", postOutage.id());
        final Node finalMidOutage = midOutage;
        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(postOutage.id()))
                    .as("post-outage node should reach HX Insight after partition heals")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(finalMidOutage.id()))
                    .as("mid-outage node (published while subscriber disconnected) must still be delivered — a zero here means the subscription is non-durable and the broker silently dropped the message")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no message should be dead-lettered during partition + recovery")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should be re-subscribed to %s after recovery", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
