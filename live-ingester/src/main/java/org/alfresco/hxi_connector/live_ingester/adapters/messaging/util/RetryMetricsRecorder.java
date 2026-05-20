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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Global Spring {@link RetryListener} bean: increments {@link LiveIngesterMetrics.Retry#ATTEMPTS} once per failed {@code @Retryable} attempt, tagged with the underlying exception class. Auto-wired by Spring Retry into every {@code @Retryable} proxy via {@code @EnableRetry}, so it covers every retry site in the module without per-site changes.
 *
 * <p>
 * The exception tag is the slicing mechanism: operators query {@code …/actuator/metrics/live_ingester_retry_attempts_total?tag=exception:EndpointServerErrorException} to break out e.g. transient 5xx from {@code ConnectException} from a stray {@code RuntimeException}.
 *
 * <p>
 * Reuse: not currently shared, but the class is module-agnostic — only the counter name is module-scoped. If bulk-ingester / nucleus-sync / prediction-applier (all of which use {@code @EnableRetry} + Camel-wrapped {@code @Retryable}) want the same observability, lift this into {@code common/} with the counter name as a constructor argument and register one bean per module.
 */
@Component
public class RetryMetricsRecorder implements RetryListener
{
    private static final String UNKNOWN_EXCEPTION_VALUE = "unknown";

    private final MeterRegistry meterRegistry;

    public RetryMetricsRecorder(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable)
    {
        String exceptionClass = resolveTag(throwable);
        Counter.builder(LiveIngesterMetrics.Retry.ATTEMPTS)
                .description(LiveIngesterMetrics.Retry.ATTEMPTS_DESCRIPTION)
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, exceptionClass)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Walks past Camel/concurrent framework wrappers (e.g. {@link org.apache.camel.CamelExecutionException} from {@code FluentProducerTemplate.request}) so the tag reflects the actual application exception rather than the wrapper.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private static String resolveTag(Throwable throwable)
    {
        if (throwable == null)
        {
            return UNKNOWN_EXCEPTION_VALUE;
        }
        Throwable current = throwable;
        // Reference identity is intentional below: a self-cycle (cause == self) is the canonical
        // sentinel for "no further unwrap target", and equals() on Throwable is identity-based anyway.
        while (isFrameworkWrapper(current) && current.getCause() != null && current.getCause() != current)
        {
            current = current.getCause();
        }
        return current.getClass().getSimpleName();
    }

    private static boolean isFrameworkWrapper(Throwable t)
    {
        String name = t.getClass().getName();
        return name.startsWith("org.apache.camel.") || name.equals("java.util.concurrent.ExecutionException");
    }
}
