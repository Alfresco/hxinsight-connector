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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.prediction_applier.config.InsightPredictionsProperties;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;

@Component
@RequiredArgsConstructor
public class PredictionSourceStub extends RouteBuilder
{
    private final InsightPredictionsProperties insightPredictionsProperties;
    private long deliveryDelayInMs;
    private Queue<List<PredictionEntry>> predictionsBatchesQueue = new LinkedList<>();

    @Override
    public void configure()
    {
        from(insightPredictionsProperties.sourceEndpoint())
                .routeId("predictions-source-stub")
                .setBody(exchange -> getPredictionsBatch())
                .marshal(new JacksonDataFormat());
    }

    @SneakyThrows
    private List<PredictionEntry> getPredictionsBatch()
    {
        Thread.sleep(deliveryDelayInMs);

        return Objects.requireNonNullElseGet(predictionsBatchesQueue.poll(), Collections::emptyList);
    }

    @SafeVarargs
    public final void shouldReturnPredictions(List<PredictionEntry>... predictionsBatches)
    {
        predictionsBatchesQueue = new LinkedList<>(Arrays.asList(predictionsBatches));
    }

    public final void shouldReturnPredictions(long delayInMs, List<List<PredictionEntry>> predictionsBatches)
    {
        this.deliveryDelayInMs = delayInMs;
        this.predictionsBatchesQueue = new LinkedList<>(predictionsBatches);
    }
}
