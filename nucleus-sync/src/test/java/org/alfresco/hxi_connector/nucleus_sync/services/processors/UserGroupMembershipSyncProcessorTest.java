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

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@ExtendWith(MockitoExtension.class)
public class UserGroupMembershipSyncProcessorTest
{
    @Mock
    private NucleusClient nucleusClient;

    private UserGroupMembershipSyncProcessor processor;

    @BeforeEach
    void setUp()
    {
        processor = new UserGroupMembershipSyncProcessor(nucleusClient, 1000);
    }

    @Test
    void shouldBatchCreateOperationsWhenExceedingBatchSize()
    {
        // Given
        List<UserMapping> userMappings = new ArrayList<>();
        Map<String, List<String>> userGroupCache = new HashMap<>();

        for (int i = 0; i < 500; i++)
        {
            String userId = "user" + i;
            userMappings.add(new UserMapping(userId + "@email.com", userId, "uuid-" + i));
            userGroupCache.put(userId, List.of("GROUP_A", "GROUP_B", "GROUP_C", "GROUP_D"));
        }

        // When
        processor.syncUserGroupMemberships(userMappings, List.of(), userGroupCache);

        // Then
        verify(nucleusClient, times(2)).assignGroupMembers(argThat(list -> list.size() <= 1000));
    }
}
