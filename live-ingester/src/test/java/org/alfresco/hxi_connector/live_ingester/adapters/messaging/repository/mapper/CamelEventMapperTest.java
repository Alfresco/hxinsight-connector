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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.repo.event.databind.ObjectMapperFactory;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class CamelEventMapperTest
{
    private static final String NODE_CREATED_EVENT = """
            {
              "specversion": "1.0",
              "type": "org.alfresco.event.node.Created",
              "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
              "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
              "time": "2021-01-26T10:29:42.99524Z",
              "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
              "datacontenttype": "application/json",
              "data": {
                "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                "resource": {
                  "@type": "NodeResource",
                  "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "name": "purchase-order-scan.doc",
                  "nodeType": "cm:content",
                  "isFolder": false,
                  "isFile": true
                }
              }
            }
            """;

    private static final String CHILD_ASSOC_CREATED_EVENT = """
            {
              "specversion": "1.0",
              "type": "org.alfresco.event.assoc.child.Created",
              "id": "97a0994d-d213-4cb8-b920-9b9006b30ad6",
              "source": "/a4d21270-d0ff-4371-9212-70d0ff337182",
              "time": "2026-03-31T12:35:34.539Z",
              "dataschema": "https://api.alfresco.com/schema/event/repo/v1/childAssocCreated",
              "datacontenttype": "application/json",
              "data": {
                "eventGroupId": "4dfb8cf0-c156-416a-bb8c-f0c156c16a9b",
                "resource": {
                  "@type": "ChildAssociationResource",
                  "assocType": "cm:inZone",
                  "parent": { "id": "AUTH.ALF" },
                  "child": { "id": "d254a448-66b2-4914-94a4-4866b2991438" },
                  "assocQName": "cm:AlfrescoTestUser"
                }
              }
            }
            """;

    @Mock
    private Exchange mockExchange;
    @Mock
    private Message mockMessage;

    private CamelEventMapper eventMapper;

    @BeforeEach
    void setUp()
    {
        ObjectMapper objectMapper = ObjectMapperFactory.createInstance();
        objectMapper.registerModule(new JavaTimeModule());
        eventMapper = new CamelEventMapper(objectMapper);
    }

    @Test
    void givenNodeCreatedEvent_whenRepoEventFrom_thenReturnsMappedRepoEvent()
    {
        // given
        given(mockExchange.getIn()).willReturn(mockMessage);
        given(mockMessage.getBody(String.class)).willReturn(NODE_CREATED_EVENT);

        // when
        RepoEvent<DataAttributes<NodeResource>> result = eventMapper.repoEventFrom(mockExchange);

        // then
        assertThat(result.getId()).isEqualTo("ae5dac3c-25d0-438d-b148-2084d1ab05a6");
        assertThat(result.getType()).isEqualTo("org.alfresco.event.node.Created");
        assertThat(result.getData().getResource().getId()).isEqualTo("d71dd823-82c7-477c-8490-04cb0e826e65");
    }

    @Test
    void givenChildAssocEvent_whenRepoEventFrom_thenReturnsNull()
    {
        // given
        given(mockExchange.getIn()).willReturn(mockMessage);
        given(mockMessage.getBody(String.class)).willReturn(CHILD_ASSOC_CREATED_EVENT);
        ListAppender<ILoggingEvent> logEntries = LoggingUtils.createLogsListAppender(CamelEventMapper.class);

        // when
        RepoEvent<DataAttributes<NodeResource>> result = eventMapper.repoEventFrom(mockExchange);

        // then
        List<String> logs = logEntries.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(result).isNull();
        assertThat(logs)
                .isNotEmpty()
                .last().asString()
                .isEqualTo("Repository :: Skipping org.alfresco.event.assoc.child.Created event - resource type is not NodeResource. Event ID: 97a0994d-d213-4cb8-b920-9b9006b30ad6");
    }

    @Test
    void givenInvalidJson_whenRepoEventFrom_thenThrowsLiveIngesterRuntimeException()
    {
        // given
        given(mockExchange.getIn()).willReturn(mockMessage);
        given(mockMessage.getBody(String.class)).willReturn("not valid json {{{");

        // when
        Exception actualException = catchException(() -> eventMapper.repoEventFrom(mockExchange));

        // then
        assertThat(actualException).isInstanceOf(LiveIngesterRuntimeException.class);
    }

    @Test
    void givenExchangePropertyNotPresent_whenAlterRepoEvent_thenEventTypeIsAltered()
    {
        // given
        given(mockExchange.getIn()).willReturn(mockMessage);
        final String eventType = NODE_DELETED.getType();
        RepoEvent<DataAttributes<NodeResource>> originalEvent = RepoEvent.<DataAttributes<NodeResource>> builder()
                .setType(NODE_UPDATED.getType())
                .build();
        given(mockMessage.getBody(any())).willReturn(originalEvent);

        // when
        RepoEvent<DataAttributes<NodeResource>> repoEvent = eventMapper.alterRepoEvent(mockExchange, eventType);

        // then
        assertThat(repoEvent.getType())
                .isEqualTo(eventType)
                .isNotEqualTo(originalEvent.getType());
    }
}
