/*-
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
package org.alfresco.hxi_connector.prediction_applier.rest.api;

import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_LATEST_PREDICTION_DATE_TIME;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.hxi_connector.prediction_applier.rest.api.model.NodeWithPrediction;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

@EntityResource(name = "nodes", title = "Nodes With Predictions")
public class NodeEntityResource implements EntityResourceAction.ReadById<NodeWithPrediction>
{
    private NodeService nodeService;
    private NodesImpl nodes;

    @Override
    public NodeWithPrediction readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(id);

        Serializable date = nodeService.getProperty(nodeRef, PROP_LATEST_PREDICTION_DATE_TIME);

        return new NodeWithPrediction(id, (Date) date);
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNodes(NodesImpl nodes)
    {
        this.nodes = nodes;
    }
}
