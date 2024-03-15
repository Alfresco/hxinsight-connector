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
package org.alfresco.hxi_connector.common.repository.filter;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AspectFilterTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    private static final String CM_ASPECT_2 = "cm:aspect2";
    private static final String CM_ASPECT_3 = "cm:aspect3";
    private static final String CM_ASPECT_4 = "cm:aspect4";
    private static final String ALLOW_NODE = "Allow node when: ";
    private static final String DENY_NODE = "Deny node when: ";
    private static final String DENIED = "Denied: ";
    private static final String ALLOWED = "Allowed: ";
    private static final String ASPECTS = "Aspects: ";

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testAspectFiltering(boolean expected, Set<String> aspects, List<String> allowed, List<String> denied)
    {
        boolean result = AspectFilter.filter(aspects, allowed, denied);
        if (expected)
        {
            assertTrue(result);
        }
        else
        {
            assertFalse(result);
        }
    }

    private static Stream<Arguments> provideParameters()
    {
        return Stream.of(
                composeArguments(Set.of(CM_ASPECT_1), emptyList(), emptyList(), true),
                composeArguments(emptySet(), List.of(CM_ASPECT_1), emptyList(), false),
                composeArguments(emptySet(), emptyList(), emptyList(), true),
                composeArguments(Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_1), emptyList(), true),
                composeArguments(Set.of(CM_ASPECT_1), emptyList(), List.of(CM_ASPECT_2), true),
                composeArguments(Set.of(CM_ASPECT_1), List.of(CM_ASPECT_1), List.of(CM_ASPECT_2), true),
                composeArguments(Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_1), List.of(CM_ASPECT_2), false),
                composeArguments(Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_3), emptyList(), false),
                composeArguments(Set.of(CM_ASPECT_2, CM_ASPECT_3), emptyList(), List.of(CM_ASPECT_3), false),
                composeArguments(Set.of(CM_ASPECT_1, CM_ASPECT_4), List.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_3), true));
    }

    private static Arguments composeArguments(Set<String> aspects, List<String> allowed, List<String> denied, boolean result)
    {

        return Arguments.of(named(result ? ALLOW_NODE : DENY_NODE, result), named(ASPECTS + aspects, aspects),
                named(ALLOWED + allowed, allowed), named(DENIED + denied, denied));
    }
}
