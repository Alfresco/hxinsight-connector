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
import static org.mockito.BDDMockito.given;

import java.io.IOException;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DlqMetricsRecorderTest
{
    private static final DlqMetric METRIC = new DlqMetric(
            "test_dlq_total",
            "Test counter for DlqMetricsRecorder unit tests");

    @Mock
    private Exchange exchange;

    private SimpleMeterRegistry meterRegistry;
    private DlqMetricsRecorder recorder;

    @BeforeEach
    void setUp()
    {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new DlqMetricsRecorder(meterRegistry);
    }

    @Test
    void shouldIncrementCounterTaggedWithExceptionShortClassName()
    {
        given(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class))
                .willReturn(new IOException("simulated"));

        recorder.record(exchange, METRIC);

        Counter counter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "IOException")
                .counter();
        assertThat(counter)
                .as("counter must be registered with the exception's short class name as tag")
                .isNotNull();
        assertThat(counter.count())
                .as("counter must be incremented exactly once per record() call")
                .isEqualTo(1.0);
    }

    @Test
    void shouldTagUnknownWhenExchangeHasNoCaughtException()
    {
        given(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class))
                .willReturn(null);

        recorder.record(exchange, METRIC);

        Counter counter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "unknown")
                .counter();
        assertThat(counter)
                .as("missing EXCEPTION_CAUGHT must tag the counter as 'unknown' rather than fail")
                .isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldTagUnknownWhenExchangeIsNull()
    {
        recorder.record(null, METRIC);

        Counter counter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "unknown")
                .counter();
        assertThat(counter)
                .as("a null exchange must be defensively tolerated and tagged as 'unknown'")
                .isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldReuseSameCounterAcrossRepeatedCallsForSameExceptionType()
    {
        given(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class))
                .willReturn(new IOException("first"), new IOException("second"), new IOException("third"));

        recorder.record(exchange, METRIC);
        recorder.record(exchange, METRIC);
        recorder.record(exchange, METRIC);

        Counter counter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "IOException")
                .counter();
        assertThat(counter.count())
                .as("repeated calls with the same exception class must increment a single counter, not create new ones (Micrometer Counter.builder().register() is idempotent for same name+tags)")
                .isEqualTo(3.0);
        assertThat(meterRegistry.find(METRIC.counterName()).counters())
                .as("only one counter instance should exist for a single (counter, exception-tag) pair")
                .hasSize(1);
    }

    @Test
    void shouldRegisterDistinctCountersPerExceptionType()
    {
        given(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class))
                .willReturn(new IOException("io"), new IllegalStateException("state"));

        recorder.record(exchange, METRIC);
        recorder.record(exchange, METRIC);

        Counter ioCounter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "IOException")
                .counter();
        Counter illegalStateCounter = meterRegistry.find(METRIC.counterName())
                .tag(LiveIngesterMetrics.Tag.EXCEPTION, "IllegalStateException")
                .counter();

        assertThat(ioCounter).isNotNull();
        assertThat(illegalStateCounter).isNotNull();
        assertThat(ioCounter.count())
                .as("each exception type must have its own counter so operators can break out failures by class on the same alert")
                .isEqualTo(1.0);
        assertThat(illegalStateCounter.count()).isEqualTo(1.0);
    }
}
