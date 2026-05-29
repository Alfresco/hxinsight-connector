/*-
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
package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.database.connector.model.AlfrescoNode;
import org.alfresco.database.connector.model.NodeProperty;
import org.alfresco.database.connector.model.PropertyKey;
import org.alfresco.database.connector.model.QName;

@ExtendWith(MockitoExtension.class)
class RequiredFieldsFilterApplierTest
{
    @Mock
    private AlfrescoNode mockAlfrescoNode;
    @Mock
    private NodeFilterConfig mockFilter;

    @InjectMocks
    private RequiredFieldsFilterApplier objectUnderTest;

    @Test
    void givenEmptyRequiredFields_thenAllowNode()
    {
        given(mockFilter.requiredFields()).willReturn(emptyList());

        assertTrue(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenUnknownFieldName_thenAllowNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("unknownField"));

        assertTrue(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenAllRequiredFieldsPresent_thenAllowNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("type", "createdAt", "createdBy", "modifiedAt", "modifiedBy"));
        given(mockAlfrescoNode.getType()).willReturn(QName.newTransientInstance("cm", "content"));
        given(mockAlfrescoNode.getCreatedAt()).willReturn(ZonedDateTime.now());
        given(mockAlfrescoNode.getCreator()).willReturn("user-id");
        given(mockAlfrescoNode.getModifiedAt()).willReturn(ZonedDateTime.now());
        given(mockAlfrescoNode.getModifier()).willReturn("user-id");

        assertTrue(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenNameRequiredAndNamePropertyPresent_thenAllowNode()
    {
        NodeProperty nameProperty = mockNodeProperty("name");
        given(mockFilter.requiredFields()).willReturn(List.of("name"));
        given(mockAlfrescoNode.getNodeProperties()).willReturn(Set.of(nameProperty));

        assertTrue(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenNameRequiredAndNamePropertyAbsent_thenDenyNode()
    {
        NodeProperty titleProperty = mockNodeProperty("title");
        given(mockFilter.requiredFields()).willReturn(List.of("name"));
        given(mockAlfrescoNode.getNodeProperties()).willReturn(Set.of(titleProperty));

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenNameRequiredAndNodePropertiesNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("name"));
        given(mockAlfrescoNode.getNodeProperties()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenTypeRequiredAndTypeNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("type"));
        given(mockAlfrescoNode.getType()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenCreatedAtRequiredAndCreatedAtNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("createdAt"));
        given(mockAlfrescoNode.getCreatedAt()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenCreatedByRequiredAndCreatorNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("createdBy"));
        given(mockAlfrescoNode.getCreator()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenModifiedAtRequiredAndModifiedAtNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("modifiedAt"));
        given(mockAlfrescoNode.getModifiedAt()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    @Test
    void givenModifiedByRequiredAndModifierNull_thenDenyNode()
    {
        given(mockFilter.requiredFields()).willReturn(List.of("modifiedBy"));
        given(mockAlfrescoNode.getModifier()).willReturn(null);

        assertFalse(objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter));
    }

    private NodeProperty mockNodeProperty(String localName)
    {
        NodeProperty property = mock();
        PropertyKey propertyKey = mock();
        given(property.getPropertyKey()).willReturn(propertyKey);
        given(propertyKey.getLocalName()).willReturn(localName);
        return property;
    }
}
