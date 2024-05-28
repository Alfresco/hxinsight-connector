/*
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

package org.alfresco.hxi_connector.prediction_applier.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.alfresco.hxi_connector.prediction_applier.exception.PredictionApplierRuntimeException;

@ConfigurationProperties(prefix = "hyland-experience.insight.predictions")
@SuppressWarnings({"PMD.LongVariable", "PMD.UnusedAssignment"})
public record InsightPredictionsProperties(
        String collectorTimerEndpoint,
        Long pollPeriodMillis,
        @NotBlank String sourceBaseUrl,
        @NotBlank String bufferEndpoint)
{
    public InsightPredictionsProperties
    {
        if (pollPeriodMillis == null && collectorTimerEndpoint == null)
        {
            throw new PredictionApplierRuntimeException("Poll period is required when predictions source endpoint is not provided");
        }

        if (pollPeriodMillis != null)
        {
            collectorTimerEndpoint = "quartz:predictions-collector-timer?autoStartScheduler=true&trigger.repeatInterval=" + pollPeriodMillis;
        }
    }
}
