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

package org.alfresco.hxi_connector.live_ingester.domain.utils;

import static org.alfresco.hxi_connector.live_ingester.util.TestUtils.assertContainsSameElements;

import java.util.List;

import org.junit.jupiter.api.Test;

class CollectionUtilsTest
{

    @Test
    void shouldReturnEmptyListIfCollectionsContainsSameElements()
    {
        // given
        List<String> first = List.of("a", "b", "c");
        List<String> second = List.of("a", "b", "c");

        // when
        List<String> actual = CollectionUtils.difference(first, second);

        // then
        List<String> expected = List.of();

        assertContainsSameElements(expected, actual);
    }

    @Test
    void shouldSubtractSecondFromFirst()
    {
        // given
        List<String> first = List.of("a", "b", "c");
        List<String> second = List.of("a", "b", "x", "y");

        // when
        List<String> actual = CollectionUtils.difference(first, second);

        // then
        List<String> expected = List.of("c");

        assertContainsSameElements(expected, actual);
    }

    @Test
    void shouldReturnFirstCollectionIfSecondIsEmpty()
    {
        // given
        List<String> first = List.of("a", "b", "c");
        List<String> second = List.of();

        // when
        List<String> actual = CollectionUtils.difference(first, second);

        // then
        List<String> expected = List.of("a", "b", "c");

        assertContainsSameElements(expected, actual);
    }
}
