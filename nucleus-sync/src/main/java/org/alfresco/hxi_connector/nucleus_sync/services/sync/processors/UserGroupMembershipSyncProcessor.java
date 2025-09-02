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
package org.alfresco.hxi_connector.nucleus_sync.services.sync.processors;

import java.time.LocalDateTime;
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
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserGroupMembership;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.UserGroupMembershipService;

@Service
@RequiredArgsConstructor
public class UserGroupMembershipSyncProcessor
{
    private final NucleusClient nucleusClient;
    private final UserGroupMembershipService userGroupMembershipService;
    private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipSyncProcessor.class);

    public void performLocalOnlyMembershipOperations(
            List<UserMapping> userMappings,
            List<GroupMapping> groupMappings,
            Map<String, List<String>> userGroupMembershipCache)
    {

        if (userGroupMembershipCache == null || userGroupMembershipCache.isEmpty())
        {
            logger.debug("No user group membership cache available for local operations");
            return;
        }

        logger.debug("Performing local-only membership operations");

        Map<String, Set<String>> groupMemberships = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : userGroupMembershipCache.entrySet())
        {
            String userId = entry.getKey();
            List<String> groupIds = entry.getValue();

            for (String groupId : groupIds)
            {
                groupMemberships.computeIfAbsent(groupId, k -> new HashSet<>()).add(userId);
            }
        }

        Set<String> activeGroupIds = groupMappings.stream()
                .filter(GroupMapping::getIsActive)
                .map(GroupMapping::getAlfrescoGroupId)
                .collect(Collectors.toSet());

        Map<String, Integer> groupUserCounts = groupMemberships.entrySet().stream()
                .filter(entry -> activeGroupIds.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));

        if (!groupUserCounts.isEmpty())
        {
            userGroupMembershipService.updateGroupUserCounts(groupUserCounts);
            logger.debug(
                    "Updated user counts for {} groups based on local state",
                    groupUserCounts.size());
        }
    }

    public void syncUserGroupMemberships(
            List<AlfrescoUser> alfrescoUsers,
            List<UserMapping> localUserMappings,
            List<GroupMapping> localGroupMappings,
            List<NucleusGroupMembershipOutput> currentNucleusMemberships,
            Map<String, List<String>> userGroupMembershipsCache)
    {

        Map<String, GroupMapping> groupMappingByAlfrescoId = localGroupMappings.stream()
                .collect(
                        Collectors.toMap(
                                GroupMapping::getAlfrescoGroupId, Function.identity()));

        Map<String, UserMapping> userMappingByAlfrescoId = localUserMappings.stream()
                .collect(
                        Collectors.toMap(
                                UserMapping::getAlfrescoUserId, Function.identity()));

        List<String> mappedEmails = localUserMappings.stream().map(UserMapping::getEmail).toList();
        List<UserGroupMembership> currentLocalMemberships = userGroupMembershipService.getMembershipsForUser(mappedEmails);

        Map<String, Set<String>> currentNucleusState = buildCurrentNucleusState(
                currentNucleusMemberships,
                userMappingByAlfrescoId,
                groupMappingByAlfrescoId);

        Map<String, Set<String>> currentLocalState = buildCurrentLocalState(currentLocalMemberships);

        Map<String, Set<String>> desiredState = buildDesiredState(
                localUserMappings, userGroupMembershipsCache, groupMappingByAlfrescoId);

        List<UserGroupMembership> membershipsToCreate = new ArrayList<>();
        List<UserGroupMembership> membershipsToDeactivate = new ArrayList<>();
        List<NucleusGroupMemberAssignmentInput> nucleusMembershipsToCreate = new ArrayList<>();
        Map<String, List<String>> nucleusMembershipsToDelete = new HashMap<>();
        Map<String, Set<String>> groupMemberShips = new HashMap<>();

        for (UserMapping userMapping : localUserMappings)
        {
            String email = userMapping.getEmail();
            String alfrescoUserId = userMapping.getAlfrescoUserId();

            Set<String> desiredGroups = desiredState.getOrDefault(alfrescoUserId, Set.of());
            Set<String> currentLocalGroups = currentLocalState.getOrDefault(alfrescoUserId, Set.of());
            Set<String> currentNucleusGroups = currentNucleusState.getOrDefault(alfrescoUserId, Set.of());

            for (String groupId : desiredGroups)
            {
                groupMemberShips.computeIfAbsent(groupId, k -> new HashSet<>()).add(alfrescoUserId);
            }

            for (String groupId : desiredGroups)
            {
                if (!currentLocalGroups.contains(groupId))
                {
                    membershipsToCreate.add(
                            new UserGroupMembership(
                                    groupId, alfrescoUserId, email, LocalDateTime.now(), true));
                }
                if (!currentNucleusGroups.contains(groupId))
                {
                    nucleusMembershipsToCreate.add(
                            new NucleusGroupMemberAssignmentInput(groupId, alfrescoUserId));
                }
            }

            for (String groupId : currentLocalGroups)
            {
                if (!desiredGroups.contains(groupId))
                {
                    UserGroupMembership membershipToDeactivate = currentLocalMemberships.stream()
                            .filter(
                                    m -> m.getEmail().equals(email)
                                            && m.getAlfrescoGroupId()
                                                    .equals(groupId)
                                            && m.getIsActive())
                            .findFirst()
                            .orElse(null);

                    if (membershipToDeactivate != null)
                    {
                        membershipsToDeactivate.add(membershipToDeactivate);
                    }
                }
            }

            for (String groupId : currentNucleusGroups)
            {
                if (!desiredGroups.contains(groupId))
                {
                    nucleusMembershipsToDelete
                            .computeIfAbsent(groupId, k -> new ArrayList<>())
                            .add(alfrescoUserId);
                }
            }
        }

        executeMembershipBatchOperations(
                membershipsToCreate,
                membershipsToDeactivate,
                nucleusMembershipsToCreate,
                nucleusMembershipsToDelete,
                groupMemberShips);
    }

    private Map<String, Set<String>> buildCurrentNucleusState(
            List<NucleusGroupMembershipOutput> currentNucleusMemberships,
            Map<String, UserMapping> userMappingByAlfrescoId,
            Map<String, GroupMapping> groupMappingByAlfrescoId)
    {

        Map<String, Set<String>> nucleusState = new HashMap<>();

        for (NucleusGroupMembershipOutput memberShip : currentNucleusMemberships)
        {
            UserMapping userMapping = userMappingByAlfrescoId.get(memberShip.getMemberExternalUserId());
            if (userMapping != null
                    && groupMappingByAlfrescoId.containsKey(memberShip.getExternalGroupId()))
            {
                nucleusState
                        .computeIfAbsent(userMapping.getAlfrescoUserId(), k -> new HashSet<>())
                        .add(memberShip.getExternalGroupId());
            }
        }

        return nucleusState;
    }

    private Map<String, Set<String>> buildCurrentLocalState(
            List<UserGroupMembership> currentLocalMemberships)
    {
        return currentLocalMemberships.stream()
                .filter(UserGroupMembership::getIsActive)
                .collect(
                        Collectors.groupingBy(
                                UserGroupMembership::getAlfrescoUserId,
                                Collectors.mapping(
                                        UserGroupMembership::getAlfrescoGroupId,
                                        Collectors.toSet())));
    }

    private Map<String, Set<String>> buildDesiredState(
            List<UserMapping> localUserMappings,
            Map<String, List<String>> userGroupMembershipsCache,
            Map<String, GroupMapping> groupMappingByAlfrescoId)
    {
        Map<String, Set<String>> desiredState = new HashMap<>();

        for (UserMapping userMapping : localUserMappings)
        {
            String alfrescoUserId = userMapping.getAlfrescoUserId();
            List<String> alfrescoGroupIds = userGroupMembershipsCache.getOrDefault(alfrescoUserId, List.of());

            Set<String> trackedGroupIds = alfrescoGroupIds.stream()
                    .filter(groupMappingByAlfrescoId::containsKey)
                    .collect(Collectors.toSet());

            if (!trackedGroupIds.isEmpty())
            {
                desiredState.put(alfrescoUserId, trackedGroupIds);
            }
        }

        return desiredState;
    }

    private void executeMembershipBatchOperations(
            List<UserGroupMembership> membershipsToCreate,
            List<UserGroupMembership> membershipsToDeactivate,
            List<NucleusGroupMemberAssignmentInput> nucleusMembershipsToCreate,
            Map<String, List<String>> nucleusMembershipsToRemove,
            Map<String, Set<String>> groupMemberShips)
    {

        if (!membershipsToCreate.isEmpty())
        {
            userGroupMembershipService.createMemberships(membershipsToCreate);
            logger.trace("Created {} local memberships.", membershipsToCreate.size());
        }

        if (!membershipsToDeactivate.isEmpty())
        {
            userGroupMembershipService.deactivateMemberships(membershipsToDeactivate);
            logger.trace("Deactivated {} local memberships.", membershipsToDeactivate.size());
        }

        if (!membershipsToCreate.isEmpty() || !membershipsToDeactivate.isEmpty())
        {
            Map<String, Integer> groupUserCounts = groupMemberShips.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
            userGroupMembershipService.updateGroupUserCounts(groupUserCounts);
            logger.trace("Updated user counts for {} groups.", groupUserCounts.size());
        }

        if (!nucleusMembershipsToCreate.isEmpty())
        {
            nucleusClient.assignGroupMembers(nucleusMembershipsToCreate);
            logger.trace(
                    "Assigned {} members to groups in Nucleus.", nucleusMembershipsToCreate.size());
        }

        for (Map.Entry<String, List<String>> entry : nucleusMembershipsToRemove.entrySet())
        {
            nucleusClient.removeGroupMembers(entry.getKey(), entry.getValue());
            logger.trace(
                    "Removed {} members from group {} in Nucleus.",
                    entry.getValue().size(),
                    entry.getKey());
        }
    }
}
