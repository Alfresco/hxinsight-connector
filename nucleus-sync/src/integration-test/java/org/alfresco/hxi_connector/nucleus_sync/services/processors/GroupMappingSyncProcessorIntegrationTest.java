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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;

@SpringBootTest(classes = GroupMappingSyncProcessor.class)
public class GroupMappingSyncProcessorIntegrationTest
{
    @Autowired
    private GroupMappingSyncProcessor processor;

    @MockitoBean
    private NucleusClient nucleusClient;

    @Test
    void shouldSyncGroupMappingsWithCreatesDeletesAndUnchangedGroups()
    {
        // Given - A scenario with creates, deletes, and unchanged mappings
        List<NucleusGroupOutput> nucleusGroups = List.of(
                // Existing mapping, keep
                new NucleusGroupOutput("GROUP_HR"),
                // Delete - group removed from alfresco
                new NucleusGroupOutput("GROUP_SALES"));

        Map<String, List<String>> userGroupMemberships = new HashMap<>();
        userGroupMemberships.put("jdoe", List.of("GROUP_ENGINEERING", "GROUP_PRODUCT"));
        userGroupMemberships.put("bsmith", List.of("GROUP_ENGINEERING"));
        userGroupMemberships.put("mjohnson", List.of("GROUP_PRODUCT", "GROUP_HR"));
        // User with null groups
        userGroupMemberships.put("tgarcia", null);

        // When
        List<String> result = processor.syncGroupMappings(nucleusGroups, userGroupMemberships);

        // Then - Verify all deletions happened
        verify(nucleusClient).deleteGroup("GROUP_SALES");

        // Then - Verify creations with correct payload (3 new groups)
        verify(nucleusClient).createGroups(argThat(groups -> groups.size() == 2 &&
                groups.contains(new NucleusGroupInput("GROUP_ENGINEERING")) &&
                groups.contains(new NucleusGroupInput("GROUP_PRODUCT"))));

        // Then - Verify returned mappings are correct (5 total: 3 new + 2 existing)
        assertThat(result)
                .containsExactlyInAnyOrder("GROUP_ENGINEERING", "GROUP_PRODUCT", "GROUP_HR");

        // Then - Verify exactly 2 interactions (1 deletes, 1 create batch)
        verify(nucleusClient, times(1)).deleteGroup(anyString());
        verify(nucleusClient, times(1)).createGroups(anyList());
    }
}
