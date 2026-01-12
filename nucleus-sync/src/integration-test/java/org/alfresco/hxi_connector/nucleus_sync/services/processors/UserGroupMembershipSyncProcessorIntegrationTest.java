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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@SpringBootTest(classes = UserGroupMembershipSyncProcessor.class)
class UserGroupMembershipSyncProcessorIntegrationTest
{
    @MockitoBean
    private NucleusClient nucleusClient;

    @Autowired
    private UserGroupMembershipSyncProcessor processor;

    @Test
    void shouldHandleLargeScaleSynchronization()
    {
        // Given - 100 users, 10 groups, mixed operations
        List<UserMapping> userMappings = new ArrayList<>();
        List<String> groupMappings = new ArrayList<>();
        List<NucleusGroupMembershipOutput> currentMemberships = new ArrayList<>();
        Map<String, List<String>> userGroupCache = new HashMap<>();

        for (int i = 0; i < 10; i++)
        {
            groupMappings.add("GROUP_" + i);
        }

        for (int i = 0; i < 100; i++)
        {
            String userId = "user" + i;
            userMappings.add(new UserMapping(userId + "@email.com", userId, "uuid-" + i));

            // Each user belongs to 3 random groups
            List<String> groups = new ArrayList<>();
            for (int j = 0; j < 3; j++)
            {
                groups.add("GROUP_" + ((i + j) % 10));
            }
            userGroupCache.put(userId, groups);

            // Half have existing memberships (some correct, some to remove)
            if (i % 2 == 0)
            {
                currentMemberships.add(new NucleusGroupMembershipOutput("GROUP_" + (i % 10), userId));
            }
        }

        // When
        processor.syncUserGroupMemberships(userMappings, groupMappings, currentMemberships, userGroupCache);

        // Then
        verify(nucleusClient).assignGroupMembers(any());
    }
}
