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
 * Sets the JMS connection {@code clientId} on the autoconfigured Spring {@link SingleConnectionFactory} (which {@link org.springframework.jms.connection.CachingConnectionFactory} extends) so the live-ingester can subscribe to the repository's {@code alfresco.repo.event2} topic as a durable consumer. The clientId must be set on the factory before any connection is established, otherwise {@code SingleConnectionFactory} rejects late {@code setClientID} calls with "setClientID call not supported on proxy for shared Connection".
 *
 * <p>
 * Activated only when {@code alfresco.repository.events-subscription.durable=true}; the default (non-durable) deployment is unaffected.
 *
 * <p>
 * Side effects: the configured clientId applies to <i>all</i> JMS connections opened by the live-ingester (including the bulk-ingester-events consumer and the transform.response consumer). JMS allows only one active connection per clientId per broker, so a second live-ingester instance using the same clientId will fail to open its connection. This matches the single-instance topology that production runs today; multi-instance HA is not yet supported.
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
