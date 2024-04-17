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
package org.alfresco.hxi_connector.live_ingester.util.auth;

import static org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationService.ENVIRONMENT_KEY_ATTRIBUTE_KEY;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;

public class WithMockOAuth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User>
{

    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2User customUser)
    {
        String tokenUri = "http://localhost" + AuthUtils.TOKEN_PATH;
        ClientRegistration clientRegistration = AuthUtils.creatClientRegistration(tokenUri);
        AuthenticationResult authenticationResult = AuthUtils.createExpectedAuthResult();
        OAuth2AuthorizationExchange oAuth2AuthorizationExchange = new OAuth2AuthorizationExchange(
                OAuth2AuthorizationRequest.authorizationCode()
                        .authorizationUri(tokenUri)
                        .clientId(clientRegistration.getClientId())
                        .build(),
                OAuth2AuthorizationResponse.success(String.valueOf(200))
                        .redirectUri(tokenUri)
                        .build());
        Map<String, Object> userAttributes = Map.of("applicationName", "app-name", ENVIRONMENT_KEY_ATTRIBUTE_KEY, "env-key");
        OAuth2UserAuthority oAuth2UserAuthority = new OAuth2UserAuthority(userAttributes);
        OAuth2User oAuth2User = new DefaultOAuth2User(Set.of(oAuth2UserAuthority), userAttributes, "applicationName");
        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                authenticationResult.accessToken(),
                Instant.now(),
                Instant.now().plus(authenticationResult.expiresIn(), authenticationResult.temporalUnit()),
                Set.of(authenticationResult.scope()));
        Authentication authentication = new OAuth2LoginAuthenticationToken(clientRegistration, oAuth2AuthorizationExchange, oAuth2User, oAuth2User.getAuthorities(), oAuth2AccessToken);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
