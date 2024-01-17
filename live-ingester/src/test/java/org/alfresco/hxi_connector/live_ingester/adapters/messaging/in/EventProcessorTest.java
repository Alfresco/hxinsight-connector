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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.in;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.mapper.RepoEventMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommandHandler;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class EventProcessorTest
{
    @Mock
    RepoEventMapper repoEventMapper;

    @Mock
    IngestMetadataCommandHandler ingestMetadataCommandHandler;

    @Mock
    IngestContentCommandHandler ingestContentCommandHandler;

    @InjectMocks
    EventProcessor eventProcessor;

    @Test
    void shouldIngestNewNodeMetadata()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_CREATED.getType());

        NodeResource nodeResource = mockNodeResource(event);
        given(nodeResource.getContent()).willReturn(null);

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestMetadataCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestMetadataCommandHandler).should().handle(any());
    }

    @Test
    void shouldIngestUpdatedNodeMetadata()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_UPDATED.getType());

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestMetadataCommand(event);
        then(repoEventMapper).shouldHaveNoMoreInteractions();

        then(ingestMetadataCommandHandler).should().handle(any());
    }

    @Test
    void shouldIngestNewNodeContent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_CREATED.getType());

        NodeResource nodeResource = mockNodeResource(event);
        given(nodeResource.getContent()).willReturn(mock());

        // when
        eventProcessor.process(event);

        // then
        then(repoEventMapper).should().mapToIngestMetadataCommand(event);
        then(repoEventMapper).should().mapToIngestContentCommand(event);

        then(ingestMetadataCommandHandler).should().handle(any());
        then(ingestContentCommandHandler).should().handle(any());
    }

    NodeResource mockNodeResource(RepoEvent<DataAttributes<NodeResource>> repoEvent)
    {
        EventData<NodeResource> eventData = mock();
        NodeResource nodeResource = mock();

        given(repoEvent.getData()).willReturn(eventData);
        given(eventData.getResource()).willReturn(nodeResource);

        return nodeResource;
    }
}
