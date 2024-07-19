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
package org.alfresco.hxi_connector.prediction_applier.hx_insight;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.PredictionModel;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.UpdateType;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;

@Mapper(componentModel = "spring")
public interface PredictionMapper
{

    default PredictionModel map(PredictionEntry predictionEntry)
    {
        if (predictionEntry == null)
        {
            return null;
        }
        if (predictionEntry.predictions().size() != 1)
        {
            throw new HxInsightConnectorRuntimeException("Currently only one prediction per entry is supported.");
        }

        return map(predictionEntry.predictions().get(0), predictionEntry.modelId(), predictionEntry.enrichmentType());
    }

    @Mapping(target = "property", source = "prediction.field")
    @Mapping(target = "confidenceLevel", source = "prediction.confidence")
    @Mapping(target = "predictionValue", source = "prediction.value")
    @Mapping(target = "modelId", source = "modelId")
    @Mapping(target = "updateType", source = "enrichmentType")
    PredictionModel map(Prediction prediction, String modelId, UpdateType enrichmentType);
}
