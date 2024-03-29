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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.RepoEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.delete.DeleteNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class EventProcessorTest
{
    private static final long CONTENT_SIZE = 123L;
    public static final long NO_CONTENT = 0L;

    @Mock
    RepoEventMapper repoEventMapper;

    @Mock
    IngestNodeCommandHandler ingestNodeCommandHandler;

    @Mock
    IngestContentCommandHandler ingestContentCommandHandler;

    @Mock
    DeleteNodeCommandHandler deleteNodeCommandHandler;

    @InjectMocks
    EventProcessor eventProcessor;

    @Test
    void shouldIngestNewNodeWithoutContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = prepareMockCreatedEvent();
        given(event.getData().getResource().getContent()).willReturn(null);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestNodeCommandHandler).should().handle(any());
    }

    @Test
    void shouldIngestUpdatedNodeMetadata()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_UPDATED.getType());
        given(event.getData()).willReturn(mock());

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestNodeCommandHandler).should().handle(any());
    }

    @Test
    void shouldIngestNewNodeContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = prepareMockCreatedEvent();
        ContentInfo contentInfo = mock();
        given(contentInfo.getSizeInBytes()).willReturn(CONTENT_SIZE);
        given(event.getData().getResource().getContent()).willReturn(contentInfo);

        TriggerContentIngestionCommand triggerContentIngestionCommand = mock();
        given(repoEventMapper.mapToIngestContentCommand(event)).willReturn(triggerContentIngestionCommand);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).should().mapToIngestContentCommand(event);

        then(ingestNodeCommandHandler).should().handle(any());
        then(ingestContentCommandHandler).should().handle(triggerContentIngestionCommand);
    }

    @Test
    void shouldNotProcessZeroByteContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = prepareMockCreatedEvent();
        ContentInfo contentInfo = mock();
        given(contentInfo.getSizeInBytes()).willReturn(NO_CONTENT);
        given(event.getData().getResource().getContent()).willReturn(contentInfo);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestNodeCommandHandler).should().handle(any());
        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldIngestUpdatedNodeContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = prepareMockUpdateEvent();
        ContentInfo contentInfo = mock();
        given(contentInfo.getSizeInBytes()).willReturn(CONTENT_SIZE);
        given(event.getData().getResource().getContent()).willReturn(contentInfo);
        given(event.getData().getResourceBefore().getContent()).willReturn(mock());

        TriggerContentIngestionCommand triggerContentIngestionCommand = mock();
        given(repoEventMapper.mapToIngestContentCommand(event)).willReturn(triggerContentIngestionCommand);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).should().mapToIngestContentCommand(event);

        then(ingestNodeCommandHandler).should().handle(any());
        then(ingestContentCommandHandler).should().handle(triggerContentIngestionCommand);
    }

    @Test
    void shouldNotReprocessUnchangedContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = prepareMockUpdateEvent();
        ContentInfo contentInfo = mock();
        given(contentInfo.getSizeInBytes()).willReturn(CONTENT_SIZE);
        given(event.getData().getResource().getContent()).willReturn(contentInfo);
        given(event.getData().getResourceBefore().getContent()).willReturn(null);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestNodeCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestNodeCommandHandler).should().handle(any());
        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldDeleteNode()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_DELETED.getType());
        given(event.getData()).willReturn(mock());
        DeleteNodeCommand deleteNodeCommand = mock();
        given(repoEventMapper.mapToDeleteNodeCommand(event)).willReturn(deleteNodeCommand);

        // when
        eventProcessor.process(event);

        // then
        then(deleteNodeCommandHandler).should().handle(deleteNodeCommand);
        then(deleteNodeCommandHandler).shouldHaveNoMoreInteractions();
    }

    RepoEvent<DataAttributes<NodeResource>> prepareMockCreatedEvent()
    {
        return prepareMockEventWithResource(NODE_CREATED);
    }

    RepoEvent<DataAttributes<NodeResource>> prepareMockUpdateEvent()
    {
        RepoEvent<DataAttributes<NodeResource>> repoEvent = prepareMockEventWithResource(NODE_UPDATED);
        DataAttributes<NodeResource> data = repoEvent.getData();
        given(data.getResourceBefore()).willReturn(mock());
        return repoEvent;
    }

    RepoEvent<DataAttributes<NodeResource>> prepareMockEventWithResource(EventType eventType)
    {
        RepoEvent<DataAttributes<NodeResource>> repoEvent = prepareMockEvent(eventType);
        DataAttributes<NodeResource> data = repoEvent.getData();
        given(data.getResource()).willReturn(mock());
        return repoEvent;
    }

    RepoEvent<DataAttributes<NodeResource>> prepareMockEvent(EventType eventType)
    {
        RepoEvent<DataAttributes<NodeResource>> repoEvent = mock();
        given(repoEvent.getType()).willReturn(eventType.getType());
        DataAttributes<NodeResource> data = mock();
        given(repoEvent.getData()).willReturn(data);
        return repoEvent;
    }
}
