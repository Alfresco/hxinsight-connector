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
import static org.apache.camel.LoggingLevel.TRACE;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.config.PredictionListenerConfig;
import org.alfresco.hxi_connector.prediction_applier.exception.PredictionApplierRuntimeException;

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
        from(config.predictionProcessorTriggerEndpoint())
                .routeId(PREDICTION_PROCESSOR_TRIGGER_ROUTE_ID)
                .choice()
                .when(this::isProcessingPending)
                    .log(DEBUG, log, "Prediction processing is pending, no need to trigger it")
                .otherwise()
                    .log(DEBUG, log, "Triggering prediction processing")
                    .to(PREDICTION_PROCESSOR);

        from(PREDICTION_PROCESSOR)
                .routeId(PREDICTION_PROCESSOR_ROUTE_ID)
                .process(setIsProcessingPending(true))
                .loopDoWhile(this::hasNextPage)
                    .to(config.predictionsSourceEndpoint())
                    .log(DEBUG, log, "Sending predictions to internal buffer: ${body}")
                    .unmarshal(new ListJacksonDataFormat(Prediction.class))
                    .process(this::setHasNextPage)
                    .process(this::saveBody)
                    .loopDoWhile(this::predictionsBatchNotEmpty)
                        .process(this::setPredictionToSend)
                        .marshal(new ListJacksonDataFormat(Prediction.class))
                        .log(TRACE, log, "Sending prediction to internal buffer: ${body}")
                        .to(config.internalPredictionsBufferEndpoint())
                        .end()
                .end()
                .log(DEBUG, log, "Finished processing predictions")
                .process(setIsProcessingPending(false));
        // @formatter:on
    }

    private boolean isProcessingPending(Exchange exchange)
    {
        return Objects.requireNonNullElse(
                getContext().getRegistry().lookupByNameAndType("is-prediction-processing-pending", Boolean.class),
                false);
    }

    private Processor setIsProcessingPending(boolean isProcessingPending)
    {
        return exchange -> {
            exchange.getIn().setBody(null);
            getContext().getRegistry().bind("is-prediction-processing-pending", isProcessingPending);
        };
    }

    private boolean hasNextPage(Exchange exchange)
    {
        return Objects.requireNonNullElse(exchange.getProperty("has-next-page", Boolean.class), true);
    }

    private void setHasNextPage(Exchange exchange)
    {
        String hasNextPageProperty = "has-next-page";

        Object body = exchange.getIn().getBody();

        if (body == null)
        {
            exchange.setProperty(hasNextPageProperty, true);
            return;
        }

        if (body instanceof List<?> list)
        {
            exchange.setProperty(hasNextPageProperty, !list.isEmpty());
            return;
        }

        throw new PredictionApplierRuntimeException("Unexpected body type: " + body.getClass().getName());
    }

    private void saveBody(Exchange exchange)
    {
        exchange.setVariable("body", exchange.getIn().getBody());
    }

    private boolean predictionsBatchNotEmpty(Exchange exchange)
    {
        return !exchange.getVariable("body", List.class).isEmpty();
    }

    private void setPredictionToSend(Exchange exchange)
    {
        Prediction predictionToSend = (Prediction) exchange.getVariable("body", List.class).remove(0);
        exchange.getIn().setBody(predictionToSend);
    }
}
