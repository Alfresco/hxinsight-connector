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
package org.alfresco.hxi_connector.prediction_applier.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = PRIVATE)
public class LoggingUtils
{

    public static ListAppender<ILoggingEvent> createLogsListAppender(Class<?> classToTrack, Class<?>... classesToTrack)
    {
        return createLogsAppender(ListAppender<ILoggingEvent>::new, Stream.concat(Stream.of(classToTrack), Arrays.stream(classesToTrack)).toArray(Class[]::new));
    }

    private static <T extends Appender<ILoggingEvent>> T createLogsAppender(Supplier<T> appenderSupplier, Class<?>... classesToTrack)
    {
        T appender = appenderSupplier.get();

        Arrays.stream(classesToTrack).forEach(classToTrack -> addAppenderForLogger(appender, classToTrack));
        appender.start();

        return appender;
    }

    private static void addAppenderForLogger(Appender<ILoggingEvent> appender, Class<?> classToTrack)
    {
        Logger logger = (Logger) LoggerFactory.getLogger(classToTrack);

        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
    }
}
