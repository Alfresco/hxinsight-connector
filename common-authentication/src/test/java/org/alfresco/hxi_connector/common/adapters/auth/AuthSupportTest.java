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
package org.alfresco.hxi_connector.common.adapters.auth;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.APP_NAME_ATTRIBUTE_KEY;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.CLIENT_REGISTRATION_ID;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.ENVIRONMENT_KEY_ATTRIBUTE_KEY;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.ENVIRONMENT_KEY_HEADER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.SERVICE_USER_ATTRIBUTE_KEY;

import java.util.Collection;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.Authorization;

@ExtendWith(MockitoExtension.class)
class AuthSupportTest
{

    @Mock
    private AuthenticationManager mockAuthManager;
    @Mock
    private OAuth2LoginAuthenticationToken mockAuthentication;

    @Test
    void givenTokenNotPresentInContext_whenAuthenticateCalled_thenSecurityContextSet()
    {
        // given
        String dummyServiceUser = "dummy-service-user";
        String dummyEnvironmentKey = "dummy-env-key";
        Authorization authorizationProperties = new Authorization("dummy-app-name", dummyServiceUser, dummyEnvironmentKey);
        String dummyClientName = "dummy-client-name";
        given(mockAuthManager.authenticate(any())).willReturn(mockAuthentication);

        // when
        AuthSupport.authenticate(dummyClientName, authorizationProperties, mockAuthManager);

        // then
        then(mockAuthManager).should().authenticate(any());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(mockAuthentication, authentication);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void givenTokenPresentInContext_whenSetAuthToken_thenSetAuthInExchange()
    {
        // given
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);
        OAuth2AccessToken mockAccessToken = mock(OAuth2AccessToken.class);
        given(mockAuthentication.getAccessToken()).willReturn(mockAccessToken);

        String dummyToken = "dummy-token";
        given(mockAccessToken.getTokenValue()).willReturn(dummyToken);

        OAuth2AccessToken.TokenType mockTokenType = mock(OAuth2AccessToken.TokenType.class);
        given(mockAccessToken.getTokenType()).willReturn(mockTokenType);

        String dummyTypeValue = "dummy-type-value";
        given(mockTokenType.getValue()).willReturn(dummyTypeValue);
        OAuth2User mockPrincipal = mock(OAuth2User.class);
        given(mockAuthentication.getPrincipal()).willReturn(mockPrincipal);

        String dummyAttribute = "dummy-attribute";
        Map<String, Object> dummyAttributes = Map.of(ENVIRONMENT_KEY_ATTRIBUTE_KEY, dummyAttribute);
        given(mockPrincipal.getAttributes()).willReturn(dummyAttributes);

        Exchange mockExchange = mock(Exchange.class);
        Message mockMessage = mock(Message.class);
        given(mockExchange.getIn()).willReturn(mockMessage);

        // when
        AuthSupport.setAuthorizationToken(mockExchange);

        // then
        String expectedAuthorization = dummyTypeValue + " " + dummyToken;
        Map<String, Object> authHeaders = Map.of(AUTHORIZATION, expectedAuthorization,
                ENVIRONMENT_KEY_HEADER, dummyAttribute);
        then(mockMessage).should().setHeaders(authHeaders);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void givenTokenNotPresentInContext_whenSetAuthToken_thenNotSetInExchange()
    {
        // given
        Exchange mockExchange = mock(Exchange.class);

        // when
        AuthSupport.setAuthorizationToken(mockExchange);

        then(mockExchange).shouldHaveNoInteractions();
    }

    @Test
    void givenAllNecessaryDataProvided_whenCreateOAuth2TokenCalled_thenReturnProperToken()
    {
        // given
        String dummyClientName = "dummy-client-name";
        String dummyServiceUser = "dummy-service-user";
        String dummyEnvironmentKey = "dummy-env-key";

        // when
        OAuth2AuthenticationToken oAuth2AuthenticationToken = AuthSupport.createOAuth2AuthenticationToken(dummyClientName, dummyServiceUser, dummyEnvironmentKey);

        // then
        Collection<GrantedAuthority> authorities = oAuth2AuthenticationToken.getAuthorities();
        OAuth2UserAuthority firstAuthority = (OAuth2UserAuthority) authorities.iterator().next();
        Map<String, Object> userAttributes = Map.of(
                APP_NAME_ATTRIBUTE_KEY, dummyClientName,
                SERVICE_USER_ATTRIBUTE_KEY, dummyServiceUser,
                ENVIRONMENT_KEY_ATTRIBUTE_KEY, dummyEnvironmentKey);
        assertEquals(userAttributes, firstAuthority.getAttributes());
    }

    @Test
    void givenNoClientRegistrationKey_whenIsTokenUriNotBlankCalled_thenReturnFalse()
    {
        // given
        OAuth2ClientProperties mockOAuth2ClientProperties = mock(OAuth2ClientProperties.class);
        OAuth2ClientProperties.Provider mockProvider = mock();
        given(mockOAuth2ClientProperties.getProvider()).willReturn(Map.of("dummy", mockProvider));

        // when
        boolean result = AuthSupport.isTokenUriNotBlank(mockOAuth2ClientProperties);

        assertFalse(result);
    }

    @Test
    void givenEmptyClientRegistrationKey_whenIsTokenUriNotBlankCalled_thenReturnFalse()
    {
        // given
        OAuth2ClientProperties mockOAuth2ClientProperties = mock(OAuth2ClientProperties.class);
        OAuth2ClientProperties.Provider mockProvider = mock();
        given(mockOAuth2ClientProperties.getProvider()).willReturn(Map.of(CLIENT_REGISTRATION_ID, mockProvider));

        // when
        boolean result = AuthSupport.isTokenUriNotBlank(mockOAuth2ClientProperties);

        assertFalse(result);
    }

    @Test
    void givenClientRegistrationKeyWithTokenUri_whenIsTokenUriNotBlankCalled_thenReturnTrue()
    {
        // given
        OAuth2ClientProperties mockOAuth2ClientProperties = mock(OAuth2ClientProperties.class);
        OAuth2ClientProperties.Provider mockProvider = mock(OAuth2ClientProperties.Provider.class);
        given(mockOAuth2ClientProperties.getProvider()).willReturn(Map.of(CLIENT_REGISTRATION_ID, mockProvider));
        given(mockProvider.getTokenUri()).willReturn("dummy-token-uri");

        // when
        boolean result = AuthSupport.isTokenUriNotBlank(mockOAuth2ClientProperties);

        assertTrue(result);
    }

}
