package org.alfresco.hxi_connector.live_ingester.messaging.in.utils;

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import lombok.NoArgsConstructor;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@NoArgsConstructor(access = PRIVATE)
public final class EventUtils
{

    public static boolean isEventTypeCreated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return NODE_CREATED.getType().equals(event.getType());
    }

    public static boolean isEventTypeUpdated(RepoEvent<DataAttributes<NodeResource>> event)
    {
        return NODE_UPDATED.getType().equals(event.getType());
    }
}
