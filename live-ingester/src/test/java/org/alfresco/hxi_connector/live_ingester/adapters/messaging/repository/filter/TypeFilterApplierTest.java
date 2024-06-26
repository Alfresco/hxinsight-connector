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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;

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
class TypeFilterApplierTest
{
    private static final String CM_CONTENT = "cm:content";
    private static final String CM_SPECIAL_FOLDER = "cm:special-folder";
    @Mock
    private NodeResource mockResource;
    @Mock
    private Filter mockFilter;
    @Mock
    private Filter.Type mockType;

    @InjectMocks
    private TypeFilterApplier objectUnderTest;

    @Test
    void givenNonEmptyFilters_whenNullCurrentNodeType_thenExceptionIsThrown()
    {
        given(mockFilter.type()).willReturn(mockType);
        given(mockResource.getNodeType()).willReturn(null);
        given(mockType.allow()).willReturn(List.of(CM_CONTENT, CM_SPECIAL_FOLDER));
        given(mockType.deny()).willReturn(emptyList());

        // when/then
        assertThrows(NullPointerException.class, () -> objectUnderTest.isNodeAllowed(mockResource, mockFilter));

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenNullPreviousNodeType_thenResultSameAsForCurrent(boolean currentlyAllowed)
    {
        given(mockResource.getNodeType()).willReturn(null);

        // when
        boolean result = objectUnderTest.isNodeBeforeAllowed(currentlyAllowed, mockResource, mockFilter);

        // then
        assertEquals(currentlyAllowed, result);
    }

}
