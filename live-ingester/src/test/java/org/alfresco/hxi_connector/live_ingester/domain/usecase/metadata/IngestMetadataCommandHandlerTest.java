/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.ASPECTS_NAMES;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.CREATED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.MODIFIED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.TYPE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.assertContainsSameElements;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@ExtendWith(MockitoExtension.class)
class IngestMetadataCommandHandlerTest
{
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";
    private static final String NODE_TYPE = "cm:folder";
    private static final String NODE_CREATED_BY_USER_WITH_ID = "admin";
    private static final String NODE_MODIFIED_BY_USER_WITH_ID = "hr_user";
    private static final Set<String> NODE_ASPECT_NAMES = Set.of(
            "cm:titled",
            "cm:auditable");
    private static final NodeProperty<String> NODE_TITLE = new NodeProperty<>("cm:title", "some title");
    private static final Set<NodeProperty<?>> NODE_PROPERTIES = Set.of(NODE_TITLE);

    @Captor
    ArgumentCaptor<UpdateNodeMetadataEvent> updateNodeMetadataEventCaptor;
    @Mock
    IngestionEngineEventPublisher ingestionEngineEventPublisher;
    @Spy
    List<CustomPropertyResolver<?>> customPropertyResolvers = Collections.emptyList();
    @InjectMocks
    IngestMetadataCommandHandler ingestMetadataCommandHandler;

    @Test
    void shouldSetNewlyCreatedNodeMetadataProperties()
    {
        // given
        IngestMetadataCommand command = new IngestMetadataCommand(
                NODE_ID,
                CREATE,
                updated(NODE_TYPE),
                updated(NODE_CREATED_BY_USER_WITH_ID),
                updated(NODE_MODIFIED_BY_USER_WITH_ID),
                updated(NODE_ASPECT_NAMES),
                NODE_PROPERTIES.stream()
                        .map(nodeProperty -> CustomPropertyDelta.updated(nodeProperty.name(), nodeProperty.value()))
                        .collect(Collectors.toSet()));

        // when
        ingestMetadataCommandHandler.handle(command);

        // then
        Set<NodeProperty<?>> expectedNodePropertiesToSet = Set.of(
                TYPE.withValue(NODE_TYPE),
                CREATED_BY_USER_WITH_ID.withValue(NODE_CREATED_BY_USER_WITH_ID),
                MODIFIED_BY_USER_WITH_ID.withValue(NODE_MODIFIED_BY_USER_WITH_ID),
                ASPECTS_NAMES.withValue(NODE_ASPECT_NAMES),
                NODE_TITLE);

        verify(ingestionEngineEventPublisher).publishMessage(updateNodeMetadataEventCaptor.capture());
        UpdateNodeMetadataEvent updateNodeMetadataEvent = updateNodeMetadataEventCaptor.getValue();

        assertContainsSameElements(expectedNodePropertiesToSet, updateNodeMetadataEvent.getMetadataPropertiesToSet().values());
        assertTrue(updateNodeMetadataEvent.getMetadataPropertiesToUnset().isEmpty(), "There should be no properties to unset");
        assertEquals(updateNodeMetadataEvent.getEventType(), CREATE);
    }

    /** Test that we handle null created by/updated by, which happens for example with log in events. */
    @Test
    void canSupportEventsWithNullUsers()
    {
        // given
        PropertyDelta<String> nullUser = updated(null);
        IngestMetadataCommand command = new IngestMetadataCommand(
                NODE_ID,
                CREATE,
                updated(NODE_TYPE),
                nullUser, // Missing created by
                nullUser, // Missing updated by
                updated(NODE_ASPECT_NAMES),
                NODE_PROPERTIES.stream()
                        .map(nodeProperty -> CustomPropertyDelta.updated(nodeProperty.name(), nodeProperty.value()))
                        .collect(Collectors.toSet()));

        // when
        ingestMetadataCommandHandler.handle(command);

        // then
        Set<NodeProperty<?>> expectedNodePropertiesToSet = Set.of(
                TYPE.withValue(NODE_TYPE),
                CREATED_BY_USER_WITH_ID.withValue(null),
                MODIFIED_BY_USER_WITH_ID.withValue(null),
                ASPECTS_NAMES.withValue(NODE_ASPECT_NAMES),
                NODE_TITLE);

        verify(ingestionEngineEventPublisher).publishMessage(updateNodeMetadataEventCaptor.capture());
        UpdateNodeMetadataEvent updateNodeMetadataEvent = updateNodeMetadataEventCaptor.getValue();

        assertContainsSameElements(expectedNodePropertiesToSet, updateNodeMetadataEvent.getMetadataPropertiesToSet().values());
        assertTrue(updateNodeMetadataEvent.getMetadataPropertiesToUnset().isEmpty(), "There should be no properties to unset");
    }
}
