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

package org.alfresco.hxi_connector.prediction_applier.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;

@Component
@RequiredArgsConstructor
public class PredictionBufferStub extends RouteBuilder
{
    private final InsightPredictionsProperties insightPredictionsProperties;
    private final List<PredictionEntry> handledPredictions = new ArrayList<>();

    @Override
    public void configure()
    {
        from(insightPredictionsProperties.bufferEndpoint())
                .routeId("predictions-buffer-stub")
                .log("Handling predictions ${body}")
                .unmarshal(new JacksonDataFormat(PredictionEntry.class))
                .process(exchange -> handledPredictions.add(exchange.getIn().getBody(PredictionEntry.class)));
    }

    public void assertAllPredictionsHandled(List<PredictionEntry> predictionEntries)
    {
        assertEquals(predictionEntries, handledPredictions);
    }

    public void reset()
    {
        handledPredictions.clear();
    }
}
