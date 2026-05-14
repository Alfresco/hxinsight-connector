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
package org.alfresco.hxi_connector.hxi_extension.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Set;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.RetryContext;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

class CicClientFactoryTest
{
    private static final Retry DEFAULT_RETRY = new Retry(3, 500, 2.0);

    @Test
    void shouldSetMaxAttempts()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(new Retry(5, 200, 2.0));

        assertEquals(5, policy.maxAttempts());
    }

    @Test
    void shouldComputeExponentialBackoffDelays()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(DEFAULT_RETRY);

        // attempt 1: jittered in [0, 500 * 2^0] = [0, 500ms]
        Duration delay1 = policy.backoffStrategy().computeDelay(context(1, new IOException()));
        assertTrue(!delay1.isNegative() && delay1.compareTo(Duration.ofMillis(500)) <= 0);
        // attempt 2: jittered in [0, 500 * 2^1] = [0, 1000ms]
        Duration delay2 = policy.backoffStrategy().computeDelay(context(2, new IOException()));
        assertTrue(!delay2.isNegative() && delay2.compareTo(Duration.ofMillis(1000)) <= 0);
        // attempt 3: jittered in [0, 500 * 2^2] = [0, 2000ms]
        Duration delay3 = policy.backoffStrategy().computeDelay(context(3, new IOException()));
        assertTrue(!delay3.isNegative() && delay3.compareTo(Duration.ofMillis(2000)) <= 0);
    }

    @Test
    void shouldRetryOnIOException()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(DEFAULT_RETRY);

        assertTrue(policy.retryCondition().shouldRetry(context(1, new IOException())));
    }

    @Test
    void shouldRetryOnUnknownHostException()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(DEFAULT_RETRY);

        assertTrue(policy.retryCondition().shouldRetry(context(1, new UnknownHostException())));
    }

    @Test
    void shouldNotRetryOnUnrelatedRuntimeException()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(DEFAULT_RETRY);

        assertFalse(policy.retryCondition().shouldRetry(context(1, new RuntimeException())));
    }

    @Test
    void shouldClampInitialDelayToOneWhenZero()
    {
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(new Retry(3, 0, 2.0));

        Duration delay = policy.backoffStrategy().computeDelay(context(1, new IOException()));
        assertTrue(!delay.isNegative() && delay.compareTo(Duration.ofMillis(1)) <= 0);
    }

    @Test
    void shouldRetryOnConfiguredReason()
    {
        Retry retry = new Retry(3, 500, 2.0, Set.of(EndpointServerErrorException.class));
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(retry);

        assertTrue(policy.retryCondition().shouldRetry(context(1, new EndpointServerErrorException("server error"))));
    }

    @Test
    void shouldRetryOnIOExceptionWrappedInConfiguredReason()
    {
        Retry retry = new Retry(3, 500, 2.0, Set.of(EndpointServerErrorException.class));
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(retry);

        // IOException in cause chain matches the IOException check before reasons check
        RuntimeException wrapper = new RuntimeException(new IOException("wrapped"));
        assertTrue(policy.retryCondition().shouldRetry(context(1, wrapper)));
    }

    @Test
    void shouldRetryOnConfiguredReasonInCauseChain()
    {
        Retry retry = new Retry(3, 500, 2.0, Set.of(EndpointServerErrorException.class));
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(retry);

        RuntimeException wrapper = new RuntimeException(new EndpointServerErrorException("wrapped"));
        assertTrue(policy.retryCondition().shouldRetry(context(1, wrapper)));
    }

    @Test
    void shouldNotRetryWhenReasonsIsEmpty()
    {
        Retry retry = new Retry(3, 500, 2.0, Set.of());
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(retry);

        assertFalse(policy.retryCondition().shouldRetry(context(1, new RuntimeException())));
    }

    @Test
    void shouldCapBackoffDelayAtTwentySeconds()
    {
        // initialDelay=1000ms, after 10 doublings uncapped would be 1024s, capped at 20s
        RetryPolicy policy = CicClientFactory.buildRetryPolicy(new Retry(20, 1_000, 2.0));

        Duration delay = policy.backoffStrategy().computeDelay(context(10, new IOException()));
        assertTrue(delay.compareTo(Duration.ofSeconds(20)) <= 0);
    }

    @Test
    void shouldBuildAuthWithNullScope() throws Exception
    {
        AuthenticationHttpClient.Builder builder = CicClientFactory.buildAuth(
                "http://token", "id", "secret", null, DEFAULT_RETRY);
        AuthenticationHttpClient client = builder.clientId("id").clientSecret("secret").build();

        assertTrue(scopes(client).isEmpty());
    }

    @Test
    void shouldBuildAuthWithBlankScope() throws Exception
    {
        AuthenticationHttpClient.Builder builder = CicClientFactory.buildAuth(
                "http://token", "id", "secret", "   ", DEFAULT_RETRY);
        AuthenticationHttpClient client = builder.clientId("id").clientSecret("secret").build();

        assertTrue(scopes(client).isEmpty());
    }

    @Test
    void shouldBuildAuthWithSingleScope() throws Exception
    {
        AuthenticationHttpClient.Builder builder = CicClientFactory.buildAuth(
                "http://token", "id", "secret", "read", DEFAULT_RETRY);
        AuthenticationHttpClient client = builder.clientId("id").clientSecret("secret").build();

        assertEquals(Set.of("read"), scopes(client));
    }

    @Test
    void shouldBuildAuthWithMultipleSpaceSeparatedScopes() throws Exception
    {
        AuthenticationHttpClient.Builder builder = CicClientFactory.buildAuth(
                "http://token", "id", "secret", "read write admin", DEFAULT_RETRY);
        AuthenticationHttpClient client = builder.clientId("id").clientSecret("secret").build();

        assertEquals(Set.of("read", "write", "admin"), scopes(client));
    }

    private static Set<String> scopes(AuthenticationHttpClient client) throws Exception
    {
        Field field = AuthenticationHttpClient.class.getDeclaredField("scopes");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> result = (Set<String>) field.get(client);
        return result;
    }

    private static RetryContext context(int attempt, Exception exception)
    {
        return new RetryContext(attempt, "POST", 0, exception);
    }
}
