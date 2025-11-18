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
import static org.mockito.Mockito.*;

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
                .hasSize(1)
                .extracting(UserMapping::getAlfrescoUserId)
                .containsExactly("rbrown");
        verify(nucleusClient).createUserMappings(argThat(mappings -> mappings.size() == 1));
    }

    @Test
    void shouldDeleteStaleMappingsWhenUserRemovedFromAlfresco()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = new ArrayList<>();
        List<IamUser> nucleusUsers = List.of(
                new IamUser("user@email.com", "4c0c7948-cd4d-4ad2-949c-923737c40150", "user@email.com"));
        List<NucleusUserMappingOutput> currentMappings = List.of(
                new NucleusUserMappingOutput("nucleus1", "alfresco1"));

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then
        assertThat(result).isEmpty();
        verify(nucleusClient).deleteUserMapping("alfresco1");
        verify(nucleusClient, never()).createUserMappings(anyList());
    }

    @Test
    void shouldDeleteStaleMappingsWhenUserRemovedFromNucleus()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("rbrown", "robert.brown@email.com", true, "Robert", "Brown", "Robert Brown"));
        List<IamUser> nucleusUsers = new ArrayList<>();
        List<NucleusUserMappingOutput> currentMappings = List.of(
                new NucleusUserMappingOutput("nucleus1", "alfresco1"));

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then
        assertThat(result).isEmpty();
        verify(nucleusClient).deleteUserMapping("alfresco1");
        verify(nucleusClient, never()).createUserMappings(anyList());
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
    void shouldNotCreateMappingWhenUserExistsOnlyInAlfresco()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("rbrown", "robert.brown@email.com", true, "Robert", "Brown", "Robert Brown"));
        List<IamUser> nucleusUsers = new ArrayList<>();
        List<NucleusUserMappingOutput> currentMappings = new ArrayList<>();

        // When
        List<UserMapping> result = processor.syncUserMappings(alfrescoUsers, nucleusUsers, currentMappings);

        // Then
        assertThat(result).isEmpty();
        verify(nucleusClient, never()).createUserMappings(anyList());
    }
}
