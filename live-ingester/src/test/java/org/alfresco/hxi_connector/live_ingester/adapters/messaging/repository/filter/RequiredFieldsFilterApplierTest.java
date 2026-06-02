/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class RequiredFieldsFilterApplierTest
{
    @Mock
    private NodeResource mockResource;
    @Mock
    private Filter mockFilter;
    @Mock
    private RepoEvent<DataAttributes<NodeResource>> mockEvent;
    @Mock
    private DataAttributes<NodeResource> mockData;

    @InjectMocks
    private RequiredFieldsFilterApplier objectUnderTest;

    @Test
    void givenAllRequiredFieldsPresent_thenNodeIsAllowed()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("name", "type"));
        given(mockResource.getName()).willReturn("test-node");
        given(mockResource.getNodeType()).willReturn("cm:content");

        assertTrue(objectUnderTest.isNodeAllowed(mockResource, mockFilter));
    }

    @Test
    void givenMissingRequiredField_thenNodeIsNotAllowed()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("name", "type"));
        given(mockResource.getName()).willReturn(null);

        assertFalse(objectUnderTest.isNodeAllowed(mockResource, mockFilter));
    }

    @Test
    void givenEmptyRequiredFields_thenNodeIsAllowed()
    {
        given(mockFilter.requiredFields()).willReturn(List.of());

        assertTrue(objectUnderTest.isNodeAllowed(mockResource, mockFilter));
    }

    @Test
    void givenUnknownFieldName_thenNodeIsAllowed()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("unknownField"));

        assertTrue(objectUnderTest.isNodeAllowed(mockResource, mockFilter));
    }

    @Test
    void givenDeleteEvent_whenRequiredFieldsConfigured_thenNodeIsAllowed()
    {
        given(mockEvent.getType()).willReturn(NODE_DELETED.getType());

        assertTrue(objectUnderTest.isNodeAllowed(mockEvent, mockFilter));
    }

    @Test
    void givenNonDeleteEvent_whenRequiredFieldsConfigured_thenDelegatesToNodeResource()
    {
        given(mockEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockEvent.getData()).willReturn(mockData);
        given(mockData.getResource()).willReturn(mockResource);
        given(mockFilter.requiredFields()).willReturn(List.of("name"));
        given(mockResource.getName()).willReturn("test-node");

        assertTrue(objectUnderTest.isNodeAllowed(mockEvent, mockFilter));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void whenCheckingPreviousNode_thenResultSameAsForCurrent(boolean currentlyAllowed)
    {
        boolean result = objectUnderTest.isNodeBeforeAllowed(currentlyAllowed, mockResource, mockFilter);

        assertEquals(currentlyAllowed, result);
    }
}
