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

package org.alfresco.hxi_connector.live_ingester.adapters.config.messaging;

import java.net.http.HttpClient;
import jakarta.jms.ConnectionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.camel.CamelContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.DiscoveryApiRepositoryInformation;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ProcessingStarter;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.RepositoryInformation;
import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.health.CamelRoutesReadyHealthIndicator;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.PropertyBasedRepositoryInformation;

@Configuration
public class LiveIngesterMessagingConfig
{

    public static final String CONFIGURATION_ERR_MESSAGE = "One of properties: 'alfresco.repository.discovery-endpoint' or 'alfresco.repository.version-override' must be set in the Live Ingester configuration.";

    private static final int BROKER_MAX_REDELIVERIES = 1;

    /**
     * Provides the {@link PlatformTransactionManager} that Camel's ActiveMQ component uses to make its JMS consumer sessions transactional (wired in {@code application.yml} via {@code camel.component.activemq.transaction-manager=#jmsTransactionManager}). With this in place, an exception bubbling out of a route triggers a JMS rollback, which the broker honours by redelivering the message up to {@link #BROKER_MAX_REDELIVERIES} times (see {@link #brokerRedeliveryPolicyCustomizer}) before routing it to {@code ActiveMQ.DLQ}.
     */
    @Bean
    public PlatformTransactionManager jmsTransactionManager(ConnectionFactory connectionFactory)
    {
        return new JmsTransactionManager(connectionFactory);
    }

    /**
     * Caps broker-level redelivery at one retry. The connector's retry strategy is layered:
     * <ul>
     * <li>In-process {@code @Retryable} on outbound clients handles short-window transient I/O (TCP resets, 5xx) with classified back-off and a bounded per-attempt budget.</li>
     * <li>Broker redelivery (1 retry) absorbs the narrow class of failures that the in-process layer cannot — JMS-level rollbacks where the route's transactional boundary couldn't reach the broker (broker disconnects, ActiveMQ flaps, transport-level timeouts on {@code commit}/{@code rollback}). Keeping it at 1 is the same value the original test profile used for each per-route in-app DLC ({@code ALFRESCO_*_MAXIMUMREDELIVERIES=1}) before this refactor consolidated DLC handling onto the broker — it gives transient broker chaos one chance to recover without re-running the whole route N times and repeating any side effects that already completed.</li>
     * <li>Once both layers exhaust, JMS routes the message to {@code ActiveMQ.DLQ}; the {@code live_ingester_*_dlq_total} counters surface it for operator redrive.</li>
     * </ul>
     *
     * <p>
     * Stacking ActiveMQ's default {@code maximumRedeliveries=6} on top of the in-process retry budget would re-execute the entire route 7 times per failed delivery, repeating any side effects (metadata POSTs, presigned-URL requests, S3 PUTs) that already succeeded in earlier route steps and stretching the per-event wall-time well past the SLAs the reliability ITs assert on. HX Insight ingestion is idempotent on {@code objectId} so the duplicates would be harmless at the destination, but 1 retry is the minimum viable broker buffer.
     *
     * <p>
     * The customizer pattern (vs. replacing the {@link ConnectionFactory} bean) keeps Spring Boot's auto-configuration in charge of the rest of the factory wiring — broker URL, credentials, packaging trust, pooling — and only patches the one knob we care about.
     */
    @Bean
    public ActiveMQConnectionFactoryCustomizer brokerRedeliveryPolicyCustomizer()
    {
        return cf -> {
            RedeliveryPolicy policy = cf.getRedeliveryPolicy();
            policy.setMaximumRedeliveries(BROKER_MAX_REDELIVERIES);
        };
    }

    @Bean
    @ConfigurationProperties(prefix = "application")
    public Application application()
    {
        return new Application();
    }

    @Bean
    public HttpClient httpClient()
    {
        return HttpClient.newHttpClient();
    }

    @Bean
    public AcsHealthProbe acsHealthProbe(HttpClient httpClient, IntegrationProperties integrationProperties)
    {
        boolean isVersionOverrideBlank = StringUtils.isBlank(integrationProperties.alfresco().repository().versionOverride());
        boolean isDiscoverEndpointBlank = StringUtils.isBlank(integrationProperties.alfresco().repository().discoveryEndpoint());
        if (isVersionOverrideBlank && isDiscoverEndpointBlank)
        {
            throw new IllegalStateException(CONFIGURATION_ERR_MESSAGE);
        }
        return new AcsHealthProbe(httpClient,
                integrationProperties.alfresco().repository().healthProbe().endpoint(),
                integrationProperties.alfresco().repository().healthProbe().timeoutSeconds(),
                integrationProperties.alfresco().repository().healthProbe().intervalSeconds(),
                isVersionOverrideBlank);
    }

    @Bean
    public RepositoryInformation repositoryInformation(AuthService authService, ObjectMapper objectMapper, IntegrationProperties integrationProperties,
            HttpClient httpClient)
    {
        if (StringUtils.isNotBlank(integrationProperties.alfresco().repository().versionOverride()))
        {
            return new PropertyBasedRepositoryInformation(integrationProperties);
        }
        else if (StringUtils.isNotBlank(integrationProperties.alfresco().repository().discoveryEndpoint()))
        {
            return new DiscoveryApiRepositoryInformation(integrationProperties.alfresco().repository().discoveryEndpoint(), authService, objectMapper, httpClient);
        }
        throw new IllegalStateException(CONFIGURATION_ERR_MESSAGE);
    }

    @Bean
    public ApplicationInfoProvider applicationInfoProvider(RepositoryInformation repositoryInformation, IntegrationProperties integrationProperties)
    {
        return new ApplicationInfoProvider(repositoryInformation, integrationProperties.application());
    }

    @Bean
    public ProcessingStarter processingStarter(CamelContext camelContext)
    {
        return new ProcessingStarter(camelContext);
    }

    /**
     * Health indicator wired into the {@code readiness} group via {@code management.endpoint.health.group.readiness.include=readinessState,camelRoutes}. Makes {@code /actuator/health/readiness} report {@code UP} only after every Camel route has transitioned to {@code Started} — closing the window where Spring's default readiness flips to {@code ACCEPTING_TRAFFIC} on {@code ApplicationReadyEvent} but {@link ProcessingStarter} has not yet started routes (it waits for the first {@code AcsHealthy} event).
     */
    @Bean("camelRoutes")
    public HealthIndicator camelRoutesReadyHealthIndicator(CamelContext camelContext)
    {
        return new CamelRoutesReadyHealthIndicator(camelContext);
    }
}
