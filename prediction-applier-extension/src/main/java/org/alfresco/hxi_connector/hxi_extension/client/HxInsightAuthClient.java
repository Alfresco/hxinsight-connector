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
package org.alfresco.hxi_connector.hxi_extension.client;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.config.properties.Retry;

@Slf4j
public class HxInsightAuthClient extends DefaultAuthenticationClient
{

    public HxInsightAuthClient(AuthProperties authProperties)
    {
        super(authProperties);
    }

    @Override
    public AuthenticationResult authenticate(String providerId)
    {
        return retryWithBackoff(() -> super.authenticate(providerId), authProperties.getRetry());
    }

    private static <T> T retryWithBackoff(Supplier<T> supplier, Retry retryProperties)
    {
        int attempt = 0;
        int maxAttempts = retryProperties.attempts();
        double delay = retryProperties.initialDelay();
        while (true)
        {
            try
            {
                return supplier.get();
            }
            catch (Exception e)
            {
                if (retryProperties.reasons() != null && retryProperties.reasons().contains(e.getClass()))
                {
                    attempt++;
                    if (attempt >= maxAttempts)
                    {
                        log.atInfo().log("Attempt {} of {} failed", attempt, maxAttempts);
                        throw e;
                    }
                    log.atInfo().log("Attempt {} of {} failed, retrying after {}ms", attempt, maxAttempts, delay);
                    try
                    {
                        TimeUnit.MILLISECONDS.sleep(Math.round(delay));
                    }
                    catch (InterruptedException ex)
                    {
                        log.atWarn().log("Cannot pause retryable operation due to InterruptedException: %s".formatted(ex.getMessage()), e);
                    }
                    delay *= retryProperties.delayMultiplier();
                }
                else
                {
                    throw e;
                }
            }
        }
    }
}
