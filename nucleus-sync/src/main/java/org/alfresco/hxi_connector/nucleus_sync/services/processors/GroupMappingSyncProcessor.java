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
import org.alfresco.hxi_connector.nucleus_sync.model.GroupMapping;

@Service
@RequiredArgsConstructor
public class GroupMappingSyncProcessor
{
    private final NucleusClient nucleusClient;
    private static final Logger logger = LoggerFactory.getLogger(GroupMappingSyncProcessor.class);

    /**
     * Performs group sync operation with nucleus.
     *
     * Only those groups are synced whose users have been synced. Groups with no users or groups with all users who could not be synced are not synced with nucleus.
     *
     * @param alfrescoGroups
     *            list of all alfresco groups
     * @param currentNucleusGroups
     *            list of groups from nucleus
     * @param userGroupMembershipsCache
     *            cache of user and their groups
     * @return list of updated group mappings
     */
    public List<GroupMapping> syncGroupMappings(
            List<AlfrescoGroup> alfrescoGroups,
            List<NucleusGroupOutput> currentNucleusGroups,
            Map<String, List<String>> userGroupMembershipsCache)
    {
        Set<String> relevantAlfrescoGroupIds = userGroupMembershipsCache.values().stream()
                .filter(groups -> groups != null)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<AlfrescoGroup> filteredAlfrescoGroups = alfrescoGroups.stream()
                .filter(g -> relevantAlfrescoGroupIds.contains(g.id()))
                .toList();

        Map<String, AlfrescoGroup> alfrescoGroupById = filteredAlfrescoGroups.stream()
                .collect(Collectors.toMap(AlfrescoGroup::id, Function.identity()));

        Map<String, NucleusGroupOutput> nucleusGroupByAlfrescoId = currentNucleusGroups.stream()
                .collect(
                        Collectors.toMap(
                                NucleusGroupOutput::externalGroupId,
                                Function.identity()));

        List<NucleusGroupInput> nucleusGroupsToCreate = new ArrayList<>();
        List<String> nucleusGroupsToDelete = new ArrayList<>();
        List<GroupMapping> cachedGroupMappings = new ArrayList<>();

        Set<String> allGroupIds = new HashSet<>();
        allGroupIds.addAll(alfrescoGroupById.keySet());
        allGroupIds.addAll(nucleusGroupByAlfrescoId.keySet());

        for (String groupId : allGroupIds)
        {
            AlfrescoGroup alfrescoGroup = alfrescoGroupById.get(groupId);
            NucleusGroupOutput nucleusGroup = nucleusGroupByAlfrescoId.get(groupId);

            boolean hasAlfrescoGroup = alfrescoGroup != null;
            boolean hasNucleusGroup = nucleusGroup != null;

            if (hasAlfrescoGroup)
            {
                if (!hasNucleusGroup)
                {
                    nucleusGroupsToCreate.add(new NucleusGroupInput(alfrescoGroup.id()));
                }
                cachedGroupMappings.add(
                        new GroupMapping(alfrescoGroup.id(), alfrescoGroup.displayName()));
            }
            else
            {
                if (hasNucleusGroup)
                {
                    nucleusGroupsToDelete.add(groupId);
                }
            }
        }

        executeGroupBatchOperations(nucleusGroupsToCreate, nucleusGroupsToDelete);

        logger.debug("Updated group count: {}", cachedGroupMappings.size());
        return cachedGroupMappings;
    }

    private void executeGroupBatchOperations(
            List<NucleusGroupInput> nucleusGroupsToCreate, List<String> nucleusGroupsToDelete)
    {

        for (String alfrescoGroupId : nucleusGroupsToDelete)
        {
            nucleusClient.deleteGroup(alfrescoGroupId);
        }
        logger.debug("Deleted {} groups from Nucleus.", nucleusGroupsToDelete.size());

        if (!nucleusGroupsToCreate.isEmpty())
        {
            nucleusClient.createGroups(nucleusGroupsToCreate);
        }
        logger.debug("Created {} groups in Nucleus.", nucleusGroupsToCreate.size());
    }
}
