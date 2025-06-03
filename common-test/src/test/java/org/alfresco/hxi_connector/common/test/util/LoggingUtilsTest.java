/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.common.test.util;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class LoggingUtilsTest
{
    private Logger testLogger1;
    private Logger testLogger2;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp()
    {
        testLogger1 = (Logger) LoggerFactory.getLogger(LoggerOneSource.class);
        testLogger2 = (Logger) LoggerFactory.getLogger(LoggerTwoSource.class);

        testLogger1.detachAndStopAllAppenders();
        testLogger2.detachAndStopAllAppenders();
    }

    @AfterEach
    void tearDown()
    {
        if (listAppender != null)
        {
            testLogger1.detachAppender(listAppender);
            testLogger2.detachAppender(listAppender);
            listAppender.stop();
        }
    }

    @Test
    void createLogsListAppender_singleClass_shouldAttachAppenderToLogger()
    {
        listAppender = LoggingUtils.createLogsListAppender(LoggerOneSource.class);

        assertTrue(testLogger1.isAttached(listAppender));
        assertEquals(Level.DEBUG, testLogger1.getLevel());
    }

    @Test
    void createLogsListAppender_multipleClasses_shouldAttachAppenderToAllLoggers()
    {
        listAppender = LoggingUtils.createLogsListAppender(LoggerOneSource.class, LoggerTwoSource.class);

        assertTrue(testLogger1.isAttached(listAppender));
        assertTrue(testLogger2.isAttached(listAppender));
        assertEquals(Level.DEBUG, testLogger1.getLevel());
        assertEquals(Level.DEBUG, testLogger2.getLevel());
    }

    @Test
    void createLogsListAppender_capturesLogMessages()
    {
        listAppender = LoggingUtils.createLogsListAppender(LoggerOneSource.class);
        String testMessage = "Test log message";

        testLogger1.info(testMessage);

        assertEquals(1, listAppender.list.size());
        assertEquals(testMessage, listAppender.list.get(0).getMessage());
    }

    @Test
    void createLogsListAppender_capturesLogMessagesFromMultipleClasses()
    {
        listAppender = LoggingUtils.createLogsListAppender(LoggerOneSource.class, LoggerTwoSource.class);
        String message1 = "Message from class 1";
        String message2 = "Message from class 2";

        testLogger1.info(message1);
        testLogger2.info(message2);

        assertEquals(2, listAppender.list.size());
        assertTrue(listAppender.list.stream()
                .map(ILoggingEvent::getMessage)
                .anyMatch(message -> message.equals(message1)));
        assertTrue(listAppender.list.stream()
                .map(ILoggingEvent::getMessage)
                .anyMatch(message -> message.equals(message2)));
    }

    private static class LoggerOneSource
    {}

    private static class LoggerTwoSource
    {}
}
