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
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.ExchangeEnricher.UPDATED_EVENT_TYPE_PROP;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.List;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
class TypeFilterApplierTest
{
    private static final String CM_CONTENT = "cm:content";
    private static final String CM_SPECIAL_FOLDER = "cm:special-folder";
    private static final String CM_FOLDER = "cm:folder";
    @Mock
    private RepoEvent<DataAttributes<NodeResource>> mockRepoEvent;
    @Mock
    private DataAttributes<NodeResource> mockData;
    @Mock
    private NodeResource mockResource;
    @Mock
    private NodeResource mockResourceBefore;
    @Mock
    private Filter mockFilter;
    @Mock
    private Filter.Type mockType;
    @Mock
    private Exchange mockExchange;

    @InjectMocks
    private TypeFilterApplier objectUnderTest;

    @BeforeEach
    void mockBasicData()
    {
        given(mockRepoEvent.getData()).willReturn(mockData);
        given(mockData.getResource()).willReturn(mockResource);
        given(mockFilter.type()).willReturn(mockType);
    }

    @Test
    void shouldThrowExceptionWhenNullNodeType()
    {
        given(mockResource.getNodeType()).willReturn(null);
        given(mockType.allow()).willReturn(List.of(CM_CONTENT, CM_SPECIAL_FOLDER));
        given(mockType.deny()).willReturn(emptyList());

        // when/then
        assertThrows(NullPointerException.class, () -> objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter));

    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeCreate()
    {
        given(mockType.allow()).willReturn(emptyList());
        given(mockType.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_CREATED.getType());
        given(mockResource.getNodeType()).willReturn(CM_CONTENT);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertTrue(result);
    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeDelete()
    {
        given(mockType.allow()).willReturn(emptyList());
        given(mockType.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_DELETED.getType());
        given(mockResource.getNodeType()).willReturn(CM_CONTENT);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertTrue(result);
    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeUpdateAndFilteringResultNotChanged()
    {
        given(mockType.allow()).willReturn(List.of(CM_CONTENT));
        given(mockType.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getNodeType()).willReturn(CM_CONTENT);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getNodeType()).willReturn(CM_CONTENT);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertTrue(result);
    }

    @Test
    void shouldEnrichExchangeWhenRepoEventTypeUpdateAndFilteringResultChangedFromAllowedToDenied()
    {
        given(mockType.allow()).willReturn(List.of(CM_SPECIAL_FOLDER));
        given(mockType.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getNodeType()).willReturn(CM_FOLDER);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getNodeType()).willReturn(CM_SPECIAL_FOLDER);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).should().setProperty(UPDATED_EVENT_TYPE_PROP, NODE_DELETED.getType());
        then(mockExchange).shouldHaveNoMoreInteractions();
        // needs to further process delete event
        assertTrue(result);
    }

    @Test
    void shouldEnrichExchangeWhenRepoEventTypeUpdateAndFilteringResultChangedFromDeniedToAllowed()
    {
        given(mockType.allow()).willReturn(List.of(CM_FOLDER));
        given(mockType.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getNodeType()).willReturn(CM_FOLDER);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getNodeType()).willReturn(CM_SPECIAL_FOLDER);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).should().setProperty(UPDATED_EVENT_TYPE_PROP, NODE_CREATED.getType());
        then(mockExchange).shouldHaveNoMoreInteractions();
        assertTrue(result);
    }
}
