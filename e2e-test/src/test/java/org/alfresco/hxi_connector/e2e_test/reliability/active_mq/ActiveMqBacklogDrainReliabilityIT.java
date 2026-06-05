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
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Backlog drain after a broker partition. Events published while the consumer is detached must all reach HX Insight after the proxy is re-enabled, with no DLQ traffic and a preserved subscription.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqBacklogDrainReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final int BACKLOG_SIZE = 3;
    private static final long SETTLE_AFTER_DISABLE_MS = 2_000L;
    private static final long SETTLE_BEFORE_RECOVERY_MS = 2_000L;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDrainBacklogPublishedDuringPartition() throws IOException, InterruptedException
    {
        @Cleanup
        InputStream pre = new ByteArrayInputStream("pre-outage".getBytes());
        Node preOutage = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "pre-outage.txt", pre, "text/plain");
        log.info("[reliability] Pre-outage node {} — waiting for baseline ingestion event", preOutage.id());
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEventsFor(preOutage.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        log.info("[reliability] Disabling Toxiproxy in front of ActiveMQ");
        environment().activemqProxy().disable();
        List<Node> backlog = new ArrayList<>(BACKLOG_SIZE);
        try
        {
            Thread.sleep(SETTLE_AFTER_DISABLE_MS);

            for (int i = 1; i <= BACKLOG_SIZE; i++)
            {
                @Cleanup
                InputStream content = new ByteArrayInputStream(("backlog-" + i).getBytes());
                Node backlogNode = environment().repositoryClient()
                        .createNodeWithContent(PARENT_ID, "backlog-" + i + ".txt", content, "text/plain");
                backlog.add(backlogNode);
                log.info("[reliability] Backlog node {} ({}) published while subscriber is disconnected",
                        i, backlogNode.id());
            }

            Thread.sleep(SETTLE_BEFORE_RECOVERY_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling Toxiproxy");
            environment().activemqProxy().enable();
        }

        RetryUtils.assertWithRetry(() -> {
            for (Node backlogNode : backlog)
            {
                assertThat(WiremockCounts.ingestionEventsFor(backlogNode.id()))
                        .as("backlog node %s (published while subscriber disconnected) must drain to HX Insight after recovery", backlogNode.id())
                        .isGreaterThanOrEqualTo(1);
            }
            assertThat(environment().jolokia().dlqDepth())
                    .as("network chaos must not move messages to ActiveMQ.DLQ — backlog drain is a normal recovery path, not an error path")
                    .isZero();
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("live-ingester should be re-subscribed to %s after recovery", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
