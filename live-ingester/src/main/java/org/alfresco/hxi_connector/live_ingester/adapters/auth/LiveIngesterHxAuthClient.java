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
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.HxAuthenticationClient;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

public class LiveIngesterHxAuthClient extends HxAuthenticationClient
{

    public LiveIngesterHxAuthClient(CamelContext camelContext, IntegrationProperties integrationProperties, OAuth2ClientProperties oAuth2ClientProperties)
    {
        super(camelContext, integrationProperties.hylandExperience().authentication().retry(), oAuth2ClientProperties);
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.authentication.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.authentication.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.authentication.retry.delayMultiplier}"))
    @Override
    public AuthenticationResult authenticate(String tokenUri, ClientRegistration clientRegistration)
    {
        return super.authenticate(tokenUri, clientRegistration);
    }
}
