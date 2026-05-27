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
 * Event references a node id ACS does not have. The content-download {@code @Retryable} must not catch 404 (no retry storm), the JMS redelivery budget exhausts, and the message lands on the DLQ. The metadata POST succeeds before the 404 so HX Insight ends up with metadata-only state.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class MissingNodeReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    private static final String PARENT_ID = "-my-";
    private static final int MIN_INGESTION_FOR_FABRICATED = 1;
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
                            MIN_INGESTION_FOR_FABRICATED)
                    .isGreaterThanOrEqualTo(MIN_INGESTION_FOR_FABRICATED);
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
