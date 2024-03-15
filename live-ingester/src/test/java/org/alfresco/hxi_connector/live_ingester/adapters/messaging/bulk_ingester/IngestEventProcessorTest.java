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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class IngestEventProcessorTest
{
    private static final String NODE_ID = "07659d13-8d64-4905-a329-6b27fe182023";
    private static final String NODE_TYPE = "cm:folder";
    private static final long CREATED_AT = 1000L;

    @Mock
    private IngestNodeCommandHandler ingestNodeCommandHandler;
    @Mock
    private IngestContentCommandHandler ingestContentCommandHandler;
    @InjectMocks
    private IngestEventProcessor ingestEventProcessor;

    @Test
    void shouldIngestJustNodeMetadataIfItDoesNotContainAnyContent()
    {
        // given
        Map<String, Serializable> properties = Map.of(
                "cm:name", "test folder",
                "cm:title", "test folder title",
                TYPE_PROPERTY, NODE_TYPE);

        IngestEvent bulkIngesterEvent = new IngestEvent(
                NODE_ID,
                null,
                properties);

        // when
        ingestEventProcessor.process(bulkIngesterEvent);

        // then
        IngestNodeCommand expectedCommand = new IngestNodeCommand(
                NODE_ID,
                CREATE,
                Set.of(
                        PropertyDelta.updated(TYPE_PROPERTY, NODE_TYPE),
                        PropertyDelta.updated("cm:name", "test folder"),
                        PropertyDelta.updated("cm:title", "test folder title")));

        then(ingestNodeCommandHandler).should().handle(eq(expectedCommand));

        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldIngestContentIfAvailable()
    {
        // given
        IngestEvent.ContentInfo contentInfo = new IngestEvent.ContentInfo(
                100L,
                "UTF-8",
                "application/pdf");

        IngestEvent ingestEvent = new IngestEvent(
                NODE_ID,
                contentInfo,
                Map.of(TYPE_PROPERTY, NODE_TYPE,
                        CREATED_AT_PROPERTY, CREATED_AT));

        // when
        ingestEventProcessor.process(ingestEvent);

        // then
        then(ingestNodeCommandHandler).should().handle(any());

        then(ingestContentCommandHandler).should().handle(eq(new IngestContentCommand(NODE_ID)));
    }
}
