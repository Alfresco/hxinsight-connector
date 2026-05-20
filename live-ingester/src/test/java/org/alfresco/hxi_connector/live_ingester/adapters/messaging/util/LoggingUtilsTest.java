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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.util;

import static org.assertj.core.api.Assertions.assertThatNoException;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.AUTHORIZATION;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

class LoggingUtilsTest
{
    private static final Logger TEST_LOG = LoggerFactory.getLogger(LoggingUtilsTest.class);

    private DefaultCamelContext camelContext;

    @BeforeEach
    void setUp()
    {
        camelContext = new DefaultCamelContext();
    }

    @AfterEach
    void tearDown()
    {
        camelContext.stop();
    }

    /**
     * A null header value must not crash the masking helper. JMS messages can legitimately carry null-valued headers (e.g. an unset {@code JMSCorrelationID} on synthetic publishers), and a crash here propagates into any {@code DeadLetterChannel} {@code onPrepareFailure} callback that uses this helper, blocking DLQ delivery.
     */
    @Test
    void shouldNotThrowOnNullHeaderValue()
    {
        Exchange exchange = newExchangeWithHeaders(headers -> {
            headers.put("JMSCorrelationID", null);
            headers.put("JMSMessageID", "ID:msg-1");
        });

        assertThatNoException()
                .isThrownBy(() -> LoggingUtils.logMaskedExchangeState(exchange, TEST_LOG, Level.ERROR));
    }

    @Test
    void shouldNotThrowOnAllNullHeaderValues()
    {
        Exchange exchange = newExchangeWithHeaders(headers -> {
            headers.put("JMSCorrelationID", null);
            headers.put("JMSMessageID", null);
        });

        assertThatNoException()
                .isThrownBy(() -> LoggingUtils.logMaskedExchangeState(exchange, TEST_LOG, Level.ERROR));
    }

    @Test
    void shouldNotThrowWhenAuthorizationHeaderIsNull()
    {
        Exchange exchange = newExchangeWithHeaders(headers -> headers.put(AUTHORIZATION, null));

        assertThatNoException()
                .isThrownBy(() -> LoggingUtils.logMaskedExchangeState(exchange, TEST_LOG, Level.ERROR));
    }

    @Test
    void shouldNotThrowWhenBodyIsNull()
    {
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setBody(null);
        exchange.getMessage().setHeader("JMSMessageID", "ID:msg-2");

        assertThatNoException()
                .isThrownBy(() -> LoggingUtils.logMaskedExchangeState(exchange, TEST_LOG, Level.ERROR));
    }

    @Test
    void shouldHandleEmptyHeadersAndProperties()
    {
        Exchange exchange = new DefaultExchange(camelContext);

        assertThatNoException()
                .isThrownBy(() -> LoggingUtils.logMaskedExchangeState(exchange, TEST_LOG, Level.ERROR));
    }

    private Exchange newExchangeWithHeaders(Consumer<Map<String, Object>> headerCustomizer)
    {
        Exchange exchange = new DefaultExchange(camelContext);
        Message message = exchange.getMessage();
        Map<String, Object> headers = new HashMap<>();
        headerCustomizer.accept(headers);
        // Use setHeaders to preserve null values; setHeader(key, null) may remove the entry depending on
        // the underlying Message implementation, defeating the test.
        message.setHeaders(headers);
        message.setBody("test-body");
        return exchange;
    }
}
