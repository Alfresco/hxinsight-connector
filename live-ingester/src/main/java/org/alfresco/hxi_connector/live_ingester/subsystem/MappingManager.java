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
import org.alfresco.hxi_connector.nucleus_client.client.ClientException;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.fetchUserId;

@Data
@Slf4j
public class MappingManager {
    private final NucleusClient nucleusClient;
    private final String USERNAME = "cm:userName";


    public boolean updateGroupMapping(RepoEvent<DataAttributes<NodeResource>> repoEvent){
        // Need to collect the new groups where these members can be added and then from where this member will be removed
        Set<String>[] groups = getCurrentAndPreviousGroups(repoEvent);
        Set<String> currentGroups = groups[0];
        Set<String> previousGroups = groups[1];

        Set<String> groupsToAdd = groupsToAdd(currentGroups, previousGroups);
        Set<String> groupsToRemove =  groupsToRemove(currentGroups, previousGroups);
        String userId = fetchUserId(repoEvent);


        if(groupsToAdd.equals(groupsToRemove)){
            // No changes in the groups, so we can skip the update
            log.debug("No changes in group membership for user {}, skipping update.",userId );
            return false;
        }

        log.debug("Groups to Add: {}", groupsToAdd);
        log.debug("Groups to Remove: {}", groupsToRemove);

        List<NucleusGroupMemberAssignmentInput> groupMemberAssignmentInputsToAdd = groupsToAdd.stream()
                .map(groupId -> new NucleusGroupMemberAssignmentInput(groupId, userId))
                .toList();

        try {
            nucleusClient.assignGroupMembers(groupMemberAssignmentInputsToAdd);
        }
        catch (ClientException e) {
            log.error("Failed to assign group members for user {}", userId, e);
        }

        // Now we need to remove the user from the groups that are no longer relevant
        groupsToRemove.stream().forEach(groupId -> {
            nucleusClient.removeGroupMembers(groupId,List.of(userId)); // Just one user at a time, so we can wrap the userId in a list
        });
        log.debug("Group Assignments updated successfully for user {}", userId);
        return true;
    }

    public Set<String>[] getCurrentAndPreviousGroups(RepoEvent<DataAttributes<NodeResource>> repoEvent){
        Set<String> currentGroups = new HashSet<>(repoEvent.getData().getResource().getSecondaryParents() == null ? List.of() : repoEvent.getData().getResource().getSecondaryParents());
        Set<String> previousGroups = new HashSet<>(repoEvent.getData().getResourceBefore().getSecondaryParents() == null ? List.of() : repoEvent.getData().getResourceBefore().getSecondaryParents());
        return new Set[]{currentGroups, previousGroups};
    }

    public boolean isGroupMembershipUpdated(RepoEvent<DataAttributes<NodeResource>> repoEvent){
        Set<String>[] groups = getCurrentAndPreviousGroups(repoEvent);
        Set<String> currentGroups = groups[0];
        Set<String> previousGroups = groups[1];
        return !currentGroups.equals(previousGroups);
    }

    private Set<String> groupsToAdd(Set<String> currentGroups, Set<String> previousGroups){
        Set<String> groupsToAdd = new HashSet<>(currentGroups);
        groupsToAdd.removeAll(previousGroups);
        return groupsToAdd;
    }

    private Set<String> groupsToRemove(Set<String> currentGroups, Set<String> previousGroups){
        Set<String> groupsToRemove = new HashSet<>(previousGroups);
        groupsToRemove.removeAll(currentGroups);
        return groupsToRemove;
    }
}
