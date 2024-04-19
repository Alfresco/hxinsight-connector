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

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.config.PredictionListenerConfig;

@Component
@RequiredArgsConstructor
public class InternalPredictionBufferStub extends RouteBuilder
{
    private final PredictionListenerConfig predictionListenerConfig;

    private final List<Prediction> handledPredictions;

    @Override
    public void configure()
    {
        from(predictionListenerConfig.internalPredictionsBufferEndpoint())
                .log("Handling predictions ${body}")
                .unmarshal(new JacksonDataFormat(Prediction.class))
                .process(exchange -> handledPredictions.add(exchange.getIn().getBody(Prediction.class)));
    }

    public void assertAllPredictionsHandled(List<Prediction> predictions)
    {
        assertEquals(predictions, handledPredictions);
    }

    public void reset()
    {
        handledPredictions.clear();
    }
}
