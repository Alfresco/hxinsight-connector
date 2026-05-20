/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;

/**
 * Verifies that {@link RetryMetricsRecorder} is auto-registered by Spring Retry's {@code @EnableRetry} and that the counter increments once per failed in-delivery attempt.
 */
@SpringBootTest(classes = RetryMetricsRecorderTest.TestConfig.class)
@DirtiesContext
@SuppressWarnings("PMD.TestClassWithoutTestCases")
class RetryMetricsRecorderTest
{
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private FlakyService flakyService;

    @Test
    void shouldIncrementOncePerFailedAttemptUntilRetryBudgetExhausts()
    {
        flakyService.reset();
        double before = currentCount();
        assertThatThrownBy(flakyService::alwaysFails)
                .isInstanceOf(IOException.class);

        assertThat(meterRegistry.find(LiveIngesterMetrics.Retry.ATTEMPTS)
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "IOException")
                .counter())
                        .as("RetryMetricsRecorder must be auto-registered by Spring Retry; missing counter means @EnableRetry didn't pick up the bean from the context")
                        .isNotNull();
        assertThat(currentCount() - before)
                .as("the counter must increment once per failed @Retryable attempt — 3 attempts (initial + 2 retries) all fail, 3 onError invocations recorded")
                .isEqualTo(3.0);
    }

    @Test
    void shouldRecordOnlyTheFailedAttemptsOnEventualSuccess() throws IOException
    {
        flakyService.reset();
        double before = currentCount();
        assertThat(flakyService.failsThenSucceeds(2)).isEqualTo("ok");

        // Spring's @Retryable counts the original invocation as attempt 1; failsThenSucceeds(2) means
        // attempts 1 and 2 fail, attempt 3 succeeds — 2 onError invocations recorded.
        assertThat(currentCount() - before)
                .as("a successful retry must record only the failed attempts that preceded it, not the final success")
                .isEqualTo(2.0);
    }

    @Test
    void shouldNotIncrementWhenFirstAttemptSucceeds() throws IOException
    {
        flakyService.reset();
        double before = currentCount();
        assertThat(flakyService.failsThenSucceeds(0)).isEqualTo("ok");

        assertThat(currentCount() - before)
                .as("a first-attempt success must record zero — no retry happened")
                .isEqualTo(0.0);
    }

    @Test
    void shouldUnwrapCamelExecutionExceptionToTagByCause()
    {
        flakyService.reset();
        double beforeIo = currentCount();
        double beforeUnknownWrapper = countTagged("WrappedRuntimeException");

        assertThatThrownBy(flakyService::alwaysFailsWithCamelWrappedIO)
                .isInstanceOf(org.apache.camel.CamelExecutionException.class);

        // alwaysFailsWithCamelWrappedIO has @Retryable(retryFor=IOException.class) and throws
        // CamelExecutionException wrapping IOException. Without unwrap, retry would not fire
        // (wrapper class doesn't match retryFor) AND the counter would be tagged
        // CamelExecutionException; with unwrap, the listener tags by IOException.
        // Spring Retry's @Retryable retryFor still sees the wrapper, so this @Retryable does not
        // retry (1 attempt, 1 onError). The contract verified here is the *tag*, not the retry count.
        assertThat(currentCount() - beforeIo)
                .as("a CamelExecutionException(IOException) must be tagged by its cause class (IOException), not the wrapper")
                .isEqualTo(1.0);
        assertThat(countTagged("CamelExecutionException") - beforeUnknownWrapper)
                .as("no counter should be created with the framework-wrapper class as the tag")
                .isEqualTo(0.0);
    }

    private double currentCount()
    {
        return countTagged("IOException");
    }

    private double countTagged(String exceptionTag)
    {
        Counter counter = meterRegistry.find(LiveIngesterMetrics.Retry.ATTEMPTS)
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, exceptionTag)
                .counter();
        return counter == null ? 0.0 : counter.count();
    }

    @Configuration
    @EnableRetry
    static class TestConfig
    {
        @Bean
        MeterRegistry meterRegistry()
        {
            return new SimpleMeterRegistry();
        }

        @Bean
        RetryMetricsRecorder retryMetricsRecorder(MeterRegistry meterRegistry)
        {
            return new RetryMetricsRecorder(meterRegistry);
        }

        @Bean
        FlakyService flakyService()
        {
            return new FlakyService();
        }
    }

    @Component
    static class FlakyService
    {
        private int attempts;

        void reset()
        {
            attempts = 0;
        }

        @Retryable(retryFor = IOException.class, maxAttempts = 3)
        String alwaysFails() throws IOException
        {
            attempts++;
            throw new IOException("attempt " + attempts);
        }

        @Retryable(retryFor = IOException.class, maxAttempts = 5)
        String failsThenSucceeds(int failuresBefore) throws IOException
        {
            attempts++;
            if (attempts <= failuresBefore)
            {
                throw new IOException("attempt " + attempts);
            }
            return "ok";
        }

        @Retryable(retryFor = IOException.class, maxAttempts = 1)
        String alwaysFailsWithCamelWrappedIO()
        {
            attempts++;
            throw new org.apache.camel.CamelExecutionException("camel wrapper",
                    null, new IOException("underlying " + attempts));
        }
    }
}
