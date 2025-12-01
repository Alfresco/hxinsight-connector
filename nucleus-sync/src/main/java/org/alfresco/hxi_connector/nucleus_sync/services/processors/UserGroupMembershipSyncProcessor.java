/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.nucleus_sync.services.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.model.UserGroupMembership;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@Service
@RequiredArgsConstructor
public class UserGroupMembershipSyncProcessor
{
    private final NucleusClient nucleusClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupMembershipSyncProcessor.class);

    /**
     * Performs user group mappings with nucleus.
     *
     * @param localUserMappings
     *            local user mappings
     * @param localGroupMappings
     *            local group mappings
     * @param currentNucleusMemberships
     *            current nucleus memberships
     * @param userGroupMembershipsCache
     *            cache of all user and their groups
     * @return list of user group mapping
     */
    public List<UserGroupMembership> syncUserGroupMemberships(
            List<UserMapping> localUserMappings,
            List<GroupMapping> localGroupMappings,
            List<NucleusGroupMembershipOutput> currentNucleusMemberships,
            Map<String, List<String>> userGroupMembershipsCache)
    {

        Map<String, GroupMapping> groupMappingByAlfrescoGroupId = localGroupMappings.stream()
                .collect(
                        Collectors.toMap(
                                GroupMapping::alfrescoGroupId, Function.identity()));

        List<UserGroupMembership> cachedMemberships = new ArrayList<>();

        Map<String, Set<String>> currentNucleusState = buildCurrentNucleusState(currentNucleusMemberships);

        Map<String, Set<String>> desiredState = buildDesiredState(
                localUserMappings,
                userGroupMembershipsCache,
                groupMappingByAlfrescoGroupId);

        List<NucleusGroupMemberAssignmentInput> nucleusMembershipsToCreate = new ArrayList<>();
        Map<String, List<String>> nucleusMembershipsToDelete = new HashMap<>();

        for (UserMapping userMapping : localUserMappings)
        {
            String alfrescoUserId = userMapping.alfrescoUserId();

            Set<String> desiredGroupsForUser = desiredState.getOrDefault(alfrescoUserId, Set.of());

            Set<String> currentNucleusGroupsForUser = currentNucleusState.getOrDefault(alfrescoUserId, Set.of());

            for (String groupId : desiredGroupsForUser)
            {
                cachedMemberships.add(
                        new UserGroupMembership(alfrescoUserId, groupId, userMapping.email()));
                if (!currentNucleusGroupsForUser.contains(groupId))
                {
                    nucleusMembershipsToCreate.add(
                            new NucleusGroupMemberAssignmentInput(groupId, alfrescoUserId));
                }
            }

            for (String groupId : currentNucleusGroupsForUser)
            {
                if (!desiredGroupsForUser.contains(groupId))
                {
                    nucleusMembershipsToDelete
                            .computeIfAbsent(groupId, k -> new ArrayList<>())
                            .add(alfrescoUserId);
                }
            }
        }

        executeNucleusMembershipBatchOperations(
                nucleusMembershipsToCreate, nucleusMembershipsToDelete);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Final user and group membership count: {}", cachedMemberships.size());
        }
        return cachedMemberships;
    }

    private Map<String, Set<String>> buildCurrentNucleusState(
            List<NucleusGroupMembershipOutput> currentNucleusMemberships)
    {

        Map<String, Set<String>> nucleusState = new HashMap<>();

        for (NucleusGroupMembershipOutput memberShip : currentNucleusMemberships)
        {
            nucleusState
                    .computeIfAbsent(memberShip.memberExternalUserId(), k -> new HashSet<>())
                    .add(memberShip.externalGroupId());
        }

        return nucleusState;
    }

    private Map<String, Set<String>> buildDesiredState(
            List<UserMapping> localUserMappings,
            Map<String, List<String>> userGroupMembershipsCache,
            Map<String, GroupMapping> groupMappingByAlfrescoGroupId)
    {
        Map<String, Set<String>> desiredState = new HashMap<>();

        for (UserMapping userMapping : localUserMappings)
        {
            String alfrescoUserId = userMapping.alfrescoUserId();
            List<String> alfrescoGroupIds = userGroupMembershipsCache.getOrDefault(alfrescoUserId, List.of());

            Set<String> trackedGroupIds = alfrescoGroupIds.stream()
                    .filter(groupMappingByAlfrescoGroupId::containsKey)
                    .collect(Collectors.toSet());

            if (!trackedGroupIds.isEmpty())
            {
                desiredState.put(alfrescoUserId, trackedGroupIds);
            }
        }

        return desiredState;
    }

    private void executeNucleusMembershipBatchOperations(
            List<NucleusGroupMemberAssignmentInput> nucleusMembershipsToCreate,
            Map<String, List<String>> nucleusMembershipsToRemove)
    {

        if (!nucleusMembershipsToCreate.isEmpty())
        {
            nucleusClient.assignGroupMembers(nucleusMembershipsToCreate);
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(
                    "Assigned {} members to groups in Nucleus.", nucleusMembershipsToCreate.size());
        }

        for (Map.Entry<String, List<String>> entry : nucleusMembershipsToRemove.entrySet())
        {
            nucleusClient.removeGroupMembers(entry.getKey(), entry.getValue());
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(
                        "Removed {} members from group {} in Nucleus.",
                        entry.getValue().size(),
                        entry.getKey());
            }
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("{} memberships deleted.", nucleusMembershipsToRemove.size());
        }
    }
}
