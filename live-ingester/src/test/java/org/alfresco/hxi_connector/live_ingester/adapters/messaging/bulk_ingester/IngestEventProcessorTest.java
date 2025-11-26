/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.PERMISSIONS_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.permissionsMetadataUpdated;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.updated;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver.AuthorityType;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.MimeTypeMapper;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.TriggerContentIngestionCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.IngestNodeCommandHandler;

@ExtendWith(MockitoExtension.class)
class IngestEventProcessorTest
{
    private static final String NODE_ID = "07659d13-8d64-4905-a329-6b27fe182023";
    private static final String NODE_TYPE = "cm:folder";
    private static final long CREATED_AT = 1000L;
    private static final long TIMESTAMP = Instant.now().toEpochMilli();

    @Mock
    private IngestNodeCommandHandler ingestNodeCommandHandler;
    @Mock
    private IngestContentCommandHandler ingestContentCommandHandler;
    @Mock
    private MimeTypeMapper mimeTypeMapper;
    @Mock
    private AuthorityTypeResolver authorityTypeResolver;
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
                properties,
                TIMESTAMP);

        // when
        ingestEventProcessor.process(bulkIngesterEvent);

        // then
        IngestNodeCommand expectedCommand = new IngestNodeCommand(
                NODE_ID,
                CREATE_OR_UPDATE,
                Set.of(
                        updated(TYPE_PROPERTY, NODE_TYPE),
                        updated("cm:name", "test folder"),
                        updated("cm:title", "test folder title")),
                TIMESTAMP);

        then(ingestNodeCommandHandler).should().handle(eq(expectedCommand));

        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }

    @Test
    void shouldIngestContentIfAvailable()
    {
        // given
        String mimeType = "application/pdf";
        IngestEvent.ContentInfo contentInfo = new IngestEvent.ContentInfo(
                100L,
                "UTF-8",
                mimeType);
        IngestEvent ingestEvent = new IngestEvent(
                NODE_ID,
                contentInfo,
                Map.of(TYPE_PROPERTY, NODE_TYPE,
                        CREATED_AT_PROPERTY, CREATED_AT),
                TIMESTAMP);
        given(mimeTypeMapper.mapMimeType(mimeType)).willReturn(mimeType);

        // when
        ingestEventProcessor.process(ingestEvent);

        // then
        IngestNodeCommand expectedCommand = new IngestNodeCommand(
                NODE_ID,
                CREATE_OR_UPDATE,
                Set.of(
                        contentMetadataUpdated(CONTENT_PROPERTY, mimeType, 100L, null),
                        updated(TYPE_PROPERTY, NODE_TYPE),
                        updated(CREATED_AT_PROPERTY, CREATED_AT)),
                TIMESTAMP);
        then(ingestNodeCommandHandler).should().handle(expectedCommand);

        then(mimeTypeMapper).should().mapMimeType(mimeType);
        then(ingestContentCommandHandler).should().handle(eq(new TriggerContentIngestionCommand(NODE_ID, mimeType, TIMESTAMP)));
    }

    @Test
    void shouldNotIngestContentIfAvailableButMimeTypeMappedToEmpty()
    {
        // given
        String mimeType = "application/octetstream";
        IngestEvent.ContentInfo contentInfo = new IngestEvent.ContentInfo(
                100L,
                "UTF-8",
                mimeType);

        IngestEvent ingestEvent = new IngestEvent(
                NODE_ID,
                contentInfo,
                Map.of(TYPE_PROPERTY, NODE_TYPE,
                        CREATED_AT_PROPERTY, CREATED_AT),
                TIMESTAMP);
        given(mimeTypeMapper.mapMimeType(mimeType)).willReturn(MimeTypeMapper.EMPTY_MIME_TYPE);

        // when
        ingestEventProcessor.process(ingestEvent);

        // then
        IngestNodeCommand expectedCommand = new IngestNodeCommand(
                NODE_ID,
                CREATE_OR_UPDATE,
                Set.of(
                        contentMetadataUpdated(CONTENT_PROPERTY, mimeType, 100L, null),
                        updated(TYPE_PROPERTY, NODE_TYPE),
                        updated(CREATED_AT_PROPERTY, CREATED_AT)),
                TIMESTAMP);
        then(ingestNodeCommandHandler).should().handle(expectedCommand);

        then(mimeTypeMapper).should().mapMimeType(mimeType);
        then(ingestContentCommandHandler).shouldHaveNoInteractions();
    }
}
