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
package org.alfresco.hxi_connector.nucleus_sync.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.model.UserGroupMembership;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@ExtendWith(MockitoExtension.class)
class UserGroupMembershipSyncProcessorTest
{
    @Mock
    private NucleusClient nucleusClient;

    @InjectMocks
    private UserGroupMembershipSyncProcessor processor;

    @Captor
    private ArgumentCaptor<List<NucleusGroupMemberAssignmentInput>> assignmentCaptor;

    @Captor
    private ArgumentCaptor<List<String>> removalCaptor;

    private UserMapping jdoeMapping;
    private UserMapping asmithMapping;
    private GroupMapping adminsGroupMapping;
    private GroupMapping developersGroupMapping;

    @BeforeEach
    void setUp()
    {
        jdoeMapping = new UserMapping("jane.doe@email.com", "jdoe", "uuid-jdoe");
        asmithMapping = new UserMapping("alice.smith@email.com", "asmith", "uuid-asmith");
        adminsGroupMapping = new GroupMapping("GROUP_ADMINS", "Admins");
        developersGroupMapping = new GroupMapping("GROUP_DEVELOPERS", "Developers");
    }

    @Test
    void shouldCreateNewMembershipsWhenUserHasGroupsNotInNucleus()
    {
        // Given
        List<UserMapping> userMappings = List.of(jdoeMapping);
        List<GroupMapping> groupMappings = List.of(adminsGroupMapping, developersGroupMapping);
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of();
        Map<String, List<String>> userGroupCache = Map.of(
                "jdoe", List.of("GROUP_ADMINS", "GROUP_DEVELOPERS"));

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient).assignGroupMembers(assignmentCaptor.capture());
        verify(nucleusClient, never()).removeGroupMembers(anyString(), anyList());

        List<NucleusGroupMemberAssignmentInput> assignments = assignmentCaptor.getValue();
        assertThat(assignments).hasSize(2);
        assertThat(assignments).containsExactlyInAnyOrder(
                new NucleusGroupMemberAssignmentInput("GROUP_ADMINS", "jdoe"),
                new NucleusGroupMemberAssignmentInput("GROUP_DEVELOPERS", "jdoe"));

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(
                new UserGroupMembership("jdoe", "GROUP_ADMINS", "jane.doe@email.com"),
                new UserGroupMembership("jdoe", "GROUP_DEVELOPERS", "jane.doe@email.com"));
    }

    @Test
    void shouldRemoveMembershipsWhenUserNoLongerBelongsToGroup()
    {
        // Given
        List<UserMapping> userMappings = List.of(jdoeMapping);
        List<GroupMapping> groupMappings = List.of(adminsGroupMapping, developersGroupMapping);
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of(
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "jdoe"),
                new NucleusGroupMembershipOutput("GROUP_DEVELOPERS", "jdoe"));
        Map<String, List<String>> userGroupCache = Map.of(
                "jdoe", List.of("GROUP_ADMINS"));

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient, never()).assignGroupMembers(anyList());
        verify(nucleusClient).removeGroupMembers(eq("GROUP_DEVELOPERS"), removalCaptor.capture());

        assertThat(removalCaptor.getValue()).containsExactly("jdoe");
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(
                new UserGroupMembership("jdoe", "GROUP_ADMINS", "jane.doe@email.com"));
    }

    @Test
    void shouldHandleBothCreationAndRemovalForDifferentUsers()
    {
        // Given
        List<UserMapping> userMappings = List.of(jdoeMapping, asmithMapping);
        List<GroupMapping> groupMappings = List.of(adminsGroupMapping, developersGroupMapping);
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of(
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "jdoe"));
        Map<String, List<String>> userGroupCache = Map.of(
                "jdoe", List.of("GROUP_DEVELOPERS"),
                "asmith", List.of("GROUP_ADMINS"));

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient).assignGroupMembers(assignmentCaptor.capture());
        verify(nucleusClient).removeGroupMembers(eq("GROUP_ADMINS"), removalCaptor.capture());

        List<NucleusGroupMemberAssignmentInput> assignments = assignmentCaptor.getValue();
        assertThat(assignments).hasSize(2);
        assertThat(assignments).containsExactlyInAnyOrder(
                new NucleusGroupMemberAssignmentInput("GROUP_DEVELOPERS", "jdoe"),
                new NucleusGroupMemberAssignmentInput("GROUP_ADMINS", "asmith"));

        assertThat(removalCaptor.getValue()).containsExactly("jdoe");
        assertThat(result).hasSize(2);
    }

    @Test
    void shouldIgnoreUntrackedGroupsInCache()
    {
        // Given
        List<UserMapping> userMappings = List.of(jdoeMapping);
        List<GroupMapping> groupMappings = List.of(adminsGroupMapping);
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of();
        Map<String, List<String>> userGroupCache = Map.of(
                "jdoe", List.of("GROUP_ADMINS", "GROUP_UNTRACKED"));

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient).assignGroupMembers(assignmentCaptor.capture());

        assertThat(assignmentCaptor.getValue()).hasSize(1);
        assertThat(assignmentCaptor.getValue()).containsExactly(
                new NucleusGroupMemberAssignmentInput("GROUP_ADMINS", "jdoe"));
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldHandleNoChangesWhenStatesMatch()
    {
        // Given
        List<UserMapping> userMappings = List.of(jdoeMapping);
        List<GroupMapping> groupMappings = List.of(adminsGroupMapping);
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of(
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "jdoe"));
        Map<String, List<String>> userGroupCache = Map.of(
                "jdoe", List.of("GROUP_ADMINS"));

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient, never()).assignGroupMembers(anyList());
        verify(nucleusClient, never()).removeGroupMembers(anyString(), anyList());
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldHandleEmptyInputsGracefully()
    {
        // Given
        List<UserMapping> userMappings = List.of();
        List<GroupMapping> groupMappings = List.of();
        List<NucleusGroupMembershipOutput> currentNucleusMemberships = List.of();
        Map<String, List<String>> userGroupCache = Map.of();

        // When
        List<UserGroupMembership> result = processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentNucleusMemberships, userGroupCache);

        // Then
        verify(nucleusClient, never()).assignGroupMembers(anyList());
        verify(nucleusClient, never()).removeGroupMembers(anyString(), anyList());
        assertThat(result).isEmpty();
    }
}
