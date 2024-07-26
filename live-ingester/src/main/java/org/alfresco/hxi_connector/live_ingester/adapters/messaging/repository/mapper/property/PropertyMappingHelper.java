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
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.contentMetadataUpdated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta.deleted;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;

import org.alfresco.enterprise.repo.event.v1.model.EnterpriseEventData;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

@NoArgsConstructor(access = PRIVATE)
public class PropertyMappingHelper
{

    public static <T> Stream<PropertyDelta<?>> calculatePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event,
            String propertyKey, Function<NodeResource, T> fieldGetter)
    {
        if (shouldNotUpdateField(event, fieldGetter))
        {
            return Stream.empty();
        }

        return ofNullable(fieldGetter.apply(event.getData().getResource()))
                .stream()
                .filter(Objects::nonNull)
                .map(value -> PropertyDelta.updated(propertyKey, value));
    }

    public static Stream<PropertyDelta<?>> calculateNamePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, NAME_PROPERTY, NodeResource::getName);
    }

    public static Stream<PropertyDelta<?>> calculateContentPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isContentRemoved(event))
        {
            return Stream.of(deleted(CONTENT_PROPERTY));
        }

        if (!shouldUpdateContentField(event))
        {
            return Stream.empty();
        }

        // Note that we cannot include the reference to the rendition until it is generated.
        Optional<ContentInfo> contentInfo = ofNullable(event.getData().getResource().getContent());
        String sourceFileName = event.getData().getResource().getName();
        String sourceMimeType = contentInfo.map(ContentInfo::getMimeType).orElse(null);
        Long sourceSizeInBytes = contentInfo.map(ContentInfo::getSizeInBytes).orElse(null);
        if (sourceMimeType == null && sourceSizeInBytes == null && sourceFileName == null)
        {
            return Stream.empty();
        }
        return Stream.of(contentMetadataUpdated(CONTENT_PROPERTY, sourceMimeType, sourceSizeInBytes, sourceFileName));
    }

    public static Stream<PropertyDelta<?>> calculateTypeDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, TYPE_PROPERTY, NodeResource::getNodeType);
    }

    public static Stream<PropertyDelta<?>> calculateCreatedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getCreatedByUser));
    }

    public static Stream<PropertyDelta<?>> calculateModifiedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, MODIFIED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getModifiedByUser));
    }

    public static Stream<PropertyDelta<?>> calculateAllowAccessDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        EnterpriseEventData enterpriseEventData = (EnterpriseEventData) event.getData();

        return Stream.of(PropertyDelta.updated(ALLOW_ACCESS, enterpriseEventData.getResourceReaderAuthorities()));
    }

    public static Stream<PropertyDelta<?>> calculateDenyAccessDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        EnterpriseEventData enterpriseEventData = (EnterpriseEventData) event.getData();

        return Stream.of(PropertyDelta.updated(DENY_ACCESS, enterpriseEventData.getResourceDeniedAuthorities()));
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

    public static Stream<PropertyDelta<?>> calculateAspectsDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, ASPECT_NAMES_PROPERTY, NodeResource::getAspectNames);
    }

    public static Stream<PropertyDelta<?>> calculateCreatedAtDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_AT_PROPERTY, nodeResource -> toMilliseconds(nodeResource.getCreatedAt()));
    }

    private static Long toMilliseconds(ZonedDateTime time)
    {
        return time == null ? null : time.toInstant().toEpochMilli();
    }

    public static boolean shouldUpdateContentField(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Function<NodeResource, Optional<ContentInfo>> contentInfoGetter = resource -> ofNullable(resource.getContent());
        Function<NodeResource, Serializable> mimeTypeGetter = resource -> contentInfoGetter.apply(resource).map(ContentInfo::getMimeType).orElse(null);
        Function<NodeResource, Serializable> sizeGetter = resource -> contentInfoGetter.apply(resource).map(ContentInfo::getSizeInBytes).orElse(null);
        DataAttributes<NodeResource> data = event.getData();
        boolean contentPresent = (mimeTypeGetter.apply(data.getResource()) != null) || (sizeGetter.apply(data.getResource()) != null);

        if (isEventTypeCreated(event))
        {
            return contentPresent;
        }

        boolean mimeTypeUpdated = fieldValueUpdated(data, mimeTypeGetter);
        boolean sizeUpdated = fieldValueUpdated(data, sizeGetter);
        boolean nameUpdated = fieldValueUpdated(data, NodeResource::getName);

        return mimeTypeUpdated || sizeUpdated || (nameUpdated && contentPresent);
    }

    private static boolean fieldValueUpdated(DataAttributes<NodeResource> data, Function<NodeResource, Serializable> fieldGetter)
    {
        Serializable oldValue = fieldGetter.apply(data.getResourceBefore());
        Serializable newValue = fieldGetter.apply(data.getResource());
        return oldValue != null && !oldValue.equals(newValue);
    }

    public static boolean shouldNotUpdateField(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, ?> fieldGetter)
    {
        return !isEventTypeCreated(event) && isFieldUnchanged(event, fieldGetter);
    }

    public static boolean isFieldUnchanged(RepoEvent<DataAttributes<NodeResource>> event, Function<NodeResource, ?> fieldGetter)
    {
        return Optional.of(event.getData())
                .map(DataAttributes::getResourceBefore)
                .map(fieldGetter::apply)
                .isEmpty();
    }
}
