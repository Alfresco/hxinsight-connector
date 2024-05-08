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

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@NoArgsConstructor(access = PRIVATE)
public final class EventUtils
{
    public static final String PREDICTION_NODE_TYPE = "hxi:prediction";
    public static final String PREDICTION_APPLIED_ASPECT = "hxi:predictionApplied";
    public static final String PREDICTION_TIME_PROPERTY = "hxi:latestPredictionDateTime";

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

    public static boolean isNotPredictionNodeEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return !PREDICTION_NODE_TYPE.equals(event.getData().getResource().getNodeType());
    }

    public static boolean isNotPredictionApplyEvent(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Set<String> aspects = Optional.ofNullable(event.getData().getResource().getAspectNames()).orElse(Collections.emptySet());
        if (aspects.contains(PREDICTION_APPLIED_ASPECT))
        {
            String actualPredictionTime = (String) event.getData().getResource().getProperties().get(PREDICTION_TIME_PROPERTY);
            String beforePredictionTime = (String) Optional.ofNullable(event.getData().getResourceBefore())
                    .map(NodeResource::getProperties)
                    .map(properties -> properties.get(PREDICTION_TIME_PROPERTY))
                    .orElse(null);

            return actualPredictionTime != null && actualPredictionTime.equals(beforePredictionTime)
                    || actualPredictionTime == null && beforePredictionTime == null;
        }

        return true;
    }
}
