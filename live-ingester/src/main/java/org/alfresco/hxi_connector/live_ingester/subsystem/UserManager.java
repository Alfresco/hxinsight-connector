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
package org.alfresco.hxi_connector.live_ingester.subsystem;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusSCIMResponse;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusUserMappingInput;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusUserMappingOutput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

import java.util.List;
import java.util.Optional;

import static org.alfresco.hxi_connector.live_ingester.subsystem.AuthorizationConstants.*;

/**
 * Mostly Will handle user related operations, such as fetching user details, syncing users from external sources, managing user roles and permissions, etc. It may also interact with other components of the system to ensure that user data is consistent and up-to-date across the application.
 */
@Data
@Slf4j
public class UserManager {
    private final NucleusClient nucleusClient;
    private final MappingManager mappingManager;


    public void mapUser(NucleusUserMappingInput userMappingInput) {
        // Logic to create a new user
            nucleusClient.createUserMappings(List.of(userMappingInput));
    }

    public void deleteUser(String externalId) {
        // Logic to delete a user

    }

    public Optional<IamUser> fetchUserByEmailId(String emailId) {
        if(StringUtil.isNullOrEmpty(emailId)){
            log.error("Email Id is null or empty, can't fetch user details");
            return Optional.empty();
        }
        // Logic to fetch user details by email id
        Optional<List<NucleusSCIMResponse.Resource>> userResources = nucleusClient.getUserByEmailId(emailId);
        if(userResources.isEmpty() || userResources.get().isEmpty()){return Optional.empty();}
        NucleusSCIMResponse.Resource userResource = userResources.get().get(0); // HxIAM has only one User Mapped to a single mail

        return
                Optional.of(new IamUser(userResource.userName(), userResource.id(), userResource.emails().get(0).value())); // considering only single mail user for now
    }

    public Optional<NucleusUserMappingOutput> fetchUserMapping(String externalId) {
        // Logic to fetch user mapping details
        log.debug("Fetching user mapping for id: {}", externalId);
        return nucleusClient.fetchUserMappingByExternalUserId(externalId);
    }

    // This External Id is the Actual Alfresco Id which is External to the Nucleus
    public void createOrMapUser(String emailId, String externalId) {
        // check if the user exists in the mapping
        Optional<NucleusUserMappingOutput> user = fetchUserMapping(externalId);
        if(user.isPresent()){
            // If user mapping exists, no need to create or map user, we can return
            log.debug("User mapping for id: {} already exists", externalId);
            return;
        }
        // If the mapping does not exist, we need to check if the user exists by email id
        Optional<IamUser> iamUser = fetchUserByEmailId(emailId);
        if(iamUser.isPresent()){
            log.debug("User with email id: {} exists in HxIAM with user id: {}. Creating mapping for this user.", emailId, iamUser.get().userId());
            mapUser(new NucleusUserMappingInput(iamUser.get().userId(), externalId));
        }
        else{
            throw new IamSyncException("No user found with email id So can't map" + emailId);
        }
        // If there is no user with this email id, then we can create a new user and mapping both
    }

    /**
     * Only Update User if Added or Removed from any Group
     * @param event
     */
    public void handleUpdateOrDelete(RepoEvent<DataAttributes<NodeResource>> event) {
        if(event.getType().equals(EVENT_DELETE_TYPE)){ // event deleted
            log.debug("Deleting user mapping for id: {}", event.getId());

            deleteUser(fetchUserId(event));
        }
        else if(event.getData().getResource().getNodeType().equals(PERSON_TYPE)){ // Only take `cm:user` events not other types
            // For Person only
            log.debug("Updating user mapping for id: {}", event.getId());
            mappingManager.updateGroupMapping(event);
        }
    }

    /**
     *Remove mapping if have by external Id.
     * @param resource
     */
    public void handleDeletionOperation(NodeResource resource){
        nucleusClient.deleteUserMapping(resource.getId());
    }

}
