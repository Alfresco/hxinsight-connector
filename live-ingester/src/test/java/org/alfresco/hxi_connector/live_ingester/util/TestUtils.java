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

package org.alfresco.hxi_connector.live_ingester.util;

import static java.lang.String.format;
import static java.util.function.Predicate.not;

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Nullable;

import lombok.NoArgsConstructor;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.UseObjectForClearerAPI"})
@NoArgsConstructor(access = PRIVATE)
public final class TestUtils
{
    public static Map<String, Serializable> mapWith(@Nullable String key, @Nullable String value)
    {
        Map<String, Serializable> map = new HashMap<>();

        map.put(key, value);

        return map;
    }

    public static Map<String, Serializable> mapWith(@Nullable String k1, @Nullable String v1, @Nullable String k2, @Nullable String v2)
    {
        Map<String, Serializable> map = new HashMap<>();

        map.put(k1, v1);
        map.put(k2, v2);

        return map;
    }

    public static <T> void assertContainsSameElements(Collection<T> expected, Collection<T> actual)
    {

        boolean areContainingSameElements = areContainingSameElements(expected, actual);

        if (areContainingSameElements)
        {
            return;
        }

        String errorMessage = format("""

                expected: %s
                actual:   %s

                present in expected and not in actual: %s
                present in actual and not in expected: %s
                """,
                expected, actual, difference(expected, actual), difference(actual, expected));

        throw new AssertionFailedError(errorMessage);
    }

    public static <T> boolean areContainingSameElements(Collection<T> expected, Collection<T> actual)
    {
        return expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected);
    }

    private static <T> List<T> difference(Collection<T> first, Collection<T> second)
    {
        return first.stream()
                .filter(not(second::contains))
                .toList();
    }
}
