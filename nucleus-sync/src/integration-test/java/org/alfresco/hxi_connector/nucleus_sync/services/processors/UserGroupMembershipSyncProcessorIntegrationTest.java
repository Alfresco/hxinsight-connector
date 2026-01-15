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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@SpringBootTest(classes = UserGroupMembershipSyncProcessor.class)
public class UserGroupMembershipSyncProcessorIntegrationTest
{
    @MockitoBean
    private NucleusClient nucleusClient;

    @Autowired
    private UserGroupMembershipSyncProcessor processor;

    @Captor
    private ArgumentCaptor<List<NucleusGroupMemberAssignmentInput>> assignmentCaptor;

    @Test
    void shouldSyncMembershipsWithCreatesDeletesAndUnchanged()
    {
        // Given
        List<UserMapping> userMappings = List.of(
                new UserMapping("alice@email.com", "alice", "uuid-alice"),
                new UserMapping("bob@email.com", "bob", "uuid-bob"),
                new UserMapping("charlie@email.com", "charlie", "uuid-charlie"));

        List<String> groupMappings = List.of(
                "GROUP_ADMINS",
                "GROUP_DEVELOPERS",
                "GROUP_DESIGNERS");

        // Current state in Nucleus:
        // - alice - ADMINS
        // - bob - DEVELOPERS
        List<NucleusGroupMembershipOutput> currentMemberships = List.of(
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "alice"),
                new NucleusGroupMembershipOutput("GROUP_DEVELOPERS", "bob"));

        // Desired state from Alfresco:
        // - alice should be in DEVELOPERS (chnage of group)
        // - bob should be in DEVELOPERS (unchanged)
        // - charlie should be in DESIGNERS (new user)
        Map<String, List<String>> userGroupCache = Map.of(
                "alice", List.of("GROUP_DEVELOPERS"),
                "bob", List.of("GROUP_DEVELOPERS"),
                "charlie", List.of("GROUP_DESIGNERS"));

        // When
        processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentMemberships, userGroupCache);

        // Then
        verify(nucleusClient).assignGroupMembers(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue())
                .containsExactlyInAnyOrder(
                        new NucleusGroupMemberAssignmentInput("GROUP_DEVELOPERS", "alice"),
                        new NucleusGroupMemberAssignmentInput("GROUP_DESIGNERS", "charlie"));

        // Then
        verify(nucleusClient).removeGroupMembers(
                eq("GROUP_ADMINS"),
                argThat(users -> users.size() == 1 && users.contains("alice")));
    }
}
