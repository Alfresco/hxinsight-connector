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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoGroup;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.GroupMapping;

@ExtendWith(MockitoExtension.class)
public class GroupMappingSyncProcessorTest
{
    @Mock
    private NucleusClient nucleusClient;

    @InjectMocks
    private GroupMappingSyncProcessor processor;

    @Test
    void shouldIgnoreGroupsWithNoUserMemberships()
    {
        // Given
        List<AlfrescoGroup> alfrescoGroups = List.of(
                new AlfrescoGroup("GROUP_ADMINISTRATORS", "Administrators"),
                new AlfrescoGroup("GROUP_DEVELOPERS", "Developers"),
                new AlfrescoGroup("GROUP_EMPTY", "Empty Group"));
        List<NucleusGroupOutput> nucleusGroups = new ArrayList<>();
        Map<String, List<String>> userGroupMemberships = new HashMap<>();
        userGroupMemberships.put("user1", List.of("GROUP_ADMINISTRATORS"));
        userGroupMemberships.put("user2", List.of("GROUP_DEVELOPERS"));

        // When
        List<GroupMapping> result = processor.syncGroupMappings(alfrescoGroups, nucleusGroups, userGroupMemberships);

        // Then
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new GroupMapping("GROUP_ADMINISTRATORS", "Administrators"),
                        new GroupMapping("GROUP_DEVELOPERS", "Developers"));
        verify(nucleusClient).createGroups(argThat(groups -> groups.size() == 2));
    }

    @Test
    void shouldKeepExistingMappingsUnchanged()
    {
        // Given
        List<AlfrescoGroup> alfrescoGroups = List.of(
                new AlfrescoGroup("GROUP_ADMINISTRATORS", "Administrators"));
        List<NucleusGroupOutput> nucleusGroups = List.of(
                new NucleusGroupOutput("GROUP_ADMINISTRATORS"));
        Map<String, List<String>> userGroupMemberships = new HashMap<>();
        userGroupMemberships.put("user1", List.of("GROUP_ADMINISTRATORS"));

        // When
        List<GroupMapping> result = processor.syncGroupMappings(alfrescoGroups, nucleusGroups, userGroupMemberships);

        // Then
        assertThat(result)
                .hasSize(1)
                .containsExactly(new GroupMapping("GROUP_ADMINISTRATORS", "Administrators"));
        verify(nucleusClient, never()).deleteGroup(any());
        verify(nucleusClient, never()).createGroups(anyList());
    }

    @Test
    void shouldHandleEmptyInputsGracefully()
    {
        // Given
        List<AlfrescoGroup> alfrescoGroups = new ArrayList<>();
        List<NucleusGroupOutput> nucleusGroups = new ArrayList<>();
        Map<String, List<String>> userGroupMemberships = new HashMap<>();

        // When
        List<GroupMapping> result = processor.syncGroupMappings(
                alfrescoGroups, nucleusGroups, userGroupMemberships);

        // Then
        assertThat(result).isEmpty();
        verify(nucleusClient, never()).deleteGroup(any());
        verify(nucleusClient, never()).createGroups(anyList());
    }
}
