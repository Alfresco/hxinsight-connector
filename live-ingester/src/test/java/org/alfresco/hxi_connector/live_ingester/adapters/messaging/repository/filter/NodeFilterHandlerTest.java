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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NodeFilterHandlerTest {

    @Mock
    private AspectFilterApplier mockAspectFilterApplier;

    @Mock
    private NodeFilterApplier anotherMockFilterApplier;

    @Mock
    private RepoEvent<DataAttributes<NodeResource>> mockRepoEvent;

    @Mock
    private Filter mockFilter;

    private NodeFilterHandler objectUnderTest;

    @BeforeEach
    void setUp() {
        final List<NodeFilterApplier> nodeFilterAppliers = List.of(mockAspectFilterApplier, anotherMockFilterApplier);
        objectUnderTest = new NodeFilterHandler(nodeFilterAppliers);
    }

    @Test
    void shouldNotFilterOutWhenAllAppliersReturnTrue() {
        given(mockAspectFilterApplier.applyFilter(any(), any())).willReturn(true);
        given(anotherMockFilterApplier.applyFilter(any(), any())).willReturn(true);

        //when
        final boolean result = objectUnderTest.filterNode(mockRepoEvent, mockFilter);

        then(mockAspectFilterApplier).should().applyFilter(mockRepoEvent, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(anotherMockFilterApplier).should().applyFilter(mockRepoEvent, mockFilter);
        then(anotherMockFilterApplier).shouldHaveNoMoreInteractions();

        assertTrue(result);
    }

    @Test
    void shouldFilterOutWhenAtLeastOneApplierReturnFalse() {
        given(mockAspectFilterApplier.applyFilter(any(), any())).willReturn(true);
        given(anotherMockFilterApplier.applyFilter(any(), any())).willReturn(false);

        //when
        final boolean result = objectUnderTest.filterNode(mockRepoEvent, mockFilter);

        then(mockAspectFilterApplier).should().applyFilter(mockRepoEvent, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(anotherMockFilterApplier).should().applyFilter(mockRepoEvent, mockFilter);
        then(anotherMockFilterApplier).shouldHaveNoMoreInteractions();

        assertFalse(result);
    }

    @Test
    void shouldFilterOutAndFailFastWhenFirstApplierReturnFalse() {
        given(mockAspectFilterApplier.applyFilter(any(), any())).willReturn(false);

        //when
        final boolean result = objectUnderTest.filterNode(mockRepoEvent, mockFilter);

        then(mockAspectFilterApplier).should().applyFilter(mockRepoEvent, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(anotherMockFilterApplier).shouldHaveNoInteractions();

        assertFalse(result);
    }

}
