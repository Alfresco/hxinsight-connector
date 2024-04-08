/*
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.NodeResource;

@ExtendWith(MockitoExtension.class)
class AspectFilterApplierTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    @Mock
    private NodeResource mockResource;
    @Mock
    private Filter mockFilter;
    @Mock
    private Filter.Aspect mockAspect;

    @InjectMocks
    private AspectFilterApplier objectUnderTest;

    @BeforeEach
    void mockBasicData()
    {
        given(mockFilter.aspect()).willReturn(mockAspect);
    }

    @Test
    void givenEmptyAllowedAndEmptyDeniedFilters_whenCurrentNodeHasNullAspects_thenAllowNode()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void givenNonEmptyAllowedAndEmptyDeniedFilters_whenCurrentNodeHasNullAspects_thenDenyNode()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.isNodeAllowed(mockResource, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void givenEmptyAllowedAndEmptyDeniedFilters_whenPreviousNodeHasNullAspects_thenAllowNode()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());
        final boolean currentlyAllowed = true;

        // when
        boolean result = objectUnderTest.isNodeBeforeAllowed(currentlyAllowed, mockResource, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void givenNonEmptyAllowedAndEmptyDeniedFilters_whenPreviousNodeHasNullAspects_thenDenyNode()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        final boolean currentlyAllowed = false;

        // when
        boolean result = objectUnderTest.isNodeBeforeAllowed(currentlyAllowed, mockResource, mockFilter);

        // then
        assertFalse(result);
    }

}
