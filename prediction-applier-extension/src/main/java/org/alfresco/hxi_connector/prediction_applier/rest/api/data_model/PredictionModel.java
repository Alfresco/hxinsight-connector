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
package org.alfresco.hxi_connector.prediction_applier.rest.api.data_model;

import org.alfresco.service.namespace.QName;

public class PredictionModel
{
    private static final String HXI_URI = "http://www.alfresco.org/model/hxinsightconnector/1.0";
    private static final String HXI_PREFIX = "hxi";

    public static final QName TYPE_PREDICTION = QName.createQName(HXI_URI, "prediction");
    public static final QName PROP_PREDICTION_DATE_TIME = QName.createQName(HXI_URI, "predictionDateTime");
    public static final QName PROP_CONFIDENCE_LEVEL = QName.createQName(HXI_URI, "confidenceLevel");
    public static final QName PROP_MODEL_ID = QName.createQName(HXI_URI, "modelId");
    public static final QName PROP_PREDICTION_VALUE = QName.createQName(HXI_URI, "predictionValue");
    public static final QName PROP_PREVIOUS_VALUE = QName.createQName(HXI_URI, "previousValue");
    public static final QName PROP_UPDATE_TYPE = QName.createQName(HXI_URI, "updateType");

    public static final QName ASPECT_PREDICTION_APPLIED = QName.createQName(HXI_URI, "predictionApplied");
    public static final QName PROP_LATEST_PREDICTION_DATE_TIME = QName.createQName(HXI_URI, "latestPredictionDateTime");
    public static final QName ASSOC_PREDICTED_BY = QName.createQName(HXI_URI, "predictedBy");
}
