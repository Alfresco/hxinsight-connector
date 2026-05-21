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
 * Backlog-drain-under-pressure guard: stages a {@value #BACKLOG_COUNT}-event backlog on the durable subscription while the connector is detached via Toxiproxy, then re-enables AMQ <i>and</i> immediately publishes a {@value #BURST_COUNT}-event burst, so the burst arrives at the broker while the connector is mid-reconnect / mid-replay. Pins that the recovery boundary itself does not lose, duplicate, or redeliver-storm events when sustained traffic overlaps with the replay.
 *
 * <p>
 * The shape:
 * <ol>
 * <li><b>Detach the consumer.</b> {@code activemqProxy().disable()} — the connector's view of the broker goes dark; the repository's own broker connection bypasses Toxiproxy and keeps publishing.</li>
 * <li><b>Stage the backlog.</b> Synthetic-publish {@value #BACKLOG_COUNT} {@code org.alfresco.event.node.Created} events directly to the broker (no {@code content} block, so the connector's metadata-only path runs and the test isolates the AMQ + publish-side legs from ACS / S3 latency variance). The broker queues them on the durable subscription.</li>
 * <li><b>Re-enable + immediate burst.</b> {@code activemqProxy().enable()} — the connector starts reconnecting. The very next call publishes another {@value #BURST_COUNT} events directly to the broker; with the connector's ~5 kHz drain rate dwarfing the synthetic publisher's ~700 ev/s, the burst publish provably overlaps with the connector's drain (visible as <i>burst publish wall-time &gt; drain wall-time</i> in the post-test logs).</li>
 * <li><b>Wait for total drain.</b> Convergence on {@code ingestionEvents() ≥ TOTAL_COUNT} within the {@value #DRAIN_SLA_MS} ms cap.</li>
 * </ol>
 *
 * <p>
 * Asserts at convergence:
 * <ul>
 * <li>{@code ≥ TOTAL_COUNT} POSTs to {@code /ingestion-events} — no event silently dropped during the recovery boundary.</li>
 * <li>{@code dlqDepth() == 0} — no redelivery storm during reconnect, no exception-classifier regression.</li>
 * <li>{@code topicSubscriberCount == 1} — subscription neither lost during disconnect nor leaked across reconnect.</li>
 * <li>Total POSTs bounded above by {@value #UPPER_BOUND_DUPLICATION_PCT}% over {@value #TOTAL_COUNT} — at-least-once delivery is permitted, but duplication beyond this points to a redelivery storm masquerading as completeness.</li>
 * </ul>
 *
 * <p>
 * What this test does <i>not</i> verify, by design:
 * <ul>
 * <li><b>Connector overrun.</b> The synthetic publisher is single-threaded and runs at ~700 ev/s; the connector drain (metadata-only) runs at ~5 kHz, so peak in-flight queue depth is bounded. To genuinely overrun the drain we would need a multi-threaded publisher staging tens of thousands of events; out of scope for this row, which guards correctness across the recovery boundary, not headroom.</li>
 * <li><b>Content pipeline.</b> Synthetic events have no {@code content} block, so {@code IngestContentCommandHandler}, presigned-URL requests, and S3 PUTs do not run. {@link org.alfresco.hxi_connector.e2e_test.reliability.throughput.BacklogDrainReliabilityIT BacklogDrainReliabilityIT} covers the full content pipeline at smaller {@code N} and serves as the companion check for that path.</li>
 * </ul>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=ActiveMqRecoveryBurstReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqRecoveryBurstReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    /**
     * Events staged on the durable subscription while the connector is detached. Sized so the staging publish takes a few seconds at the synthetic publisher's ~700 ev/s rate, which is enough wall-time for the broker to persist the messages but short enough that the test stays inside CI budgets.
     */
    private static final int BACKLOG_COUNT = 2_000;
    /**
     * Events published immediately after {@code enable()}. Sized to provably overlap with the connector's drain wall-time at the empirically observed rates: at ~700 ev/s publish vs ~5 kHz drain, the publisher cannot keep up and the connector finishes draining the union mid-burst — i.e. the burst genuinely arrives at the recovery boundary rather than at a settled subscription.
     */
    private static final int BURST_COUNT = 2_000;
    private static final int TOTAL_COUNT = BACKLOG_COUNT + BURST_COUNT;

    /**
     * Drain SLA from "burst publish complete" to "convergence threshold met". Generous on the first run; observed drain is ~5 kHz on the synthetic-event path so {@value #TOTAL_COUNT} events drain in &lt; 1 s. The {@value #DRAIN_SLA_MS} ms cap leaves &gt; 30× headroom for CI cold-cache variance and tightens once we have a corpus of green runs.
     */
    private static final long DRAIN_SLA_MS = 30_000;
    private static final int CONVERGENCE_DELAY_MS = 200;
    private static final int CONVERGENCE_MAX_ATTEMPTS = (int) (DRAIN_SLA_MS / CONVERGENCE_DELAY_MS);

    /**
     * Maximum tolerated duplication ratio expressed as a percentage on top of {@link #TOTAL_COUNT}. A small amount of duplication is the price of at-least-once delivery (a JMS message in mid-dispatch when the connector reconnects can be redelivered once); anything materially above this points to a redelivery storm or a dedup regression masquerading as completeness.
     */
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

    /**
     * Mirrors the synthetic-event shape used by {@link org.alfresco.hxi_connector.e2e_test.reliability.throughput.SyntheticEventThroughputReliabilityIT SyntheticEventThroughputReliabilityIT}: an {@code org.alfresco.event.node.Created} payload with the {@code content} block omitted, so the connector's {@code EventProcessor.handleContentChange} short-circuits and only the metadata flow runs. Helper duplicated across reliability ITs rather than hoisted into the shared harness — accept the duplication while the reliability suite is still accumulating new chaos shapes; consolidate once the suite stabilises.
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
