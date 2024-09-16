/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.stream.Stream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class MaskingPatternLayoutTest
{

    @Mock
    ILoggingEvent loggingEventMock;

    MaskingPatternLayout maskingPatternLayout = createMaskingPatternLayout("Authorization", "password", "secret");

    @BeforeEach
    void setUp()
    {
        given(loggingEventMock.getTimeStamp()).willReturn(1629857730123L);
        given(loggingEventMock.getThreadName()).willReturn("thread1");
        given(loggingEventMock.getLevel()).willReturn(Level.DEBUG);
        given(loggingEventMock.getLoggerName()).willReturn(MaskingPatternLayoutTest.class.getName());
    }

    private static Stream<Arguments> loggingInputAndExpectedEntries()
    {
        return Stream.of(
                Arguments.of(
                        "Headers: {Authorization=Basic random-token, Content-Type=application/json}",
                        "Headers: {Authorization=*****, Content-Type=application/json}"),
                Arguments.of(
                        "Authorization = Basic random-token, Content-Type= application/json",
                        "Authorization = *****, Content-Type= application/json"),
                Arguments.of(
                        "\"Authorization\": \"Basic random-token\", \"Content-Type\": \"application/json\"",
                        "\"Authorization\": \"*****\", \"Content-Type\": \"application/json\""),
                Arguments.of(
                        "Body: { \"login\": \"nick\", \"password\": \"SecretPass\" }",
                        "Body: { \"login\": \"nick\", \"password\": \"*****\" }"),
                Arguments.of(
                        "{ \"login\": \"nick\", \"password\": \"secret\", \"secret\": \"password\", \"name\": \"John\" }",
                        "{ \"login\": \"nick\", \"password\": \"*****\", \"secret\": \"*****\", \"name\": \"John\" }"),
                Arguments.of(
                        "{ \"login\": \"nick\", \"name\": \"John\" }",
                        "{ \"login\": \"nick\", \"name\": \"John\" }"));
    }

    @ParameterizedTest
    @MethodSource("loggingInputAndExpectedEntries")
    void testLogMasking(String logInput, String expectedLogEntry)
    {
        // given
        given(loggingEventMock.getFormattedMessage()).willReturn(logInput);

        // when
        String actualLogMessage = maskingPatternLayout.doLayout(loggingEventMock);

        // then
        String expectedLogMessage = "2021-08-25 04:15:30.123 [thread1] DEBUG o.a.h.l.l.MaskingPatternLayoutTest - ".concat(expectedLogEntry);
        assertThat(actualLogMessage.strip()).isEqualTo(expectedLogMessage.strip());
    }

    private static MaskingPatternLayout createMaskingPatternLayout(String... sensitiveFields)
    {
        MaskingPatternLayout maskingPatternLayout = new MaskingPatternLayout();
        maskingPatternLayout.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        maskingPatternLayout.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        Stream.of(sensitiveFields).forEach(maskingPatternLayout::addMaskField);
        maskingPatternLayout.start();
        return maskingPatternLayout;
    }
}
