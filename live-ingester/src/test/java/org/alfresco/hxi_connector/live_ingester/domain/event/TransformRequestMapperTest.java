/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.live_ingester.domain.event.TransformRequestMapper.PDF_MIMETYPE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.model.transform.request.TransformRequest;

@ExtendWith(MockitoExtension.class)
class TransformRequestMapperTest
{
    static final long TIMESTAMP = 1_234_567_890L;
    static final String NODE_REF = "123412341234-1234-1234-1234-12341234";
    @Mock
    IngestNewNodeEvent ingestNewNodeEvent;
    @Mock
    Node node;
    @InjectMocks
    TransformRequestMapper transformRequestMapper;

    @BeforeEach
    void setUp()
    {
        given(ingestNewNodeEvent.node()).willReturn(node);
    }

    @Test
    void createTransformRequest()
    {
        // given
        given(ingestNewNodeEvent.time()).willReturn(TIMESTAMP);
        given(node.id()).willReturn(NODE_REF);

        // when
        TransformRequest transformRequest = transformRequestMapper.map(ingestNewNodeEvent);

        // then
        TransformRequest expected = new TransformRequest(TIMESTAMP, NODE_REF, PDF_MIMETYPE);
        assertEquals(transformRequest, expected);
    }
}
