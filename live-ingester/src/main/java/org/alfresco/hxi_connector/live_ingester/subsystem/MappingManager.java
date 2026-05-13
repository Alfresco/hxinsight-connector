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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.nucleus_client.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_client.client.ClientException;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.GROUP_PREFIX;
import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.fetchUserId;

/**
 * Reconciles a user's group memberships in HxIAM (Nucleus) against the
 * {@code secondaryParents} from a repo event.
 */
@Slf4j
@RequiredArgsConstructor
public class MappingManager
{
    private final NucleusClient nucleusClient;
    private final AlfrescoClient alfrescoClient;

    /**
     * Computes the diff between the user's previous and current secondary parents
     * and applies the corresponding add/remove operations in Nucleus.
     *
     * @return {@code true} if any change was applied (or attempted); {@code false} when nothing changed.
     */
    public boolean updateGroupMapping(RepoEvent<DataAttributes<NodeResource>> repoEvent)
    {
        GroupDiff diff = computeGroupDiff(repoEvent);

        if (diff.isEmpty())
        {
            log.debug("No group membership changes for user {}, skipping update.", safeUserId(repoEvent));
            return false;
        }

        String userId = fetchUserId(repoEvent);
        log.debug("User {} - groups to add: {}, groups to remove: {}", userId, diff.toAdd(), diff.toRemove());

        boolean addOk = assignGroups(userId, diff.toAdd());
        boolean removeOk = removeFromGroups(userId, diff.toRemove());

        if (addOk && removeOk)
        {
            log.debug("Group assignments updated successfully for user {}", userId);
        }
        else
        {
            log.warn("Group assignments for user {} completed with errors (add ok={}, remove ok={})", userId, addOk, removeOk);
        }
        return true;
    }

    /**
     * @return {@code true} if {@code secondaryParents} differs between resource and resourceBefore.
     */
    public boolean isGroupMembershipUpdated(RepoEvent<DataAttributes<NodeResource>> repoEvent)
    {
        return !computeGroupDiff(repoEvent).isEmpty();
    }

    GroupDiff computeGroupDiff(RepoEvent<DataAttributes<NodeResource>> repoEvent)
    {
        Set<String> current = secondaryParentsOf(Optional.ofNullable(repoEvent.getData().getResource()));
        Set<String> previous = secondaryParentsOf(Optional.ofNullable(repoEvent.getData().getResourceBefore()));

        Set<String> toAdd = new HashSet<>(current);
        toAdd.removeAll(previous);

        Set<String> toRemove = new HashSet<>(previous);
        toRemove.removeAll(current);

        return new GroupDiff(toAdd, toRemove);
    }

    private static Set<String> secondaryParentsOf(Optional<NodeResource> resource)
    {
        return resource
                .map(NodeResource::getSecondaryParents)
                .<Set<String>>map(HashSet::new)
                .orElseGet(HashSet::new);
    }

    private boolean assignGroups(String userId, Set<String> groupsToAdd)
    {
        if (groupsToAdd.isEmpty())
        {
            return true;
        }
        List<NucleusGroupMemberAssignmentInput> assignments = groupsToAdd.stream()
                .map(groupId -> GROUP_PREFIX + alfrescoClient.getGroupNameByNodeId(groupId)
                         .orElseThrow(() -> new RuntimeException("Failed to fetch group name for groupId " + groupId)))
                .map(groupId -> new NucleusGroupMemberAssignmentInput(groupId, userId))
                .toList();
        try
        {
            nucleusClient.assignGroupMembers(assignments);
            return true;
        }
        catch (ClientException e)
        {
            log.error("Failed to assign user {} to groups {}", userId, groupsToAdd, e);
            return false;
        }
    }

    private boolean removeFromGroups(String userId, Set<String> groupsToRemove)
    {
        boolean allOk = true;
        for (String groupId : groupsToRemove)
        {
            try
            {
                nucleusClient.removeGroupMembers(GROUP_PREFIX + alfrescoClient.getGroupNameByNodeId(groupId)
                                                               .orElseThrow(),
                                                 List.of(userId));
            }
            catch (ClientException e)
            {
                allOk = false;
                log.error("Failed to remove user {} from group {}", userId, groupId, e);
            }
        }
        return allOk;
    }

    private static String safeUserId(RepoEvent<DataAttributes<NodeResource>> repoEvent)
    {
        try
        {
            return fetchUserId(repoEvent);
        }
        catch (RuntimeException ex)
        {
            return "<unknown>";
        }
    }

    /** Diff between two membership snapshots. */
    record GroupDiff(Set<String> toAdd, Set<String> toRemove)
    {
        boolean isEmpty()
        {
            return toAdd.isEmpty() && toRemove.isEmpty();
        }

        GroupDiff(Collection<String> toAdd, Collection<String> toRemove)
        {
            this(Set.copyOf(toAdd), Set.copyOf(toRemove));
        }
    }
}
