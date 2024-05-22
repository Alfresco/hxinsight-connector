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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DefaultAccessTokenProviderTest
{

    private static final String CLIENT_REGISTRATION_ID = "testClient";
    private static final String TEST_TOKEN = "testToken";
    @Mock
    private CamelContext mockCamelContext;
    @Mock
    private AuthenticationClient mockAuthenticationClient;

    @InjectMocks
    private DefaultAccessTokenProvider objectUnderTest;

    @BeforeEach
    void setUp()
    {
        given(mockCamelContext.isStarted()).willReturn(true);
    }

    @Test
    void givenTokenNotPresent_whenGetAccessToken_thenRefreshToken()
    {
        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.accessToken()).willReturn(TEST_TOKEN);
        given(mockResult.expiresIn()).willReturn(3600);
        given(mockResult.temporalUnit()).willReturn(ChronoUnit.SECONDS);
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willReturn(mockResult);

        // when
        String token = objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID);

        then(mockAuthenticationClient).should().authenticate(CLIENT_REGISTRATION_ID);
        then(mockAuthenticationClient).shouldHaveNoMoreInteractions();
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void givenTokenExpired_whenGetAccessToken_thenRefreshToken()
    {
        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.accessToken()).willReturn(TEST_TOKEN);
        given(mockResult.expiresIn()).willReturn(3600);
        given(mockResult.temporalUnit()).willReturn(ChronoUnit.SECONDS);
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willReturn(mockResult);

        Map<String, Map.Entry<AuthenticationResult, OffsetDateTime>> tokens = new HashMap<>();
        tokens.put(CLIENT_REGISTRATION_ID, Map.entry(mockResult, OffsetDateTime.now().minusSeconds(1)));
        ReflectionTestUtils.setField(objectUnderTest, "accessTokens", tokens);

        // when
        String token = objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID);

        then(mockAuthenticationClient).should().authenticate(CLIENT_REGISTRATION_ID);
        then(mockAuthenticationClient).shouldHaveNoMoreInteractions();
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void givenTokenValid_whenGetAccessToken_thenReturnTokenWithoutRefresh()
    {
        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.accessToken()).willReturn(TEST_TOKEN);

        Map<String, Map.Entry<AuthenticationResult, OffsetDateTime>> tokens = new HashMap<>();
        tokens.put(CLIENT_REGISTRATION_ID, Map.entry(mockResult, OffsetDateTime.now().plusSeconds(3600)));
        ReflectionTestUtils.setField(objectUnderTest, "accessTokens", tokens);

        // when
        String token = objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID);

        then(mockAuthenticationClient).shouldHaveNoInteractions();
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void givenAuthenticationError_whenGetAccessToken_thenThrowException()
    {
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willThrow(RuntimeException.class);

        // then
        assertThrows(RuntimeException.class, () -> objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID));
    }

}
