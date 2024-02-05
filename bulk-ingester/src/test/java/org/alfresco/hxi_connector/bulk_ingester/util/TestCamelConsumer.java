/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

package org.alfresco.hxi_connector.bulk_ingester.util;

import static java.lang.String.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.bulk_ingester.event.NodePublisherConfig;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.UnusedPrivateMethod"})
public class TestCamelConsumer extends RouteBuilder
{
    private static final int DELAY = 100;

    private final NodePublisherConfig config;

    private final List<String> messages;

    @Override
    public void configure()
    {
        from(config.endpoint())
                .process(this::registerMessage);
    }

    private void registerMessage(Exchange exchange)
    {
        messages.add(exchange.getIn().getBody(String.class));
    }

    public void cleanUp()
    {
        messages.clear();
    }

    @SneakyThrows
    public void assertNMessagesReceived(int messagesCount)
    {
        if (messagesCount != messages.size())
        {
            Thread.sleep(DELAY);
        }

        assertEquals(messages.size(), messagesCount);
    }

    @SneakyThrows
    public void asertMessageReceived(String message)
    {
        if (wasMessageCaptured(message))
        {
            Thread.sleep(DELAY);
        }

        assertTrue(wasMessageCaptured(message),
                format("""
                        Cannot find message %s
                        All messages: %s
                        """, message, messages));
    }

    private boolean wasMessageCaptured(String expectedMessage)
    {
        return messages.stream()
                .anyMatch(message -> areJsonsEqual(expectedMessage, message));
    }

    @SneakyThrows
    private boolean areJsonsEqual(String expected, String actual)
    {
        return JsonEqualityUtils.jsonEquals(expected, actual);
    }
}
