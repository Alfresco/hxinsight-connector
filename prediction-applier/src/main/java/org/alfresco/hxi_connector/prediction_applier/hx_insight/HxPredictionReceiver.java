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

import static org.apache.camel.LoggingLevel.DEBUG;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.prediction_applier.config.PredictionListenerConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class HxPredictionReceiver extends RouteBuilder
{
    private static final String PREDICTION_PROCESSOR_TRIGGER_ROUTE_ID = "prediction-processor-trigger-route";
    private static final String PREDICTION_PROCESSOR_ROUTE_ID = "prediction-processor-route";
    private static final String PREDICTION_PROCESSOR = "direct:prediction-processor";

    private final PredictionListenerConfig config;

    @Override
    public void configure()
    {
        // @formatter:off
        getContext().getRegistry().bind("is-prediction-processing-pending", false);

        from(config.predictionProcessorTriggerEndpoint())
                .routeId(PREDICTION_PROCESSOR_TRIGGER_ROUTE_ID)
                .choice()
                .when(isProcessingPending())
                    .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
                .otherwise()
                    .log(DEBUG, log, "Triggering prediction processing")
                    .to(PREDICTION_PROCESSOR);

        from(PREDICTION_PROCESSOR)
                .routeId(PREDICTION_PROCESSOR_ROUTE_ID)
                .process(setIsProcessingPending(true))
                .loopDoWhile(hasNextPage())
                    .to(config.predictionsEndpoint())
                    .unmarshal(new JacksonDataFormat(List.class))
                    .process(exchange -> {
                        log.info("Processing exchange: {}", exchange.getIn().getBody(List.class));
                    })
                .end()
                .log(DEBUG, log, "Finished processing predictions")
                .process(setIsProcessingPending(false));
        // @formatter:on
    }

    Predicate isProcessingPending()
    {
        return exchange -> getContext().getRegistry().lookupByNameAndType("is-prediction-processing-pending", Boolean.class);
    }

    Processor setIsProcessingPending(boolean isProcessingPending)
    {
        return exchange -> {
            exchange.getIn().setBody(null);
            getContext().getRegistry().bind("is-prediction-processing-pending", isProcessingPending);
        };
    }

    Predicate hasNextPage()
    {
        return exchange -> {
            Object body = exchange.getIn().getBody();

            if (body == null)
            {
                return true;
            }

            if (body instanceof List<?> list)
            {
                return !list.isEmpty();
            }

            throw new RuntimeException("Unexpected body type: " + body.getClass().getName());
        };
    }
}
