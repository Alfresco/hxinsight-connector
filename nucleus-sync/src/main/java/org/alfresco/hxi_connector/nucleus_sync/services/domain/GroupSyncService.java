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
package org.alfresco.hxi_connector.nucleus_sync.services.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.repository.GroupMappingRepository;

@Service
@RequiredArgsConstructor
public class GroupSyncService
{
    private final GroupMappingRepository groupMappingRepository;

    public List<GroupMapping> findOrCreateGroupMappings(List<GroupMapping> mappingsToCreate)
    {
        List<String> alfrescoGroupIds = mappingsToCreate.stream().map(GroupMapping::getAlfrescoGroupId).toList();

        List<GroupMapping> existingMappings = groupMappingRepository.findByAlfrescoGroupIdIn(alfrescoGroupIds);
        Map<String, GroupMapping> existingMappingMap = existingMappings.stream()
                .collect(
                        Collectors.toMap(
                                GroupMapping::getAlfrescoGroupId, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        existingMappings.forEach(mapping -> {
            mapping.setLastSynced(now);
            mapping.setIsActive(true);
        });

        List<GroupMapping> newMappings = mappingsToCreate.stream()
                .filter(
                        mapping -> !existingMappingMap.containsKey(
                                mapping.getAlfrescoGroupId()))
                .peek(
                        mapping -> {
                            mapping.setIsActive(true);
                            mapping.setLastSynced(now);
                        })
                .toList();

        List<GroupMapping> allMappings = new ArrayList<>();
        allMappings.addAll(existingMappings);
        allMappings.addAll(newMappings);

        return groupMappingRepository.saveAll(allMappings);
    }

    public List<GroupMapping> getAllActiveGroups()
    {
        return groupMappingRepository.findByIsActiveTrue();
    }

    public void deactivateGroupMappings(List<String> alfrescoGroupIds)
    {
        if (alfrescoGroupIds == null || alfrescoGroupIds.isEmpty())
        {
            return;
        }

        List<GroupMapping> mappings = groupMappingRepository.findByAlfrescoGroupIdIn(alfrescoGroupIds);
        LocalDateTime now = LocalDateTime.now();

        mappings.forEach(
                mapping -> {
                    mapping.setIsActive(false);
                    mapping.setLastSynced(now);
                });

        groupMappingRepository.saveAll(mappings);
    }

    public void updateGroupUserCount(String alfrescoGroupId, int userCount)
    {
        Optional<GroupMapping> groupMappingOpt = groupMappingRepository.findByAlfrescoGroupId(alfrescoGroupId);

        if (groupMappingOpt.isPresent())
        {
            GroupMapping mapping = groupMappingOpt.get();
            mapping.setUserCount(userCount);
            mapping.setLastSynced(LocalDateTime.now());
            groupMappingRepository.save(mapping);
        }
        else
        {
            throw new IllegalArgumentException(
                    "Group mapping not found for Alfresco Group ID: " + alfrescoGroupId);
        }
    }
}
