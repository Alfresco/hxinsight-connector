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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.hxi_connector.common.test.util.LoggingUtils.createLogsListAppender;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.database.connector.model.AlfrescoNode;
import org.alfresco.hxi_connector.bulk_ingester.repository.filter.AlfrescoNodeFilterHandler;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BulkIngesterNodeRepositoryTest
{
    private static final int PAGE_SIZE = 2;
    private final InMemoryAlfrescoMetadataRepository metadataRepository = new InMemoryAlfrescoMetadataRepository();
    private final BulkIngesterRepositoryConfig repositoryConfig = new BulkIngesterRepositoryConfig(PAGE_SIZE);

    private final AlfrescoNodeFilterHandler mockFilterHandler = mock(AlfrescoNodeFilterHandler.class);
    private final BulkIngesterNodeRepository nodeRepository = new BulkIngesterNodeRepository(metadataRepository, repositoryConfig, mockFilterHandler);

    @BeforeEach
    void mockNodeFilter()
    {
        given(mockFilterHandler.filterNode(any())).willReturn(true);
    }

    @Test
    void shouldFindAllNodes()
    {
        // given
        IdRange idRange = new IdRange(0, 4);
        List<AlfrescoNode> nodes = List.of(mockNode(0), mockNode(1), mockNode(2), mockNode(3));

        metadataRepository.setNodes(nodes);
        // when
        List<AlfrescoNode> foundNodes = nodeRepository.find(idRange)
                .toList();

        // then
        assertEquals(nodes, foundNodes);
    }

    @Test
    void shouldReturnEmptyListIfNoNodes()
    {
        // given
        IdRange idRange = new IdRange(0, 5);
        List<AlfrescoNode> nodes = List.of();

        metadataRepository.setNodes(nodes);
        // when
        List<AlfrescoNode> foundNodes = nodeRepository.find(idRange)
                .toList();

        // then
        assertEquals(nodes, foundNodes);
    }

    @Test
    void ensureNodeStreamIsLazyEvaluated()
    {
        // given
        ListAppender<ILoggingEvent> testLogsAppender = createLogsListAppender(BulkIngesterNodeRepository.class, BulkIngesterNodeRepositoryTest.class);

        metadataRepository.setNodes(List.of(mockNode(0), mockNode(1), mockNode(2), mockNode(3)));

        // when
        nodeRepository.find(new IdRange(0, 4))
                .forEach(node -> log.debug("Found node {}", node.getId()));

        // then
        List<String> logs = testLogsAppender.list.stream()
                .map(Object::toString)
                .toList();

        assertEquals(10, logs.size());

        assertTrue(logs.get(0).contains("Looking for nodes"));
        assertTrue(logs.get(1).contains("Found 2 nodes"));
        assertTrue(logs.get(2).contains("Found node 0"));
        assertTrue(logs.get(3).contains("Found node 1"));
        assertTrue(logs.get(4).contains("Looking for nodes"));
    }
    @Test
    void shouldFindAllNodesWithPrimaryHierarchy()
    {
        IdRange idRange = new IdRange(0, 4);
        List<AlfrescoNode> nodes = List.of(mockNode(0), mockNode(1), mockNode(2), mockNode(3));

        metadataRepository.setNodes(nodes);
        metadataRepository.setPrimaryHierarchyEnabled(true);

        List<AlfrescoNode> foundNodes = nodeRepository.find(idRange)
                .toList();

        assertEquals(nodes, foundNodes);
        assertTrue(metadataRepository.wasPrimaryHierarchyRequested());
    }

    @Test
    void shouldReturnEmptyListIfNoNodesWithPrimaryHierarchy()
    {
        IdRange idRange = new IdRange(0, 5);
        List<AlfrescoNode> nodes = List.of();

        metadataRepository.setNodes(nodes);
        metadataRepository.setPrimaryHierarchyEnabled(true);

        List<AlfrescoNode> foundNodes = nodeRepository.find(idRange)
                .toList();

        assertEquals(nodes, foundNodes);
        assertTrue(metadataRepository.wasPrimaryHierarchyRequested());
    }

    @Test
    void shouldRequestPrimaryHierarchyForEachPage()
    {

        metadataRepository.setNodes(List.of(mockNode(0), mockNode(1), mockNode(2), mockNode(3)));
        metadataRepository.setPrimaryHierarchyEnabled(true);

        nodeRepository.find(new IdRange(0, 4))
                .forEach(node -> log.debug("Found node {}", node.getId()));

        assertEquals(3, metadataRepository.getPrimaryHierarchyRequestCount());
    }
    private AlfrescoNode mockNode(long id)
    {
        AlfrescoNode node = mock();

        when(node.getId()).thenReturn(id);

        return node;
    }
}
