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

package org.alfresco.hxi_connector.common.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNonNull;
import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNotBlank;
import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensurePositive;
import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;
import org.alfresco.hxi_connector.common.exception.ValidationException;

@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
class EnsureUtilsTest
{

    @Test
    void shouldThrowIfBlank()
    {
        assertThrows(ValidationException.class, () -> ensureNotBlank(null, "String should not be null"));
        assertThrows(ValidationException.class, () -> ensureNotBlank("", "String should not be blank"));
    }

    @Test
    void shouldPassIfNotBlank()
    {
        ensureNotBlank("something", "String should not be blank");
    }

    @Test
    void shouldThrowIfNull()
    {
        assertThrows(ValidationException.class, () -> ensureNonNull(null, "Object should not be null"));
    }

    @Test
    void shouldThrowIfConditionIsNotMet()
    {
        // given
        int first = 1;
        int second = 2;

        String expectedErrorMessage = "first: 1 is not equal to second: 2";

        // when, then

        assertThrows(
                ValidationException.class,
                () -> ensureThat(first == second, "first: %s is not equal to second: %s", first, second), expectedErrorMessage);
    }

    @Test
    void shouldPassIfConditionIsMet()
    {
        // given
        int first = 1;
        int second = 1;

        ensureThat(first == second, "first: %s is not equal to second: %s", first, second);
    }

    @Test
    void shouldThrowCustomExceptionIfConditionIsNotMet()
    {
        assertThrows(CustomTestException.class, () -> ensureThat(1 == 2, () -> new CustomTestException("some message")));
    }

    @Test
    void shouldDoNothingIfConditionIsMet()
    {
        ensureThat(1 == 1, () -> new CustomTestException("some message"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -1, 0})
    void shouldThrowIfNumberIsNotPositive(int number)
    {
        assertThrows(ValidationException.class, () -> ensurePositive(number, "Number should be positive"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100})
    void shouldDoNothingIfNumberIsPositive(int number)
    {
        ensurePositive(number, "Number should be positive");
    }

    static class CustomTestException extends HxInsightConnectorRuntimeException
    {

        public CustomTestException(String message)
        {
            super(message);
        }
    }
}
