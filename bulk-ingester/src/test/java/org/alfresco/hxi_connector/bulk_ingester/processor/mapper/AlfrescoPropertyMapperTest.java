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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.BOOLEAN;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.CONTENT_DATA_ID;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.DATE;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.DOUBLE;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.FLOAT;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.LONG;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.NODEREF;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.NULL;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.PATH;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.SERIALIZABLE;
import static org.alfresco.elasticsearch.db.connector.model.PropertyValueType.STRING;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.ContentMetadata;
import org.alfresco.elasticsearch.db.connector.model.NodeProperty;
import org.alfresco.elasticsearch.db.connector.model.PropertyKey;
import org.alfresco.elasticsearch.db.connector.model.PropertyValue;
import org.alfresco.elasticsearch.db.connector.model.PropertyValueType;

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
                        createNodeProperty(description, stringValue(descriptionText))));

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
                        createNodeProperty(description, "en_UK_", stringValue(ukDescription))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, description).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(description, property.get().getKey());
        assertThat(List.of(usDescription, ukDescription)).hasSameElementsAs((Iterable<String>) property.get().getValue());
    }

    @Test
    void shouldProcessStringProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String description = "description";
        String descriptionText = "This document is about different animals legs length";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(description, stringValue(descriptionText))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, description).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(description, property.get().getKey());
        assertEquals(descriptionText, property.get().getValue());
    }

    @Test
    void shouldProcessNullProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String description = "description";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(description, nullValue())));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, description).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessBoolProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String isIndexed = "isIndexed";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(isIndexed, boolValue(true))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, isIndexed).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(isIndexed, property.get().getKey());
        assertTrue((Boolean) property.get().getValue());
    }

    @Test
    void shouldProcessLongProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String legsCount = "legsCount";
        long legsCountValue = 3;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(legsCount, longValue(legsCountValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, legsCount).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(legsCount, property.get().getKey());
        assertEquals(legsCountValue, property.get().getValue());
    }

    @Test
    void shouldProcessFloatProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String legLength = "legLength";
        float legLengthValue = 3.5f;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(legLength, floatValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, legLength).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(legLength, property.get().getKey());
        assertEquals(legLengthValue, property.get().getValue());
    }

    @Test
    void shouldProcessDoubleProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String legLength = "legLength";
        double legLengthValue = 3.5d;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(legLength, doubleValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, legLength).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(legLength, property.get().getKey());
        assertEquals(legLengthValue, property.get().getValue());
    }

    @Test
    void shouldProcessSerializableProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String colors = "colors";
        List<String> colorsValue = List.of("blue", "red", "black");

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(colors, serializableValue(SerializationUtils.serialize(colorsValue)))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, colors).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(colors, property.get().getKey());
        assertEquals(colorsValue, property.get().getValue());
    }

    @Test
    void shouldNotProcessUnsupportedTypes()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String someRandomUnsupportedPropertyType = "someRandomUnsupportedType";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(someRandomUnsupportedPropertyType, propertyValue(PATH))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, someRandomUnsupportedPropertyType).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessContentComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String content = "content";
        long contendId = 1;

        ContentMetadata contentMetadata = contentMetadata(contendId);

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(content, contentValue(contendId))));
        alfrescoNode.setContentData(
                Set.of(contentMetadata));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, content).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(content, property.get().getKey());
        assertEquals(contentMetadata, property.get().getValue());
    }

    @Test
    void shouldProcessDateComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String createdAt = "createdAt";
        String createdAtString = "2024-01-31T10:15:30+00:00";
        ZonedDateTime createdAtValue = ZonedDateTime.parse(createdAtString);

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(createdAt, dateValue(createdAtString))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, createdAt).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(createdAt, property.get().getKey());
        assertEquals(createdAtValue, property.get().getValue());
    }

    @Test
    void shouldProcessNodeRefComplexType()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String copyOf = "copyOf";
        String copyOfValue = "workspace://SpacesStore/f70cb080-9d45-4f21-b936-475b053a23f1";
        Map<String, Object> copyOfExpectedValue = Map.of(
                "id", "f70cb080-9d45-4f21-b936-475b053a23f1",
                "storeRef", Map.of(
                        "identifier", "SpacesStore",
                        "protocol", "workspace"));

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(copyOf, nodeRefValue(copyOfValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, copyOf).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(copyOf, property.get().getKey());
        assertEquals(copyOfExpectedValue, property.get().getValue());
    }

    private PropertyValue stringValue(String value)
    {
        PropertyValue propertyValue = propertyValue(STRING);
        propertyValue.setStringValue(value);

        return propertyValue;
    }

    private PropertyValue nullValue()
    {
        return propertyValue(NULL);
    }

    private PropertyValue boolValue(boolean value)
    {
        PropertyValue propertyValue = propertyValue(BOOLEAN);

        propertyValue.setBooleanValue(value);

        return propertyValue;
    }

    private PropertyValue longValue(long value)
    {
        PropertyValue propertyValue = propertyValue(LONG);

        propertyValue.setLongValue(value);

        return propertyValue;
    }

    private PropertyValue floatValue(float value)
    {
        PropertyValue propertyValue = propertyValue(FLOAT);

        propertyValue.setFloatValue(value);

        return propertyValue;
    }

    private PropertyValue doubleValue(double value)
    {
        PropertyValue propertyValue = propertyValue(DOUBLE);

        propertyValue.setDoubleValue(value);

        return propertyValue;
    }

    private PropertyValue serializableValue(byte[] value)
    {
        PropertyValue propertyValue = propertyValue(SERIALIZABLE);

        propertyValue.setSerializableValue(value);

        return propertyValue;
    }

    private PropertyValue contentValue(long contentId)
    {
        PropertyValue propertyValue = propertyValue(LONG, CONTENT_DATA_ID);

        propertyValue.setLongValue(contentId);

        return propertyValue;
    }

    private PropertyValue dateValue(String date)
    {
        PropertyValue propertyValue = propertyValue(STRING, DATE);

        propertyValue.setStringValue(date);

        return propertyValue;
    }

    private PropertyValue nodeRefValue(String nodeRef)
    {
        PropertyValue propertyValue = propertyValue(STRING, NODEREF);

        propertyValue.setStringValue(nodeRef);

        return propertyValue;
    }

    private PropertyValue propertyValue(PropertyValueType persistedType)
    {
        return propertyValue(persistedType, persistedType);
    }

    private PropertyValue propertyValue(PropertyValueType persistedType, PropertyValueType actualType)
    {
        PropertyValue propertyValue = new PropertyValue();

        propertyValue.setPersistedType(persistedType.getOrd());
        propertyValue.setActualType(actualType.getOrd());

        return propertyValue;
    }

    private ContentMetadata contentMetadata(long contentId)
    {
        ContentMetadata contentMetadata = new ContentMetadata();

        contentMetadata.setId(contentId);

        return contentMetadata;
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
