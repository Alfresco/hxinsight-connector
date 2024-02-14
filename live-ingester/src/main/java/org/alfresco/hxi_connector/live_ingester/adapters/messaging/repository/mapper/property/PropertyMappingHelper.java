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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.property;

import static java.util.Optional.ofNullable;

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.EventUtils.isEventTypeCreated;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta.deleted;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;

@NoArgsConstructor(access = PRIVATE)
public class PropertyMappingHelper
{
    public static final String NAME_PROPERTY_KEY = "cm:name";
    public static final String CONTENT_PROPERTY_KEY = "cm:content";
    public static final String TYPE_PROPERTY = "type";
    public static final String CREATED_BY_PROPERTY = "createdBy";
    public static final String MODIFIED_BY_PROPERTY = "modifiedBy";
    public static final String ASPECT_NAMES_PROPERTY = "aspectsNames";
    public static final String CREATED_AT_PROPERTY = "createdAt";

    public static <T> Stream<CustomPropertyDelta<?>> calculatePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event,
            String propertyKey, Function<NodeResource, T> fieldGetter)
    {
        if (shouldNotUpdateField(event, fieldGetter))
        {
            return Stream.empty();
        }

        return ofNullable(fieldGetter.apply(event.getData().getResource()))
                .stream()
                .filter(Objects::nonNull)
                .map(value -> CustomPropertyDelta.updated(propertyKey, value));
    }

    public static Stream<CustomPropertyDelta<?>> calculateNamePropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, NAME_PROPERTY_KEY, NodeResource::getName);
    }

    public static Stream<CustomPropertyDelta<?>> calculateContentPropertyDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isContentRemoved(event))
        {
            return Stream.of(deleted(CONTENT_PROPERTY_KEY));
        }
        // New or updated content can only be sent once the transformation is complete.
        return Stream.empty();
    }

    public static Stream<CustomPropertyDelta<?>> calculateTypeDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, TYPE_PROPERTY, NodeResource::getNodeType);
    }

    public static Stream<CustomPropertyDelta<?>> calculateCreatedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getCreatedByUser));
    }

    public static Stream<CustomPropertyDelta<?>> calculateModifiedByDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, MODIFIED_BY_PROPERTY, nodeResource -> getUserId(nodeResource, NodeResource::getModifiedByUser));
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

    public static Stream<CustomPropertyDelta<?>> calculateAspectsDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, ASPECT_NAMES_PROPERTY, NodeResource::getAspectNames);
    }

    public static Stream<CustomPropertyDelta<?>> calculateCreatedAtDelta(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return calculatePropertyDelta(event, CREATED_AT_PROPERTY, nodeResource -> toMilliseconds(nodeResource.getCreatedAt()));
    }

    private static Long toMilliseconds(ZonedDateTime time)
    {
        return time == null ? null : time.toInstant().toEpochMilli();
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
