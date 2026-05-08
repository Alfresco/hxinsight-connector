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
package org.alfresco.hxi_connector.e2e_test.reliability.poison_pill;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins the default-deployment behaviour when a syntactically-valid CloudEvent arrives on {@code alfresco.repo.event2} carrying an {@code eventType} the live-ingester does not understand.
 *
 * <p>
 * {@code EventProcessor} dispatches via {@code isEventType*} predicates ({@code Created} / {@code Updated} / {@code PermissionUpdated} / {@code Deleted}) and prediction-event checks. For an unrecognised type none of those match — the explicit unhandled-type branch emits one INFO log line naming the type and increments {@code live_ingester_repo_events_unhandled_total{type=<the.unknown.type>}} so operators can grep / alert on it. The JMS message is then ACK'd: this is intentional forward-compatibility for new ACS event types — those should not flood the DLQ until the connector adds explicit handling.
 *
 * <p>
 * Deployments that prefer hard-fail inventory (DLQ entry per unrecognised event) opt in via {@code ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES=true}, exercised by the sister class {@link UnsupportedEventTypeWithDlqOptInReliabilityIT}.
 *
 * <p>
 * The test publishes a synthetic CloudEvent shaped exactly like a real {@code org.alfresco.event.node.Created} payload, only the {@code type} field carries an unrecognised value ({@value #UNKNOWN_EVENT_TYPE}). It bypasses Toxiproxy via {@link DirectTopicPublisher} so the route's dispatch logic is exercised, not its reconnect logic. A sentinel event is published afterwards through the normal ACS create path to prove the route stayed alive past the synthetic event.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class UnsupportedEventTypeReliabilityIT extends BaseReliabilityIT
{
    static final String PARENT_ID = "-my-";
    static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    static final String UNKNOWN_EVENT_TYPE = "org.alfresco.event.node.Garbled";
    static final String UNHANDLED_LOG_FRAGMENT = "unsupported eventType=" + UNKNOWN_EVENT_TYPE;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldLogAndCountUnsupportedEventTypeWithoutDlq() throws IOException
    {
        SyntheticUnknownTypeEvent event = injectSyntheticUnknownTypeEvent(environment());

        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(event.sentinelNode().id()))
                    .as("liveness: sentinel published after the unknown-type event must reach HX Insight — failure here means the route stopped on the unsupported event")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().liveIngesterContainer().getLogs())
                    .as("default deployment must emit one INFO log line naming the unsupported eventType so the always-on Micrometer counter (live_ingester_repo_events_unhandled_total{type=...}) increment is also operator-traceable in stdout")
                    .contains(UNHANDLED_LOG_FRAGMENT);
            assertThat(environment().jolokia().dlqDepth())
                    .as("default deployment: an unrecognised eventType is observable via log + counter and ACK'd — DLQ stays at zero so adding a new ACS event type does not flood ActiveMQ.DLQ. Sister IT pins the opt-in path that lands on the DLQ")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Shared trigger used by both this IT and {@link UnsupportedEventTypeWithDlqOptInReliabilityIT}: publishes a synthetic CloudEvent with an unsupported {@code eventType} directly onto {@code alfresco.repo.event2} via {@link DirectTopicPublisher}, then publishes a sentinel node through the normal ACS path so liveness can be verified independently of the failure-mode under inspection.
     */
    static SyntheticUnknownTypeEvent injectSyntheticUnknownTypeEvent(ReliabilityEnvironment environment) throws IOException
    {
        String eventId = UUID.randomUUID().toString();
        String unknownTypePayload = buildCloudEventWithType(eventId, UNKNOWN_EVENT_TYPE);
        log.info("[reliability] Publishing CloudEvent id={} with unsupported type='{}' to topic {}",
                eventId, UNKNOWN_EVENT_TYPE, REPO_EVENT_TOPIC);
        DirectTopicPublisher.publishTextMessage(
                environment.activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                unknownTypePayload);

        Node sentinelNode;
        try (InputStream sentinel = new ByteArrayInputStream("post-unknown-type-sentinel".getBytes()))
        {
            sentinelNode = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "post-unknown-type-sentinel.txt", sentinel, "text/plain");
        }
        log.info("[reliability] Sentinel node {} published — waiting for liveness signal at HX Insight", sentinelNode.id());

        return new SyntheticUnknownTypeEvent(eventId, sentinelNode);
    }

    private static String buildCloudEventWithType(String eventId, String type)
    {
        return """
                {
                  "specversion": "1.0",
                  "type": "%s",
                  "id": "%s",
                  "source": "/reliability-it",
                  "time": "2026-04-28T12:00:00.000Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "%s",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "%s",
                      "name": "unsupported-event-type.txt",
                      "nodeType": "cm:content",
                      "isFolder": false,
                      "isFile": true,
                      "primaryHierarchy": ["-my-"]
                    }
                  }
                }
                """.formatted(type, eventId, UUID.randomUUID(), UUID.randomUUID());
    }

    /**
     * Holder for the identifiers produced by the shared trigger so paired ITs can drive their assertions off the same publish without duplicating the setup code.
     */
    record SyntheticUnknownTypeEvent(String eventId, Node sentinelNode)
    {
    }
}
