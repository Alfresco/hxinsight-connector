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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CollectionFilterTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    private static final String CM_ASPECT_2 = "cm:aspect2";
    private static final String CM_ASPECT_3 = "cm:aspect3";
    private static final String CM_ASPECT_4 = "cm:aspect4";
    private static final String ROOT_NODE_ID = "root-node-id";
    private static final String PARENT_NODE_ID = "parent-node-id";
    private static final String GRANDPARENT_NODE_ID = "grandparent-node-id";
    private static final String CHILD_NODE_ID = "child-node-id";
    private static final String ALLOW_NODE = "Allow node when: ";
    private static final String DENY_NODE = "Deny node when: ";
    private static final String DENIED = "Denied: ";
    private static final String ALLOWED = "Allowed: ";
    private static final String VALUES = "Values: ";

    @ParameterizedTest
    @MethodSource("provideAspectParameters")
    void testAspectFiltering(boolean expected, Set<String> aspects, List<String> allowed, List<String> denied)
    {
        boolean result = CollectionFilter.filter(aspects, allowed, denied);
        if (expected)
        {
            assertTrue(result);
        }
        else
        {
            assertFalse(result);
        }
    }

    @ParameterizedTest
    @MethodSource("provideAncestorParameters")
    void testAncestorFiltering(boolean expected, List<String> aspects, List<String> allowed, List<String> denied)
    {
        boolean result = CollectionFilter.filter(aspects, allowed, denied);
        if (expected)
        {
            assertTrue(result);
        }
        else
        {
            assertFalse(result);
        }
    }

    private static Stream<Arguments> provideAspectParameters()
    {
        return Stream.of(
                composeArguments(true, Set.of(CM_ASPECT_1), emptyList(), emptyList()),
                composeArguments(false, emptySet(), List.of(CM_ASPECT_1), emptyList()),
                composeArguments(true, emptySet(), emptyList(), emptyList()),
                composeArguments(true, Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_1), emptyList()),
                composeArguments(true, Set.of(CM_ASPECT_1), emptyList(), List.of(CM_ASPECT_2)),
                composeArguments(true, Set.of(CM_ASPECT_1), List.of(CM_ASPECT_1), List.of(CM_ASPECT_2)),
                composeArguments(false, Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_1), List.of(CM_ASPECT_2)),
                composeArguments(false, Set.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_3), emptyList()),
                composeArguments(false, Set.of(CM_ASPECT_2, CM_ASPECT_3), emptyList(), List.of(CM_ASPECT_3)),
                composeArguments(true, Set.of(CM_ASPECT_1, CM_ASPECT_4), List.of(CM_ASPECT_1, CM_ASPECT_2), List.of(CM_ASPECT_3)));
    }

    private static Stream<Arguments> provideAncestorParameters()
    {
        return Stream.of(
                composeArguments(true, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID), emptyList(), emptyList()),
                composeArguments(true, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID), List.of(GRANDPARENT_NODE_ID), emptyList()),
                composeArguments(true, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID), emptyList(), List.of(PARENT_NODE_ID)),
                composeArguments(true, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID), List.of(GRANDPARENT_NODE_ID), List.of(PARENT_NODE_ID)),
                composeArguments(false, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID, PARENT_NODE_ID), List.of(PARENT_NODE_ID), List.of(GRANDPARENT_NODE_ID)),
                composeArguments(false, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID), List.of(PARENT_NODE_ID), emptyList()),
                composeArguments(false, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID, PARENT_NODE_ID), emptyList(), List.of(GRANDPARENT_NODE_ID)),
                composeArguments(true, List.of(ROOT_NODE_ID, GRANDPARENT_NODE_ID, PARENT_NODE_ID), List.of(GRANDPARENT_NODE_ID, PARENT_NODE_ID), List.of(
                        CHILD_NODE_ID)));
    }

    private static Arguments composeArguments(boolean allowNode, Collection<String> values, Collection<String> allowed, Collection<String> denied)
    {

        return Arguments.of(named(allowNode ? ALLOW_NODE : DENY_NODE, allowNode), named(VALUES + values, values),
                named(ALLOWED + allowed, allowed), named(DENIED + denied, denied));
    }
}
