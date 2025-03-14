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

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ALLOW_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.DENY_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.elasticsearch.db.connector.model.AccessControlEntry;
import org.alfresco.elasticsearch.db.connector.model.AccessControlEntryKey;
import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlfrescoNodeMapper
{
    private static final Set<String> PREDEFINED_PROPERTIES = Set.of(CONTENT_PROPERTY);

    private final AlfrescoPropertyMapperFactory propertyMapperFactory;
    private final NamespacePrefixMapper namespacePrefixMapper;
    private final TimeProvider timeProvider;

    @SuppressWarnings("PMD.LooseCoupling") // HashSet implements both Set and Serializable.
    public IngestEvent map(AlfrescoNode alfrescoNode)
    {
        String nodeId = alfrescoNode.getNodeRef();
        String type = namespacePrefixMapper.toPrefixedName(alfrescoNode.getType());
        String creatorId = alfrescoNode.getCreator();
        String modifierId = alfrescoNode.getModifier();
        HashSet<String> aspectNames = alfrescoNode.getAspects().stream().map(namespacePrefixMapper::toPrefixedName).collect(Collectors.toCollection(HashSet::new));
        String createdAt = getCreatedAt(alfrescoNode);
        Map<String, Serializable> allProperties = calculateAllProperties(alfrescoNode);

        allProperties.put(TYPE_PROPERTY, type);
        allProperties.put(CREATED_BY_PROPERTY, creatorId);
        allProperties.put(MODIFIED_BY_PROPERTY, modifierId);
        if (!aspectNames.isEmpty())
        {
            allProperties.put(ASPECT_NAMES_PROPERTY, aspectNames);
        }
        allProperties.put(CREATED_AT_PROPERTY, createdAt);

        Set<String> allowAccess = (Set<String>) getResourceReaderAuthorities(alfrescoNode);
        if (!allowAccess.isEmpty())
        {
            allProperties.put(ALLOW_ACCESS, (Serializable) allowAccess);
        }

        Set<String> denyAccess = (Set<String>) getResourceDeniedAuthorities(alfrescoNode);
        if (!denyAccess.isEmpty())
        {
            allProperties.put(DENY_ACCESS, (Serializable) denyAccess);
        }
        IngestEvent.ContentInfo content = (IngestEvent.ContentInfo) allProperties.get(CONTENT_PROPERTY);

        Map<String, Serializable> properties = getProperties(allProperties);

        return new IngestEvent(
                nodeId,
                content,
                properties,
                timeProvider.getCurrentTimestamp());
    }

    private String getCreatedAt(AlfrescoNode alfrescoNode)
    {
        return ofNullable(alfrescoNode.getCreatedAt())
                .map(ZonedDateTime::toInstant)
                .map(DateTimeFormatter.ISO_INSTANT::format)
                .orElse(null);
    }

    private Map<String, Serializable> calculateAllProperties(AlfrescoNode alfrescoNode)
    {
        return alfrescoNode.getNodeProperties()
                .stream()
                .filter(Objects::nonNull)
                .map(property -> namespacePrefixMapper.toPrefixedName(property.getPropertyKey()))
                .distinct()
                .map(propertyName -> propertyMapperFactory.create(alfrescoNode, propertyName).performMapping())
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Serializable> getProperties(Map<String, Serializable> allProperties)
    {
        return allProperties.entrySet()
                .stream()
                .filter(property -> Objects.nonNull(property.getValue()))
                .filter(property -> !PREDEFINED_PROPERTIES.contains(property.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Serializable getResourceReaderAuthorities(AlfrescoNode node)
    {
        return (Serializable) ofNullable(node.getAccessControlList())
                .stream()
                .flatMap(Collection::stream)
                .filter(AccessControlEntry::getAllowed)
                .map(AccessControlEntry::getAccessControlEntryKey)
                .map(AccessControlEntryKey::getAuthority)
                .collect(Collectors.toSet());
    }

    private Serializable getResourceDeniedAuthorities(AlfrescoNode node)
    {
        return (Serializable) ofNullable(node.getAccessControlList())
                .stream()
                .flatMap(Collection::stream)
                .filter(not(AccessControlEntry::getAllowed))
                .map(AccessControlEntry::getAccessControlEntryKey)
                .map(AccessControlEntryKey::getAuthority)
                .collect(Collectors.toSet());
    }
}
