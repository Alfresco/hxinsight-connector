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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.TYPE_PROPERTY;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.NodeProperty;
import org.alfresco.elasticsearch.db.connector.model.PropertyKey;
import org.alfresco.elasticsearch.db.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.ContentInfo;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.Node;

class AlfrescoNodeMapperTest
{
    private static final String TEST_PREFIX = "test";
    private static final String NODE_UUID = "859fd51f-a311-4daf-a460-1d9c7e80aa8d";
    private static final String TYPE_FOLDER = "folder";
    private static final String PREFIXED_TYPE_FOLDER = "test:folder";
    private static final String CREATOR_ID = "7cd6bd1f-3ec3-461d-86ec-63cdd32b285e";
    private static final String MODIFIER_ID = "36013be3-4646-4146-b94d-5f72f3a8f717";
    private static final String ASPECT_TITLED = "titled";
    private static final String PREFIXED_ASPECT_TITLED = "test:titled";
    private static final ZonedDateTime CREATED_AT = ZonedDateTime.parse("2024-01-31T10:15:30+00:00");
    private static final long CREATED_AT_TIMESTAMP = CREATED_AT.toInstant().getEpochSecond();

    private final AlfrescoPropertyMapper alfrescoPropertyMapper = mock();
    private final NamespacePrefixMapper namespacePrefixMapper = new TestNamespaceToPrefixMapper(TEST_PREFIX);
    private final AlfrescoNodeMapper alfrescoNodeMapper = new AlfrescoNodeMapper((node, propertyName) -> alfrescoPropertyMapper, namespacePrefixMapper);

    @Test
    void shouldMapNodeWithoutContentAndProperties()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        alfrescoNode.setNodeRef(NODE_UUID);
        alfrescoNode.setType(QName.newTransientInstance("", TYPE_FOLDER));
        alfrescoNode.setCreator(CREATOR_ID);
        alfrescoNode.setModifier(MODIFIER_ID);
        alfrescoNode.setAspects(Set.of(QName.newTransientInstance("", ASPECT_TITLED)));
        alfrescoNode.setCreatedAt(CREATED_AT);
        alfrescoNode.setNodeProperties(Set.of());

        // when
        Node node = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(NODE_UUID, node.nodeId());
        assertEquals(Set.of(PREFIXED_ASPECT_TITLED), node.aspectNames());
        assertNull(node.contentInfo());
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_TIMESTAMP), node.properties());
    }

    @Test
    void shouldMapNodeWithProperties()
    {
        // given
        AlfrescoNode alfrescoNode = nodeWithDefaultProperties();

        String namePropertyKey = "name";
        String namePropertyValue = "Documents";

        alfrescoNode.setNodeProperties(Set.of(mockProperty(namePropertyKey)));

        given(alfrescoPropertyMapper.performMapping()).willReturn(
                Optional.of(Map.entry(
                        namePropertyKey, namePropertyValue)));

        // when
        Node node = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(Map.of(namePropertyKey, namePropertyValue,
                TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_TIMESTAMP), node.properties());
    }

    @Test
    void shouldMapNodeWithContent()
    {
        // given
        AlfrescoNode alfrescoNode = nodeWithDefaultProperties();

        String contentPropertyKey = "cm:content";
        ContentInfo contentInfo = mock();

        alfrescoNode.setNodeProperties(Set.of(mockProperty(contentPropertyKey)));

        given(alfrescoPropertyMapper.performMapping()).willReturn(
                Optional.of(Map.entry(
                        contentPropertyKey, contentInfo)));
        // when
        Node node = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(contentInfo, node.contentInfo());
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_TIMESTAMP), node.properties());
    }

    @Test
    void shouldSkipEmptyCustomProperties()
    {
        // given
        AlfrescoNode alfrescoNode = nodeWithDefaultProperties();

        alfrescoNode.setNodeProperties(Set.of(mockProperty("title")));

        given(alfrescoPropertyMapper.performMapping()).willReturn(Optional.empty());

        // when
        Node node = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_TIMESTAMP), node.properties());
    }

    private NodeProperty mockProperty(String propertyName)
    {
        NodeProperty property = mock();
        PropertyKey propertyKey = mock();

        given(property.getPropertyKey()).willReturn(propertyKey);
        given(propertyKey.getLocalName()).willReturn(propertyName);

        return property;
    }

    private AlfrescoNode nodeWithDefaultProperties()
    {
        AlfrescoNode alfrescoNode = new AlfrescoNode();

        alfrescoNode.setNodeRef(NODE_UUID);
        alfrescoNode.setType(QName.newTransientInstance("", TYPE_FOLDER));
        alfrescoNode.setCreator(CREATOR_ID);
        alfrescoNode.setModifier(MODIFIER_ID);
        alfrescoNode.setAspects(Set.of(QName.newTransientInstance("", ASPECT_TITLED)));
        alfrescoNode.setCreatedAt(CREATED_AT);
        alfrescoNode.setNodeProperties(Set.of());

        return alfrescoNode;
    }
}
