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

package org.alfresco.hxi_connector.bulk_ingester.processor;

import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.hxi_connector.bulk_ingester.event.IngestEventPublisher;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper;
import org.alfresco.hxi_connector.bulk_ingester.repository.BulkIngesterNodeRepository;
import org.alfresco.hxi_connector.bulk_ingester.repository.IdRange;
import org.alfresco.hxi_connector.bulk_ingester.spring.ApplicationManager;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkIngestionProcessor
{
    private final BulkIngesterNodeRepository bulkIngesterNodeRepository;
    private final BulkIngesterConfig bulkIngesterConfig;
    private final AlfrescoNodeMapper alfrescoNodeMapper;
    private final IngestEventPublisher ingestEventPublisher;
    private final ApplicationManager applicationManager;

    public void process()
    {
        IdRange idRange = new IdRange(bulkIngesterConfig.fromId(), bulkIngesterConfig.toId());

        bulkIngesterNodeRepository.find(idRange)
                .flatMap(this::mapToIngestEventStream)
                .forEach(ingestEventPublisher::publish);

        applicationManager.shutDown();
    }

    private Stream<IngestEvent> mapToIngestEventStream(AlfrescoNode node)
    {
        try
        {
            return Stream.of(alfrescoNodeMapper.map(node));
        }
        catch (Exception e)
        {
            log.error("Failed to map node {}", node.getId(), e);
            return Stream.empty();
        }
    }
}
