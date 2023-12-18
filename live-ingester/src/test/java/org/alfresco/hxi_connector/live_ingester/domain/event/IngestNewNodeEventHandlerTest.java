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

package org.alfresco.hxi_connector.live_ingester.domain.event;

import static java.util.Optional.ofNullable;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.model.transform.request.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.model.transform.request.TransformRequester;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.EventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;

// PMD doesn't recognise Mockito then().should() syntax.
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class IngestNewNodeEventHandlerTest
{
    @Mock
    UpdateNodeEventMapper updateNodeEventMapper;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    TransformRequestMapper transformRequestMapper;
    @Mock
    TransformRequester transformRequester;
    @InjectMocks
    IngestNewNodeEventHandler ingestNewNodeEventHandler;

    @Test
    void nodeWithoutContent_PublishMetadataButNoTransform()
    {
        // given
        IngestNewNodeEvent ingestNewNodeEvent = mockIngestNodeEvent(null);
        UpdateNodeMetadataEvent updateNodeMetadataEvent = mock(UpdateNodeMetadataEvent.class);
        given(updateNodeEventMapper.map(ingestNewNodeEvent)).willReturn(updateNodeMetadataEvent);

        // when
        ingestNewNodeEventHandler.handle(ingestNewNodeEvent);

        // then
        then(eventPublisher).should().publishMessage(updateNodeMetadataEvent);
        then(transformRequestMapper).shouldHaveNoInteractions();
        then(transformRequester).shouldHaveNoInteractions();
    }

    @Test
    void nodeWithContent_PublishMetadataAndRequestTransform()
    {
        // given
        IngestNewNodeEvent ingestNewNodeEvent = mockIngestNodeEvent("application/msword");
        UpdateNodeMetadataEvent updateNodeMetadataEvent = mock(UpdateNodeMetadataEvent.class);
        given(updateNodeEventMapper.map(ingestNewNodeEvent)).willReturn(updateNodeMetadataEvent);
        TransformRequest transformRequest = mock(TransformRequest.class);
        given(transformRequestMapper.map(ingestNewNodeEvent)).willReturn(transformRequest);

        // when
        ingestNewNodeEventHandler.handle(ingestNewNodeEvent);

        // then
        then(eventPublisher).should().publishMessage(updateNodeMetadataEvent);
        then(transformRequester).should().requestTransform(transformRequest);
    }

    IngestNewNodeEvent mockIngestNodeEvent(String sourceContentType)
    {
        Node node = mock();
        given(node.contentMimeType()).willReturn(ofNullable(sourceContentType));

        IngestNewNodeEvent ingestNewNodeEvent = mock();
        given(ingestNewNodeEvent.node()).willReturn(node);
        return ingestNewNodeEvent;
    }
}
