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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util;

import static java.util.Optional.ofNullable;

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings({"PMD.PrematureDeclaration", "PMD.SimplifyBooleanReturns"})
public final class EventUtils
{
    public static final String PREDICTION_NODE_TYPE = "hxi:prediction";
    public static final String PREDICTION_APPLIED_ASPECT = "hxi:predictionApplied";
    public static final String PREDICTION_TIME_PROPERTY = "hxi:latestPredictionDateTime";
    public static final String PREDICTION_VALUE_PROPERTY = "hxi:predictionValue";
    public static final String PREDICTION_REVIEW_STATUS_PROPERTY = "hxi:reviewStatus";
    public static final String PREDICTION_UNREVIEWED = "UNREVIEWED";
    public static final String PREDICTION_CONFIRMED = "CONFIRMED";

    public static boolean isEventTypeCreated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return NODE_CREATED.getType().equals(event.getType());
    }

    public static boolean isEventTypeUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return NODE_UPDATED.getType().equals(event.getType());
    }

    public static boolean isEventTypeDeleted(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return NODE_DELETED.getType().equals(event.getType());
    }

    public static EventType getEventType(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (isEventTypeCreated(event))
        {
            return CREATE;
        }
        if (isEventTypeUpdated(event))
        {
            return UPDATE;
        }
        if (isEventTypeDeleted(event))
        {
            return DELETE;
        }
        throw new LiveIngesterRuntimeException("Unsupported Repo event type " + event.getType());
    }

    public static boolean isPredictionNodeEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return PREDICTION_NODE_TYPE.equals(event.getData().getResource().getNodeType());
    }

    public static boolean isPredictionApplyEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Set<String> aspects = ofNullable(event.getData().getResource().getAspectNames()).orElse(Collections.emptySet());
        if (aspects.contains(PREDICTION_APPLIED_ASPECT))
        {
            String actualPredictionTime = (String) event.getData().getResource().getProperties().get(PREDICTION_TIME_PROPERTY);

            Map<String, Serializable> beforeProperties = ofNullable(event.getData().getResourceBefore())
                    .map(NodeResource::getProperties)
                    .orElse(null);

            if (beforeProperties != null && beforeProperties.containsKey(PREDICTION_TIME_PROPERTY))
            {
                String beforePredictionTime = (String) beforeProperties.get(PREDICTION_TIME_PROPERTY);
                return !Objects.equals(actualPredictionTime, beforePredictionTime);
            }
        }

        return false;
    }

    /**
     * We can determine if there is new content that needs processing by looking at the resourceBefore.content and resource.content fields.
     * <p>
     * For newly created nodes we have:
     * <ul>
     * <li>null -> zero bytes: No content
     * <li>null -> non-zero bytes: New content
     * </ul>
     * For updated nodes we have:
     * <ul>
     * <li>null -> zero bytes: No content
     * <li>null -> non-zero bytes: No change to content
     * <li>non-zero bytes -> zero bytes: Content deleted
     * <li>non-zero bytes -> non-zero bytes : Content updated
     * <li>zero bytes -> non-zero bytes : Content added (no content on node before)
     * </ul>
     */
    public static boolean wasContentChanged(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Optional<ContentInfo> latestContentInfo = ofNullable(event.getData().getResource()).map(NodeResource::getContent);
        // If there's no content info in the current resource then the node cannot contain content.
        if (latestContentInfo.isEmpty())
        {
            return false;
        }
        boolean latestContentPresent = !latestContentInfo.get().getSizeInBytes().equals(0L);
        // If there is content on a new node then we should process it.
        if (isEventTypeCreated(event))
        {
            return latestContentPresent;
        }
        else if (isEventTypeUpdated(event))
        {
            Optional<ContentInfo> oldContentInfo = ofNullable(event.getData().getResourceBefore()).map(NodeResource::getContent);
            // We only need to process the content if it was mentioned in the resourceBefore _and_ is non-zero now.
            return oldContentInfo.isPresent() && latestContentPresent;
        }
        // For events other than create or update then we do not need to process the content.
        return false;
    }

    public static boolean wasPredictionConfirmed(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Map<String, Serializable> oldProperties = event.getData().getResourceBefore().getProperties();
        Map<String, Serializable> newProperties = event.getData().getResource().getProperties();

        if (oldProperties == null || newProperties == null)
        {
            return false;
        }

        Serializable previousStatus = oldProperties.get(PREDICTION_REVIEW_STATUS_PROPERTY);
        Serializable actualStatus = newProperties.get(PREDICTION_REVIEW_STATUS_PROPERTY);

        return Objects.equals(previousStatus, PREDICTION_UNREVIEWED) &&
                Objects.equals(actualStatus, PREDICTION_CONFIRMED);
    }

    public static String getNodeParent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return event.getData().getResource()
                .getPrimaryHierarchy()
                .stream()
                .findFirst()
                .orElseThrow(() -> new LiveIngesterRuntimeException("Node %s has no parent".formatted(event.getData().getResource())));
    }

    public static Set<PropertyDelta<?>> getPredictionNodeProperties(RepoEvent<DataAttributes<NodeResource>> event)
    {
        String predictedPropertyName = event.getData().getResource().getPrimaryAssocQName();
        Serializable predictionValue = event.getData().getResource().getProperties().get(PREDICTION_VALUE_PROPERTY);

        return Set.of(PropertyDelta.updated(predictedPropertyName, predictionValue));
    }
}
