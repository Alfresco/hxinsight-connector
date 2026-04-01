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
package org.alfresco.hxi_connector.common.test.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class RetryUtils
{
    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);
    private static final int MAX_ATTEMPTS = 15;
    private static final int INITIAL_DELAY_MS = 100;

    public static void retryWithBackoff(ErrorCatchingRunnable runnable)
    {
        retryWithBackoff(() -> {
            runnable.run();
            return null;
        });
    }

    public static void retryWithBackoff(ErrorCatchingRunnable runnable, int delayMs)
    {
        retryWithBackoff(runnable, MAX_ATTEMPTS, delayMs);
    }

    public static void retryWithBackoff(ErrorCatchingRunnable runnable, int maxAttempts, int delayMs)
    {
        retryWithBackoff(() -> {
            runnable.run();
            return null;
        }, maxAttempts, delayMs);
    }

    public static <T> T retryWithBackoff(Supplier<T> supplier)
    {
        return retryWithBackoff(supplier, MAX_ATTEMPTS, INITIAL_DELAY_MS);
    }

    public static <T> T retryWithBackoff(Supplier<T> supplier, int maxAttempts, int delayMs)
    {
        int attempt = 0;
        while (true)
        {
            try
            {
                return supplier.get();
            }
            catch (AssertionError e)
            {
                attempt++;
                if (attempt >= maxAttempts)
                {
                    log.atDebug().log("Attempt {} failed", attempt);
                    throw e;
                }
                log.atDebug().log("Attempt {} failed, retrying after {}ms", attempt, delayMs);
                try
                {
                    TimeUnit.MILLISECONDS.sleep(delayMs);
                }
                catch (InterruptedException interruptedException)
                {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Retry interrupted", interruptedException);
                }
            }
        }
    }

    public interface ErrorCatchingRunnable extends Runnable
    {
        void runUnsafe() throws Exception;

        @Override
        default void run()
        {
            try
            {
                runUnsafe();
            }
            catch (Exception exception)
            {
                throw new RuntimeException(exception);
            }
        }
    }
}
