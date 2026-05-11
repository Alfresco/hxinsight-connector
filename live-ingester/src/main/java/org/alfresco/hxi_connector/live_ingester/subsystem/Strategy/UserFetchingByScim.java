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
package org.alfresco.hxi_connector.live_ingester.subsystem.Strategy;

import io.netty.util.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusSCIMResponse;

import java.util.List;
import java.util.Optional;

/**
 * Fetch Users Using SCIM Endpoint
 * NOTE: Don't Use it for now as HxIAM doesn't support filter for email
 */
@Data
@Slf4j
public class UserFetchingByScim implements UserFetchingStrategy {
    private final NucleusClient nucleusClient;


    @Override
    public Optional<IamUser> fetchUserByEmailId(String emailId) {
        // Implementation for fetching user by email ID using SCIM API
        return fetchUser(emailId);
    }

    private Optional<IamUser> fetchUser(String emailId) {
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
}
