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

import static org.alfresco.hxi_connector.prediction_applier.repository.NodesClient.NODE_ID_HEADER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;
import org.alfresco.hxi_connector.prediction_applier.repository.NodesClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PredictionListener extends RouteBuilder
{
    static final String ROUTE_ID = "prediction-listener";

    private final PredictionMapper predictionMapper;
    private final InsightPredictionsProperties insightPredictionsProperties;

    @Override
    public void configure()
    {
        onException(Exception.class)
                .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
                .stop();

        from(insightPredictionsProperties.bufferEndpoint())
                .routeId(ROUTE_ID)
                .log(LoggingLevel.TRACE, log, "Prediction :: started processing of: ${body}")
                .unmarshal()
                .json(JsonLibrary.Jackson, PredictionEntry.class)
                .setHeader(NODE_ID_HEADER, simple("${body.objectId}"))
                .setBody(exchange -> predictionMapper.map(exchange.getIn().getBody(PredictionEntry.class)))
                .to(NodesClient.NODES_DIRECT_ENDPOINT)
                .end();
    }
}
