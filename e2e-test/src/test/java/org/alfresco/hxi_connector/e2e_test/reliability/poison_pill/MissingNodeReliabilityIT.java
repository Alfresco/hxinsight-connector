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

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Pins the connector's behaviour when a syntactically-valid {@code Created}-with-content event references a node id that ACS does not have.
 *
 * <p>
 * For metadata-only events ACS REST is never consulted (the event itself carries the metadata), so 404 cannot surface. To exercise the lookup path the test publishes an event with a non-zero {@code content.sizeInBytes}, which makes {@code EventProcessor} run {@code handleContentChange} and call {@code AlfrescoRepositoryContentClient.downloadContent(nodeId)} against {@code /api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content}.
 *
 * <p>
 * Expected behaviour (PASS today):
 * <ol>
 * <li><b>No retry storm at the HTTP layer</b> — {@code @Retryable(retryFor = EndpointServerErrorException.class)} on {@code downloadContent} does not catch {@link org.alfresco.hxi_connector.common.exception.ResourceNotFoundException} (404 is special-cased in {@code ErrorUtils.throwExceptionOnUnexpectedStatusCode}), so the call fails immediately on the first attempt instead of retrying for ~8.5 minutes.</li>
 * <li><b>Bounded redelivery and dead-letter</b> — the exception propagates through the route's {@code onException(Exception.class).process(::wrapErrorIfNecessary).stop()} and the JMS-level handler then performs at most {@code maximumRedeliveries} attempts (1 in the test profile, 6 in production) before parking the message on {@code ActiveMQ.DLQ} with {@code live_ingester_repo_events_dlq_total} incremented. Operators see one DLQ entry per such event, never an infinite loop.</li>
 * <li><b>Liveness</b> — the route survives the failure: a sentinel event published afterwards via the normal ACS create path still reaches HX Insight.</li>
 * </ol>
 *
 * <p>
 * Note on partial state at HX Insight: {@code EventProcessor.process} runs {@code handleMetadataPropertiesChange} <i>before</i> {@code handleContentChange}, so the metadata POST to {@code /ingestion-events} succeeds <i>before</i> the ACS lookup 404s. Each JMS redelivery re-POSTs the metadata (HX Insight is replay-safe — duplicate metadata POSTs for the same {@code (sourceId, objectId)} are de-duplicated server-side), so this test asserts {@link WiremockCounts#ingestionEventsFor(String)} with a {@code >= 1} bound rather than an exact count. The end-state is: HX Insight has the metadata for the missing node id, the original message is on DLQ, and the content was never uploaded.
 *
 * <p>
 * Bypasses Toxiproxy via {@link DirectTopicPublisher} so the test exercises the dispatch + ACS-lookup + dead-letter path, not reconnect logic. The fabricated node id is fresh per run so we never collide with anything else in the topic.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test failsafe:integration-test failsafe:verify -Preliability-tests -Dit.test=MissingNodeReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class MissingNodeReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final String PARENT_ID = "-my-";
    private static final int MIN_INGESTION_EVENTS_FOR_FABRICATED_ID = 1;
    /**
     * Convergence budget tuned to the test-profile JMS DLC: 1 redelivery with 200 ms delay (see {@link ReliabilityEnvironment}). 4s gives the broker comfortable headroom to land the parked message on the DLQ even when the host is loaded, while staying short enough to keep the IT under 30s wall-time.
     */
    private static final int CONVERGENCE_DELAY_MS = 4_000;

    @Test
    void shouldDeadLetterEventReferencingMissingNodeAndKeepRouteAlive() throws IOException
    {
        String fabricatedNodeId = UUID.randomUUID().toString();
        String fabricatedEventId = UUID.randomUUID().toString();
        String payload = buildCreatedWithContentEvent(fabricatedEventId, fabricatedNodeId);
        log.info("[reliability] Publishing CloudEvent id={} referencing fabricated nodeId={} (ACS will return 404 on /content)", fabricatedEventId, fabricatedNodeId);
        DirectTopicPublisher.publishTextMessage(
                environment().activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                payload);

        @Cleanup
        InputStream sentinel = new ByteArrayInputStream("post-missing-node-sentinel".getBytes());
        Node sentinelNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-missing-node-sentinel.txt", sentinel, "text/plain");
        log.info("[reliability] Sentinel node {} published — waiting for liveness signal at HX Insight", sentinelNode.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(sentinelNode.id()))
                    .as("liveness: sentinel event published after the missing-node event must reach HX Insight — failure here means the route stopped on the ACS 404")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: an event whose nodeId is unknown to ACS must surface as a parked message on ActiveMQ.DLQ for operator visibility, not an infinite retry loop or a silent drop")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(fabricatedNodeId))
                    .as("metadata flowed to HX Insight before the ACS 404: handleMetadataPropertiesChange runs ahead of handleContentChange so we expect at least %d POSTs for the fabricated objectId across the initial attempt + JMS redeliveries. A count of 0 here would mean the route failed before even POSTing metadata, which is unexpected for an unknown-node scenario",
                            MIN_INGESTION_EVENTS_FOR_FABRICATED_ID)
                    .isGreaterThanOrEqualTo(MIN_INGESTION_EVENTS_FOR_FABRICATED_ID);
        }, CONVERGENCE_DELAY_MS);
    }

    private static String buildCreatedWithContentEvent(String eventId, String nodeId)
    {
        return """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
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
                      "name": "missing-node-fabrication.txt",
                      "nodeType": "cm:content",
                      "createdAt": "2026-04-28T12:00:00.000Z",
                      "modifiedAt": "2026-04-28T12:00:00.000Z",
                      "createdByUser": { "id": "admin", "displayName": "Administrator" },
                      "modifiedByUser": { "id": "admin", "displayName": "Administrator" },
                      "content": {
                        "mimeType": "text/plain",
                        "sizeInBytes": 42,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:title": "missing-node fabrication"
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
                """.formatted(eventId, UUID.randomUUID(), nodeId);
    }
}
