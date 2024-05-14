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

import java.util.List;

import lombok.Setter;

import org.alfresco.hxi_connector.prediction_applier.rest.api.model.PredictionModel;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.ReviewStatus;
import org.alfresco.hxi_connector.prediction_applier.service.PredictionService;
import org.alfresco.hxi_connector.prediction_applier.service.model.Prediction;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.ResourceParameter;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;

@Setter
@RelationshipResource(name = "predictions", entityResource = NodeEntityResource.class, title = "Predictions for a node")
public class PredictionChildrenRelation implements RelationshipResourceAction.Read<PredictionModel>,
        RelationshipResourceAction.Create<PredictionModel>
{
    private final String PARAM_REVIEW_STATUS = "reviewStatus";
    private NodesImpl nodes;
    private TransactionService transactionService;
    private PredictionService predictionService;

    @Override
    public CollectionWithPagingInfo<PredictionModel> readAll(String nodeId, Parameters params)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);

        List<Prediction> predictions = predictionService.getPredictions(nodeRef);
        List<PredictionModel> predictionModels = predictions.stream().map(PredictionModel::fromServiceModel).collect(toList());

        Paging paging = params.getPaging();
        return ListPage.of(predictionModels, paging);
    }

    @Override
    public List<PredictionModel> create(String nodeId, List<PredictionModel> predictionModels, Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        List<Prediction> predictions = predictionModels.stream().map(PredictionModel::toServiceModel).collect(toList());

        RetryingTransactionCallback<List<Prediction>> callback = () -> predictionService.applyPredictions(nodeRef, predictions);
        List<Prediction> outputPredictions = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        return outputPredictions.stream().map(PredictionModel::fromServiceModel).collect(toList());
    }

    @Operation("prediction-review")
    @WebApiDescription(title = "Confirm or reject predictions on the node properties")
    @WebApiParam(name = "predictions", title="predictions", description = "List of predictions to confirm/reject", kind= ResourceParameter.KIND.HTTP_BODY_OBJECT)
    public void handlePredictions(String nodeId, List<Prediction> predictions, Parameters parameters, WithResponse withResponse)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId);
        //TODO add IllegalArgumentException handling
        ReviewStatus reviewStatus = ReviewStatus.valueOf(parameters.getParameter(PARAM_REVIEW_STATUS));
        predictionService.handlePredictions(nodeRef, predictions, reviewStatus);
    }
}
