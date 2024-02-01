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

@SuppressWarnings({"PMD.TooManyMethods"})
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

        String propertyName = "description";
        String usDescription = "French fries recipe";
        String ukDescription = "Chips recipe";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty("title"),
                        createNodeProperty(propertyName, "en_US_", stringValue(usDescription)),
                        createNodeProperty(propertyName, "en_UK_", stringValue(ukDescription))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertThat(List.of(usDescription, ukDescription)).hasSameElementsAs((Iterable<String>) property.get().getValue());
    }

    @Test
    void shouldProcessStringProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";
        String descriptionText = "This document is about different animals legs length";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, stringValue(descriptionText))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(descriptionText, property.get().getValue());
    }

    @Test
    void shouldProcessNullProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, nullValue())));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessBoolProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "isIndexed";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, boolValue(true))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertTrue((Boolean) property.get().getValue());
    }

    @Test
    void shouldProcessLongProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legsCount";
        long legsCountValue = 3;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, longValue(legsCountValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(legsCountValue, property.get().getValue());
    }

    @Test
    void shouldProcessFloatProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legLength";
        float legLengthValue = 3.5f;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, floatValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(legLengthValue, property.get().getValue());
    }

    @Test
    void shouldProcessDoubleProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legLength";
        double legLengthValue = 3.5d;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, doubleValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(legLengthValue, property.get().getValue());
    }

    @Test
    void shouldProcessSerializableProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "colors";
        List<String> colorsValue = List.of("blue", "red", "black");

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, serializableValue(SerializationUtils.serialize(colorsValue)))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(colorsValue, property.get().getValue());
    }

    @Test
    void shouldNotProcessUnsupportedTypes()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "someRandomPropertyWithUnsupportedType";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, propertyValue(PATH))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessContentComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "content";
        long contendId = 1;

        ContentMetadata contentMetadata = contentMetadata(contendId);

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, contentValue(contendId))));
        alfrescoNode.setContentData(
                Set.of(contentMetadata));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(contentMetadata, property.get().getValue());
    }

    @Test
    void shouldProcessDateComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "createdAt";
        String createdAtString = "2024-01-31T10:15:30+00:00";
        ZonedDateTime createdAtValue = ZonedDateTime.parse(createdAtString);

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, dateValue(createdAtString))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
        assertEquals(createdAtValue, property.get().getValue());
    }

    @Test
    void shouldProcessNodeRefComplexType()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "copyOf";
        String copyOfValue = "workspace://SpacesStore/f70cb080-9d45-4f21-b936-475b053a23f1";
        Map<String, Object> copyOfExpectedValue = Map.of(
                "id", "f70cb080-9d45-4f21-b936-475b053a23f1",
                "storeRef", Map.of(
                        "identifier", "SpacesStore",
                        "protocol", "workspace"));

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, nodeRefValue(copyOfValue))));

        // when
        var property = new AlfrescoPropertyMapper(alfrescoNode, propertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(propertyName, property.get().getKey());
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
