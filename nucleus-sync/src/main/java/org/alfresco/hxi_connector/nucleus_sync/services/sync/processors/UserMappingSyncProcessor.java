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
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.UserSyncService;

@Service
@RequiredArgsConstructor
public class UserMappingSyncProcessor
{
    private final NucleusClient nucleusClient;
    private final UserSyncService userSyncService;
    private static final Logger logger = LoggerFactory.getLogger(UserMappingSyncProcessor.class);

    /**
     * Sync alfresco and nucleus user mappings with nucleus and local db
     *
     * @param alfrescoUsers
     *            list of alfresco users
     * @param nucleusIamUsers
     *            list of IAM users of nucleus
     * @param currentUserMappings
     *            current nucleus user mappings
     * @param localUserMappings
     *            current mappings stored in db
     *
     * @return list of updated user mappings
     */
    public List<UserMapping> syncUserMappings(
            List<AlfrescoUser> alfrescoUsers,
            List<IamUser> nucleusIamUsers,
            List<NucleusUserMappingOutput> currentUserMappings,
            List<UserMapping> localUserMappings)
    {

        Map<String, AlfrescoUser> alfrescoUserByEmail = alfrescoUsers.stream()
                .filter(u -> u.getEmail() != null && !u.getEmail().isEmpty())
                .collect(Collectors.toMap(AlfrescoUser::getEmail, Function.identity()));
        Map<String, IamUser> nucleusUserByEmail = nucleusIamUsers.stream()
                .collect(Collectors.toMap(IamUser::getEmail, Function.identity()));
        Map<String, NucleusUserMappingOutput> nucleusMapingByAlfrescoId = currentUserMappings.stream()
                .collect(
                        Collectors.toMap(
                                NucleusUserMappingOutput::getExternalUserId,
                                Function.identity()));
        Map<String, UserMapping> localMappingByEmail = localUserMappings.stream()
                .collect(Collectors.toMap(UserMapping::getEmail, Function.identity()));

        List<String> nucleusMappingsToDelete = new ArrayList<>();
        List<String> localMappingsToDelete = new ArrayList<>();
        List<NucleusUserMappingInput> nucleusMappingsToCreate = new ArrayList<>();
        List<UserMapping> localMappingsToCreate = new ArrayList<>();

        Set<String> allEmails = new HashSet<>();
        allEmails.addAll(alfrescoUserByEmail.keySet());
        allEmails.addAll(nucleusUserByEmail.keySet());

        for (String email : allEmails)
        {
            AlfrescoUser alfrescoUser = alfrescoUserByEmail.get(email);
            IamUser nucleusIamUser = nucleusUserByEmail.get(email);

            boolean isAlfrescoUser = alfrescoUser != null;
            boolean isNucleusUser = nucleusIamUser != null;

            if (isAlfrescoUser && isNucleusUser)
            {
                ensureMappingsExist(
                        alfrescoUser,
                        nucleusIamUser,
                        nucleusMapingByAlfrescoId,
                        localMappingByEmail,
                        nucleusMappingsToCreate,
                        localMappingsToCreate);
            }
            else
            {
                removeMapping(
                        alfrescoUser,
                        nucleusIamUser,
                        nucleusMapingByAlfrescoId,
                        localMappingByEmail,
                        nucleusMappingsToDelete,
                        localMappingsToDelete);
            }
        }

        Set<String> validAlfrescoIds = alfrescoUsers.stream().map(AlfrescoUser::getId).collect(Collectors.toSet());

        for (NucleusUserMappingOutput nucleusMapping : currentUserMappings)
        {
            String alfrescoUserId = nucleusMapping.getExternalUserId();
            if (!validAlfrescoIds.contains(alfrescoUserId)
                    && !nucleusMappingsToDelete.contains(alfrescoUserId))
            {
                nucleusMappingsToDelete.add(alfrescoUserId);
            }
        }

        executeUserBatchOperations(
                nucleusMappingsToDelete,
                localMappingsToDelete,
                nucleusMappingsToCreate,
                localMappingsToCreate);

        List<UserMapping> updatedUserMappings = userSyncService.getAllActiveUserMappings();
        logger.debug("Updated mappings count: {}", updatedUserMappings.size());
        return updatedUserMappings;
    }

    private void ensureMappingsExist(
            AlfrescoUser alfrescoUser,
            IamUser nucleusIamUser,
            Map<String, NucleusUserMappingOutput> nucleusMappingByAlfrescoId,
            Map<String, UserMapping> localMappingByEmail,
            List<NucleusUserMappingInput> nucleusMappingsToCreate,
            List<UserMapping> localMappingsToCreate)
    {

        String alfrescoUserId = alfrescoUser.getId();
        String email = alfrescoUser.getEmail();
        String nucleusUserId = nucleusIamUser.getUserId();

        if (!nucleusMappingByAlfrescoId.containsKey(alfrescoUserId))
        {
            nucleusMappingsToCreate.add(new NucleusUserMappingInput(nucleusUserId, alfrescoUserId));
        }

        if (!localMappingByEmail.containsKey(email))
        {
            localMappingsToCreate.add(
                    new UserMapping(
                            email, alfrescoUserId, nucleusUserId, LocalDateTime.now(), true));
        }
    }

    private void removeMapping(
            AlfrescoUser alfrescoUser,
            IamUser nucleusIamUser,
            Map<String, NucleusUserMappingOutput> nucleusMappingsByAlfrescoId,
            Map<String, UserMapping> localMappingsByEmail,
            List<String> nucleusMappingsToDelete,
            List<String> localMappingsToDelete)
    {

        String email = alfrescoUser != null ? alfrescoUser.getEmail() : nucleusIamUser.getEmail();

        if (alfrescoUser != null && nucleusMappingsByAlfrescoId.containsKey(alfrescoUser.getId()))
        {
            nucleusMappingsToDelete.add(alfrescoUser.getId());
        }

        if (localMappingsByEmail.containsKey(email))
        {
            localMappingsToDelete.add(email);
        }
    }

    private void executeUserBatchOperations(
            List<String> nucleusMappingsToDelete,
            List<String> localMappingsToDelete,
            List<NucleusUserMappingInput> nucleusMappingsToCreate,
            List<UserMapping> localMappingsToCreate)
    {

        for (String alfrescoUserId : nucleusMappingsToDelete)
        {
            nucleusClient.deleteUserMapping(alfrescoUserId);
        }
        logger.debug("Deleted {} user mapping in Nucleus.", nucleusMappingsToDelete.size());

        if (!localMappingsToDelete.isEmpty())
        {
            userSyncService.deactivateUserMappings(localMappingsToDelete);
        }
        logger.debug("Deactivated {} local user mappings.", localMappingsToDelete.size());

        if (!nucleusMappingsToCreate.isEmpty())
        {
            nucleusClient.createUserMappings(nucleusMappingsToCreate);
        }
        logger.debug("Created {} user mappings in nucleus", nucleusMappingsToCreate.size());

        if (!localMappingsToCreate.isEmpty())
        {
            userSyncService.findOrCreateUserMappings(localMappingsToCreate);
        }
        logger.debug("Created {} local user mappings.", localMappingsToCreate.size());
    }
}
