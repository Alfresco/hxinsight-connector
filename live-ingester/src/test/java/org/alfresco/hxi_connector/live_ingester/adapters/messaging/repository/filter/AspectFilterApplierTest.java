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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AspectFilterApplierTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    private static final String CM_ASPECT_2 = "cm:aspect2";
    private static final String CM_ASPECT_3 = "cm:aspect3";
    private static final String CM_ASPECT_4 = "cm:aspect4";
    @Mock
    private RepoEvent<DataAttributes<NodeResource>> mockRepoEvent;
    @Mock
    private DataAttributes<NodeResource> mockData;
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
        given(mockRepoEvent.getData()).willReturn(mockData);
        given(mockData.getResource()).willReturn(mockResource);
        given(mockFilter.aspect()).willReturn(mockAspect);
    }

    @Test
    void shouldNotFilterOutNonEmptyAspectsWhenEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1));
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldFilterOutWhenAspectsEmptyAndNonEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(emptySet());
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldNotFilterOutNullAspectsWhenEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(null);
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

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
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldNotFilterOutEmptyAspectsWhenEmptyAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(emptySet());
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldNotFilterOutWhenAtLeastOneAspectInAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1, CM_ASPECT_2));
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldNotFilterOutWhenEmptyAllowedAndAspectNotInDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1));
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(List.of(CM_ASPECT_2));

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldNotFilterOutWhenAspectInAllowedAndAspectNotInDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1));
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(List.of(CM_ASPECT_2));

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldFilterOutWhenAspectInAllowedAndAspectInDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1, CM_ASPECT_2));
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(List.of(CM_ASPECT_2));

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldFilterOutWhenAspectNotAllowedAndEmptyDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1, CM_ASPECT_2));
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_3));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldFilterOutWhenEmptyAllowedAndAspectInDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_2, CM_ASPECT_3));
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(List.of(CM_ASPECT_3));

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldNotFilterOutWhenAtLeastOneAspectInAllowedAndAspectNotInDenied()
    {
        given(mockResource.getAspectNames()).willReturn(Set.of(CM_ASPECT_1, CM_ASPECT_4));
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1, CM_ASPECT_2));
        given(mockAspect.deny()).willReturn(List.of(CM_ASPECT_3));

        // when
        boolean result = objectUnderTest.applyFilter(mockRepoEvent, mockFilter);

        // then
        assertTrue(result);
    }
}
