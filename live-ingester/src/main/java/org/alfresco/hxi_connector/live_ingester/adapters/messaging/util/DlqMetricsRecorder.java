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

import java.util.Optional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

/**
 * Single Micrometer-backed recorder used by every dead-letter channel wired through {@link DeadLetterChannels#forRoute}. Increments a per-route counter (named by the supplied {@link DlqMetric}) tagged with the short class name of the exception that caused the failure, so operators can break out parse failures from downstream failures on the same alert.
 *
 * <p>
 * The counter is incremented from {@link org.apache.camel.builder.DeadLetterChannelBuilder#onPrepareFailure(org.apache.camel.Processor)}, which Camel invokes immediately before the message is routed to the configured DLQ endpoint. If the DLQ send itself fails (e.g. broker unreachable) the counter has already been bumped — the small over-count is preferred over missing the visibility entirely.
 *
 * <p>
 * No log line is emitted from this recorder: each route's {@code onPrepareFailure} hook calls {@link LoggingUtils#logMaskedExchangeState} with the route's own logger at {@code ERROR} immediately before invoking this recorder, and Camel emits a {@code WARN} of its own via {@code logExhausted(true)}. A third per-recorder log line would just duplicate one of those two.
 */
@Component
public class DlqMetricsRecorder
{
    private static final String UNKNOWN_EXCEPTION_VALUE = "unknown";

    private final MeterRegistry meterRegistry;

    public DlqMetricsRecorder(MeterRegistry meterRegistry)
    {
        this.meterRegistry = meterRegistry;
    }

    public void record(Exchange exchange, DlqMetric metric)
    {
        String exceptionClass = Optional.ofNullable(exchange)
                .map(e -> e.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class))
                .map(t -> t.getClass().getSimpleName())
                .orElse(UNKNOWN_EXCEPTION_VALUE);

        Counter.builder(metric.counterName())
                .description(metric.counterDescription())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, exceptionClass)
                .register(meterRegistry)
                .increment();
    }
}
