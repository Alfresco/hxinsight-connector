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
import java.net.UnknownHostException;
import java.time.Duration;

import org.hyland.sdk.cic.http.client.retry.RetryContext;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.config.properties.Retry;

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

        // attempt 1: 500 * 2^0 = 500ms
        assertEquals(Duration.ofMillis(500), policy.backoffStrategy().computeDelay(context(1, new IOException())));
        // attempt 2: 500 * 2^1 = 1000ms
        assertEquals(Duration.ofMillis(1000), policy.backoffStrategy().computeDelay(context(2, new IOException())));
        // attempt 3: 500 * 2^2 = 2000ms
        assertEquals(Duration.ofMillis(2000), policy.backoffStrategy().computeDelay(context(3, new IOException())));
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

    private static RetryContext context(int attempt, Exception exception)
    {
        return new RetryContext(attempt, "POST", 0, exception);
    }
}
