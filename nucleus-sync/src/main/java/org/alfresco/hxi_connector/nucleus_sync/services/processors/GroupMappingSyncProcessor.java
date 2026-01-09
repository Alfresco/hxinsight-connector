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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;

@Service
@RequiredArgsConstructor
public class GroupMappingSyncProcessor
{
    private final NucleusClient nucleusClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupMappingSyncProcessor.class);

    /**
     * Performs group sync operation with nucleus.
     *
     * Only those groups are synced whose users have been synced. Groups with no users or groups with all users who could not be synced are not synced with nucleus.
     *
     * @param currentNucleusGroups
     *            list of groups from nucleus
     * @param userGroupMembershipsCache
     *            cache of user and their groups
     * @return list of updated group mappings
     */
    public List<String> syncGroupMappings(
            List<NucleusGroupOutput> currentNucleusGroups,
            Map<String, List<String>> userGroupMembershipsCache)
    {
        Set<String> groupsWithUsers = userGroupMembershipsCache.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        Set<String> existingNucleusGroupIds = currentNucleusGroups.stream()
                .map(NucleusGroupOutput::externalGroupId)
                .collect(Collectors.toSet());

        List<String> groupsToCreate = groupsWithUsers.stream()
                .filter(id -> !existingNucleusGroupIds.contains(id))
                .toList();

        List<String> groupsToDelete = existingNucleusGroupIds.stream()
                .filter(id -> !groupsWithUsers.contains(id))
                .toList();

        executeGroupOperations(groupsToCreate, groupsToDelete);

        LOGGER.atDebug()
                .setMessage("Updated group count: {}")
                .addArgument(groupsWithUsers.size())
                .log();

        return groupsWithUsers.stream().toList();
    }

    private void executeGroupOperations(List<String> toCreate, List<String> toDelete)
    {
        toDelete.forEach(nucleusClient::deleteGroup);
        if (!toDelete.isEmpty())
        {
            LOGGER.atTrace()
                    .setMessage("Deleted groups: {}")
                    .addArgument(() -> String.join(", ", toDelete))
                    .log();
        }
        LOGGER.atDebug()
                .setMessage("Deleted {} groups from Nucleus.")
                .addArgument(toDelete.size())
                .log();

        if (!toCreate.isEmpty())
        {
            List<NucleusGroupInput> inputs = toCreate.stream()
                    .map(NucleusGroupInput::new)
                    .toList();
            nucleusClient.createGroups(inputs);
            LOGGER.atTrace()
                    .setMessage("Created groups: {}")
                    .addArgument(() -> String.join(", ", toCreate))
                    .log();
        }

        LOGGER.atDebug()
                .setMessage("Created {} groups in Nucleus.")
                .addArgument(toCreate.size())
                .log();
    }
}
