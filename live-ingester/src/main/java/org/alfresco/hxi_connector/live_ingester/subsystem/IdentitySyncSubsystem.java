/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.subsystem;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.EMAIL_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.EVENT_CREATED_TYPE;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.EVENT_DELETE_TYPE;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.EVENT_UPDATED_TYPE;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.GROUP_TYPE;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.PERSON_TYPE;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.USER_TYPES;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.fetchUserId;

/**
 * Entry point that dispatches Alfresco repo events to user / group identity-sync handlers.
 * Handles the following situations:
 * <ul>
 *   <li>User created / deleted</li>
 *   <li>User added to / removed from a group (membership change)</li>
 *   <li>Group created / updated / deleted</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class IdentitySyncSubsystem
{
    private final UserManager userManager;
    private final GroupManager groupManager;

    public void handleIAMEvents(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (event == null || event.getData() == null)
        {
            log.warn("Received malformed IAM event, skipping");
            return;
        }
        log.debug("Received IAM event: {}", event.getId());

        // For Deleted events the live resource is null - fall back to resourceBefore for type detection.
        NodeResource typeSource = firstNonNull(event.getData().getResource(), event.getData().getResourceBefore());
        if (typeSource == null || typeSource.getNodeType() == null)
        {
            log.warn("IAM event {} has no node type information, skipping", event.getId());
            return;
        }

        String nodeType = typeSource.getNodeType();
        if (USER_TYPES.contains(nodeType))
        {
            handleUserEvents(event);
        }
        else if (GROUP_TYPE.equals(nodeType))
        {
            handleGroupEvents(event);
        }
        else
        {
            log.debug("Ignoring IAM event {} of unsupported node type '{}'", event.getId(), nodeType);
        }
    }

    private void handleUserEvents(RepoEvent<DataAttributes<NodeResource>> event)
    {
        String type = event.getType();
        NodeResource resource = firstNonNull(event.getData().getResource(), event.getData().getResourceBefore());
        if (resource == null)
        {
            throw new IamSyncException("NodeResource is null in user event " + event.getId());
        }

        if (EVENT_CREATED_TYPE.equals(type))
        {
            String userId = fetchUserId(event);
            String email = readProperty(resource, EMAIL_PROPERTY).orElse("");
            log.debug("Handling user creation event for user: {}", userId);
            userManager.createOrMapUser(email, userId);
        }
        else if (EVENT_DELETE_TYPE.equals(type))
        {
            log.debug("Handling user delete event for resource: {}", resource.getId());
            userManager.handleUpdateOrDelete(event);
        }
        else if (EVENT_UPDATED_TYPE.equals(type))
        {
            // Only person events carry group membership changes we care about.
            if (!PERSON_TYPE.equals(resource.getNodeType()))
            {
                log.debug("Ignoring user update event for node type '{}'", resource.getNodeType());
                return;
            }
            log.debug("Handling user update event for user: {}", fetchUserId(event));
            userManager.handleUpdateOrDelete(event);
        }
        else
        {
            log.debug("Ignoring user event of type '{}'", type);
        }
    }

    private void handleGroupEvents(RepoEvent<DataAttributes<NodeResource>> event)
    {
        String type = event.getType();
        NodeResource resource = firstNonNull(event.getData().getResource(), event.getData().getResourceBefore());
        if (resource == null)
        {
            throw new IamSyncException("NodeResource is null in group event " + event.getId());
        }

        if (EVENT_CREATED_TYPE.equals(type))
        {
            log.debug("Handling group creation event for group: {}", resource.getId());
            groupManager.createGroup(resource);
        }
        else if (EVENT_UPDATED_TYPE.equals(type))
        {
            log.debug("Handling group update event for group: {}", resource.getId());
            groupManager.handleDeletionOrUpdation(event);
        }
        else if (EVENT_DELETE_TYPE.equals(type))
        {
            log.debug("Handling group delete event for group: {}", resource.getId());
            groupManager.deleteGroup(resource.getId());
        }
        else
        {
            log.debug("Ignoring group event of type '{}'", type);
        }
    }

    private static Optional<String> readProperty(NodeResource resource, String key)
    {
        Map<String, ?> props = resource.getProperties();
        if (props == null)
        {
            return Optional.empty();
        }
        return Optional.ofNullable(props.get(key)).map(Object::toString);
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values)
    {
        if (values == null)
        {
            return null;
        }
        for (T v : values)
        {
            if (Objects.nonNull(v))
            {
                return v;
            }
        }
        return null;
    }
}
