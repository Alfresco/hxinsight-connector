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

package org.alfresco.hxi_connector.bulk_ingester.util;

import static java.lang.String.format;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("PMD.LooseCoupling")
public class AssertionsUtils
{
    public static void assertMapsEquals(Map<String, Serializable> expected, Map<String, Serializable> actual)
    {
        String failureMessage = format("""
                Expected and actual are not equal

                expected:   %s
                actual:     %s
                """, expected, actual);

        assertEquals(expected.size(), actual.size(), failureMessage);
        expected.keySet()
                .forEach(key -> assertTrue(actual.containsKey(key), failureMessage));

        expected.keySet()
                .forEach(
                        key -> {
                            Serializable expectedValue = expected.get(key);
                            Serializable actualValue = actual.get(key);

                            if (expectedValue instanceof Collection<?>)
                            {
                                assertCollectionsEquals((Collection<?>) expectedValue, (Collection<?>) actualValue);
                            }
                            else
                            {
                                assertEquals(expectedValue, actualValue);
                            }
                        });
    }

    public static void assertCollectionsEquals(Collection<?> expected, Collection<?> actual)
    {
        String failureMessage = format("""
                Expected and actual are not equal

                expected:   %s
                actual:     %s
                """, expected, actual);

        assertEquals(expected.size(), actual.size(), failureMessage);

        ArrayList<?> l1 = new ArrayList<>(expected);
        ArrayList<?> l2 = new ArrayList<>(actual);

        l1.forEach(l2::remove);

        failureMessage = format("""
                %s
                difference:
                %s
                """, failureMessage, l2);

        assertTrue(l2.isEmpty(), failureMessage);
    }
}
