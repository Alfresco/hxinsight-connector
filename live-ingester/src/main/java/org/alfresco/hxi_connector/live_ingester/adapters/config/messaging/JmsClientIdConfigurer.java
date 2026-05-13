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
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

/**
 * Sets the JMS {@code clientId} on the autoconfigured {@link SingleConnectionFactory} so the live-ingester can subscribe to {@code alfresco.repo.event2} as a durable consumer. Runs as a {@link BeanPostProcessor} because {@code SingleConnectionFactory} rejects late {@code setClientID} calls once a connection is open ("setClientID call not supported on proxy for shared Connection").
 *
 * <p>
 * The clientId applies to every JMS connection in this process (repo events, bulk-ingester events, transform.response). JMS permits one active connection per clientId per broker, so the durable-subscription path is single-instance only;
 *
 * <p>
 * Activated by {@code alfresco.repository.events-subscription.durable=true}.
 */
@Component
@ConditionalOnProperty(prefix = "alfresco.repository.events-subscription", name = "durable", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class JmsClientIdConfigurer implements BeanPostProcessor
{
    private final IntegrationProperties integrationProperties;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
    {
        if (bean instanceof SingleConnectionFactory factory)
        {
            String clientId = integrationProperties.alfresco().repository().eventsSubscription().name();
            factory.setClientId(clientId);
            log.info("JMS :: configured clientId={} on bean '{}' to enable durable topic subscription", clientId, beanName);
        }
        return bean;
    }
}
