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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.ContentMetadata;
import org.alfresco.elasticsearch.db.connector.model.NodeProperty;
import org.alfresco.elasticsearch.db.connector.model.PropertyKey;
import org.alfresco.elasticsearch.db.connector.model.PropertyValue;
import org.alfresco.elasticsearch.db.connector.model.PropertyValueType;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@SuppressWarnings({"PMD.TooManyMethods"})
class AlfrescoPropertyMapperTest
{
    private static final String TEST_PREFIX = "test";
    private final NamespacePrefixMapper namespacePrefixMapper = new TestNamespaceToPrefixMapper(TEST_PREFIX);

    @Test
    void shouldMapJustPropertiesWithGivenName()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";
        String prefixedPropertyName = "test:description";
        String descriptionText = "The purpose of document is...";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty("title"),
                        createNodeProperty(propertyName, stringValue(descriptionText))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, descriptionText), property);
    }

    @Test
    void shouldProcessPropertiesWithSameNameTogether()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";
        String prefixedPropertyName = "test:description";
        String usDescription = "French fries recipe";
        String ukDescription = "Chips recipe";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty("title"),
                        createNodeProperty(propertyName, "en_US_", stringValue(usDescription)),
                        createNodeProperty(propertyName, "en_UK_", stringValue(ukDescription))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertTrue(property.isPresent());
        assertEquals(prefixedPropertyName, property.get().getKey());
        assertThat((Iterable<String>) property.get().getValue()).hasSameElementsAs(List.of(usDescription, ukDescription));
    }

    @Test
    void shouldProcessStringProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";
        String prefixedPropertyName = "test:description";
        String descriptionText = "This document is about different animals legs length";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, stringValue(descriptionText))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, descriptionText), property);
    }

    @Test
    void shouldProcessNullProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "description";
        String prefixedPropertyName = "test:description";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, nullValue())));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessBoolProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "isIndexed";
        String prefixedPropertyName = "test:isIndexed";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, boolValue(true))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, true), property);
    }

    @Test
    void shouldProcessLongProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legsCount";
        String prefixedPropertyName = "test:legsCount";
        long legsCountValue = 3;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, longValue(legsCountValue))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, legsCountValue), property);
    }

    @Test
    void shouldProcessFloatProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legLength";
        String prefixedPropertyName = "test:legLength";
        float legLengthValue = 3.5f;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, floatValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, legLengthValue), property);
    }

    @Test
    void shouldProcessDoubleProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "legLength";
        String prefixedPropertyName = "test:legLength";
        double legLengthValue = 3.5d;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, doubleValue(legLengthValue))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, legLengthValue), property);
    }

    @Test
    void shouldProcessSerializableProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "colors";
        String prefixedPropertyName = "test:colors";
        List<String> colorsValue = List.of("blue", "red", "black");

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, serializableValue(SerializationUtils.serialize(colorsValue)))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, (Serializable) colorsValue), property);
    }

    @Test
    void shouldNotProcessUnsupportedTypes()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "someRandomPropertyWithUnsupportedType";
        String prefixedPropertyName = "test:someRandomPropertyWithUnsupportedType";

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, propertyValue(PATH))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertFalse(property.isPresent());
    }

    @Test
    void shouldProcessContentComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "content";
        String prefixedPropertyName = "cm:content";

        long contentId = 1;
        long contentSize = 1000;
        String encoding = "UTF-8";
        String mimeType = "application/pdf";

        ContentMetadata contentMetadata = contentMetadata(contentId, contentSize, encoding, mimeType);

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, contentValue(contentId))));
        alfrescoNode.setContentData(
                Set.of(contentMetadata));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        IngestEvent.ContentInfo expectedContentInfo = new IngestEvent.ContentInfo(contentSize, encoding, mimeType);

        assertEquals(expectedProperty(prefixedPropertyName, expectedContentInfo), property);
    }

    @Test
    void shouldProcessDateComplexProperty()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "createdAt";
        String prefixedPropertyName = "test:createdAt";
        String createdAtString = "2024-01-31T10:15:30+00:00";
        long createdAtValue = ZonedDateTime.parse(createdAtString).toInstant().toEpochMilli();

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, dateValue(createdAtString))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, createdAtValue), property);
    }

    @Test
    void shouldProcessNodeRefComplexType()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        String propertyName = "copyOf";
        String prefixedPropertyName = "test:copyOf";

        String nodeId = "f70cb080-9d45-4f21-b936-475b053a23f1";
        String copyOfValue = "workspace://SpacesStore/" + nodeId;

        alfrescoNode.setNodeProperties(
                Set.of(
                        createNodeProperty(propertyName, nodeRefValue(copyOfValue))));

        // when
        var property = new AlfrescoPropertyMapper(namespacePrefixMapper, alfrescoNode, prefixedPropertyName).performMapping();

        // then
        assertEquals(expectedProperty(prefixedPropertyName, nodeId), property);
    }

    private Optional<Map.Entry<String, Serializable>> expectedProperty(String propertyName, Serializable propertyValue)
    {
        return Optional.of(Map.entry(propertyName, propertyValue));
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

    private ContentMetadata contentMetadata(long contentId, long size, String encoding, String mimeType)
    {
        ContentMetadata contentMetadata = new ContentMetadata();

        contentMetadata.setId(contentId);
        contentMetadata.setContentSize(size);
        contentMetadata.setEncodingStr(encoding);
        contentMetadata.setMimetypeStr(mimeType);

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
