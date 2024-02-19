/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.config.auth;

import java.time.Instant;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationClient;
import org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationResult;

@RequiredArgsConstructor
public class HxOAuth2AuthenticationProvider implements AuthenticationProvider
{

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final AuthenticationClient hxAuthenticationClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = authenticationToken.getAuthorizedClientRegistrationId();
        OAuth2ClientPropertiesMapper mapper = new OAuth2ClientPropertiesMapper(oAuth2ClientProperties);
        ClientRegistration clientRegistration = mapper.asClientRegistrations().get(clientRegistrationId);
        String tokenUri = oAuth2ClientProperties.getProvider().get(clientRegistrationId).getTokenUri();

        AuthenticationResult authenticationResult = hxAuthenticationClient.authenticate(tokenUri, clientRegistration);

        OAuth2AuthorizationExchange oAuth2AuthorizationExchange = new OAuth2AuthorizationExchange(
                OAuth2AuthorizationRequest.authorizationCode()
                        .authorizationUri(tokenUri)
                        .clientId(clientRegistration.getClientId())
                        .build(),
                OAuth2AuthorizationResponse.success(String.valueOf(authenticationResult.statusCode()))
                        .redirectUri(tokenUri)
                        .build());
        OAuth2User oAuth2User = authenticationToken.getPrincipal();
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                authenticationResult.accessToken(),
                Instant.now(),
                Instant.now().plus(authenticationResult.expiresIn(), authenticationResult.temporalUnit()),
                Set.of(authenticationResult.scope()));

        return new OAuth2LoginAuthenticationToken(clientRegistration, oAuth2AuthorizationExchange, oAuth2User, oAuth2User.getAuthorities(), oAuth2AccessToken);
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return OAuth2AuthenticationToken.class.isAssignableFrom(authentication);
    }
}
