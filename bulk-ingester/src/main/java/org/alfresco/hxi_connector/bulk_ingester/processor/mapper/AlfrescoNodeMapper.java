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

import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.ContentInfo;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.Node;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlfrescoNodeMapper
{
    public static final String CONTENT_PROPERTY = "content";
    private static final Set<String> PREDEFINED_PROPERTIES = Set.of(CONTENT_PROPERTY);

    private final AlfrescoPropertyMapperFactory propertyMapperFactory;

    public AlfrescoNodeMapper()
    {
        propertyMapperFactory = AlfrescoPropertyMapper::new;
    }

    public Node map(AlfrescoNode alfrescoNode)
    {
        String nodeId = alfrescoNode.getNodeRef();
        String type = alfrescoNode.getType().getLocalName();
        String creatorId = alfrescoNode.getCreator();
        String modifierId = alfrescoNode.getModifier();
        Set<String> aspectNames = alfrescoNode.getAspects().stream().map(QName::getLocalName).collect(Collectors.toSet());
        long createdAt = getCreatedAt(alfrescoNode);
        Map<String, Serializable> allProperties = calculateAllProperties(alfrescoNode);

        ContentInfo content = (ContentInfo) allProperties.get(CONTENT_PROPERTY);

        Map<String, Serializable> customProperties = getCustomProperties(allProperties);

        return new Node(
                nodeId,
                type,
                creatorId,
                modifierId,
                aspectNames,
                content,
                createdAt,
                customProperties);
    }

    private long getCreatedAt(AlfrescoNode alfrescoNode)
    {
        return Optional.ofNullable(alfrescoNode.getCreatedAt())
                .map(ZonedDateTime::toInstant)
                .map(Instant::getEpochSecond)
                .orElse(0L);
    }

    private Map<String, Serializable> calculateAllProperties(AlfrescoNode alfrescoNode)
    {
        return alfrescoNode.getNodeProperties()
                .stream()
                .filter(Objects::nonNull)
                .map(property -> property.getPropertyKey().getLocalName())
                .distinct()
                .map(propertyName -> propertyMapperFactory.create(alfrescoNode, propertyName).performMapping())
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Serializable> getCustomProperties(Map<String, Serializable> allProperties)
    {
        return allProperties.entrySet()
                .stream()
                .filter(entry -> !PREDEFINED_PROPERTIES.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
