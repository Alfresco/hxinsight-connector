///*
// * #%L
// * Alfresco HX Insight Connector
// * %%
// * Copyright (C) 2023 Alfresco Software Limited
// * %%
// * This file is part of the Alfresco software.
// * If the software was purchased under a paid Alfresco license, the terms of
// * the paid license agreement will prevail.  Otherwise, the software is
// * provided under the following open source license terms:
// *
// * Alfresco is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Alfresco is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
// * #L%
// */
//
//package org.alfresco.hxi_connector.live_ingester.messaging.in;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
//
//import org.junit.jupiter.api.Test;
//
//import org.alfresco.hxi_connector.live_ingester.domain.event.IngestNewNodeEventHandler;
//import org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.RepoEventMapper;
//import org.alfresco.repo.event.v1.model.DataAttributes;
//import org.alfresco.repo.event.v1.model.NodeResource;
//import org.alfresco.repo.event.v1.model.RepoEvent;
//
//class EventProcessorTest
//{
//    private final RepoEventMapper repoEventMapper = mock();
//
//    private final EventProcessor eventProcessor = new EventProcessor(ingestNewNodeEventHandler, updateNodeEventHandler, repoEventMapper);
//
//    @Test
//    void shouldIngestNewNodeIfEventTypeIsCreated()
//    {
//        // given
//        RepoEvent<DataAttributes<NodeResource>> event = mock();
//        when(event.getType()).thenReturn(NODE_CREATED.getType());
//
//        // when
//        eventProcessor.process(event);
//
//        // then
//        verify(ingestNewNodeEventHandler).handle(any());
//    }
//}
