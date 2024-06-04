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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.ENVIRONMENT_KEY_HEADER;

import java.util.Base64;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@ExtendWith(MockitoExtension.class)
class AuthSupportTest
{

    public static final String VALID_TOKEN = "valid-token";
    @Mock
    private AccessTokenProvider mockAccessTokenProvider;

    @Mock
    private AuthProperties mockAuthProperties;

    @Mock
    private AuthProperties.AuthProvider mockAuthProvider;

    @Mock
    private Exchange mockExchange;

    @BeforeEach
    void setUp() {
        when(mockExchange.getIn()).thenReturn(mock());
    }

    @Test
    void givenValidBearerToken_whenSetAlfrescoAuthorizationHeaders_thenBearerHeaderIsSet()
    {
        // given
        given(mockAccessTokenProvider.getAccessToken(AuthSupport.ALFRESCO_AUTH_PROVIDER)).willReturn(VALID_TOKEN);
        given(mockAuthProperties.getProviders()).willReturn(Map.of(AuthSupport.ALFRESCO_AUTH_PROVIDER, mockAuthProvider));
        given(mockAuthProvider.getType()).willReturn(AuthSupport.BEARER.trim().toLowerCase());

        // when
        AuthSupport.setAlfrescoAuthorizationHeaders(mockExchange, mockAccessTokenProvider, mockAuthProperties);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(HttpHeaders.AUTHORIZATION, AuthSupport.BEARER + VALID_TOKEN);
    }

    @Test
    void givenBasicAlfrescoAuthProvided_whenSetAlfrescoAuthorizationHeaders_thenBasicHeaderIsSet()
    {
        // given
        given(mockAuthProperties.getProviders()).willReturn(Map.of(AuthSupport.ALFRESCO_AUTH_PROVIDER, mockAuthProvider));
        given(mockAuthProvider.getType()).willReturn(AuthSupport.BASIC.trim().toLowerCase());
        String username = "username";
        given(mockAuthProvider.getUsername()).willReturn(username);
        String password = "password";
        given(mockAuthProvider.getPassword()).willReturn(password);

        // when
        AuthSupport.setAlfrescoAuthorizationHeaders(mockExchange, mockAccessTokenProvider, mockAuthProperties);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(HttpHeaders.AUTHORIZATION, AuthSupport.BASIC + getEncodedCredentials(username, password));
    }

    @Test
    void givenValidBearerToken_whenSetHxIAuthorizationHeaders_thenHeadersAreSet()
    {
        // given
        given(mockAccessTokenProvider.getAccessToken(AuthSupport.HXI_AUTH_PROVIDER)).willReturn(VALID_TOKEN);
        given(mockAuthProperties.getProviders()).willReturn(Map.of(AuthSupport.HXI_AUTH_PROVIDER, mockAuthProvider));
        String dummyEnvKey = "dummy-environment";
        given(mockAuthProvider.getEnvironmentKey()).willReturn(dummyEnvKey);

        // when
        AuthSupport.setHxIAuthorizationHeaders(mockExchange, mockAccessTokenProvider, mockAuthProperties);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(HttpHeaders.AUTHORIZATION, AuthSupport.BEARER + VALID_TOKEN);
        then(mockExchange.getIn()).should().setHeader(ENVIRONMENT_KEY_HEADER, dummyEnvKey);
    }

    private static String getEncodedCredentials(String username, String password)
    {
        String valueToEncode = username + ":" + password;
        return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private void thenExpectedAuthHeadersCleared()
    {
        then(mockExchange.getIn()).should().removeHeader(HttpHeaders.AUTHORIZATION);
        then(mockExchange.getIn()).should().removeHeader(ENVIRONMENT_KEY_HEADER);
    }
}
