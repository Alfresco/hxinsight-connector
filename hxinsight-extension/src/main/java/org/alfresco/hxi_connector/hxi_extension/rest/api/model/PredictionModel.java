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
package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsonInclude(NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(prefix = {"_", ""})
@SuppressWarnings("PMD.FieldNamingConventions")
public class PredictionModel
{
    private String _id;
    private String property;
    private Date predictionDateTime;
    private float confidenceLevel;
    private String modelId;
    private Serializable predictionValue;
    private Serializable _previousValue;
    private UpdateType updateType;
    private ReviewStatus reviewStatus;

    public PredictionModel(String property, Date predictionDateTime, float confidenceLevel, String modelId, Serializable predictionValue, UpdateType updateType)
    {
        this.property = property;
        this.predictionDateTime = predictionDateTime;
        this.confidenceLevel = confidenceLevel;
        this.modelId = modelId;
        this.predictionValue = predictionValue;
        this.updateType = updateType;
    }
}
