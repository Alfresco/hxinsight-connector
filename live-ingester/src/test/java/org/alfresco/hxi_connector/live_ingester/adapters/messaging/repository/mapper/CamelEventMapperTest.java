/*-
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.ExchangeEnricher.UPDATED_EVENT_TYPE_PROP;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class CamelEventMapperTest
{

    @Mock
    private ObjectMapper mockMapper;
    @Mock
    private Exchange mockExchange;
    @Mock
    private Message mockMessage;

    @InjectMocks
    private CamelEventMapper objectUnderTest;

    @Test
    @SneakyThrows
    void givenExchangePropertyNotPresent_whenMapping_thenEventTypeNotAltered()
    {
        given(mockExchange.getIn()).willReturn(mockMessage);
        final String mockBody = "mockBody";
        given(mockMessage.getBody(any())).willReturn(mockBody);
        final String eventType = NODE_UPDATED.getType();
        RepoEvent<DataAttributes<NodeResource>> originalEvent = RepoEvent.<DataAttributes<NodeResource>> builder()
                .setType(eventType)
                .build();
        given(mockMapper.readValue(any(String.class), any(TypeReference.class))).willReturn(originalEvent);

        // when
        RepoEvent<DataAttributes<NodeResource>> repoEvent = objectUnderTest.repoEventFrom(mockExchange);

        // then
        assertEquals(eventType, repoEvent.getType());
    }

    @Test
    @SneakyThrows
    void givenExchangePropertyPresent_whenMapping_thenEventTypeIsAltered()
    {
        given(mockExchange.getIn()).willReturn(mockMessage);
        final String mockBody = "mockBody";
        given(mockMessage.getBody(any())).willReturn(mockBody);
        final String eventType = NODE_CREATED.getType();
        RepoEvent<DataAttributes<NodeResource>> originalEvent = RepoEvent.<DataAttributes<NodeResource>> builder()
                .setType(eventType)
                .build();
        final String nodeDeletedType = NODE_DELETED.getType();
        given(mockExchange.getProperty(UPDATED_EVENT_TYPE_PROP, String.class)).willReturn(nodeDeletedType);
        given(mockMapper.readValue(any(String.class), any(TypeReference.class))).willReturn(originalEvent);

        // when
        final RepoEvent<DataAttributes<NodeResource>> repoEvent = objectUnderTest.repoEventFrom(mockExchange);

        // then
        assertEquals(nodeDeletedType, repoEvent.getType());
    }
}
