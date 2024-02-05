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

package org.alfresco.hxi_connector.bulk_ingester.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;

@Slf4j
class BulkIngesterNodeRepositoryTest
{
    private static final int PAGE_SIZE = 2;
    private final InMemoryAlfrescoMetadataRepository metadataRepository = new InMemoryAlfrescoMetadataRepository();
    private final BulkIngesterRepositoryConfig repositoryConfig = new BulkIngesterRepositoryConfig(PAGE_SIZE);
    private final BulkIngesterNodeRepository nodeRepository = new BulkIngesterNodeRepository(metadataRepository, repositoryConfig);

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
        ListAppender<ILoggingEvent> testLogsAppender = createTestLogsAppender();

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

    private AlfrescoNode mockNode(long id)
    {
        AlfrescoNode node = mock();

        when(node.getId()).thenReturn(id);

        return node;
    }

    private ListAppender<ILoggingEvent> createTestLogsAppender()
    {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

        addAppenderForLogger(listAppender, BulkIngesterNodeRepository.class);
        addAppenderForLogger(listAppender, BulkIngesterNodeRepositoryTest.class);
        listAppender.start();

        return listAppender;
    }

    private void addAppenderForLogger(ListAppender<ILoggingEvent> appender, Class<?> classToTrack)
    {
        Logger logger = (Logger) LoggerFactory.getLogger(classToTrack);

        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }
}
