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
package org.alfresco.hxi_connector.live_ingester.adapters.config.properties;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Set;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.Data;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;

import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;

@Data
public class Retryable
{
    @NotNull private Retry retry = new Retry();

    @Data
    public static class Retry
    {
        private static final int RETRY_ATTEMPTS_DEFAULT = 10;
        private static final int RETRY_INITIAL_DELAY_DEFAULT = 500;
        private static final double RETRY_DELAY_MULTIPLIER_DEFAULT = 2;
        private static final Set<Class<? extends Throwable>> RETRY_REASONS = Set.of(
                EndpointServerErrorException.class,
                UnknownHostException.class,
                MalformedURLException.class,
                JsonEOFException.class,
                MismatchedInputException.class,
                HttpHostConnectException.class,
                NoHttpResponseException.class,
                MalformedChunkCodingException.class);

        @PositiveOrZero
        private int attempts = RETRY_ATTEMPTS_DEFAULT;
        @PositiveOrZero
        private int initialDelay = RETRY_INITIAL_DELAY_DEFAULT;
        @Positive private double delayMultiplier = RETRY_DELAY_MULTIPLIER_DEFAULT;
        @NotNull private Set<Class<? extends Throwable>> reasons = RETRY_REASONS;
    }
}
