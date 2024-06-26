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
package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static java.util.stream.Collectors.toList;

import static org.alfresco.hxi_connector.hxi_extension.rest.api.util.NodesUtils.validateOrLookupNode;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.PredictionModel;
import org.alfresco.hxi_connector.hxi_extension.service.PredictionService;
import org.alfresco.hxi_connector.hxi_extension.service.model.Prediction;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.ListPage;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;

@Component
@RelationshipResource(name = "predictions", entityResource = NodeEntityResource.class, title = "Predictions for a node")
public class PredictionChildrenRelation implements RelationshipResourceAction.Read<PredictionModel>,
        RelationshipResourceAction.Create<PredictionModel>
{
    private final NodesImpl nodes;
    private final TransactionService transactionService;
    private final PredictionService predictionService;

    public PredictionChildrenRelation(@Qualifier("nodes") NodesImpl nodes, @Qualifier("TransactionService") TransactionService transactionService,
            PredictionService predictionService)
    {
        this.nodes = nodes;
        this.transactionService = transactionService;
        this.predictionService = predictionService;
    }

    @Override
    public CollectionWithPagingInfo<PredictionModel> readAll(String nodeId, Parameters params)
    {
        NodeRef nodeRef = validateOrLookupNode(nodes, nodeId);

        List<Prediction> predictions = predictionService.getPredictions(nodeRef);
        List<PredictionModel> predictionModels = predictions.stream().map(PredictionModel::fromServiceModel).collect(toList());

        Paging paging = params.getPaging();
        return ListPage.of(predictionModels, paging);
    }

    @Override
    public List<PredictionModel> create(String nodeId, List<PredictionModel> predictionModels, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodes, nodeId);
        List<Prediction> predictions = predictionModels.stream().map(PredictionModel::toServiceModel).collect(toList());

        RetryingTransactionCallback<List<Prediction>> callback = () -> predictionService.applyPredictions(nodeRef, predictions);
        List<Prediction> outputPredictions = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        return outputPredictions.stream().map(PredictionModel::fromServiceModel).collect(toList());
    }
}
