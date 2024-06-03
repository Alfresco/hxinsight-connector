/*-
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
package org.alfresco.hxi_connector.live_ingester.adapters.auth;

import org.apache.camel.CamelContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@Component
public class LiveIngesterAuthClient extends DefaultAuthenticationClient
{

    public LiveIngesterAuthClient(CamelContext camelContext, AuthProperties authProperties)
    {
        super(camelContext, authProperties);
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@authorizationProperties.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@authorizationProperties.retry.initialDelay}",
                    multiplierExpression = "#{@authorizationProperties.retry.delayMultiplier}"))
    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        return super.authenticate(providerId);
    }
}
