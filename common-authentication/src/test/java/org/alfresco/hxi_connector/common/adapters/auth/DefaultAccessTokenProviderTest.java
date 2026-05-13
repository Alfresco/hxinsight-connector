/*-
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
package org.alfresco.hxi_connector.common.adapters.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.alfresco.hxi_connector.common.adapters.auth.DefaultAccessTokenProvider.REFRESH_OFFSET_SECS;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private AuthenticationClient mockAuthenticationClient;

    @InjectMocks
    private DefaultAccessTokenProvider objectUnderTest;

    @Test
    void givenTokenNotPresent_whenGetAccessToken_thenRefreshToken()
    {
        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.getAccessToken()).willReturn(TEST_TOKEN);
        given(mockResult.getExpiresIn()).willReturn(3600);
        given(mockResult.getTemporalUnit()).willReturn(ChronoUnit.SECONDS);
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willReturn(mockResult);

        // when
        String token = objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID);

        then(mockAuthenticationClient).should().authenticate(CLIENT_REGISTRATION_ID);
        then(mockAuthenticationClient).shouldHaveNoMoreInteractions();
        assertEquals(TEST_TOKEN, token);
        Map<String, DefaultAccessTokenProvider.Token> accessTokens = (Map<String, DefaultAccessTokenProvider.Token>) ReflectionTestUtils.getField(objectUnderTest, "accessTokens");
        OffsetDateTime offsetDateTime = OffsetDateTime.now().plusSeconds(3600 - REFRESH_OFFSET_SECS).truncatedTo(ChronoUnit.SECONDS);
        assertEquals(offsetDateTime, accessTokens.get(CLIENT_REGISTRATION_ID).getRefreshAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void givenTokenExpired_whenGetAccessToken_thenRefreshToken()
    {
        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.getAccessToken()).willReturn(TEST_TOKEN);
        given(mockResult.getExpiresIn()).willReturn(3600);
        given(mockResult.getTemporalUnit()).willReturn(ChronoUnit.SECONDS);
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willReturn(mockResult);

        Map<String, DefaultAccessTokenProvider.Token> tokens = new HashMap<>();
        tokens.put(CLIENT_REGISTRATION_ID, new DefaultAccessTokenProvider.Token(TEST_TOKEN, OffsetDateTime.now().minusSeconds(2)));
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
        Map<String, DefaultAccessTokenProvider.Token> tokens = new HashMap<>();
        tokens.put(CLIENT_REGISTRATION_ID, new DefaultAccessTokenProvider.Token(TEST_TOKEN, OffsetDateTime.now().plusSeconds(2)));
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

    /**
     * Two concurrent {@code getAccessToken} calls that both observe the same expired cached token must result in a single auth-endpoint round-trip — the second caller, after acquiring the monitor, has to re-check the cache and pick up the token the first caller just refreshed.
     *
     * <p>
     * Trigger: pre-seed an expired token, then race two threads. Thread A enters the {@code synchronized} block first; the mock {@code authenticate} blocks until Thread B has done its outside-the-monitor read of the cache map and is queued on the monitor. When the mock returns, Thread A puts the fresh token into the map and releases the monitor; Thread B acquires it. A correctly-implemented double-checked-locking pattern re-reads the map inside the monitor and short-circuits the refresh; the buggy pattern checks a stale local variable captured before the monitor and triggers a redundant {@code authenticate} call.
     */
    @Test
    void shouldRefreshOnlyOnceWhenConcurrentCallersRaceForExpiredToken() throws Exception
    {
        Map<String, DefaultAccessTokenProvider.Token> tokens = new HashMap<>();
        tokens.put(CLIENT_REGISTRATION_ID, new DefaultAccessTokenProvider.Token("stale", OffsetDateTime.now().minusSeconds(10)));
        ReflectionTestUtils.setField(objectUnderTest, "accessTokens", tokens);

        AuthenticationResult mockResult = Mockito.mock(AuthenticationResult.class);
        given(mockResult.getAccessToken()).willReturn(TEST_TOKEN);
        given(mockResult.getExpiresIn()).willReturn(3600);
        given(mockResult.getTemporalUnit()).willReturn(ChronoUnit.SECONDS);

        CountDownLatch threadAInsideAuthenticate = new CountDownLatch(1);
        CountDownLatch releaseThreadA = new CountDownLatch(1);
        given(mockAuthenticationClient.authenticate(CLIENT_REGISTRATION_ID)).willAnswer(invocation -> {
            threadAInsideAuthenticate.countDown();
            releaseThreadA.await(2, TimeUnit.SECONDS);
            return mockResult;
        });

        ExecutorService exec = Executors.newFixedThreadPool(2);
        try
        {
            CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID), exec);
            threadAInsideAuthenticate.await(2, TimeUnit.SECONDS);

            CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> objectUnderTest.getAccessToken(CLIENT_REGISTRATION_ID), exec);
            // Generous gap so Thread B definitely completes the outside-the-monitor map read and is queued on the monitor before we release A. The stale-vs-fresh race only manifests when B's outside read happens before A's mutation; the gap is the wall-clock proxy for that ordering.
            Thread.sleep(200);
            releaseThreadA.countDown();

            assertEquals(TEST_TOKEN, a.get(2, TimeUnit.SECONDS));
            assertEquals(TEST_TOKEN, b.get(2, TimeUnit.SECONDS));
        }
        finally
        {
            exec.shutdownNow();
        }

        then(mockAuthenticationClient).should(times(1)).authenticate(CLIENT_REGISTRATION_ID);
        then(mockAuthenticationClient).shouldHaveNoMoreInteractions();
    }

}
