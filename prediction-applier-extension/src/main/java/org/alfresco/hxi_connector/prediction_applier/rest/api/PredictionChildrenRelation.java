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

import static java.util.stream.Collectors.toList;

import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_CONFIDENCE_LEVEL;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_MODEL_ID;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_PREDICTION_DATE_TIME;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_PREDICTION_VALUE;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.PROP_UPDATE_TYPE;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionModel.TYPE_PREDICTION;
import static org.alfresco.model.ContentModel.PROP_NAME;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.hxi_connector.prediction_applier.rest.api.model.Prediction;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.UpdateType;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

@RelationshipResource(name = "predictions", entityResource = NodeEntityResource.class, title = "Predictions for a node")
public class PredictionChildrenRelation implements RelationshipResourceAction.Read<Prediction>
{
    private NodeService nodeService;
    private NodesImpl nodes;

    @Override
    public CollectionWithPagingInfo<Prediction> readAll(String nodeId, Parameters params)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, Set.of(TYPE_PREDICTION));
        List<Prediction> predictions = childAssocs.stream().map(this::childAssocToPrediction).collect(toList());

        Paging paging = params.getPaging();
        return CollectionWithPagingInfo.asPaged(
                paging,
                predictions,
                predictions.size() > paging.getSkipCount() + paging.getMaxItems(),
                predictions.size());
    }

    private Prediction childAssocToPrediction(ChildAssociationRef childAssociationRef)
    {
        NodeRef predictionNodeRef = childAssociationRef.getChildRef();
        Map<QName, Serializable> properties = nodeService.getProperties(predictionNodeRef);

        String property = ((String) properties.get(PROP_NAME)).replaceFirst("_", ":");
        Date predictionDateTime = (Date) properties.get(PROP_PREDICTION_DATE_TIME);
        Float confidenceLevel = (Float) properties.get(PROP_CONFIDENCE_LEVEL);
        String modelId = (String) properties.get(PROP_MODEL_ID);
        Serializable predictionValue = properties.get(PROP_PREDICTION_VALUE);
        UpdateType updateType = UpdateType.valueOf((String) properties.get(PROP_UPDATE_TYPE));

        return new Prediction(property, predictionDateTime, confidenceLevel, modelId, predictionValue, updateType);
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
