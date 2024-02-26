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

package org.alfresco.hxi_connector.bulk_ingester.processor;

import static java.lang.String.format;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.hxi_connector.bulk_ingester.event.NodePublisher;
import org.alfresco.hxi_connector.bulk_ingester.exception.BulkIngesterRuntimeException;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.Node;
import org.alfresco.hxi_connector.bulk_ingester.repository.BulkIngesterNodeRepository;
import org.alfresco.hxi_connector.bulk_ingester.repository.IdRange;
import org.alfresco.hxi_connector.bulk_ingester.spring.ApplicationManager;

@ExtendWith(MockitoExtension.class)
class BulkIngestionProcessorTest
{
    @Mock
    private BulkIngesterNodeRepository bulkIngesterNodeRepository;
    @Mock
    private BulkIngesterConfig bulkIngesterConfig;
    @Mock
    private AlfrescoNodeMapper alfrescoNodeMapper;
    @Mock
    private NodePublisher nodePublisher;
    @Mock
    private ApplicationManager applicationManager;
    @InjectMocks
    private BulkIngestionProcessor bulkIngestionProcessor;

    @Test
    public void shouldPublishNodesFromGivenIdRangeAndShoutDownApplication()
    {
        // given
        IdRange idRange = new IdRange(10, 13);
        List<AlfrescoNode> nodes = List.of(mock("10"), mock("11"), mock("12"));

        given(bulkIngesterConfig.fromId()).willReturn(idRange.from());
        given(bulkIngesterConfig.toId()).willReturn(idRange.to());

        given(bulkIngesterNodeRepository.find(any())).willReturn(nodes.stream());

        // when
        bulkIngestionProcessor.process();

        // then
        then(bulkIngesterNodeRepository).should().find(idRange);

        then(alfrescoNodeMapper).should(times(nodes.size())).map(any());
        then(nodePublisher).should(times(nodes.size())).publish(any());

        then(applicationManager).should().shoutDown();
    }

    @Test
    public void shouldProcessAllNodesEvenIfMappingOfSomeWillFail()
    {
        // given
        List<AlfrescoNode> nodes = List.of(mock("10"), mock("11"), mock("12"));

        given(bulkIngesterNodeRepository.find(any())).willReturn(nodes.stream());
        given(alfrescoNodeMapper.map(any())).will((invocation -> {
            AlfrescoNode givenNode = invocation.getArgument(0);

            if (givenNode == nodes.get(0))
            {
                throw new BulkIngesterRuntimeException(format("Failed to map node %s", givenNode));
            }

            return mock(Node.class);
        }));

        // when
        bulkIngestionProcessor.process();

        // then
        then(alfrescoNodeMapper).should(times(3)).map(any());
        then(nodePublisher).should(times(2)).publish(any());

        then(applicationManager).should().shoutDown();
    }
}
