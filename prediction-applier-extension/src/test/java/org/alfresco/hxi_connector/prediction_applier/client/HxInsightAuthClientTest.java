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
package org.alfresco.hxi_connector.prediction_applier.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class HxInsightAuthClientTest
{
    private static final String PROVIDER_ID = "some-provider";

    @Mock
    AuthProperties authPropertiesMock;
    @InjectMocks
    HxInsightAuthClient hxInsightAuthClient;

    ListAppender testAppender;
    LoggerConfig rootLoggerConfig;

    @BeforeAll
    void beforeAll()
    {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration loggerConfig = loggerContext.getConfiguration();
        testAppender = ListAppender.newBuilder().setName("test-appender").build();
        rootLoggerConfig = loggerConfig.getLoggerConfig(StringUtils.EMPTY);
        rootLoggerConfig.setLevel(Level.INFO);
        rootLoggerConfig.addAppender(testAppender, Level.ALL, null);
    }

    @AfterAll
    void afterAll()
    {
        rootLoggerConfig.removeAppender(testAppender.getName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenAuthException_whenAuthenticate_thenFailAfterRetry()
    {
        // given
        when(authPropertiesMock.getRetry()).thenReturn(mock(Retry.class));
        when(authPropertiesMock.getRetry().attempts()).thenReturn(3);
        when(authPropertiesMock.getRetry().initialDelay()).thenReturn(500);
        when(authPropertiesMock.getRetry().delayMultiplier()).thenReturn(2.0);
        when(authPropertiesMock.getRetry().reasons()).thenReturn(Set.of(EndpointServerErrorException.class));
        when(authPropertiesMock.getProviders()).thenReturn(mock(Map.class));
        when(authPropertiesMock.getProviders().get(PROVIDER_ID)).thenThrow(new EndpointServerErrorException("some error"));

        // when
        Throwable actualException = catchThrowable(() -> hxInsightAuthClient.authenticate(PROVIDER_ID));

        // then
        assertThat(actualException).isInstanceOf(EndpointServerErrorException.class);
        assertThat(testAppender.getEvents())
            .hasSize(3)
            .extracting(events -> events.getMessage().getFormattedMessage())
            .containsExactlyInAnyOrder(
                "Attempt 1 of 3 failed, retrying after 500ms",
                "Attempt 2 of 3 failed, retrying after 1000ms",
                "Attempt 3 of 3 failed"
            );
    }
}
