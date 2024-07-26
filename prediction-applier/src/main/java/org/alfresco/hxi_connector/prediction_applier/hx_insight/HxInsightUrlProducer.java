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
package org.alfresco.hxi_connector.prediction_applier.hx_insight;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LongVariable"})
public class HxInsightUrlProducer
{
    private static final String BATCHES_URL_PATTERN = "%s/prediction-batches?httpMethod=GET&status=APPROVED&page=${headers.%s}";
    private static final String PREDICTIONS_URL_PATTERN = "%s/prediction-batches/${headers.%s}?httpMethod=GET&page=${headers.%s}";
    private static final String PREDICTIONS_CONFIRMATION_URL_PATTERN = "%s/prediction-batches/${headers.%s}?httpMethod=PUT";
    public static final String BATCH_ID_HEADER = "batchId";
    public static final String BATCHES_PAGE_NO_HEADER = "batchesPageNo";
    public static final String PREDICTIONS_PAGE_NO_HEADER = "predictionsPageNo";

    private final InsightPredictionsProperties insightPredictionsProperties;

    public String getBatchesUrl()
    {
        return withUserAgentDataPathParam(BATCHES_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCHES_PAGE_NO_HEADER));
    }

    public String getPredictionsUrl()
    {
        return withUserAgentDataPathParam(PREDICTIONS_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCH_ID_HEADER, PREDICTIONS_PAGE_NO_HEADER));
    }

    public String getConfirmationUrl()
    {
        return withUserAgentDataPathParam(PREDICTIONS_CONFIRMATION_URL_PATTERN.formatted(insightPredictionsProperties.sourceBaseUrl(), BATCH_ID_HEADER));
    }

    private String withUserAgentDataPathParam(String url)
    {
        return url + ApplicationInfoProvider.USER_AGENT_PARAM;
    }
}
