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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@ExtendWith(MockitoExtension.class)
public class UserMappingSyncProcessorTest
{
    @Mock
    private NucleusClient nucleusClient;

    @InjectMocks
    private UserMappingSyncProcessor processor;

    @Test
    void shouldIgnoreAlfrescoUsersWithInvalidEmail()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("jdoe", null, true, "John", "Doe", "John Doe"),
                new AlfrescoUser("rbrown", "robert.brown@email.com", true, "Robert", "Brown", "Robert Brown"),
                new AlfrescoUser("ewilson", "", true, "Emma", "Wilson", "Emma Wilson"));
        List<IamUser> nucleusUsers = List.of(
                new IamUser("robert.brown@email.com", "ecff0ce9-5da0-4c72-8942-f66111651712", "robert.brown@email.com"));
        List<NucleusUserMappingOutput> currentMappings = new ArrayList<>();

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then
        assertThat(result)
                .containsExactlyInAnyOrder(
                        new UserMapping("robert.brown@email.com", "rbrown", "ecff0ce9-5da0-4c72-8942-f66111651712"));

        verify(nucleusClient).createUserMappings(argThat(mappings -> mappings.size() == 1));
    }

    @Test
    void shouldHandleEmptyInputsGracefully()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = new ArrayList<>();
        List<IamUser> nucleusUsers = new ArrayList<>();
        List<NucleusUserMappingOutput> currentMappings = new ArrayList<>();

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then
        assertThat(result).isEmpty();
        verify(nucleusClient, never()).deleteUserMapping(any());
        verify(nucleusClient, never()).createUserMappings(anyList());
    }

    @Test
    void shouldCreateAllMappingsInSingleBatchCall()
    {
        // Given - Multiple new mappings to create
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("jdoe", "john.doe@email.com", true, "John", "Doe", "John Doe"),
                new AlfrescoUser("moliver", "michael.oliver@email.com", true, "Michael", "Oliver", "Michael Oliver"),
                new AlfrescoUser("ewilson", "emma.wilson@email.com", true, "Emma", "Wilson", "Emma Wilson"));

        List<IamUser> nucleusUsers = List.of(
                new IamUser("john.doe@email.com", "f80c46b6-0eed-4408-8652-fd9957004e6f", "john.doe@email.com"),
                new IamUser("michael.oliver@email.com", "2ce4ba51-351e-4e77-8a4b-124e696efa27", "michael.oliver@email.com"),
                new IamUser("emma.wilson@email.com", "3dcea0dd-1aea-4f46-af04-f15b5f7b4890", "emma.wilson@email.com"));

        List<NucleusUserMappingOutput> currentMappings = new ArrayList<>();

        // When
        processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then - Verify single batch call with all mappings
        verify(nucleusClient, times(1)).createUserMappings(argThat(mappings -> mappings.size() == 3));
        verify(nucleusClient, never()).deleteUserMapping(any());
    }
}
