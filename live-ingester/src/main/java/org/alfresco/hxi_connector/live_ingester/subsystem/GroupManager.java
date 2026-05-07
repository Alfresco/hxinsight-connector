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

import lombok.Data;
import org.alfresco.hxi_connector.live_ingester.subsystem.dto.Group;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupOutput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

import java.util.List;
import java.util.Optional;

/**
 * Group Related Operations
 */
@Data
public class GroupManager {
    private final NucleusClient nucleusClient;
    private static final String EXTENDED_TO_PATH_ATTRIBUTE = "toPath";
    private final MappingManager mappingManager;

    // MAPPING NOT Available for the Groups Directly created
    public void mapGroup(String externalId, String id) {
        // Logic to create a new group
    }

    public void deleteGroup(String externalId) {
        // Logic to delete a group
        nucleusClient.deleteGroup(externalId); // Delete the group from nucleus
    }

    public Group fetchGroupMapping(String externalId) {
        // Logic to fetch group mapping details
        return null;
    }

    // Check if group has been deleted or updated
    public void handleDeletionOrUpdation(RepoEvent<DataAttributes<NodeResource>> event) {
        /**
         * THERE IS A POSSIBILITY IF I ADD A GROUP INTO ANOTHER THIS TO_PATH CAN BE CHANGED
         * TODO: Need to eliminate this extended attribute toPath to identify the group related issue
         */
        if (!mappingManager.isGroupMembershipUpdated(event) && event.getExtensionAttributes().getExtension(EXTENDED_TO_PATH_ATTRIBUTE) != null) {
            // Group has been deleted, perform necessary operations such as deleting the group from nucleus as well
            deleteGroup(event.getData().getResource().getId());
        }
    }

    public void createGroup(NodeResource nodeResource) {
        Optional<NucleusGroupOutput> group = nucleusClient.getGroupByExternalId(nodeResource.getId());
        if (group.isPresent()) {
            // group already exists, no need to perform any operation as of now
            return;
        }
        // create group in nucleus
        nucleusClient.createGroups(List.of(
                new NucleusGroupInput(nodeResource.getId())));
    }
}