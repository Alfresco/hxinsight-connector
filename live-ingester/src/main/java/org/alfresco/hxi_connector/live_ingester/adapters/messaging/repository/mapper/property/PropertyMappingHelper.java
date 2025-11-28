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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property;

import static java.util.Optional.ofNullable;

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ANCESTORS_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.PERMISSIONS_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.permissionsMetadataUpdated;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

@NoArgsConstructor(access = PRIVATE)
public class PropertyMappingHelper
{
    private static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static Optional<PropertyDelta<?>> calculatePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event,
            String propertyKey, Function<NodeResource, ?> fieldGetter)
    {
        return ofNullable(event.getData().getResource())
                .map(fieldGetter)
                .map(propertyValue -> PropertyDelta.updated(propertyKey, propertyValue));
    }

    public static Optional<PropertyDelta<?>> calculateNamePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, NAME_PROPERTY, NodeResource::getName);
    }

    public static Optional<PropertyDelta<?>> calculateTypeDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, TYPE_PROPERTY, NodeResource::getNodeType);
    }

    public static Optional<PropertyDelta<?>> calculateCreatedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getCreatedByUser));
    }

    public static Optional<PropertyDelta<?>> calculateModifiedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, MODIFIED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getModifiedByUser));
    }

    public static Optional<PropertyDelta<?>> calculateAspectsDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, ASPECT_NAMES_PROPERTY, NodeResource::getAspectNames);
    }

    public static Optional<PropertyDelta<?>> calculateCreatedAtDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_AT_PROPERTY, nodeResource -> formatDateTime(nodeResource.getCreatedAt()));
    }

    public static Optional<PropertyDelta<?>> calculateModifiedAtDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, MODIFIED_AT_PROPERTY, nodeResource -> formatDateTime(nodeResource.getModifiedAt()));
    }

    public static Optional<PropertyDelta<?>> calculateAncestorsPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        List<String> primaryHierarchy = event.getData().getResource().getPrimaryHierarchy();

        if (primaryHierarchy == null || primaryHierarchy.isEmpty())
        {
            return Optional.empty();
        }
        String primaryParentId = primaryHierarchy.get(0);
        List<String> reversedHierarchy = new ArrayList<>(primaryHierarchy);
        Collections.reverse(reversedHierarchy);
        Map<String, Serializable> ancestorsData = new HashMap<>();
        ancestorsData.put("primaryParentId", primaryParentId);
        ancestorsData.put("primaryAncestorIds", (Serializable) reversedHierarchy);

        return Optional.of(PropertyDelta.updated(ANCESTORS_PROPERTY, ancestorsData));
    }

    public static Optional<PropertyDelta<?>> calculatePermissionsPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event, AuthorityTypeResolver authorityTypeResolver)
    {
        EventData eventData = (EventData) event.getData();
        if ((eventData.getResourceReaderAuthorities() == null || eventData.getResourceReaderAuthorities().isEmpty())
                && (eventData.getResourceDeniedAuthorities() == null || eventData.getResourceDeniedAuthorities().isEmpty()))
        {
            return Optional.empty();
        }
        // We store permissions as a map with two entries: allowAccess and denyAccess
        // This way we can update both properties in a single PropertyDelta
        // If either of the lists is null, we treat it as "everyone" for allowAccess and empty for denyAccess
        Set<String> allowAccess = eventData.getResourceReaderAuthorities() == null
                ? Set.of(GROUP_EVERYONE)
                : eventData.getResourceReaderAuthorities();
        Set<String> denyAccess = eventData.getResourceDeniedAuthorities() == null
                ? Collections.emptySet()
                : eventData.getResourceDeniedAuthorities();

        List<AuthorityInfo> allowAccessWithTypes = convertToAuthorityInfoList(allowAccess, authorityTypeResolver);
        List<AuthorityInfo> denyAccessWithTypes = convertToAuthorityInfoList(denyAccess, authorityTypeResolver);

        return Optional.of(permissionsMetadataUpdated(PERMISSIONS_PROPERTY, allowAccessWithTypes, denyAccessWithTypes));
    }

    public static List<AuthorityInfo> convertToAuthorityInfoList(Collection<String> authorities, AuthorityTypeResolver authorityTypeResolver)
    {
        if (authorities == null || authorities.isEmpty())
        {
            return Collections.emptyList();
        }
        return authorities.stream()
                .map(authorityId -> new AuthorityInfo(authorityId, authorityTypeResolver.resolveAuthorityType(authorityId)))
                .collect(Collectors.toList());
    }

    private static String formatDateTime(ZonedDateTime time)
    {
        return time == null ? null : DATE_TIME_FORMATTER.format(time);
    }

    public static Optional<PropertyDelta<?>> calculateContentPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        // Note that we cannot include the reference to the rendition until it is generated.
        Optional<ContentInfo> contentInfo = ofNullable(event.getData().getResource().getContent());
        String sourceFileName = event.getData().getResource().getName();
        String sourceMimeType = contentInfo.map(ContentInfo::getMimeType).orElse(null);
        Long sourceSizeInBytes = contentInfo.map(ContentInfo::getSizeInBytes).orElse(null);
        if (sourceMimeType == null && sourceSizeInBytes == null)
        {
            return Optional.empty();
        }
        return Optional.of(contentMetadataUpdated(CONTENT_PROPERTY, sourceMimeType, sourceSizeInBytes, sourceFileName));
    }

    private static String getUserId(NodeResource node, Function<NodeResource, UserInfo> userInfoGetter)
    {
        return ofNullable(node)
                .map(userInfoGetter)
                .map(UserInfo::getId)
                .orElse(null);
    }
}
