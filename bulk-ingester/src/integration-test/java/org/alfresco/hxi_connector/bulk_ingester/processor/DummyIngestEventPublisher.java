/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester.processor;

import static java.lang.String.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.bulk_ingester.event.IngestEventPublisher;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@Slf4j
public class DummyIngestEventPublisher implements IngestEventPublisher
{
    private final Map<String, IngestEvent> ingestEvents = new HashMap<>();

    @Override
    public void publish(IngestEvent ingestEvent)
    {
        log.info("Publishing node {}", ingestEvent.toString());

        ingestEvents.put(ingestEvent.nodeId(), ingestEvent);
    }

    public void assertPublishedNodes(List<IngestEvent> ingestEvents)
    {
        ingestEvents.forEach(this::assertPublishedNode);
    }

    public void assertPublishedNode(IngestEvent expected)
    {
        IngestEvent actual = ingestEvents.values().stream()
                .filter(event -> event.nodeId().equals(expected.nodeId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Node not published: " + expected.nodeId()));

        // Compare individual fields
        assertEquals(expected.contentInfo(), actual.contentInfo(), "Content info mismatch");
        assertEquals(expected.timestamp(), actual.timestamp(), "Timestamp mismatch");

        // Compare properties map with deep comparison
        Map<String, Serializable> expectedProps = expected.properties();
        Map<String, Serializable> actualProps = actual.properties();

        assertEquals(expectedProps, actualProps, "Properties size mismatch");

        expectedProps.forEach((key, expectedValue) -> {
            assertTrue(actualProps.containsKey(key), "Missing property key: " + key);
            assertValuesEqual(expectedValue, actualProps.get(key), "Property mismatch for key: " + key);
        });
    }

    private void assertValuesEqual(Object expected, Object actual, String contextMessage)
    {
        if (expected instanceof Map && actual instanceof Map)
        {
            assertMapsEqual((Map<?, ?>) expected, (Map<?, ?>) actual, contextMessage);
        }
        else if (expected instanceof Collection && actual instanceof Collection)
        {
            // Compare collections by converting to sets to ignore order and type differences
            assertEquals(new HashSet<>((Collection<?>) expected), new HashSet<>((Collection<?>) actual), contextMessage);
        }
        else
        {
            assertEquals(expected, actual, contextMessage);
        }
    }
    private void assertMapsEqual(Map<?, ?> expected, Map<?, ?> actual, String message)
    {
        assertEquals(expected.size(), actual.size(), message + " - size mismatch");
        expected.forEach((key, expectedValue) -> {
            assertTrue(actual.containsKey(key), message + " - missing key: " + key);
            assertValuesEqual(expectedValue, actual.get(key), message + " - value mismatch for key: " + key);
        });
    }

    public void assertNodeNotPublished(IngestEvent ingestEvent)
    {
        IngestEvent publishedIngestEvent = ingestEvents.get(ingestEvent.nodeId());

        assertNull(publishedIngestEvent, format("Node %s should have not been published.", ingestEvent.nodeId()));
    }

    public void cleanUpEvents()
    {
        ingestEvents.clear();
    }
}