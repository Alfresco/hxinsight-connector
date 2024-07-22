/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.assertContainsSameElements;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.ContentPropertyUpdated;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@ExtendWith(MockitoExtension.class)
class IngestNodeCommandHandlerTest
{
    private static final String NODE_ID = "0fe2919a-e0a6-4033-8d35-168a16cf33fc";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final NodeProperty<String> NODE_TITLE = new NodeProperty<>("cm:title", "some title");
    private static final Set<NodeProperty<?>> NODE_PROPERTIES = Set.of(NODE_TITLE);

    @Captor
    ArgumentCaptor<UpdateNodeEvent> updateNodeEventCaptor;
    @Mock
    IngestionEngineEventPublisher ingestionEngineEventPublisher;
    @Mock
    private IntegrationProperties integrationProperties;
    @Spy
    List<PropertyResolver<?>> propertyResolvers = Collections.emptyList();
    @InjectMocks
    IngestNodeCommandHandler ingestNodeCommandHandler;

    @Test
    void emptyDeleteMessageThrowsException()
    {
        // given
        IngestNodeCommand command = new IngestNodeCommand(NODE_ID, DELETE, emptySet());

        // then
        assertThrows(ValidationException.class, () -> ingestNodeCommandHandler.handle(command));
    }

    @Nested
    class IngestNodeCommandHandlerTestWithBeforeEach
    {
        @BeforeEach
        void setUp()
        {
            given(integrationProperties.application()).willReturn(mock(IntegrationProperties.Application.class));
            given(integrationProperties.application().sourceId()).willReturn(SOURCE_ID);
        }

        @Test
        void shouldSetNewlyCreatedNodeMetadataProperties()
        {
            // given
            IngestNodeCommand command = new IngestNodeCommand(
                    NODE_ID,
                    CREATE,
                    NODE_PROPERTIES.stream()
                            .map(nodeProperty -> PropertyDelta.updated(nodeProperty.name(), nodeProperty.value()))
                            .collect(Collectors.toSet()));

            // when
            ingestNodeCommandHandler.handle(command);

            // then
            Set<NodeProperty<?>> expectedNodePropertiesToSet = Set.of(NODE_TITLE);

            then(ingestionEngineEventPublisher).should().publishMessage(updateNodeEventCaptor.capture());
            UpdateNodeEvent updateNodeEvent = updateNodeEventCaptor.getValue();

            assertContainsSameElements(expectedNodePropertiesToSet, updateNodeEvent.getMetadataPropertiesToSet().values());
            assertTrue(updateNodeEvent.getPropertiesToUnset().isEmpty(), "There should be no properties to unset");
            assertEquals(updateNodeEvent.getEventType(), CREATE);
        }

        @Test
        void shouldSetContentProperty()
        {
            // given
            IngestNodeCommand command = new IngestNodeCommand(
                    NODE_ID,
                    CREATE,
                    Set.of(new ContentPropertyUpdated(CONTENT_PROPERTY, "content-id", "application/pdf", "application/msword", 123L, "something.doc")));

            // when
            ingestNodeCommandHandler.handle(command);

            // then
            then(ingestionEngineEventPublisher).should().publishMessage(updateNodeEventCaptor.capture());
            UpdateNodeEvent updateNodeEvent = updateNodeEventCaptor.getValue();

            UpdateNodeEvent expected = new UpdateNodeEvent(NODE_ID, CREATE, SOURCE_ID);
            expected.addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "content-id", "application/pdf", "application/msword", 123L, "something.doc"));
            assertEquals(expected, updateNodeEvent);
        }

        @Test
        void shouldNotSendEmptyUpdate()
        {
            // given
            IngestNodeCommand command = new IngestNodeCommand(NODE_ID, UPDATE, emptySet());

            // when
            ingestNodeCommandHandler.handle(command);

            // then
            then(ingestionEngineEventPublisher).shouldHaveNoInteractions();
        }

        @Test
        void emptyCreateMessageCreatesNode()
        {
            // given
            IngestNodeCommand command = new IngestNodeCommand(NODE_ID, CREATE, emptySet());

            // when
            ingestNodeCommandHandler.handle(command);

            // then
            NodeEvent expected = new UpdateNodeEvent(NODE_ID, CREATE, SOURCE_ID);
            then(ingestionEngineEventPublisher).should().publishMessage(expected);
        }

    }

}
