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
package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.exception.BulkIngesterRuntimeException;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;

@ExtendWith(MockitoExtension.class)
class TypeFilterApplierTest
{

    @Mock
    private AlfrescoNode mockAlfrescoNode;
    @Mock
    private NodeFilterConfig mockFilter;
    @Mock
    private NamespacePrefixMapper mockPrefixMapper;

    @InjectMocks
    private TypeFilterApplier objectUnderTest;

    @Test
    void shouldThrowExceptionWhenNullNodeType()
    {
        given(mockAlfrescoNode.getType()).willReturn(null);

        // when / then
        assertThrows(NullPointerException.class, () -> objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void shouldThrowExceptionWhenNamespacePrefixMappingNotFound()
    {
        QName type = QName.newTransientInstance("cm", "special-folder");
        given(mockAlfrescoNode.getType()).willReturn(type);
        given(mockPrefixMapper.toPrefixedName(type)).willThrow(BulkIngesterRuntimeException.class);

        // when / then
        assertThrows(BulkIngesterRuntimeException.class, () -> objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }
}
