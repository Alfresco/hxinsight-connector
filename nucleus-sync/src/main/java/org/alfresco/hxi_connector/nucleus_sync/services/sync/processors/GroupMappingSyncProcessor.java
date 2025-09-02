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
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoGroup;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.GroupSyncService;

@Service
@RequiredArgsConstructor
public class GroupMappingSyncProcessor
{
    private final NucleusClient nucleusClient;
    private final GroupSyncService groupSyncService;
    private static final Logger logger = LoggerFactory.getLogger(GroupMappingSyncProcessor.class);

    public List<GroupMapping> performNucleusOnlyGroupCleanup(
            List<GroupMapping> localGroupMappings, List<NucleusGroupOutput> currentNucleusGroups)
    {

        logger.debug("Performing Nucleus-only group cleanup.");

        Set<String> nucleusGroupIds = currentNucleusGroups.stream()
                .map(NucleusGroupOutput::getExternalGroupId)
                .collect(Collectors.toSet());

        List<String> groupsToDeactivate = localGroupMappings.stream()
                .filter(GroupMapping::getIsActive)
                .filter(gm -> !nucleusGroupIds.contains(gm.getAlfrescoGroupId()))
                .map(GroupMapping::getAlfrescoGroupId)
                .toList();

        if (!groupsToDeactivate.isEmpty())
        {
            groupSyncService.deactivateGroupMappings(groupsToDeactivate);

            logger.info(
                    "Deactivated {} groups that no longer exist in Nucleus",
                    groupsToDeactivate.size());
        }

        return groupSyncService.getAllActiveGroups();
    }

    public List<GroupMapping> syncGroupMappings(
            List<AlfrescoGroup> alfrescoGroups,
            List<NucleusGroupOutput> currentNucleusGroups,
            List<GroupMapping> localGroupMappings,
            Map<String, List<String>> userGroupMembershipsCache)
    {

        Set<String> relevantAlfrescoGroupIds = userGroupMembershipsCache.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<AlfrescoGroup> filteredAlfrescoGroups = alfrescoGroups.stream()
                .filter(g -> relevantAlfrescoGroupIds.contains(g.getId()))
                .toList();

        Map<String, AlfrescoGroup> alfrescoGroupById = filteredAlfrescoGroups.stream()
                .collect(Collectors.toMap(AlfrescoGroup::getId, Function.identity()));
        Map<String, NucleusGroupOutput> nucleusGroupByAlfrescoId = currentNucleusGroups.stream()
                .collect(
                        Collectors.toMap(
                                NucleusGroupOutput::getExternalGroupId,
                                Function.identity()));
        Map<String, GroupMapping> localGroupMappingByAlfrescoId = localGroupMappings.stream()
                .collect(
                        Collectors.toMap(
                                GroupMapping::getAlfrescoGroupId, Function.identity()));

        List<NucleusGroupInput> nucleusGroupsToCreate = new ArrayList<>();
        List<GroupMapping> localGroupsToCreate = new ArrayList<>();
        List<String> nucleusGroupsToDelete = new ArrayList<>();
        List<String> localGroupsToDelete = new ArrayList<>();

        Set<String> allGroupIds = new HashSet<>();
        allGroupIds.addAll(alfrescoGroupById.keySet());
        allGroupIds.addAll(nucleusGroupByAlfrescoId.keySet());

        for (String groupId : allGroupIds)
        {
            AlfrescoGroup alfrescoGroup = alfrescoGroupById.get(groupId);
            NucleusGroupOutput nucleusGroup = nucleusGroupByAlfrescoId.get(groupId);
            GroupMapping localGroupMapping = localGroupMappingByAlfrescoId.get(groupId);

            boolean hasAlfrescoGroup = alfrescoGroup != null;
            boolean hasNucleusGroup = nucleusGroup != null;
            boolean hasLocalMapping = localGroupMapping != null;

            if (hasAlfrescoGroup)
            {
                if (!hasNucleusGroup)
                {
                    nucleusGroupsToCreate.add(new NucleusGroupInput(alfrescoGroup.getId()));
                }

                if (!hasLocalMapping)
                {
                    localGroupsToCreate.add(
                            new GroupMapping(
                                    groupId,
                                    alfrescoGroup.getDisplayName(),
                                    LocalDateTime.now(),
                                    true,
                                    0));
                }
            }
            else
            {
                if (hasNucleusGroup)
                {
                    nucleusGroupsToDelete.add(groupId);
                }

                if (hasLocalMapping && localGroupMapping.getIsActive())
                {
                    localGroupsToDelete.add(groupId);
                }
            }
        }

        executeGroupBatchOperations(
                nucleusGroupsToCreate,
                localGroupsToCreate,
                nucleusGroupsToDelete,
                localGroupsToDelete);

        List<GroupMapping> updatedGroupMappings = groupSyncService.getAllActiveGroups();
        logger.debug("Updated group count: {}", updatedGroupMappings.size());
        return updatedGroupMappings;
    }

    private void executeGroupBatchOperations(
            List<NucleusGroupInput> nucleusGroupsToCreate,
            List<GroupMapping> localGroupsToCreate,
            List<String> nucleusGroupsToDelete,
            List<String> localGroupsToDelete)
    {

        for (String alfrescoGroupId : nucleusGroupsToDelete)
        {
            nucleusClient.deleteGroup(alfrescoGroupId);
            logger.trace("Deleted group {} in Nucleus.", alfrescoGroupId);
        }

        if (!localGroupsToDelete.isEmpty())
        {
            groupSyncService.deactivateGroupMappings(localGroupsToDelete);
            logger.trace("Deactivated {} local group mappings.", localGroupsToDelete.size());
        }

        if (!nucleusGroupsToCreate.isEmpty())
        {
            nucleusClient.createGroups(nucleusGroupsToCreate);
            logger.trace(
                    "Created groups {} in Nucleus.",
                    nucleusGroupsToCreate.stream()
                            .map(NucleusGroupInput::getExternalGroupId)
                            .toList());
        }

        if (!localGroupsToCreate.isEmpty())
        {
            groupSyncService.findOrCreateGroupMappings(localGroupsToCreate);
            logger.trace("Created {} local group mappings.", localGroupsToCreate.size());
        }
    }
}
