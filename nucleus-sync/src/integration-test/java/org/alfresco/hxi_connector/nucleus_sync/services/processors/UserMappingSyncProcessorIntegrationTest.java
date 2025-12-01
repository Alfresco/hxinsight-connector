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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@SpringBootTest(classes = UserMappingSyncProcessor.class)
public class UserMappingSyncProcessorIntegrationTest
{
    @Autowired
    private UserMappingSyncProcessor processor;

    @MockitoBean
    private NucleusClient nucleusClient;

    @Test
    void shouldPerformCompleteUserMappingSyncWithCorrectApiCalls()
    {
        // Given - Realistic scenario with creates, deletes, and unchanged mappings
        List<AlfrescoUser> alfrescoUsers = List.of(
                // new mapping - create
                new AlfrescoUser("jdoe", "john.doe@company.com", true, "John", "Doe", "John Doe"),
                // new mapping - create
                new AlfrescoUser("sjohnson", "sarah.johnson@company.com", true, "Sarah", "Johnson", "Sarah Johnson"),
                // new mapping - create
                new AlfrescoUser("rbrown", "robert.brown@company.com", true, "Robert", "Brown", "Robert Brown"),
                // existing mapping - keep
                new AlfrescoUser("moliver", "michael.oliver@company.com", true, "Michael", "Oliver", "Michael Oliver"),
                // existing mapping - keep
                new AlfrescoUser("ewilson", "emma.wilson@company.com", true, "Emma", "Wilson", "Emma Wilson"),
                // No nucleus match, ignore
                new AlfrescoUser("ataylor", "anthony.taylor@company.com", true, "Anthony", "Taylor", "Anthony Taylor"),
                // No email, ignore
                new AlfrescoUser("dcoot", null, true, "David", "Coot", "David Coot"));

        List<IamUser> nucleusUsers = List.of(
                // New mapping to create
                new IamUser("john.doe@company.com", "be81a981-3726-483e-b9b1-ecf1d3f36b7d", "john.doe@company.com"),
                // New mapping to create
                new IamUser("sarah.johnson@company.com", "7c92b082-4837-594f-c0c2-fdf2e4g47c8e", "sarah.johnson@company.com"),
                // New mapping to create
                new IamUser("robert.brown@company.com", "8da3c193-5948-6a5g-d1d3-geg3f5h58d9f", "robert.brown@company.com"),
                // Existing mapping, keep
                new IamUser("michael.oliver@company.com", "6b73fd36-d76e-40b7-8624-2d897f35603c", "michael.oliver@company.com"),
                // Existing mapping, keep
                new IamUser("emma.wilson@company.com", "9eb4d2a4-6a59-7b6h-e2e4-hfh4g6i69eag", "emma.wilson@company.com"),
                // No alfresco match, ignore
                new IamUser("user5@company.com", "2bd22c19-91e7-4002-8f26-4dfeb3fee12f", "user5@company.com"));

        List<NucleusUserMappingOutput> currentMappings = List.of(
                // Keep - still valid
                new NucleusUserMappingOutput("michael.oliver@company.com", "moliver"),
                // Keep - still valid
                new NucleusUserMappingOutput("emma.wilson@company.com", "ewilson"),
                // Delete - stale mapping (user left company)
                new NucleusUserMappingOutput("mark.clattenburg", "mclattenburg"),
                // Delete - stale mapping (user no longer in alfresco)
                new NucleusUserMappingOutput("old.user@company.com", "ouser"),
                // Delete - stale mapping (orphaned)
                new NucleusUserMappingOutput("inactive.user@company.com", "iuser"));

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then - Verify all deletions happened
        verify(nucleusClient).deleteUserMapping("mclattenburg");
        verify(nucleusClient).deleteUserMapping("ouser");
        verify(nucleusClient).deleteUserMapping("iuser");

        // Then - Verify creations with correct payload (3 new mappings)
        verify(nucleusClient).createUserMappings(argThat(mappings -> mappings.size() == 3 &&
                mappings.stream().anyMatch(m -> m.userId().equals("be81a981-3726-483e-b9b1-ecf1d3f36b7d") &&
                        m.externalUserId().equals("jdoe"))
                &&
                mappings.stream().anyMatch(m -> m.userId().equals("7c92b082-4837-594f-c0c2-fdf2e4g47c8e") &&
                        m.externalUserId().equals("sjohnson"))
                &&
                mappings.stream().anyMatch(m -> m.userId().equals("8da3c193-5948-6a5g-d1d3-geg3f5h58d9f") &&
                        m.externalUserId().equals("rbrown"))));

        // Then - Verify returned mappings are correct (5 total: 3 new + 2 existing)
        assertThat(result)
                .hasSize(5)
                .anySatisfy(m -> {
                    assertThat(m.getEmail()).isEqualTo("john.doe@company.com");
                    assertThat(m.getAlfrescoUserId()).isEqualTo("jdoe");
                    assertThat(m.getNucleusUserId()).isEqualTo("be81a981-3726-483e-b9b1-ecf1d3f36b7d");
                })
                .anySatisfy(m -> {
                    assertThat(m.getEmail()).isEqualTo("sarah.johnson@company.com");
                    assertThat(m.getAlfrescoUserId()).isEqualTo("sjohnson");
                    assertThat(m.getNucleusUserId()).isEqualTo("7c92b082-4837-594f-c0c2-fdf2e4g47c8e");
                })
                .anySatisfy(m -> {
                    assertThat(m.getEmail()).isEqualTo("robert.brown@company.com");
                    assertThat(m.getAlfrescoUserId()).isEqualTo("rbrown");
                    assertThat(m.getNucleusUserId()).isEqualTo("8da3c193-5948-6a5g-d1d3-geg3f5h58d9f");
                })
                .anySatisfy(m -> {
                    assertThat(m.getEmail()).isEqualTo("michael.oliver@company.com");
                    assertThat(m.getAlfrescoUserId()).isEqualTo("moliver");
                    assertThat(m.getNucleusUserId()).isEqualTo("6b73fd36-d76e-40b7-8624-2d897f35603c");
                })
                .anySatisfy(m -> {
                    assertThat(m.getEmail()).isEqualTo("emma.wilson@company.com");
                    assertThat(m.getAlfrescoUserId()).isEqualTo("ewilson");
                    assertThat(m.getNucleusUserId()).isEqualTo("9eb4d2a4-6a59-7b6h-e2e4-hfh4g6i69eag");
                });

        // Then - Verify exactly 4 interactions (3 deletes, 1 create batch)
        verify(nucleusClient, times(3)).deleteUserMapping(anyString());
        verify(nucleusClient, times(1)).createUserMappings(anyList());
    }
}
