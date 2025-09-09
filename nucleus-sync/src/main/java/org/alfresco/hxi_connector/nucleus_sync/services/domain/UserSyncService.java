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
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.repository.UserMappingRepository;

@Service
@RequiredArgsConstructor
public class UserSyncService
{
    private final UserMappingRepository userMappingRepository;

    public List<UserMapping> findOrCreateUserMappings(List<UserMapping> mappingsToCreate)
    {
        List<String> emails = mappingsToCreate.stream().map(UserMapping::getEmail).toList();

        List<UserMapping> existingMappings = userMappingRepository.findByEmailIn(emails);
        Map<String, UserMapping> existingMappingMap = existingMappings.stream()
                .collect(Collectors.toMap(UserMapping::getEmail, Function.identity()));

        LocalDateTime now = LocalDateTime.now();
        existingMappings.forEach(mapping -> {
            mapping.setLastSynced(now);
            mapping.setIsActive(true);
        });

        List<UserMapping> newMappings = mappingsToCreate.stream()
                .filter(mapping -> !existingMappingMap.containsKey(mapping.getEmail()))
                .peek(
                        mapping -> {
                            mapping.setIsActive(true);
                            mapping.setLastSynced(now);
                        })
                .toList();

        List<UserMapping> allMappings = new ArrayList<>();
        allMappings.addAll(existingMappings);
        allMappings.addAll(newMappings);

        return userMappingRepository.saveAll(allMappings);
    }

    public List<UserMapping> getAllActiveUserMappings()
    {
        return userMappingRepository.findByIsActiveTrue();
    }

    public void deactivateUserMappings(List<String> emails)
    {
        if (emails == null || emails.isEmpty())
        {
            return;
        }

        List<UserMapping> mappings = userMappingRepository.findByEmailIn(emails);

        LocalDateTime now = LocalDateTime.now();
        mappings.forEach(
                mapping -> {
                    mapping.setIsActive(false);
                    mapping.setLastSynced(now);
                });

        userMappingRepository.saveAll(mappings);
    }
}
