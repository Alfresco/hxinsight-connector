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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.delete;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.DeleteNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.IngestionEngineEventPublisher;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;

@ExtendWith(MockitoExtension.class)
public class DeleteNodeCommandHandlerTest
{
    private static final String NODE_ID = "12341234-1234-1234-1234-123412341234";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();

    @InjectMocks
    private DeleteNodeCommandHandler deleteNodeCommandHandler;
    @Mock
    private IngestionEngineEventPublisher ingestionEngineEventPublisher;
    @Mock
    private IntegrationProperties integrationProperties;

    @Test
    public void testHandle()
    {
        // given
        DeleteNodeCommand deleteNodeCommand = new DeleteNodeCommand(
                NODE_ID,
                TIMESTAMP);

        given(integrationProperties.application()).willReturn(mock(Application.class));
        given(integrationProperties.application().getSourceId()).willReturn(SOURCE_ID);

        // when
        deleteNodeCommandHandler.handle(deleteNodeCommand);

        // then
        NodeEvent expectedNodeEvent = new DeleteNodeEvent(NODE_ID, SOURCE_ID, TIMESTAMP);
        then(ingestionEngineEventPublisher).should().publishMessage(expectedNodeEvent);
    }
}
