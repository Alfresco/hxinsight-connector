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
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@Service
@RequiredArgsConstructor
public class UserMappingSyncProcessor
{
    private final NucleusClient nucleusClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserMappingSyncProcessor.class);

    /**
     * Sync alfresco and nucleus user mappings
     *
     * @param alfrescoUsers
     *            list of alfresco users
     * @param nucleusIamUsers
     *            list of IAM users of nucleus
     * @param currentUserMappings
     *            current nucleus user mappings
     * @return list of updated user mappings
     */
    public List<UserMapping> syncUserMappings(
            List<AlfrescoUser> alfrescoUsers,
            List<IamUser> nucleusIamUsers,
            List<NucleusUserMappingOutput> currentUserMappings)
    {

        // AlfrescoUsers must have email to map
        Map<String, AlfrescoUser> alfrescoUserByEmail = alfrescoUsers.stream()
                .filter(u -> u.email() != null && !u.email().isEmpty())
                .collect(Collectors.toMap(AlfrescoUser::email, Function.identity()));

        Map<String, IamUser> nucleusIamUserByEmail = nucleusIamUsers.stream()
                .collect(Collectors.toMap(IamUser::email, Function.identity()));

        Map<String, NucleusUserMappingOutput> nucleusMappingByAlfrescoId = currentUserMappings.stream()
                .collect(
                        Collectors.toMap(
                                NucleusUserMappingOutput::externalUserId,
                                Function.identity()));

        List<String> nucleusMappingsToDelete = new ArrayList<>();
        List<NucleusUserMappingInput> nucleusMappingsToCreate = new ArrayList<>();
        List<UserMapping> updatedUserMappings = new ArrayList<>();
        Set<String> validAlfrescoIds = new HashSet<>();

        Set<String> commonEmails = new HashSet<>(alfrescoUserByEmail.keySet());
        commonEmails.retainAll(nucleusIamUserByEmail.keySet());

        for (String email : commonEmails)
        {
            AlfrescoUser alfrescoUser = alfrescoUserByEmail.get(email);
            IamUser nucleusIamUser = nucleusIamUserByEmail.get(email);

            String alfrescoUserId = alfrescoUser.id();
            String nucleusUserId = nucleusIamUser.userId();

            validAlfrescoIds.add(alfrescoUserId);
            updatedUserMappings.add(
                    new UserMapping(alfrescoUser.email(), alfrescoUserId, nucleusUserId));

            if (!nucleusMappingByAlfrescoId.containsKey(alfrescoUserId))
            {
                nucleusMappingsToCreate.add(
                        new NucleusUserMappingInput(nucleusUserId, alfrescoUserId));
            }
        }

        for (NucleusUserMappingOutput nucleusMapping : currentUserMappings)
        {
            String alfrescoUserId = nucleusMapping.externalUserId();
            if (!validAlfrescoIds.contains(alfrescoUserId))
            {
                nucleusMappingsToDelete.add(alfrescoUserId);
            }
        }

        executeUserBatchOperations(nucleusMappingsToDelete, nucleusMappingsToCreate);

        LOGGER.atDebug()
                .setMessage("Final user mappings count: {}")
                .addArgument(updatedUserMappings.size())
                .log();

        return updatedUserMappings;
    }

    private void executeUserBatchOperations(
            List<String> nucleusMappingsToDelete,
            List<NucleusUserMappingInput> nucleusMappingsToCreate)
    {
        for (String alfrescoUserId : nucleusMappingsToDelete)
        {
            nucleusClient.deleteUserMapping(alfrescoUserId);
            LOGGER.atTrace()
                    .setMessage("Deleted user {} from nucleus.")
                    .addArgument(alfrescoUserId)
                    .log();
        }
        LOGGER.atDebug()
                .setMessage("Deleted {} user mapping in Nucleus.")
                .addArgument(nucleusMappingsToDelete.size())
                .log();

        if (!nucleusMappingsToCreate.isEmpty())
        {
            nucleusClient.createUserMappings(nucleusMappingsToCreate);

            LOGGER.atTrace()
                    .setMessage("Created user mappings for user ID: {}")
                    .addArgument(nucleusMappingsToCreate.stream().map(NucleusUserMappingInput::userId).collect(Collectors.joining(",")))
                    .log();
        }
        LOGGER.atDebug()
                .setMessage("Created {} user mappings in nucleus")
                .addArgument(nucleusMappingsToCreate.size())
                .log();
    }
}
