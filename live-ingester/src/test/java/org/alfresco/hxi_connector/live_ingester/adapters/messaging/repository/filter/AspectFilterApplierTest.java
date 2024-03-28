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
import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.ExchangeEnricher.UPDATED_EVENT_TYPE_PROP;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.List;
import java.util.Set;

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
class AspectFilterApplierTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    private static final String CM_ASPECT_2 = "cm:aspect2";
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
    private Filter.Aspect mockAspect;
    @Mock
    private Exchange mockExchange;

    @InjectMocks
    private AspectFilterApplier objectUnderTest;

    @BeforeEach
    void mockBasicData()
    {
        given(mockRepoEvent.getData()).willReturn(mockData);
        given(mockData.getResource()).willReturn(mockResource);
        given(mockFilter.aspect()).willReturn(mockAspect);
    }

    @Test
    void shouldNotFilterOutNullAspectsWhenEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldFilterOutWhenAspectsNullAndNonEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeCreate()
    {
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_CREATED.getType());
        given(mockResource.getAspectNames()).willReturn(null);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertFalse(result);
    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeDelete()
    {
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_DELETED.getType());
        given(mockResource.getAspectNames()).willReturn(null);

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertFalse(result);
    }

    @Test
    void shouldNotEnrichExchangeWhenRepoEventTypeUpdateAndFilteringResultNotChanged()
    {
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getAspectNames()).willReturn(emptySet());

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).shouldHaveNoInteractions();
        assertFalse(result);
    }

    @Test
    void shouldEnrichExchangeWhenRepoEventTypeUpdateAndFilteringResultChangedFromAllowedToDenied()
    {
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getAspectNames()).willReturn(Set.of(CM_ASPECT_1));

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
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());
        given(mockRepoEvent.getType()).willReturn(NODE_UPDATED.getType());
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1));
        given(mockData.getResourceBefore()).willReturn(mockResourceBefore);
        given(mockResourceBefore.getAspectNames()).willReturn(Set.of(CM_ASPECT_2));

        // when
        boolean result = objectUnderTest.applyFilter(mockExchange, mockRepoEvent, mockFilter);

        then(mockExchange).should().setProperty(UPDATED_EVENT_TYPE_PROP, NODE_CREATED.getType());
        then(mockExchange).shouldHaveNoMoreInteractions();
        assertTrue(result);
    }
}
