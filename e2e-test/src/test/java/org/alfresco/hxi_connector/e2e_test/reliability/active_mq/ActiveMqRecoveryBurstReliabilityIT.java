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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;

/**
 * Recovery boundary under load. Stages a backlog on the durable subscription with the connector detached, then re-enables AMQ and publishes another burst on top so it overlaps with the replay. Asserts no loss, no DLQ entries, and bounded duplication.
 *
 * <p>
 * Synthetic events skip the content block, so the S3 / presigned-URL path stays out of the equation — the test isolates the AMQ recovery boundary from upload variance.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqRecoveryBurstReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    private static final int BACKLOG_COUNT = 2_000;
    private static final int BURST_COUNT = 2_000;
    private static final int TOTAL_COUNT = BACKLOG_COUNT + BURST_COUNT;

    private static final long DRAIN_SLA_MS = 30_000;
    private static final int CONVERGENCE_DELAY_MS = 200;
    private static final int CONVERGENCE_MAX_ATTEMPTS = (int) (DRAIN_SLA_MS / CONVERGENCE_DELAY_MS);

    /** At-least-once delivery permits some duplication when a message in mid-dispatch is redelivered after reconnect; above this points to a redelivery storm. */
    private static final int UPPER_BOUND_DUPLICATION_PCT = 10;
    private static final int UPPER_BOUND_TOTAL_POSTS = TOTAL_COUNT + (TOTAL_COUNT * UPPER_BOUND_DUPLICATION_PCT / 100);

    @Test
    void shouldDrainBacklogPlusBurstAcrossRecoveryWithoutLossOrStorm() throws IOException
    {
        log.info("[reliability] Disabling Toxiproxy in front of ActiveMQ — connector goes detached for backlog staging");
        environment().activemqProxy().disable();

        long backlogPublishStartNanos = System.nanoTime();
        publishSyntheticEvents(BACKLOG_COUNT, environment().activemqDirectBrokerUrl());
        long backlogPublishMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - backlogPublishStartNanos);
        log.info("[reliability] Backlog of {} events staged in {} ms ({} ev/s) while connector detached",
                BACKLOG_COUNT, backlogPublishMs, BACKLOG_COUNT * 1_000L / Math.max(backlogPublishMs, 1));

        log.info("[reliability] Re-enabling Toxiproxy — connector reconnecting; publishing {}-event burst into the recovering subscription", BURST_COUNT);
        environment().activemqProxy().enable();

        long burstPublishStartNanos = System.nanoTime();
        publishSyntheticEvents(BURST_COUNT, environment().activemqDirectBrokerUrl());
        long burstPublishMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - burstPublishStartNanos);
        log.info("[reliability] Burst of {} events published in {} ms ({} ev/s) — backlog drain provably overlaps with this phase if burst wall-time > drain wall-time below",
                BURST_COUNT, burstPublishMs, BURST_COUNT * 1_000L / Math.max(burstPublishMs, 1));

        long drainStartNanos = System.nanoTime();
        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            assertThat(totalIngestionEvents)
                    .as("backlog + burst recovery: %d events queued (%d backlog + %d burst), expected ≥ %d ingestion-event POSTs at WireMock — a count below this means the durable subscription dropped messages across the AMQ recovery boundary, or the burst arrival raced with reconnect and lost arrivals",
                            TOTAL_COUNT, BACKLOG_COUNT, BURST_COUNT, TOTAL_COUNT)
                    .isGreaterThanOrEqualTo(TOTAL_COUNT);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);
        long drainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);
        log.info("[reliability] Drain complete: {} events in {} ms ({} ev/s connector drain rate). Backlog publish was {} ms; burst publish was {} ms; end-to-end wall-time {} ms.",
                TOTAL_COUNT, drainMs, TOTAL_COUNT * 1_000L / Math.max(drainMs, 1),
                backlogPublishMs, burstPublishMs, backlogPublishMs + burstPublishMs + drainMs);

        int finalIngestionEvents = WiremockCounts.ingestionEvents();
        assertThat(finalIngestionEvents)
                .as("backlog + burst recovery: total POSTs %d > %d (%d expected + %d%% duplication budget) — points to a redelivery storm during reconnect or a dedup regression masquerading as completeness",
                        finalIngestionEvents, UPPER_BOUND_TOTAL_POSTS, TOTAL_COUNT, UPPER_BOUND_DUPLICATION_PCT)
                .isLessThanOrEqualTo(UPPER_BOUND_TOTAL_POSTS);
        assertThat(environment().jolokia().dlqDepth())
                .as("backlog + burst recovery must produce zero DLQ entries — any depth means the reconnect produced a redelivery storm or the route's exception classifier regressed under the combined replay + new-arrival pressure")
                .isZero();
        assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                .as("subscription on %s must be 1 after recovery — zero = subscription lost during disconnect; >1 = leaked across reconnect", REPO_EVENT_TOPIC)
                .isEqualTo(1);
        assertThat(WiremockCounts.presignedUrlRequests())
                .as("synthetic events have no content block, so the upload leg must not run — any /presigned-urls POSTs here mean the connector dispatched the content path on a metadata-only event, which is a regression")
                .isZero();
    }

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private static void publishSyntheticEvents(int count, String brokerUrl)
    {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        try
        {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            try
            {
                Topic topic = session.createTopic(REPO_EVENT_TOPIC);
                MessageProducer producer = session.createProducer(topic);
                try
                {
                    producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                    for (int i = 0; i < count; i++)
                    {
                        producer.send(session.createTextMessage(buildSyntheticMetadataEvent(i)));
                        if ((i + 1) % 1_000 == 0)
                        {
                            log.info("[reliability] Synthetic publish progress: {} / {}", i + 1, count);
                        }
                    }
                }
                finally
                {
                    producer.close();
                }
            }
            finally
            {
                session.close();
            }
        }
        finally
        {
            connection.close();
        }
    }

    /** Metadata-only synthetic event (no {@code content} block) so only the metadata flow runs. */
    private static String buildSyntheticMetadataEvent(int index)
    {
        String eventId = UUID.randomUUID().toString();
        String eventGroupId = UUID.randomUUID().toString();
        String nodeId = UUID.randomUUID().toString();
        return """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "%s",
                  "source": "/recovery-burst-reliability-it",
                  "time": "2026-05-19T10:00:00.000Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "%s",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "%s",
                      "name": "recovery-burst-%d.txt",
                      "nodeType": "cm:content",
                      "createdAt": "2026-05-19T10:00:00.000Z",
                      "modifiedAt": "2026-05-19T10:00:00.000Z",
                      "createdByUser": { "id": "admin", "displayName": "Administrator" },
                      "modifiedByUser": { "id": "admin", "displayName": "Administrator" },
                      "properties": {
                        "cm:title": "Recovery-burst event %d"
                      },
                      "aspectNames": [ "cm:titled" ],
                      "primaryHierarchy": [ "-my-" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceReaderAuthorities": ["GROUP_EVERYONE"],
                    "resourceDeniedAuthorities": []
                  }
                }
                """.formatted(eventId, eventGroupId, nodeId, index, index);
    }
}
