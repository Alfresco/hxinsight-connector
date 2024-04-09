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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.CamelEventMapper;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class RepoEventFilterHandlerTest
{

    @Mock
    private AspectFilterApplier mockAspectFilterApplier;

    @Mock
    private TypeFilterApplier mockTypeFilterApplier;

    @Mock
    private AncestorFilterApplier mockAncestorFilterApplier;

    @Mock
    private CamelEventMapper mockCamelEventMapper;

    @Mock
    private RepoEvent<DataAttributes<NodeResource>> mockRepoEvent;

    @Mock
    private Filter mockFilter;
    @Mock
    private Exchange mockExchange;
    @Mock
    private Message mockMessage;
    @Mock
    private DataAttributes<NodeResource> mockData;
    @Mock
    private NodeResource mockResource;
    @Mock
    private NodeResource mockResourceBefore;

    private RepoEventFilterHandler objectUnderTest;

    @BeforeEach
    void setUp()
    {
        final List<RepoEventFilterApplier> repoEventFilterAppliers = List.of(mockAspectFilterApplier, mockTypeFilterApplier, mockAncestorFilterApplier);
        objectUnderTest = new RepoEventFilterHandler(repoEventFilterAppliers, mockCamelEventMapper);
        given(mockExchange.getIn()).willReturn(mockMessage);
        given(mockMessage.getBody(RepoEvent.class)).willReturn(mockRepoEvent);
        given(mockRepoEvent.getData()).willReturn(mockData);
        given(mockData.getResource()).willReturn(mockResource);
    }

    @Test
    void givenCreateEvent_whenAllFiltersAllow_thenAllowNodeAndNoOtherActions()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_CREATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertTrue(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should(never()).alterRepoEvent(eq(mockExchange), any(String.class));
    }

    @Test
    void givenCreateEvent_whenOneFilterDenies_thenDenyNodeAndNoOtherActions()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_CREATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(false);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertFalse(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should(never()).alterRepoEvent(eq(mockExchange), any(String.class));
    }

    @Test
    void givenDeleteEvent_whenAllFiltersAllow_thenAllowNodeAndNoOtherActions()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_DELETED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertTrue(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should(never()).alterRepoEvent(eq(mockExchange), any(String.class));
    }

    @Test
    void givenUpdateEvent_whenAllFiltersAllowCurrentAndPreviousVersion_thenAllowNodeAndNoOtherActions()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAspectFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertTrue(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should(never()).alterRepoEvent(eq(mockExchange), any(String.class));
    }

    @Test
    void givenUpdateEvent_whenPreviousVersionDeniedAndCurrentVersionDeniedByDifferentFilter_thenDenyAndNoOtherActions()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(false);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAspectFilterApplier.isNodeBeforeAllowed(false, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(false);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertFalse(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).should().isNodeBeforeAllowed(false, mockResourceBefore, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should(never()).alterRepoEvent(eq(mockExchange), any(String.class));
    }

    @Test
    void givenUpdateEvent_whenPreviousVersionDeniedAndCurrentVersionAllowed_thenAllowAndAlterEventTypeToCreated()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAspectFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(false);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertTrue(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should().alterRepoEvent(mockExchange, NODE_CREATED.getType());
    }

    @Test
    void givenUpdateEvent_whenPreviousVersionAllowedAndCurrentVersionDenied_thenAllowAndAlterEventTypeToDeleted()
    {
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockAspectFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(false);
        given(mockTypeFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeAllowed(mockResource, mockFilter)).willReturn(true);
        given(mockAspectFilterApplier.isNodeBeforeAllowed(false, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockTypeFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockAncestorFilterApplier.isNodeBeforeAllowed(true, mockResourceBefore, mockFilter)).willReturn(true);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);

        // when
        boolean allow = objectUnderTest.handleAndGetAllowed(mockExchange, mockFilter);

        assertTrue(allow);
        then(mockAspectFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAspectFilterApplier).should().isNodeBeforeAllowed(false, mockResourceBefore, mockFilter);
        then(mockAspectFilterApplier).shouldHaveNoMoreInteractions();
        then(mockTypeFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockTypeFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockTypeFilterApplier).shouldHaveNoMoreInteractions();
        then(mockAncestorFilterApplier).should().isNodeAllowed(mockResource, mockFilter);
        then(mockAncestorFilterApplier).should().isNodeBeforeAllowed(true, mockResourceBefore, mockFilter);
        then(mockAncestorFilterApplier).shouldHaveNoMoreInteractions();

        then(mockCamelEventMapper).should().alterRepoEvent(mockExchange, NODE_DELETED.getType());
    }

}
