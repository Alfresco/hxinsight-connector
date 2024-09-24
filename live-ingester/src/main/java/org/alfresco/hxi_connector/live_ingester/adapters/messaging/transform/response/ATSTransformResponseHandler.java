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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.WARN;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.exception.ResourceNotFoundException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.ATSTransformRequester;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommand;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.IngestContentCommandHandler;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.EmptyRenditionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ATSTransformResponseHandler extends RouteBuilder
{
    private static final String ROUTE_ID = "transform-events-consumer";
    private static final int EXPECTED_STATUS_CODE = 201;
    private static final String EXPECTED_STATUS_CODE_REGEX = "[\\s\\S]*\"status\"\\s*:\\s*%s[^0-9][\\s\\S]*".formatted(EXPECTED_STATUS_CODE);

    private final IngestContentCommandHandler ingestContentCommandHandler;
    private final IntegrationProperties integrationProperties;
    private final ATSTransformRequester atsTransformRequester;

    @Override
    public void configure()
    {
        String transformationSource = integrationProperties.alfresco().transform().response().endpoint();
        onException(EmptyRenditionException.class, ResourceNotFoundException.class)
                .log(WARN, log, "Transform :: Unexpected state while processing rendition from: %s due to: ${exception.message}. Body: ${body}".formatted(transformationSource))
                .process(this::retryContentTransformation);

        onException(Exception.class)
                .log(ERROR, log, "Transform :: Retrying ${routeId}, attempt ${header.CamelRedeliveryCounter} due to ${exception.message}. Body: ${body}")
                .maximumRedeliveries(integrationProperties.alfresco().transform().response().retryIngestion().attempts())
                .redeliveryDelay(integrationProperties.alfresco().transform().response().retryIngestion().initialDelay())
                .backOffMultiplier(integrationProperties.alfresco().transform().response().retryIngestion().delayMultiplier());

        from(transformationSource)
                .routeId(ROUTE_ID)
                .choice().when(body().regex(EXPECTED_STATUS_CODE_REGEX))
                .log(DEBUG, log, "Transform :: Received transform completed event: ${body}")
                .otherwise()
                .log(WARN, log, "Transform :: Transformation failed. Body: ${body}")
                .end()
                .unmarshal()
                .json(JsonLibrary.Jackson, TransformResponse.class)
                .process(this::ingestContent)
                .end();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void ingestContent(Exchange exchange)
    {
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);
        if (transformResponse.status() == 400)
        {
            return;
        }

        IngestContentCommand command = new IngestContentCommand(
                transformResponse.targetReference(),
                transformResponse.clientData().nodeRef(),
                transformResponse.clientData().targetMimeType(),
                transformResponse.clientData().timestamp());

        ingestContentCommandHandler.handle(command);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void retryContentTransformation(Exchange exchange)
    {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        TransformResponse transformResponse = exchange.getIn().getBody(TransformResponse.class);

        int maxAttempts = integrationProperties.alfresco().transform().response().retryTransformation().attempts();
        int retryAttempt = transformResponse.clientData().retryAttempt() + 1;

        if (retryAttempt > maxAttempts)
        {
            log.error("Transform :: Transformation of node {} failed with error {}, max number of retries ({}) exceeded", transformResponse.clientData().nodeRef(), exception.getMessage(), maxAttempts);
            return;
        }

        log.info("Transform :: Transformation of node {} failed with error {}, retrying (attempt: {})", transformResponse.clientData().nodeRef(), exception.getMessage(), retryAttempt);

        TransformRequest transformRequest = new TransformRequest(
                transformResponse.clientData().nodeRef(),
                transformResponse.clientData().targetMimeType(),
                transformResponse.clientData().timestamp());
        atsTransformRequester.requestTransformRetry(transformRequest, retryAttempt);
    }
}
