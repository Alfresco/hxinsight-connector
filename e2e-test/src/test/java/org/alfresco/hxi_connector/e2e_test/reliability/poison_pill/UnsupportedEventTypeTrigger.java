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

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.DirectTopicPublisher;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Sentinels;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/** Publishes a synthetic CloudEvent with an unrecognised {@code type} plus a normal sentinel. Shared by the default-path and opt-out unsupported-event-type ITs. */
@Slf4j
final class UnsupportedEventTypeTrigger
{
    static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    static final String UNKNOWN_EVENT_TYPE = "org.alfresco.event.node.Garbled";
    static final String UNHANDLED_LOG_FRAGMENT = "unsupported eventType=" + UNKNOWN_EVENT_TYPE;

    private UnsupportedEventTypeTrigger()
    {}

    static SyntheticUnknownTypeEvent inject(ReliabilityEnvironment environment) throws IOException
    {
        String eventId = UUID.randomUUID().toString();
        String unknownTypePayload = buildCloudEventWithType(eventId, UNKNOWN_EVENT_TYPE);
        log.info("[reliability] Publishing CloudEvent id={} with unsupported type='{}' to topic {}",
                eventId, UNKNOWN_EVENT_TYPE, REPO_EVENT_TOPIC);
        DirectTopicPublisher.publishTextMessage(
                environment.activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                unknownTypePayload);

        Node sentinelNode = Sentinels.create(environment, "post-unknown-type-sentinel.txt", "post-unknown-type-sentinel");
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

    record SyntheticUnknownTypeEvent(String eventId, Node sentinelNode)
    {}
}
