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
package org.alfresco.hxi_connector.common.config.properties;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@Data
@Getter(AccessLevel.NONE)
public class Retry
{
    private static final int RETRY_ATTEMPTS_DEFAULT = 10;
    private static final int RETRY_INITIAL_DELAY_DEFAULT = 500;
    private static final double RETRY_DELAY_MULTIPLIER_DEFAULT = 2;
    private static final Set<Class<? extends Throwable>> RETRY_REASONS_BASIC = Set.of(
            EndpointServerErrorException.class,
            UnknownHostException.class,
            MalformedURLException.class,
            JsonEOFException.class,
            MismatchedInputException.class);

    @Min(-1) private int attempts;
    @PositiveOrZero private int initialDelay;
    @Positive private double delayMultiplier;
    @NotNull private Set<Class<? extends Throwable>> reasons;

    public Retry()
    {
        this(RETRY_ATTEMPTS_DEFAULT, RETRY_INITIAL_DELAY_DEFAULT, RETRY_DELAY_MULTIPLIER_DEFAULT);
    }

    public Retry(int attempts, int initialDelay, double delayMultiplier)
    {
        this(attempts, initialDelay, delayMultiplier,
                Stream.concat(RETRY_REASONS_BASIC.stream(), Stream.of(
                        HttpHostConnectException.class,
                        NoHttpResponseException.class,
                        MalformedChunkCodingException.class)).collect(Collectors.toSet()));
    }

    public Retry(int attempts, int initialDelay, double delayMultiplier, Set<Class<? extends Throwable>> reasons)
    {
        this.attempts = attempts;
        this.initialDelay = initialDelay;
        this.delayMultiplier = delayMultiplier;
        this.reasons = reasons;
    }

    public int attempts()
    {
        return attempts;
    }

    public int initialDelay()
    {
        return initialDelay;
    }

    public double delayMultiplier()
    {
        return delayMultiplier;
    }

    public Set<Class<? extends Throwable>> reasons()
    {
        return reasons;
    }
}
