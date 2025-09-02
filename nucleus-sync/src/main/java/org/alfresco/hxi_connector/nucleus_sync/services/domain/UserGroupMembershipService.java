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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserGroupMembership;
import org.alfresco.hxi_connector.nucleus_sync.repository.GroupMappingRepository;
import org.alfresco.hxi_connector.nucleus_sync.repository.UserGroupMembershipRepository;

@Service
@RequiredArgsConstructor
public class UserGroupMembershipService
{
    private final GroupMappingRepository groupMappingRepository;
    private final UserGroupMembershipRepository userGroupMembershipRepository;

    public List<UserGroupMembership> createMemberships(
            List<UserGroupMembership> membershipsToCreate)
    {

        if (membershipsToCreate == null || membershipsToCreate.isEmpty())
        {
            return new ArrayList<>();
        }

        Map<String, UserGroupMembership> existingMemberships = new HashMap<>();
        for (UserGroupMembership membership : membershipsToCreate)
        {
            String key = membership.getAlfrescoGroupId() + ":" + membership.getAlfrescoUserId();
            Optional<UserGroupMembership> existingMembershipOpt = userGroupMembershipRepository.findByAlfrescoGroupIdAndAlfrescoUserId(
                    membership.getAlfrescoGroupId(), membership.getAlfrescoUserId());
            if (existingMembershipOpt.isPresent())
            {
                existingMemberships.put(key, existingMembershipOpt.get());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<UserGroupMembership> membershipsToSave = new ArrayList<>();

        for (UserGroupMembership newMembership : membershipsToCreate)
        {
            String key = newMembership.getAlfrescoGroupId() + ":" + newMembership.getAlfrescoUserId();
            UserGroupMembership existingMembership = existingMemberships.get(key);

            if (existingMembership != null && !existingMembership.getIsActive())
            {
                existingMembership.setIsActive(true);
                existingMembership.setLastSynced(now);
                membershipsToSave.add(existingMembership);
            }
            else
            {
                newMembership.setIsActive(true);
                newMembership.setLastSynced(now);
                membershipsToSave.add(newMembership);
            }
        }

        return userGroupMembershipRepository.saveAll(membershipsToSave);
    }

    public void deactivateMemberships(List<UserGroupMembership> membershipsToDeactivate)
    {
        if (membershipsToDeactivate == null || membershipsToDeactivate.isEmpty())
        {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        membershipsToDeactivate.forEach(
                membership -> {
                    membership.setIsActive(false);
                    membership.setLastSynced(now);
                });

        userGroupMembershipRepository.saveAll(membershipsToDeactivate);
    }

    public List<UserGroupMembership> getMembershipsForUser(List<String> emails)
    {
        if (emails == null || emails.isEmpty())
        {
            return new ArrayList<>();
        }

        return userGroupMembershipRepository.findByEmailInAndIsActiveTrue(emails);
    }

    public List<UserGroupMembership> getMembershipsForGroup(String alfrescoGroupId)
    {
        return userGroupMembershipRepository.findByAlfrescoGroupIdAndIsActiveTrue(alfrescoGroupId);
    }

    public void updateGroupUserCounts(Map<String, Integer> groupUserCounts)
    {
        if (groupUserCounts == null || groupUserCounts.isEmpty())
        {
            return;
        }

        List<String> alfrescoGroupIds = new ArrayList<>(groupUserCounts.keySet());
        List<GroupMapping> groupMappings = groupMappingRepository.findByAlfrescoGroupIdIn(alfrescoGroupIds);

        LocalDateTime now = LocalDateTime.now();
        groupMappings.forEach(
                mapping -> {
                    Integer userCount = groupUserCounts.get(mapping.getAlfrescoGroupId());
                    if (userCount != null)
                    {
                        mapping.setUserCount(userCount);
                        mapping.setLastSynced(now);
                    }
                });

        groupMappingRepository.saveAll(groupMappings);
    }
}
