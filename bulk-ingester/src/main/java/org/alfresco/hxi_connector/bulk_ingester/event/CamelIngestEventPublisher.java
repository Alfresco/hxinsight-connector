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

package org.alfresco.hxi_connector.bulk_ingester.event;

import static org.apache.camel.LoggingLevel.DEBUG;

import java.net.ConnectException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
class CamelIngestEventPublisher extends RouteBuilder implements IngestEventPublisher
{
    private static final String LOCAL_ENDPOINT = "direct:" + CamelIngestEventPublisher.class.getSimpleName();

    private final CamelContext camelContext;
    private final IngestEventPublisherConfig ingestEventPublisherConfig;

    @Override
    public void configure()
    {
        from(LOCAL_ENDPOINT)
                .marshal()
                .json()
                .log(DEBUG, log, "Sending event ${body}")
                .to(ingestEventPublisherConfig.endpoint());
    }

    @Override
    @Retryable(retryFor = ConnectException.class,
            maxAttemptsExpression = "${alfresco.bulk.ingest.publisher.retry.attempts}",
            backoff = @Backoff(
                    delayExpression = "${alfresco.bulk.ingest.publisher.retry.initial-delay}",
                    multiplierExpression = "${alfresco.bulk.ingest.publisher.retry.delay-multiplier}"))
    public void publish(IngestEvent ingestEvent)
    {
        camelContext.createProducerTemplate()
                .sendBody(LOCAL_ENDPOINT, ingestEvent);
    }
}
