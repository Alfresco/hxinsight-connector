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
import static org.alfresco.hxi_connector.prediction_applier.rest.api.data_model.PredictionDataModel.PROP_REVIEW_STATUS;
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

import org.alfresco.hxi_connector.common.util.EnsureUtils;
import org.alfresco.hxi_connector.prediction_applier.rest.api.exception.PredictionStateChangedException;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.ReviewStatus;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.UpdateType;
import org.alfresco.hxi_connector.prediction_applier.service.model.Prediction;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
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
        EnsureUtils.ensureNotNullOrEmpty(predictions, "Predictions list cannot be null or empty");

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
                Serializable previousValue = existingPredictionMetadata.get(PROP_PREVIOUS_VALUE);
                Map<QName, Serializable> predictionProperties = propertiesFromPrediction(prediction, previousValue);
                nodeService.setProperties(predictionNodeRef, predictionProperties);
                prediction.setId(predictionNodeRef.getId());
                prediction.setPreviousValue(previousValue);
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

        if (!returnList.isEmpty())
        {
            nodeProperties.put(PROP_LATEST_PREDICTION_DATE_TIME, new Date());
            nodeService.setProperties(nodeRef, nodeProperties);
        }

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
        ReviewStatus reviewStatus = (ReviewStatus) properties.get(PROP_REVIEW_STATUS);
        reviewStatus = reviewStatus != null ? reviewStatus : ReviewStatus.UNREVIEWED;

        return new Prediction(predictionNodeRef.getId(), property, predictionDateTime, confidenceLevel, modelId, predictionValue, previousValue, updateType, reviewStatus);
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

    @Override
    public void reviewPrediction(NodeRef predictionNodeRef, ReviewStatus reviewStatus) throws EntityNotFoundException, PredictionStateChangedException
    {
        // get prediction node parent (there should be only one)
        for (ChildAssociationRef parent : nodeService.getParentAssocs(predictionNodeRef))
        {
            NodeRef parentNode = parent.getParentRef();
            Map<QName, Serializable> existingProperties = new HashMap<>(nodeService.getProperties(parentNode));
            Map<QName, Serializable> predictionNodeProperties = new HashMap<>(nodeService.getProperties(predictionNodeRef));
            Prediction prediction = getPredictions(parentNode)
                    .stream()
                    .filter(pred -> pred.getId().equals(predictionNodeRef.getId()))
                    .findFirst()
                    .orElse(null);
            if (prediction != null)
            {
                QName propertyQName = QName.createQName(prediction.getProperty(), namespaceService);
                if (existingProperties.get(propertyQName).equals(prediction.getPredictionValue()))
                {
                    if (predictionNodeProperties.get(PROP_REVIEW_STATUS).equals(ReviewStatus.UNREVIEWED))
                    {
                        if (reviewStatus.equals(ReviewStatus.CONFIRMED))
                        {
                            predictionNodeProperties.put(PROP_REVIEW_STATUS, ReviewStatus.CONFIRMED);
                        }
                        else if (reviewStatus.equals(ReviewStatus.REJECTED))
                        {
                            existingProperties.put(propertyQName, prediction.getPreviousValue());
                            nodeService.setProperties(parentNode, existingProperties);
                            predictionNodeProperties.put(PROP_REVIEW_STATUS, ReviewStatus.REJECTED);
                        }
                        nodeService.setProperties(predictionNodeRef, predictionNodeProperties);
                    }
                    else
                    {
                        if (!reviewStatus.equals(prediction.getReviewStatus()))
                        {
                            throw new PredictionStateChangedException("Prediction for " + prediction.getProperty() + " property has already been reviewed.");
                        }
                    }
                }
                else
                {
                    throw new PredictionStateChangedException(prediction.getProperty() + " property value has changed, prediction is no longer valid!");
                }
            }
            else
            {
                throw new EntityNotFoundException(predictionNodeRef.getId());
            }
        }
    }
}
