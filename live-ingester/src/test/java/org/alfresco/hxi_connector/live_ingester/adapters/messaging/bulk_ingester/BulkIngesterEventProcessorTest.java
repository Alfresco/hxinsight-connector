/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.bulk_ingester.model.BulkIngesterEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestMetadataCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class BulkIngesterEventProcessorTest
{
    private static final String NODE_ID = "07659d13-8d64-4905-a329-6b27fe182023";
    private static final String NODE_TYPE = "cm:folder";
    private static final String CREATOR_ID = "admin";
    private static final String MODIFIER_ID = "hr_user";
    private static final Set<String> ASPECT_NAMES = Set.of("cm:titled");
    private static final long CREATED_AT = 1000L;

    @Mock
    private IngestMetadataCommandHandler ingestMetadataCommandHandler;
    @Mock
    private IngestContentCommandHandler ingestContentCommandHandler;
    @InjectMocks
    private BulkIngesterEventProcessor bulkIngesterEventProcessor;

    @Test
    void shouldIngestJustNodeMetadataIfItDoesNotContainAnyContent()
    {
        // given
        Map<String, Serializable> properties = Map.of(
                "cm:name", "test folder",
                "cm:title", "test folder title");

        BulkIngesterEvent bulkIngesterEvent = new BulkIngesterEvent(
                NODE_ID,
                NODE_TYPE,
                CREATOR_ID,
                MODIFIER_ID,
                ASPECT_NAMES,
                null,
                CREATED_AT,
                properties);

        // when
        bulkIngesterEventProcessor.process(bulkIngesterEvent);

        // then
        IngestMetadataCommand expectedCommand = new IngestMetadataCommand(
                NODE_ID,
                false,
                PropertyDelta.updated(NODE_TYPE),
                PropertyDelta.updated(CREATOR_ID),
                PropertyDelta.updated(MODIFIER_ID),
                PropertyDelta.updated(ASPECT_NAMES),
                PropertyDelta.updated(CREATED_AT),
                Set.of(
                        CustomPropertyDelta.updated("cm:name", "test folder"),
                        CustomPropertyDelta.updated("cm:title", "test folder title")));

        then(ingestMetadataCommandHandler).should().handle(eq(expectedCommand));

        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldIngestContentIfAvailable()
    {
        // given
        BulkIngesterEvent.ContentInfo contentInfo = new BulkIngesterEvent.ContentInfo(
                100L,
                "UTF-8",
                "application/pdf");

        BulkIngesterEvent bulkIngesterEvent = new BulkIngesterEvent(
                NODE_ID,
                NODE_TYPE,
                CREATOR_ID,
                MODIFIER_ID,
                ASPECT_NAMES,
                contentInfo,
                CREATED_AT,
                Map.of());

        // when
        bulkIngesterEventProcessor.process(bulkIngesterEvent);

        // then
        then(ingestMetadataCommandHandler).should().handle(any());

        then(ingestContentCommandHandler).should().handle(eq(new IngestContentCommand(NODE_ID)));
    }
}
