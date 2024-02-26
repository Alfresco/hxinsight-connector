/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.hxi_connector.live_ingester.utils.TestUtils.assertContainsSameElements;

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

import org.alfresco.hxi_connector.live_ingester.domain.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class IngestMetadataCommandHandlerTest
{
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";
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
                NODE_PROPERTIES.stream()
                        .map(nodeProperty -> CustomPropertyDelta.updated(nodeProperty.name(), nodeProperty.value()))
                        .collect(Collectors.toSet()));

        // when
        ingestMetadataCommandHandler.handle(command);

        // then
        Set<NodeProperty<?>> expectedNodePropertiesToSet = Set.of(NODE_TITLE);

        then(ingestionEngineEventPublisher).should().publishMessage(updateNodeMetadataEventCaptor.capture());
        UpdateNodeMetadataEvent updateNodeMetadataEvent = updateNodeMetadataEventCaptor.getValue();

        assertContainsSameElements(expectedNodePropertiesToSet, updateNodeMetadataEvent.getMetadataPropertiesToSet().values());
        assertTrue(updateNodeMetadataEvent.getMetadataPropertiesToUnset().isEmpty(), "There should be no properties to unset");
        assertEquals(updateNodeMetadataEvent.getEventType(), CREATE);
    }

    @Test
    void shouldNotSendEmptyUpdate()
    {
        // given
        IngestMetadataCommand command = new IngestMetadataCommand(NODE_ID, UPDATE, emptySet());

        // when
        ingestMetadataCommandHandler.handle(command);

        // then
        then(ingestionEngineEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    void emptyCreateMessageCreatesNode()
    {
        // given
        IngestMetadataCommand command = new IngestMetadataCommand(NODE_ID, CREATE, emptySet());

        // when
        ingestMetadataCommandHandler.handle(command);

        // then
        NodeEvent expected = new UpdateNodeMetadataEvent(NODE_ID, CREATE);
        then(ingestionEngineEventPublisher).should().publishMessage(expected);
    }

    @Test
    void emptyDeleteMessageThrowsException()
    {
        // given
        IngestMetadataCommand command = new IngestMetadataCommand(NODE_ID, DELETE, emptySet());

        // then
        assertThrows(ValidationException.class, () -> ingestMetadataCommandHandler.handle(command));
    }
}
