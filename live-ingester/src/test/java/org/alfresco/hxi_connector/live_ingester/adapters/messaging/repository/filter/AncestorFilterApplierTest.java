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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.filter;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.NodeResource;

@ExtendWith(MockitoExtension.class)
class AncestorFilterApplierTest
{
    private static final String ANCESTOR_ID = "parent-node-id";
    private static final String GRAND_ANCESTOR_ID = "grandparent-node-id";
    private static final String NODE_ID = "node-id";
    @Mock
    private NodeResource mockResource;
    @Mock
    private Filter mockFilter;
    @Mock
    private Filter.Path mockPath;

    @InjectMocks
    private AncestorFilterApplier objectUnderTest;

    @BeforeEach
    void mockNodeId()
    {
        given(mockResource.getId()).willReturn(NODE_ID);
    }

    @Test
    void givenEmptyAllowedAndEmptyDeniedFilters_whenNullPrimaryHierarchyOnCurrentNode_thenAllowNode()
    {
        given(mockFilter.path()).willReturn(mockPath);
        given(mockResource.getPrimaryHierarchy()).willReturn(null);
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void givenNonEmptyAllowedAndEmptyDeniedFilters_whenNullPrimaryHierarchyOnCurrentNode_thenDenyNode()
    {
        given(mockFilter.path()).willReturn(mockPath);
        given(mockResource.getPrimaryHierarchy()).willReturn(null);
        given(mockPath.allow()).willReturn(List.of(ANCESTOR_ID));
        given(mockPath.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenNullPrimaryHierarchyOnPreviousNode_thenReturnCurrentlyAllowed(boolean currentlyAllowed)
    {
        given(mockResource.getPrimaryHierarchy()).willReturn(null);

        // when
        boolean result = objectUnderTest.isNodeBeforeAllowed(currentlyAllowed, mockResource, mockFilter);

        // then
        assertEquals(currentlyAllowed, result);
    }

    @Test
    void givenFilteredNodeInAllowedAndEmptyDeniedFilters_whenNullPrimaryHierarchyOnCurrentNode_thenAllowNode()
    {
        given(mockFilter.path()).willReturn(mockPath);
        given(mockResource.getPrimaryHierarchy()).willReturn(null);
        given(mockPath.allow()).willReturn(List.of(ANCESTOR_ID, NODE_ID));
        given(mockPath.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void givenFilteredNodeInDeniedAndEmptyAllowedFilters_whenNullPrimaryHierarchyOnCurrentNode_thenDenyNode()
    {
        given(mockFilter.path()).willReturn(mockPath);
        given(mockResource.getPrimaryHierarchy()).willReturn(null);
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(ANCESTOR_ID, NODE_ID));

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void givenFilteredNodeInAllowedAndNonEmptyDeniedFilters_whenAncestorDeniedOnCurrentNode_thenDenyNode()
    {
        given(mockFilter.path()).willReturn(mockPath);
        given(mockResource.getPrimaryHierarchy()).willReturn(Stream.of(ANCESTOR_ID, GRAND_ANCESTOR_ID).toList());
        given(mockPath.allow()).willReturn(List.of(NODE_ID));
        given(mockPath.deny()).willReturn(List.of(GRAND_ANCESTOR_ID));

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertFalse(result);
    }

}
