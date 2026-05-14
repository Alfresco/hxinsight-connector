/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.INFO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Repository.EventsSubscription;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.CamelEventMapper;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DeadLetterChannels;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DlqMetric;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.DlqMetricsRecorder;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LiveIngesterMetrics;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiveIngesterEventHandler extends RouteBuilder
{
    private static final String ROUTE_ID = "repo-events-consumer";
    private static final DlqMetric DLQ_METRIC = new DlqMetric(
            LiveIngesterMetrics.Dlq.REPO_EVENTS,
            LiveIngesterMetrics.Dlq.REPO_EVENTS_DESCRIPTION);

    private final EventProcessor eventProcessor;
    private final IntegrationProperties integrationProperties;
    private final CamelEventMapper camelEventMapper;
    private final DlqMetricsRecorder dlqMetricsRecorder;

    @Override
    public void configure()
    {
        String eventSource = buildEventSourceUri();
        EventsSubscription subscription = integrationProperties.alfresco().repository().eventsSubscription();

        if (subscription.deadLetterEnabled())
        {
            errorHandler(DeadLetterChannels.forRoute(subscription, dlqMetricsRecorder, DLQ_METRIC, log));
        }
        else
        {
            log.warn("Repository :: dead-letter channel disabled. Set alfresco.repository.events-subscription.dead-letter-enabled=true to re-enable.");
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        from(eventSource)
                .transacted()
                .routeId(ROUTE_ID)
                .log(DEBUG, log, "Repository :: Received event: ${body}")
                .setBody(camelEventMapper::repoEventFrom)
                .filter(body().isNotNull())
                .log(INFO, log, "Repository :: Received event with ID: ${body.id} and type: ${body.type} for node: ${body.data?.resource?.id}")
                .process(exchange -> SecurityContextHolder.setContext(securityContext))
                .process(eventProcessor::process)
                .end();
    }

    private String buildEventSourceUri()
    {
        String baseUri = integrationProperties.alfresco().repository().eventsEndpoint();
        var subscription = integrationProperties.alfresco().repository().eventsSubscription();
        if (subscription == null || !subscription.durable())
        {
            log.info("Repository :: Subscribing to {} as a non-durable consumer. Events published while disconnected will be dropped by the broker. Set ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DURABLE=true to enable a durable subscription.", baseUri);
            return baseUri;
        }

        // URL-encode the subscription name before splicing it into the Camel endpoint URI: the field is only @NotBlank,
        // so an operator-configured value containing a space or a reserved URI character (& ? = #) would otherwise
        // produce a malformed query string and prevent the route from starting. Camel decodes endpoint-URI query
        // values as application/x-www-form-urlencoded, so a percent-encoded value round-trips back to the original
        // identifier on the JMS ConnectionFactory.
        String encodedName = URLEncoder.encode(subscription.name(), StandardCharsets.UTF_8);
        String separator = baseUri.contains("?") ? "&" : "?";
        String durableUri = baseUri
                + separator
                + "subscriptionDurable=true"
                + "&durableSubscriptionName=" + encodedName;
        log.info("Repository :: Subscribing to {} as a durable consumer with durableSubscriptionName={}. The connection's clientId is configured on the JMS ConnectionFactory by JmsClientIdConfigurer. Only one live-ingester instance can hold this subscription at a time; multi-instance HA is not yet supported.",
                baseUri, subscription.name());
        return durableUri;
    }
}
