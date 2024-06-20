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

import static org.alfresco.hxi_connector.prediction_applier.rest.api.util.NodesUtils.validateOrLookupNode;

import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.prediction_applier.rest.api.model.ReviewStatus;
import org.alfresco.hxi_connector.prediction_applier.service.PredictionService;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.NodeRef;

@Component
@EntityResource(name = "predictions", title = "Predictions")
public class PredictionsEntityResource
{
    private static final String PARAM_REVIEW_STATUS = "reviewStatus";

    private final NodesImpl nodes;

    public PredictionsEntityResource(NodesImpl nodes, PredictionService predictionService)
    {
        this.nodes = nodes;
        this.predictionService = predictionService;
    }

    private final PredictionService predictionService;

    @Operation("review")
    @WebApiDescription(title = "Review prediction")
    public void reviewPrediction(String predictionNodeId, Void body, Parameters parameters, WithResponse withResponse)
    {
        NodeRef predictionNodeRef = validateOrLookupNode(nodes, predictionNodeId);
        ReviewStatus reviewStatus = ReviewStatus.fromString(parameters.getParameter(PARAM_REVIEW_STATUS));
        predictionService.reviewPrediction(predictionNodeRef, reviewStatus);
    }
}
