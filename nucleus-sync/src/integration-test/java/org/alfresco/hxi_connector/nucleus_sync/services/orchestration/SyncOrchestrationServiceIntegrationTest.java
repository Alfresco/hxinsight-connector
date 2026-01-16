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
package org.alfresco.hxi_connector.nucleus_sync.services.orchestration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.GroupMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserGroupMembershipSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.util.UserGroupMembershipService;

@SpringBootTest(classes = SyncOrchestrationService.class)
public class SyncOrchestrationServiceIntegrationTest
{
    @MockitoBean
    private AlfrescoClient alfrescoClient;

    @MockitoBean
    private NucleusClient nucleusClient;

    @MockitoBean
    private UserGroupMembershipService userGrpMembershipService;

    @MockitoBean
    private UserMappingSyncProcessor userMappingSyncProcessor;

    @MockitoBean
    private GroupMappingSyncProcessor groupMappingSyncProcessor;

    @MockitoBean
    private UserGroupMembershipSyncProcessor userGroupMembershipSyncProcessor;

    @Autowired
    private SyncOrchestrationService service;

    @Test
    void shouldExecuteFullSyncSuccessfully()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("asmith", "alice@email.com", true, "Alice", "Smith", "Alice Smith"));
        List<IamUser> iamUsers = List.of(
                new IamUser("alice@email.com", "uuid-alice", "alice@email.com"));
        List<NucleusUserMappingOutput> currentUserMappings = List.of();
        List<NucleusGroupOutput> currentGroups = List.of();
        List<NucleusGroupMembershipOutput> currentMemberships = List.of();

        when(alfrescoClient.getAllUsers()).thenReturn(alfrescoUsers);
        when(nucleusClient.getAllIamUsers()).thenReturn(iamUsers);
        when(nucleusClient.getCurrentUserMappings()).thenReturn(currentUserMappings);
        when(nucleusClient.getAllExternalGroups()).thenReturn(currentGroups);
        when(nucleusClient.getCurrentGroupMemberships()).thenReturn(currentMemberships);

        List<UserMapping> userMappings = List.of(
                new UserMapping("alice@email.com", "alice", "iam-alice"));
        when(userMappingSyncProcessor.syncUserMappings(alfrescoUsers, iamUsers, currentUserMappings))
                .thenReturn(userMappings);

        Map<String, List<String>> userGroupMemberships = Map.of(
                "alice", List.of("GROUP_ADMINS"));
        when(userGrpMembershipService.buildUserGroupMemberships(userMappings))
                .thenReturn(userGroupMemberships);

        List<String> groupMappings = List.of("GROUP_ADMINS");
        when(groupMappingSyncProcessor.syncGroupMappings(currentGroups, userGroupMemberships))
                .thenReturn(groupMappings);

        // When
        String result = service.performFullSync();

        // Then - Verify all sync steps executed in order
        InOrder inOrder = inOrder(
                alfrescoClient,
                nucleusClient,
                userMappingSyncProcessor,
                userGrpMembershipService,
                groupMappingSyncProcessor,
                userGroupMembershipSyncProcessor);

        inOrder.verify(alfrescoClient).getAllUsers();
        inOrder.verify(nucleusClient).getAllIamUsers();
        inOrder.verify(nucleusClient).getCurrentUserMappings();
        inOrder.verify(nucleusClient).getAllExternalGroups();
        inOrder.verify(nucleusClient).getCurrentGroupMemberships();
        inOrder.verify(userMappingSyncProcessor).syncUserMappings(alfrescoUsers,
                iamUsers, currentUserMappings);
        inOrder.verify(userGrpMembershipService).buildUserGroupMemberships(userMappings);
        inOrder.verify(groupMappingSyncProcessor).syncGroupMappings(currentGroups, userGroupMemberships);
        inOrder.verify(userGroupMembershipSyncProcessor).syncUserGroupMemberships(
                userMappings, groupMappings, currentMemberships, userGroupMemberships);

        assertThat(result).isEqualTo("Sync completed successfully");

        // Verify status
        Map<String, Object> status = service.getSyncStatus();
        assertThat(status.get("syncInProgress")).isEqualTo(false);
        assertThat(status.get("lastSyncResult")).isEqualTo("Sync completed successfully");
        assertThat(status.get("alfrescoStatus")).isEqualTo("HEALTHY");
        assertThat(status.get("nucleusStatus")).isEqualTo("HEALTHY");
    }
}
