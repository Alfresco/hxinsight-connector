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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.ALFRESCO_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXI_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_APP_HEADER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_ENVIRONMENT_HEADER;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.constant.HttpHeaders;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest
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

    @InjectMocks
    private AuthService objectUnderTest;

    @BeforeEach
    void setUp()
    {
        lenient().when(mockExchange.getIn()).thenReturn(mock());
    }

    @Test
    void givenValidBearerToken_whenSetAlfrescoAuthorizationHeaders_thenBearerHeaderIsSet()
    {
        // given
        given(mockAccessTokenProvider.getAccessToken(ALFRESCO_AUTH_PROVIDER)).willReturn(VALID_TOKEN);
        given(mockAuthProperties.getProviders()).willReturn(Map.of(ALFRESCO_AUTH_PROVIDER, mockAuthProvider));
        given(mockAuthProvider.getType()).willReturn(AuthService.BEARER.trim().toLowerCase(Locale.getDefault()));

        // when
        objectUnderTest.setAlfrescoAuthorizationHeaders(mockExchange);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(AUTHORIZATION, AuthService.BEARER + VALID_TOKEN);
    }

    @Test
    void givenBasicAlfrescoAuthProvided_whenSetAlfrescoAuthorizationHeaders_thenBasicHeaderIsSet()
    {
        // given
        given(mockAuthProperties.getProviders()).willReturn(Map.of(ALFRESCO_AUTH_PROVIDER, mockAuthProvider));
        given(mockAuthProvider.getType()).willReturn(AuthService.BASIC.trim().toLowerCase(Locale.getDefault()));
        String username = "username";
        given(mockAuthProvider.getUsername()).willReturn(username);
        String password = "password";
        given(mockAuthProvider.getPassword()).willReturn(password);

        // when
        objectUnderTest.setAlfrescoAuthorizationHeaders(mockExchange);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(AUTHORIZATION, AuthService.BASIC + getEncodedCredentials(username, password));
    }

    @Test
    void givenValidBearerToken_whenSetHxIAuthorizationHeaders_thenHeadersAreSet()
    {
        // given
        given(mockAccessTokenProvider.getAccessToken(AuthService.HXI_AUTH_PROVIDER)).willReturn(VALID_TOKEN);
        given(mockAuthProperties.getProviders()).willReturn(Map.of(AuthService.HXI_AUTH_PROVIDER, mockAuthProvider));
        String dummyEnvKey = "dummy-environment";
        given(mockAuthProvider.getEnvironmentKey()).willReturn(dummyEnvKey);

        // when
        objectUnderTest.setHxIAuthorizationHeaders(mockExchange);

        // then
        thenExpectedAuthHeadersCleared();
        then(mockExchange.getIn()).should().setHeader(AUTHORIZATION, AuthService.BEARER + VALID_TOKEN);
        then(mockExchange.getIn()).should().setHeader(HXP_ENVIRONMENT_HEADER, dummyEnvKey);
        then(mockExchange.getIn()).should().setHeader(HXP_APP_HEADER, "hxai-discovery");
    }

    @Test
    void testGetHxIAuthHeaders()
    {
        // given
        given(mockAuthProperties.getProviders()).willReturn(mock());
        given(mockAuthProperties.getProviders().get("hyland-experience")).willReturn(mock());
        given(mockAuthProperties.getProviders().get("hyland-experience").getEnvironmentKey()).willReturn("test-environment");
        given(mockAccessTokenProvider.getAccessToken("hyland-experience")).willReturn("token");

        // when
        Map<String, String> headers = objectUnderTest.getAuthHeaders(HXI_AUTH_PROVIDER);

        // then
        assertThat(headers).containsExactlyInAnyOrderEntriesOf(Map.of(HttpHeaders.AUTHORIZATION, "Bearer token",
                HXP_ENVIRONMENT_HEADER, "test-environment", HXP_APP_HEADER, "hxai-discovery"));
    }

    @Test
    void testGetAlfrescoAuthHeaders()
    {
        // given
        given(mockAuthProperties.getProviders()).willReturn(mock());
        given(mockAuthProperties.getProviders().get("alfresco")).willReturn(mock());
        given(mockAuthProperties.getProviders().get("alfresco").getType()).willReturn("Basic");
        given(mockAuthProperties.getProviders().get("alfresco").getUsername()).willReturn("user");
        given(mockAuthProperties.getProviders().get("alfresco").getPassword()).willReturn("pass");

        // when
        Map<String, String> headers = objectUnderTest.getAuthHeaders(ALFRESCO_AUTH_PROVIDER);

        // then
        assertThat(headers).containsExactlyEntriesOf(Map.of(
                HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes())));
    }

    private static String getEncodedCredentials(String username, String password)
    {
        String valueToEncode = username + ":" + password;
        return Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    private void thenExpectedAuthHeadersCleared()
    {
        then(mockExchange.getIn()).should().removeHeader(AUTHORIZATION);
        then(mockExchange.getIn()).should().removeHeader(HXP_ENVIRONMENT_HEADER);
        then(mockExchange.getIn()).should().removeHeader(HXP_APP_HEADER);
    }
}
