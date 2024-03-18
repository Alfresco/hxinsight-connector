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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Named.named;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TypeFilterTest
{

    private static final String ALLOW_NODE = "Allow node when: ";
    private static final String DENY_NODE = "Deny node when: ";
    private static final String DENIED = "Denied: ";
    private static final String ALLOWED = "Allowed: ";
    private static final String CM_FOLDER = "cm:folder";
    private static final String CM_CONTENT = "cm:content";
    private static final String CM_SPECIAL_FOLDER = "cm:special-folder";
    private static final String TYPE = "Node type: ";

    @ParameterizedTest
    @MethodSource("provideParameters")
    void testAspectFiltering(boolean expected, String nodeType, List<String> allowed, List<String> denied)
    {
        // when
        boolean result = TypeFilter.filter(nodeType, allowed, denied);
        // then
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
                composeArguments(true, CM_FOLDER, emptyList(), emptyList()),
                composeArguments(true, CM_CONTENT, List.of(CM_CONTENT, CM_SPECIAL_FOLDER), emptyList()),
                composeArguments(true, CM_FOLDER, emptyList(), List.of(CM_SPECIAL_FOLDER)),
                composeArguments(true, CM_CONTENT, List.of(CM_CONTENT), List.of(CM_SPECIAL_FOLDER)),
                composeArguments(false, CM_SPECIAL_FOLDER, List.of(CM_FOLDER), emptyList()),
                composeArguments(false, CM_FOLDER, emptyList(), List.of(CM_FOLDER, CM_SPECIAL_FOLDER)));
    }

    private static Arguments composeArguments(boolean allowNode, String nodeType, List<String> allowed, List<String> denied)
    {

        return Arguments.of(named(allowNode ? ALLOW_NODE : DENY_NODE, allowNode), named(TYPE + nodeType, nodeType),
                named(ALLOWED + allowed, allowed), named(DENIED + denied, denied));
    }

}
