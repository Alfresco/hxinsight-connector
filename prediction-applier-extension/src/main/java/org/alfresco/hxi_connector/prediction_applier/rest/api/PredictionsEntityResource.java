package org.alfresco.hxi_connector.prediction_applier.rest.api;

import lombok.Setter;
import org.alfresco.hxi_connector.prediction_applier.rest.api.model.ReviewStatus;
import org.alfresco.hxi_connector.prediction_applier.service.PredictionService;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.NodeRef;

@Setter
@EntityResource(name="predictions", title = "Predictions")
public class PredictionsEntityResource
{
    private final String PARAM_REVIEW_STATUS = "reviewStatus";
    private NodesImpl nodes;
    private PredictionService predictionService;

    @Operation("review")
    @WebApiDescription(title = "Review prediction")
    public void reviewPrediction(String predictionId, Void body, Parameters parameters, WithResponse withResponse)
    {
        NodeRef predictionNodeRef = nodes.validateOrLookupNode(predictionId);
        //TODO add IllegalArgumentException handling
        ReviewStatus reviewStatus = ReviewStatus.valueOf(parameters.getParameter(PARAM_REVIEW_STATUS));
        predictionService.reviewPrediction(predictionNodeRef, reviewStatus);
    }
}
