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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.database.connector.AlfrescoMetadataRepository;
import org.alfresco.database.connector.ChildAssocParams;
import org.alfresco.database.connector.NodeParams;
import org.alfresco.database.connector.model.AlfrescoNode;
import org.alfresco.database.connector.model.ChildAssocMetaData;
import org.alfresco.database.connector.model.TagData;

class InMemoryAlfrescoMetadataRepository implements AlfrescoMetadataRepository
{
    private final List<AlfrescoNode> nodes = new ArrayList<>();
    private List<NodeParams> requestList = new ArrayList<>();

    public void setNodes(List<AlfrescoNode> nodes)
    {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    @Override
    public List<AlfrescoNode> getAlfrescoNodes(NodeParams nodeParams)
    {
        if (nodeParams.getTimestampRange().isPresent())
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        if (nodeParams.getIdRange().isEmpty())
        {
            throw new IllegalArgumentException("NodeParams idRange parameter is required");
        }

        if (nodeParams.getPaging().isEmpty())
        {
            throw new IllegalArgumentException("NodeParams paging parameter is required");
        }

        requestList.add(nodeParams);

        NodeParams.Range idRange = nodeParams.getIdRange().get();
        NodeParams.Paging paging = nodeParams.getPaging().get();

        return nodes.stream()
                .filter(node -> idRange.getBegin() <= node.getId() && node.getId() < idRange.getEnd())
                .skip((long) paging.getPageSize() * paging.getPage())
                .limit(paging.getPageSize())
                .toList();
    }

    @Override
    public Set<ChildAssocMetaData> getChildAssocMetaData(ChildAssocParams childAssocParams)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, TagData> getAllTags()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Long getDBIdFromNodeRef(String s)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<NodeParams> getRequestList()
    {
        return requestList;
    }

    public void resetRequestList(List<NodeParams> requestList)
    {
        this.requestList = new ArrayList<>();
    }
}
