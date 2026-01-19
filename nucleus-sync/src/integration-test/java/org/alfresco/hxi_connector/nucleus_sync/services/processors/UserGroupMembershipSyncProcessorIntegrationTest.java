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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Captor
    private ArgumentCaptor<String> groupIdCaptor;

    @Captor
    private ArgumentCaptor<List<String>> userIdsCaptor;

    @Test
    void shouldSyncMembershipsWithCreatesDeletesAndUnchanged()
    {
        // Given
        List<UserMapping> userMappings = List.of(
                new UserMapping("alice@email.com", "alice", "uuid-alice"),
                new UserMapping("bob@email.com", "bob", "uuid-bob"),
                new UserMapping("charlie@email.com", "charlie", "uuid-charlie"),
                new UserMapping("dave@email.com", "dave", "uuid-dave"));

        List<String> groupMappings = List.of(
                "GROUP_ADMINS",
                "GROUP_DEVELOPERS",
                "GROUP_DESIGNERS");

        // Current state in Nucleus:
        // - alice - ADMINS
        // - bob - DEVELOPERS
        // - dave - ADMINS, DEVELOPERS
        List<NucleusGroupMembershipOutput> currentMemberships = List.of(
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "alice"),
                new NucleusGroupMembershipOutput("GROUP_DEVELOPERS", "bob"),
                new NucleusGroupMembershipOutput("GROUP_ADMINS", "dave"),
                new NucleusGroupMembershipOutput("GROUP_DEVELOPERS", "dave"));

        // Desired state from Alfresco:
        // - alice should be in DEVELOPERS (chnage of group)
        // - bob should be in DEVELOPERS (unchanged)
        // - charlie should be in DESIGNERS and DEVELOPERS (new user, multiple groups)
        // - dave should be removed from all group
        Map<String, List<String>> userGroupMap = Map.of(
                "alice", List.of("GROUP_DEVELOPERS"),
                "bob", List.of("GROUP_DEVELOPERS"),
                "charlie", List.of("GROUP_DESIGNERS", "GROUP_DEVELOPERS"));
        // dave not in cache - removed from all groups

        // When
        processor.syncUserGroupMemberships(
                userMappings, groupMappings, currentMemberships, userGroupMap);

        // Then
        verify(nucleusClient).assignGroupMembers(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue())
                .containsExactlyInAnyOrder(
                        new NucleusGroupMemberAssignmentInput("GROUP_DEVELOPERS", "alice"),
                        new NucleusGroupMemberAssignmentInput("GROUP_DESIGNERS", "charlie"),
                        new NucleusGroupMemberAssignmentInput("GROUP_DEVELOPERS", "charlie"));

        // Then - verify removals
        verify(nucleusClient, times(2)).removeGroupMembers(
                groupIdCaptor.capture(),
                userIdsCaptor.capture());

        // Build a map for easier verification
        Map<String, List<String>> removalsByGroup = IntStream.range(0, groupIdCaptor.getAllValues().size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> groupIdCaptor.getAllValues().get(i),
                        i -> userIdsCaptor.getAllValues().get(i)));

        // Verify removals
        assertThat(removalsByGroup.get("GROUP_ADMINS"))
                .containsExactlyInAnyOrder("alice", "dave");
        assertThat(removalsByGroup.get("GROUP_DEVELOPERS"))
                .containsExactly("dave");
    }
}
