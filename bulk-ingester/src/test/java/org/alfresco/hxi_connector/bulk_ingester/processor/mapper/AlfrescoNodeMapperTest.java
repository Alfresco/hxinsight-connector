/*
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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ALLOW_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.DENY_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.elasticsearch.db.connector.model.AccessControlEntry;
import org.alfresco.elasticsearch.db.connector.model.AccessControlEntryKey;
import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.NodeProperty;
import org.alfresco.elasticsearch.db.connector.model.PropertyKey;
import org.alfresco.elasticsearch.db.connector.model.QName;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

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
    private static final String CREATED_AT_ISO = "2024-01-31T10:15:30Z";
    private static final ZonedDateTime MODIFIED_AT = ZonedDateTime.parse("2025-01-31T10:15:30+00:00");
    private static final String MODIFIED_AT_ISO = "2025-01-31T10:15:30Z";

    private static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    private static final String BOB = "bob";
    private static final long TIMESTAMP = 1_308_061_016L;

    private final AlfrescoPropertyMapper alfrescoPropertyMapper = mock();
    private final NamespacePrefixMapper namespacePrefixMapper = new TestNamespaceToPrefixMapper(TEST_PREFIX);
    private final AlfrescoNodeMapper alfrescoNodeMapper = new AlfrescoNodeMapper((node, propertyName) -> alfrescoPropertyMapper, namespacePrefixMapper, () -> TIMESTAMP);

    @Test
    void shouldAddTimestampToEvent()
    {
        // given
        AlfrescoNode alfrescoNode = new AlfrescoNode();
        alfrescoNode.setType(QName.newTransientInstance("", TYPE_FOLDER)); // required to avoid NPE

        // when
        IngestEvent ingestEvent = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(TIMESTAMP, ingestEvent.timestamp());
    }

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
        alfrescoNode.setModifiedAt(MODIFIED_AT);
        alfrescoNode.setNodeProperties(Set.of());
        alfrescoNode.setAccessControlList(
                Set.of(
                        createAccessControlEntry(true, GROUP_EVERYONE),
                        createAccessControlEntry(false, BOB)));

        // when
        IngestEvent ingestEvent = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(NODE_UUID, ingestEvent.nodeId());
        assertNull(ingestEvent.contentInfo());
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                ASPECT_NAMES_PROPERTY, Set.of(PREFIXED_ASPECT_TITLED),
                CREATED_AT_PROPERTY, CREATED_AT_ISO,
                MODIFIED_AT_PROPERTY, MODIFIED_AT_ISO,
                ALLOW_ACCESS, Set.of(GROUP_EVERYONE),
                DENY_ACCESS, Set.of(BOB)), ingestEvent.properties());
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
        IngestEvent ingestEvent = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(Map.of(namePropertyKey, namePropertyValue,
                TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_ISO,
                MODIFIED_AT_PROPERTY, MODIFIED_AT_ISO), ingestEvent.properties());
    }

    @Test
    void shouldMapNodeWithContent()
    {
        // given
        AlfrescoNode alfrescoNode = nodeWithDefaultProperties();

        String contentPropertyKey = "cm:content";
        IngestEvent.ContentInfo contentInfo = mock();

        alfrescoNode.setNodeProperties(Set.of(mockProperty(contentPropertyKey)));

        given(alfrescoPropertyMapper.performMapping()).willReturn(
                Optional.of(Map.entry(
                        contentPropertyKey, contentInfo)));
        // when
        IngestEvent ingestEvent = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(contentInfo, ingestEvent.contentInfo());
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_ISO,
                MODIFIED_AT_PROPERTY, MODIFIED_AT_ISO), ingestEvent.properties());
    }

    @Test
    void shouldSkipEmptyProperties()
    {
        // given
        AlfrescoNode alfrescoNode = nodeWithDefaultProperties();

        alfrescoNode.setNodeProperties(Set.of(mockProperty("title")));

        given(alfrescoPropertyMapper.performMapping()).willReturn(Optional.empty());

        // when
        IngestEvent ingestEvent = alfrescoNodeMapper.map(alfrescoNode);

        // then
        assertEquals(Map.of(TYPE_PROPERTY, PREFIXED_TYPE_FOLDER,
                CREATED_BY_PROPERTY, CREATOR_ID,
                MODIFIED_BY_PROPERTY, MODIFIER_ID,
                CREATED_AT_PROPERTY, CREATED_AT_ISO,
                MODIFIED_AT_PROPERTY, MODIFIED_AT_ISO), ingestEvent.properties());
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
        alfrescoNode.setCreatedAt(CREATED_AT);
        alfrescoNode.setModifiedAt(MODIFIED_AT);
        alfrescoNode.setNodeProperties(Set.of());
        alfrescoNode.setAccessControlList(Set.of());

        return alfrescoNode;
    }

    private AccessControlEntry createAccessControlEntry(boolean allowed, String authority)
    {
        AccessControlEntryKey accessControlEntryKey = new AccessControlEntryKey();
        accessControlEntryKey.setNodeId(123L);
        accessControlEntryKey.setAuthority(authority);

        AccessControlEntry accessControlEntry = new AccessControlEntry();
        accessControlEntry.setAllowed(allowed);
        accessControlEntry.setAccessControlEntryKey(accessControlEntryKey);

        return accessControlEntry;
    }
}
