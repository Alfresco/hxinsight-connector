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

package org.alfresco.hxi_connector.bulk_ingester.repository;

import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.database.connector.AlfrescoMetadataRepository;
import org.alfresco.database.connector.NodeParams;
import org.alfresco.database.connector.model.AlfrescoNode;
import org.alfresco.hxi_connector.bulk_ingester.repository.filter.AlfrescoNodeFilterHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkIngesterNodeRepository
{
    private final AlfrescoMetadataRepository metadataRepository;

    private final BulkIngesterRepositoryConfig bulkIngesterRepositoryConfig;

    private final AlfrescoNodeFilterHandler alfrescoNodeFilterHandler;

    public Stream<AlfrescoNode> find(IdRange idRange)
    {
        NodeParams nodeParams = NodeParams.searchByIdRange(idRange.from(), idRange.to());

        return IntStream.iterate(0, page -> page + 1)
                .mapToObj(page -> nodeParams.withPaging(page, bulkIngesterRepositoryConfig.pageSize()))
                .map(this::findNodes)
                .peek(nodes -> log.debug("Found {} nodes", nodes.size()))
                .takeWhile(not(Collection::isEmpty))
                .flatMap(Collection::stream);
    }

    private List<AlfrescoNode> findNodes(NodeParams nodeParams)
    {
        log.debug("Looking for nodes: {}", nodeParams);

        return metadataRepository.getAlfrescoNodes(nodeParams).stream()
                .filter(alfrescoNodeFilterHandler::filterNode)
                .collect(Collectors.toList());
    }
}
