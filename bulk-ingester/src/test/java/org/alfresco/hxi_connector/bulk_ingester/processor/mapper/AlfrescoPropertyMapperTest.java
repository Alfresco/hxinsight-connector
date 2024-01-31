/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.NodeProperty;
import org.alfresco.elasticsearch.db.connector.model.PropertyKey;
import org.alfresco.elasticsearch.db.connector.model.PropertyValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlfrescoPropertyMapperTest
{

    @Test
    void shouldMapJustPropertiesWithGivenName()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String description = "description";
        String descriptionText = "The purpose of document is...";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty("title"),
                        createNodeProperty(description, stringValue(descriptionText))
                )
        );

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, description).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(description, property.get().getKey());
        assertEquals(descriptionText, property.get().getValue());
    }

    @Test
    void shouldProcessPropertiesWithSameNameTogether()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String description = "description";
        String usDescription = "French fries recipe";
        String ukDescription = "Chips recipe";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty("title"),
                        createNodeProperty(description, "en_US_", stringValue(usDescription)),
                        createNodeProperty(description, "en_UK_", stringValue(ukDescription))
                )
        );

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, description).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(description, property.get().getKey());
        assertEquals(List.of(usDescription, ukDescription), property.get().getValue());
    }

    private PropertyValue stringValue(String value)
    {
        PropertyValue propertyValue = new PropertyValue();

        propertyValue.setPersistedType(STRING.getOrd());
        propertyValue.setStringValue(value);

        return propertyValue;
    }

    private NodeProperty createNodeProperty(String name)
    {
        return createNodeProperty(name, null, null);
    }

    private NodeProperty createNodeProperty(String name, PropertyValue propertyValue)
    {
        return createNodeProperty(name, null, propertyValue);
    }

    private NodeProperty createNodeProperty(String name, String locale, PropertyValue propertyValue)
    {
        PropertyKey propertyKey = new PropertyKey();
        propertyKey.setLocalName(name);
        propertyKey.setLocaleStr(locale);

        NodeProperty nodeProperty = new NodeProperty();
        nodeProperty.setPropertyKey(propertyKey);

        nodeProperty.setPropertyValue(propertyValue);

        return nodeProperty;
    }
}
