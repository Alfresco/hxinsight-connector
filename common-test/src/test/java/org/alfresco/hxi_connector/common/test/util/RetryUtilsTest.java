/*-
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
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryUtilsTest
{
    @Mock
    private Supplier<String> mockSupplier;

    @Test
    void retryWithBackoff_supplier_shouldReturnOnFirstTry()
    {
        when(mockSupplier.get()).thenReturn("success");

        String result = RetryUtils.retryWithBackoff(mockSupplier, 3, 10);

        assertEquals("success", result);
        verify(mockSupplier, times(1)).get();
    }

    @Test
    void retryWithBackoff_supplier_shouldRetryUntilSuccess()
    {
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<String> failingSupplier = () -> {
            if (counter.incrementAndGet() < 3)
            {
                throw new AssertionError("Not ready yet");
            }
            return "success after retries";
        };

        String result = RetryUtils.retryWithBackoff(failingSupplier, 5, 10);

        assertEquals("success after retries", result);
        assertEquals(3, counter.get());
    }

    @Test
    void retryWithBackoff_supplier_shouldThrowExceptionAfterMaxAttempts()
    {
        when(mockSupplier.get()).thenThrow(new AssertionError("Always fails"));

        AssertionError exception = assertThrows(AssertionError.class,
                () -> RetryUtils.retryWithBackoff(mockSupplier, 3, 10));

        assertEquals("Always fails", exception.getMessage());
        verify(mockSupplier, times(3)).get();
    }

    @Test
    void retryWithBackoff_runnable_shouldCompleteOnFirstTry()
    {
        AtomicInteger counter = new AtomicInteger(0);
        RetryUtils.ErrorCatchingRunnable runnable = () -> {
            counter.incrementAndGet();
        };

        RetryUtils.retryWithBackoff(runnable, 3, 10);

        assertEquals(1, counter.get());
    }

    @Test
    void retryWithBackoff_runnable_shouldRetryUntilSuccess()
    {
        AtomicInteger counter = new AtomicInteger(0);
        RetryUtils.ErrorCatchingRunnable failingRunnable = () -> {
            if (counter.incrementAndGet() < 3)
            {
                throw new AssertionError("Not ready yet");
            }
        };

        RetryUtils.retryWithBackoff(failingRunnable, 5, 10);

        assertEquals(3, counter.get());
    }

    @Test
    void retryWithBackoff_runnable_shouldThrowExceptionAfterMaxAttempts()
    {
        RetryUtils.ErrorCatchingRunnable alwaysFailingRunnable = () -> {
            throw new AssertionError("Always fails");
        };

        AssertionError exception = assertThrows(AssertionError.class,
                () -> RetryUtils.retryWithBackoff(alwaysFailingRunnable, 3, 10));

        assertEquals("Always fails", exception.getMessage());
    }

    @Test
    void retryWithBackoff_defaultParameters_supplierShouldUseDefaultValues()
    {
        when(mockSupplier.get()).thenReturn("success");

        String result = RetryUtils.retryWithBackoff(mockSupplier);

        assertEquals("success", result);
        verify(mockSupplier, times(1)).get();
    }

    @Test
    void retryWithBackoff_defaultParameters_runnableShouldUseDefaultValues()
    {
        AtomicInteger counter = new AtomicInteger(0);
        RetryUtils.ErrorCatchingRunnable runnable = () -> {
            counter.incrementAndGet();
        };

        RetryUtils.retryWithBackoff(runnable);

        assertEquals(1, counter.get());
    }

    @Test
    void retryWithBackoff_customDelayOnly_shouldUseDefaultMaxAttempts()
    {
        RetryUtils.ErrorCatchingRunnable alwaysFailingRunnable = () -> {
            throw new AssertionError("Always fails");
        };
        int customDelay = 20;

        assertThrows(AssertionError.class,
                () -> RetryUtils.retryWithBackoff(alwaysFailingRunnable, customDelay));
    }
}
