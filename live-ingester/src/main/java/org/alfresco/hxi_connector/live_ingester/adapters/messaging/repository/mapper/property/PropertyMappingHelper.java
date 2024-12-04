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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property;

import static java.util.Optional.ofNullable;

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ALLOW_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.DENY_ACCESS;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.deleted;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import lombok.NoArgsConstructor;

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
        return calculatePropertyDelta(event, CREATED_AT_PROPERTY, nodeResource -> toMilliseconds(nodeResource.getCreatedAt()));
    }

    public static Optional<PropertyDelta<?>> calculateModifiedAtDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, MODIFIED_AT_PROPERTY, nodeResource -> toMilliseconds(nodeResource.getCreatedAt()));
    }

    public static Optional<PropertyDelta<?>> calculateAllowAccessDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        EventData EventData = (EventData) event.getData();

        if (EventData.getResourceReaderAuthorities() == null)
        {
            return Optional.of(PropertyDelta.updated(ALLOW_ACCESS, Set.of(GROUP_EVERYONE)));
        }

        return Optional.of(PropertyDelta.updated(ALLOW_ACCESS, EventData.getResourceReaderAuthorities()));
    }

    public static Optional<PropertyDelta<?>> calculateDenyAccessDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        EventData EventData = (EventData) event.getData();

        if (EventData.getResourceDeniedAuthorities() == null)
        {
            return Optional.of(PropertyDelta.updated(DENY_ACCESS, Set.of()));
        }

        return Optional.of(PropertyDelta.updated(DENY_ACCESS, EventData.getResourceDeniedAuthorities()));
    }

    private static Long toMilliseconds(ZonedDateTime time)
    {
        return time == null ? null : time.toInstant().toEpochMilli();
    }

    public static Optional<PropertyDelta<?>> calculateContentPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isContentRemoved(event))
        {
            return Optional.of(deleted(CONTENT_PROPERTY));
        }

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

    private static boolean isContentRemoved(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Optional<Long> oldSizeOptional = sizeOfContent(event.getData().getResourceBefore());
        if (oldSizeOptional.isEmpty())
        {
            // Content wasn't updated.
            return false;
        }
        long oldSize = oldSizeOptional.get();
        long newSize = sizeOfContent(event.getData().getResource()).orElse(0L);
        return newSize == 0 && oldSize != 0;
    }

    /**
     * Find the specified content size in bytes, or an empty Optional if it is not mentioned.
     */
    private static Optional<Long> sizeOfContent(NodeResource nodeResource)
    {
        return ofNullable(nodeResource)
                .map(NodeResource::getContent)
                .map(ContentInfo::getSizeInBytes);
    }

    private static String getUserId(NodeResource node, Function<NodeResource, UserInfo> userInfoGetter)
    {
        return ofNullable(node)
                .map(userInfoGetter)
                .map(UserInfo::getId)
                .orElse(null);
    }
}
