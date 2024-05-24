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
package org.alfresco.hxi_connector.prediction_applier.auth;

import java.util.Objects;

import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.HxAuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.TokenRequest;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.prediction_applier.config.HxInsightProperties;
import org.alfresco.hxi_connector.prediction_applier.config.NodesApiProperties;

@Component
public class PredictionApplierHxAuthClient extends HxAuthenticationClient
{
    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final NodesApiProperties nodesApiProperties;

    public PredictionApplierHxAuthClient(CamelContext camelContext, HxInsightProperties hxInsightProperties,
            OAuth2ClientProperties oAuth2ClientProperties, NodesApiProperties nodesApiProperties)
    {
        super(camelContext, hxInsightProperties.hylandExperience().authentication().retry());
        this.oAuth2ClientProperties = oAuth2ClientProperties;
        this.nodesApiProperties = nodesApiProperties;
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.initialDelay}",
                    multiplierExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.delayMultiplier}"))
    @Override
    public AuthenticationResult authenticate(String tokenUri, ClientRegistration clientRegistration)
    {
        return super.authenticate(tokenUri, clientRegistration);
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.initialDelay}",
                    multiplierExpression = "#{@hxInsightProperties.hylandExperience.authentication.retry.delayMultiplier}"))
    @Override
    public AuthenticationResult authenticate(String clientRegistrationId)
    {
        OAuth2ClientProperties.Provider provider = oAuth2ClientProperties.getProvider().get(clientRegistrationId);
        Objects.requireNonNull(provider, "Auth Provider not found for client registration id: " + clientRegistrationId);
        OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get(clientRegistrationId);
        Objects.requireNonNull(registration, "Auth Registration not found for client registration id: " + clientRegistrationId);
        String tokenUri = provider.getTokenUri();
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(clientRegistrationId)
                .tokenUri(tokenUri)
                .authorizationGrantType(new AuthorizationGrantType(registration.getAuthorizationGrantType()))
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .scope(registration.getScope())
                .build();
        return super.authenticate(tokenUri, clientRegistration);
    }

    @Override
    protected String createEncodedBody(ClientRegistration clientRegistration)
    {
        if (AuthorizationGrantType.PASSWORD.getValue().equals(clientRegistration.getAuthorizationGrantType().getValue()))
        {
            return TokenRequest.builder()
                    .grantType(clientRegistration.getAuthorizationGrantType().getValue())
                    .clientId(clientRegistration.getClientId())
                    .scope(clientRegistration.getScopes())
                    .username(nodesApiProperties.username())
                    .password(nodesApiProperties.password())
                    .build()
                    .getTokenRequestBody();

        }
        return super.createEncodedBody(clientRegistration);
    }
}
