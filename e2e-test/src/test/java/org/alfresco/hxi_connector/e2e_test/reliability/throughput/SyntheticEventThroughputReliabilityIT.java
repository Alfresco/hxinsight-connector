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
package org.alfresco.hxi_connector.e2e_test.reliability.throughput;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;

/**
 * Connector publish-side throughput baseline using synthetic metadata-only events (no content block). Publishes JMS messages straight to the broker so ACS REST is not on the hot path, then times the drain to HX Insight.
 *
 * <p>
 * Complement to {@link BacklogDrainReliabilityIT}, which exercises the full content pipeline.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class SyntheticEventThroughputReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final int EVENT_COUNT = 5000;
    private static final long DRAIN_SLA_MS = 20_000;
    private static final int CONVERGENCE_DELAY_MS = 200;
    private static final int CONVERGENCE_MAX_ATTEMPTS = (int) (DRAIN_SLA_MS / CONVERGENCE_DELAY_MS);

    @Test
    void shouldDrainSyntheticEventsAtConnectorPublishThroughput()
    {
        long publishStartNanos = System.nanoTime();
        publishSyntheticEvents(EVENT_COUNT, environment().activemqDirectBrokerUrl());
        long publishDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - publishStartNanos);
        log.info("[reliability] Synthetic publish complete: {} events in {} ms ({} ev/s publish rate, JMS-direct — no ACS REST involvement)",
                EVENT_COUNT, publishDurationMs, EVENT_COUNT * 1_000L / Math.max(publishDurationMs, 1));

        long drainStartNanos = System.nanoTime();
        RetryUtils.assertWithRetry(() -> {
            int totalIngestionEvents = WiremockCounts.ingestionEvents();
            assertThat(totalIngestionEvents)
                    .as("synthetic-event drain: %d events published, expected ≥ %d ingestion-event POSTs at WireMock (1 per metadata-only event, no content path involved)",
                            EVENT_COUNT, EVENT_COUNT)
                    .isGreaterThanOrEqualTo(EVENT_COUNT);
        }, CONVERGENCE_MAX_ATTEMPTS, CONVERGENCE_DELAY_MS);
        long drainDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - drainStartNanos);
        log.info("[reliability] Drain complete: {} events in {} ms ({} ev/s connector drain rate — metadata-only path, no /presigned-urls or S3 PUT). Publish was {} ms. End-to-end wall-time {} ms.",
                EVENT_COUNT, drainDurationMs, EVENT_COUNT * 1_000L / Math.max(drainDurationMs, 1),
                publishDurationMs, publishDurationMs + drainDurationMs);

        assertThat(environment().jolokia().dlqDepth())
                .as("synthetic-event drain must produce zero DLQ entries — any depth here means the metadata path bounced an event despite the payload being well-formed")
                .isZero();
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

    /**
     * Adapted from {@link org.alfresco.hxi_connector.e2e_test.reliability.poison_pill.MissingNodeReliabilityIT}'s {@code buildCreatedWithContentEvent}, but with the {@code content} block removed so {@code wasContentChanged(event)} returns {@code false} and {@code EventProcessor.handleContentChange} is skipped — only metadata flows. Each call uses fresh UUIDs for {@code event id}, {@code eventGroupId}, and {@code resource id} so the stream of synthetic events looks like real ACS events from the connector's perspective and the per-event idempotency key on HXI's side is unique.
     */
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
                  "source": "/synthetic-throughput-reliability-it",
                  "time": "2026-05-18T10:00:00.000Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "%s",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "%s",
                      "name": "synthetic-%d.txt",
                      "nodeType": "cm:content",
                      "createdAt": "2026-05-18T10:00:00.000Z",
                      "modifiedAt": "2026-05-18T10:00:00.000Z",
                      "createdByUser": { "id": "admin", "displayName": "Administrator" },
                      "modifiedByUser": { "id": "admin", "displayName": "Administrator" },
                      "properties": {
                        "cm:title": "Synthetic throughput event %d"
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
