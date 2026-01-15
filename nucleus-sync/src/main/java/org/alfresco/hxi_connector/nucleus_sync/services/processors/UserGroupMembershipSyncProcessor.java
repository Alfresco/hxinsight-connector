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
package org.alfresco.hxi_connector.nucleus_sync.services.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@Service
public class UserGroupMembershipSyncProcessor
{
    private final NucleusClient nucleusClient;
    private final int createBatchSize;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupMembershipSyncProcessor.class);

    public UserGroupMembershipSyncProcessor(
            NucleusClient nucleusClient,
            @Value("${alfresco.sync-batch-size:1000}") int createBatchSize)
    {
        this.nucleusClient = nucleusClient;
        this.createBatchSize = createBatchSize;
    }

    /**
     * Performs user group mappings with nucleus.
     *
     * @param localUserMappings
     *            local user mappings
     * @param localGroupMappings
     *            local group mappings
     * @param currentNucleusMemberships
     *            current nucleus memberships
     * @param userGroupMemberships
     *            cache of all user and their groups
     */
    public void syncUserGroupMemberships(
            List<UserMapping> localUserMappings,
            List<String> localGroupMappings,
            List<NucleusGroupMembershipOutput> currentNucleusMemberships,
            Map<String, List<String>> userGroupMemberships)
    {
        // lookups
        Map<String, String> alfUsrEmailByUsrId = localUserMappings.stream()
                .collect(Collectors.toMap(UserMapping::alfrescoUserId, UserMapping::email));

        Set<String> syncGrpIds = new HashSet<>(localGroupMappings);
        Set<String> syncUsrIds = new HashSet<>(alfUsrEmailByUsrId.keySet());

        // desired and current states
        Set<UserGroupPair> desiredMemberships = calculateDesiredMemberships(syncUsrIds,
                syncGrpIds, userGroupMemberships);

        Set<UserGroupPair> currentMemberships = currentNucleusMemberships.stream()
                .map(m -> new UserGroupPair(m.memberExternalUserId(), m.externalGroupId()))
                .collect(Collectors.toSet());

        // the delta
        Set<UserGroupPair> mappingsToCreate = new HashSet<>(desiredMemberships);
        mappingsToCreate.removeAll(currentMemberships);

        Set<UserGroupPair> mappingsToDelete = new HashSet<>(currentMemberships);
        mappingsToDelete.removeAll(desiredMemberships);

        // execute
        executeNucleusMembershipOperations(mappingsToCreate, mappingsToDelete);

        LOGGER.atDebug()
                .setMessage("Final user and group membership count: {}")
                .addArgument(desiredMemberships.size())
                .log();
    }

    private Set<UserGroupPair> calculateDesiredMemberships(
            Set<String> syncedUserIds,
            Set<String> syncedGroupIds,
            Map<String, List<String>> userGroupMembershipCache)
    {
        return syncedUserIds.stream()
                .flatMap(userId -> {
                    List<String> userGroups = userGroupMembershipCache.getOrDefault(userId, List.of());
                    return userGroups.stream()
                            .filter(syncedGroupIds::contains)
                            .map(groupId -> new UserGroupPair(userId, groupId));

                }).collect(Collectors.toSet());
    }

    private void executeNucleusMembershipOperations(
            Set<UserGroupPair> nucleusMembershipsToCreate,
            Set<UserGroupPair> nucleusMembershipsToRemove)
    {
        if (!nucleusMembershipsToCreate.isEmpty())
        {
            List<UserGroupPair> membershipsList = new ArrayList<>(nucleusMembershipsToCreate);

            for (int i = 0; i < nucleusMembershipsToCreate.size(); i += createBatchSize)
            {
                int endIndex = Math.min(i + createBatchSize, nucleusMembershipsToCreate.size());
                List<UserGroupPair> batch = membershipsList.subList(i, endIndex);

                List<NucleusGroupMemberAssignmentInput> input = batch.stream()
                        .map(pair -> new NucleusGroupMemberAssignmentInput(
                                pair.alfrescoGroupId(), pair.alfresoUserId()))
                        .toList();
                nucleusClient.assignGroupMembers(input);
            }

            LOGGER.atTrace()
                    .setMessage("Created memberships: {}")
                    .addArgument(() -> nucleusMembershipsToCreate.stream()
                            .map(p -> String.format("user=%s -> group=%s", p.alfresoUserId(), p.alfrescoGroupId()))
                            .collect(Collectors.joining(", ")))
                    .log();
        }
        LOGGER.atDebug()
                .setMessage("Assigned {} members to groups in Nucleus.")
                .addArgument(nucleusMembershipsToCreate.size())
                .log();

        if (!nucleusMembershipsToRemove.isEmpty())
        {
            Map<String, List<String>> deleteByGroup = nucleusMembershipsToRemove.stream()
                    .collect(Collectors.groupingBy(
                            UserGroupPair::alfrescoGroupId,
                            Collectors.mapping(UserGroupPair::alfresoUserId, Collectors.toList())));
            deleteByGroup.forEach((groupId, userIds) -> {
                nucleusClient.removeGroupMembers(groupId, userIds);
            });
            LOGGER.atTrace()
                    .setMessage("Deleted memberships: {}")
                    .addArgument(() -> nucleusMembershipsToRemove.stream()
                            .map(p -> String.format("user=%s -> group=%s", p.alfresoUserId(), p.alfrescoGroupId()))
                            .collect(Collectors.joining(", ")))
                    .log();
        }

        LOGGER.atDebug()
                .setMessage("Deleted {} memberships from Nucleus.")
                .addArgument(nucleusMembershipsToRemove.size())
                .log();
    }

    private record UserGroupPair(String alfresoUserId, String alfrescoGroupId)
    {}
}
