/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

import static java.util.function.Predicate.not;

import static lombok.AccessLevel.PRIVATE;

import java.util.Collection;
import java.util.List;

import lombok.NoArgsConstructor;

@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.SystemPrintln"})
@NoArgsConstructor(access = PRIVATE)
public final class TestUtils
{
    public static <T> void assertContainsSameElements(Collection<T> expected, Collection<T> actual)
    {

        boolean areContainingSameElements = expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected);

        if (areContainingSameElements)
        {
            return;
        }

        System.err.println("expected: " + expected);
        System.err.println("actual: " + actual);

        System.err.println("present in expected and not in actual: " + difference(expected, actual));
        System.err.println("present in actual and not in expected: " + difference(actual, expected));
    }

    private static <T> List<T> difference(Collection<T> first, Collection<T> second)
    {
        return first.stream()
                .filter(not(second::contains))
                .toList();
    }
}
