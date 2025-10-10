/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.database.connector.model.AlfrescoNode;
import org.alfresco.database.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.exception.BulkIngesterRuntimeException;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;

@ExtendWith(MockitoExtension.class)
class AspectFilterApplierTest
{

    private static final String CM_ASPECT_1 = "cm:aspect1";
    @Mock
    private AlfrescoNode mockAlfrescoNode;
    @Mock
    private NodeFilterConfig mockFilter;
    @Mock
    private NodeFilterConfig.Aspect mockAspect;
    @Mock
    private NamespacePrefixMapper mockPrefixMapper;

    @InjectMocks
    private AspectFilterApplier objectUnderTest;

    @BeforeEach
    void mockBasicData(TestInfo info)
    {
        if (!info.getDisplayName().equals("shouldThrowExceptionWhenNamespacePrefixMappingNotFound()"))
        {
            given(mockFilter.aspect()).willReturn(mockAspect);
        }
    }

    @Test
    void shouldNotFilterOutNullAspectsWhenEmptyAllowedAndEmptyDenied()
    {
        given(mockAlfrescoNode.getAspects()).willReturn(null);
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldFilterOutWhenAspectsNullAndNonEmptyAllowedAndEmptyDenied()
    {
        given(mockAlfrescoNode.getAspects()).willReturn(null);
        given(mockAspect.allow()).willReturn(List.of(CM_ASPECT_1));
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        // then
        assertFalse(result);
    }

    @Test
    void shouldNotFilterOutWhenEmptyAllowedAndEmptyDenied()
    {
        QName aspect = QName.newTransientInstance("cm", "aspect1");
        given(mockPrefixMapper.toPrefixedName(aspect)).willReturn(CM_ASPECT_1);
        given(mockAlfrescoNode.getAspects()).willReturn(Set.of(aspect));
        given(mockAspect.allow()).willReturn(emptyList());
        given(mockAspect.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        // then
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenNamespacePrefixMappingNotFound()
    {
        QName aspect = QName.newTransientInstance("cm", "aspect1");
        given(mockPrefixMapper.toPrefixedName(aspect)).willThrow(BulkIngesterRuntimeException.class);
        given(mockAlfrescoNode.getAspects()).willReturn(Set.of(aspect));

        // when / then
        assertThrows(BulkIngesterRuntimeException.class, () -> objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }
}
