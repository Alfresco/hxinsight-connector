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

import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

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
        final String eventType = NODE_DELETED.getType();
        RepoEvent<DataAttributes<NodeResource>> originalEvent = RepoEvent.<DataAttributes<NodeResource>> builder()
                .setType(NODE_UPDATED.getType())
                .build();
        given(mockMessage.getBody(any())).willReturn(originalEvent);

        // when
        RepoEvent<DataAttributes<NodeResource>> repoEvent = objectUnderTest.alterRepoEvent(mockExchange, eventType);
        //
        // then
        assertEquals(eventType, repoEvent.getType());
        assertNotEquals(originalEvent.getType(), repoEvent.getType());
    }
}
