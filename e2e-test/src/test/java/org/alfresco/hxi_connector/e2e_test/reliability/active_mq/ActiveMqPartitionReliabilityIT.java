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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Mid-stream partition between live-ingester and ActiveMQ. The repository keeps publishing through the broker's direct alias; the connector's view goes dark via Toxiproxy. Asserts that the durable subscription preserves the mid-outage event and the route recovers on its own.
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
        RetryUtils.assertWithRetry(
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
        RetryUtils.assertWithRetry(() -> {
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
