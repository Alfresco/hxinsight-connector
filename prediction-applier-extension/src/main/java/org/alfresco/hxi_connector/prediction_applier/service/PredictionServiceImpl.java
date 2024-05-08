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
package org.alfresco.hxi_connector.prediction_applier.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.ASSOC_PREDICTED_BY;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_CONFIDENCE_LEVEL;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_LATEST_PREDICTION_DATE_TIME;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_MODEL_ID;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_PREDICTION_DATE_TIME;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_PREDICTION_VALUE;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_PREVIOUS_VALUE;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_UPDATE_TYPE;
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.TYPE_PREDICTION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import org.alfresco.hxi_connector.prediction_applier.rest.api.model.UpdateType;
import org.alfresco.hxi_connector.prediction_applier.service.model.Prediction;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService
{
    private final NodeService nodeService;
    private final NamespaceService namespaceService;

    @Override
    public List<Prediction> applyPredictions(NodeRef nodeRef, List<Prediction> predictions)
    {
        Map<QName, Serializable> nodeProperties = new HashMap<>(nodeService.getProperties(nodeRef));

        Map<QName, ChildAssociationRef> existingPredictedProperties = nodeService.getChildAssocs(nodeRef, Set.of(TYPE_PREDICTION))
                .stream().collect(toMap(ChildAssociationRef::getQName, childAssociationRef -> childAssociationRef));

        List<Prediction> returnList = new ArrayList<>();
        for (Prediction prediction : predictions)
        {
            QName propertyQName = QName.createQName(prediction.getProperty(), namespaceService);

            if (existingPredictedProperties.containsKey(propertyQName))
            {
                NodeRef predictionNodeRef = existingPredictedProperties.get(propertyQName).getChildRef();
                Map<QName, Serializable> existingPredictionMetadata = nodeService.getProperties(predictionNodeRef);
                Serializable previousPredictedValue = existingPredictionMetadata.get(PROP_PREDICTION_VALUE);
                // Don't use prediction if existing prediction was rejected.
                if (!nodeProperties.get(propertyQName).equals(previousPredictedValue))
                {
                    continue;
                }
                Map<QName, Serializable> predictionProperties = propertiesFromPrediction(prediction, previousPredictedValue);
                nodeService.setProperties(predictionNodeRef, predictionProperties);
                prediction.setId(predictionNodeRef.getId());
            }
            else
            {
                Serializable previousValue = nodeProperties.get(propertyQName);
                Map<QName, Serializable> predictionProperties = propertiesFromPrediction(prediction, previousValue);
                ChildAssociationRef childAssociationRef = nodeService.createNode(nodeRef, ASSOC_PREDICTED_BY, propertyQName, TYPE_PREDICTION, predictionProperties);
                prediction.setId(childAssociationRef.getChildRef().getId());
                prediction.setPreviousValue(previousValue);
            }

            returnList.add(prediction);
            nodeProperties.put(propertyQName, prediction.getPredictionValue());
        }

        nodeProperties.put(PROP_LATEST_PREDICTION_DATE_TIME, new Date());
        nodeService.setProperties(nodeRef, nodeProperties);

        return returnList;
    }

    private Map<QName, Serializable> propertiesFromPrediction(Prediction prediction, Serializable previousValue)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(PROP_PREDICTION_DATE_TIME, prediction.getPredictionDateTime());
        properties.put(PROP_CONFIDENCE_LEVEL, prediction.getConfidenceLevel());
        properties.put(PROP_MODEL_ID, prediction.getModelId());
        properties.put(PROP_PREDICTION_VALUE, prediction.getPredictionValue());
        properties.put(PROP_UPDATE_TYPE, prediction.getUpdateType());
        properties.put(PROP_PREVIOUS_VALUE, previousValue);
        return properties;
    }

    @Override
    public List<Prediction> getPredictions(NodeRef nodeRef)
    {
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, Set.of(TYPE_PREDICTION));
        return childAssocs.stream().map(this::childAssocToPrediction).collect(toList());
    }

    private Prediction childAssocToPrediction(ChildAssociationRef childAssociationRef)
    {
        String property = childAssociationRef.getQName().toPrefixString(namespaceService);
        NodeRef predictionNodeRef = childAssociationRef.getChildRef();
        Map<QName, Serializable> properties = nodeService.getProperties(predictionNodeRef);

        Date predictionDateTime = (Date) properties.get(PROP_PREDICTION_DATE_TIME);
        Float confidenceLevel = (Float) properties.get(PROP_CONFIDENCE_LEVEL);
        String modelId = (String) properties.get(PROP_MODEL_ID);
        Serializable predictionValue = properties.get(PROP_PREDICTION_VALUE);
        Serializable previousValue = properties.get(PROP_PREVIOUS_VALUE);
        UpdateType updateType = UpdateType.valueOf((String) properties.get(PROP_UPDATE_TYPE));

        return new Prediction(predictionNodeRef.getId(), property, predictionDateTime, confidenceLevel, modelId, predictionValue, previousValue, updateType);
    }

    @Override
    public List<String> getPredictedProperties(NodeRef nodeRef)
    {
        List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(nodeRef, Set.of(TYPE_PREDICTION));
        return childAssociations.stream()
                .map(ChildAssociationRef::getQName)
                .map(qName -> qName.toPrefixString(namespaceService))
                .collect(toList());
    }
}
