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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.activemq.autoconfigure.ActiveMQConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

/**
 * Sets the JMS {@code clientID} on the autoconfigured {@code ActiveMQConnectionFactory} so the live-ingester can subscribe to {@code alfresco.repo.event2} as a durable consumer. Implemented as an {@link ActiveMQConnectionFactoryCustomizer} (vs. a {@code BeanPostProcessor} on Spring's {@code SingleConnectionFactory}) because the customizer hook fires at the right point in Spring Boot's ActiveMQ auto-configuration lifecycle and does not depend on bean-instantiation ordering — a {@code BeanPostProcessor} that depends on application beans (here, {@code IntegrationProperties}) can be created late, after Spring has already instantiated and shared the {@code ConnectionFactory}, leaving the durable consumer without a clientID.
 *
 * <p>
 * The clientID applies to every JMS connection that the {@code ActiveMQConnectionFactory} produces in this process (repo events, bulk-ingester events, transform.response). JMS permits one active connection per clientID per broker, so the durable-subscription path is single-instance only.
 *
 * <p>
 * Activated by {@code alfresco.repository.events-subscription.durable=true}.
 */
@Component
@ConditionalOnProperty(prefix = "alfresco.repository.events-subscription", name = "durable", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class JmsClientIdConfigurer implements ActiveMQConnectionFactoryCustomizer
{
    private final IntegrationProperties integrationProperties;

    @Override
    public void customize(org.apache.activemq.ActiveMQConnectionFactory factory)
    {
        String clientId = integrationProperties.alfresco().repository().eventsSubscription().name();
        factory.setClientID(clientId);
        log.info("JMS :: configured clientID={} on ActiveMQConnectionFactory to enable durable topic subscription", clientId);
    }
}
