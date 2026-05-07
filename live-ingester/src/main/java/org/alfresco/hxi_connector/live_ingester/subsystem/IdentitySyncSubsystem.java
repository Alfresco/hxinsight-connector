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


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

import java.util.Arrays;

import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.*;

// Main class to sync Identity
@Data
@Slf4j
public class IdentitySyncSubsystem {

    private final UserManager userManager;
    private final GroupManager groupManager;
    // user types we are considering

    /**
     * Situations ->
     * 1. USER Created
     * 2. USER Deleted
     * 3. USER Added to a Group [membership updation]
     * 4. USER Removed from a Group [membership updation]
     * 5. Group Created
     * @param event
     */
    public void handleIAMEvents(RepoEvent<DataAttributes<NodeResource>> event){
        // Point of contract from where this event will be handled.
        NodeResource nodeResource = event.getData().getResource();
        log.debug("Received IAM event: {}", event.getId());
         //If the extensionAttribute toPath doesn't have value then I can make sure this node is deleted
        if(Arrays.stream(USER_TYPES).anyMatch(type -> type.equals(nodeResource.getNodeType()))){handleUserEvents(event);}
        if(nodeResource.getNodeType().equals(GROUP_TYPE)){handleGroupEvents(event);}
    }

    private void handleUserEvents(RepoEvent<DataAttributes<NodeResource>> event){
        // Logic to handle user management
        if(event.getData().getResource() == null){
            throw new IamSyncException("NodeResource is null in the event data, cannot process user event");
        }
        NodeResource nodeResource = event.getData().getResource();
        if(event.getType().equals(EVENT_CREATED_TYPE)){
            // see if mapping exists for this mail
            // TODO: We Don't have any provision for the get user by email so need to skip this
            log.debug("Handling user creation event for user: {}", fetchUserId(event));
            userManager.createOrMapUser(nodeResource.getProperties().getOrDefault(EMAIL_PROPERTY,"").toString(), fetchUserId(event));
        }
        else if(Arrays.stream(UPDATE_OR_DELETE).anyMatch(type -> type.equals(event.getType()))) {
            log.debug("Handling user update or delete event for user: {}", fetchUserId(event));
            userManager.handleUpdateOrDelete(event);
        }
    }


    private void handleGroupEvents(RepoEvent<DataAttributes<NodeResource>> event){
        // Logic to handle group events
            NodeResource nodeResource = event.getData().getResource();
            if(event.getType().equals(EVENT_CREATED_TYPE)){
                log.debug("Handling group creation event for group: {}",event.getData().getResource().getId());
                groupManager.createGroup(nodeResource);
            }
            else if(event.getType().equals(EVENT_UPDATED_TYPE)) {
                log.debug("Handling group update event for group: {}", event.getData().getResource().getId());
                groupManager.handleDeletionOrUpdation(event);
            }
    }

}
