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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.AlfrescoUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.NucleusUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.GroupMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserGroupMembershipSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.util.UserGroupMembershipService;

@ExtendWith(MockitoExtension.class)
public class SyncOrchestrationServiceTest
{
    @Mock
    private AlfrescoClient alfrescoClient;
    @Mock
    private NucleusClient nucleusClient;
    @Mock
    private UserGroupMembershipService userGrpMembershipService;
    @Mock
    private UserMappingSyncProcessor userMappingSyncProcessor;
    @Mock
    private GroupMappingSyncProcessor groupMappingSyncProcessor;
    @Mock
    private UserGroupMembershipSyncProcessor userGroupMembershipSyncProcessor;

    @InjectMocks
    private SyncOrchestrationService service;

    @Test
    void shouldThrowAlfrescoUnavailableExceptionWhenAlfrescoFails()
    {
        // Given
        when(alfrescoClient.getAllUsers())
                .thenThrow(new RuntimeException("Connection timeout"));

        // When/Then
        assertThatThrownBy(() -> service.performFullSync())
                .isInstanceOf(AlfrescoUnavailableException.class)
                .hasMessageContaining("Alfresco unavailable");

        // Verify sync stopped
        verify(nucleusClient, never()).getAllIamUsers();
        verify(userMappingSyncProcessor, never()).syncUserMappings(any(), any(), any());
    }

    @Test
    void shouldThrowNucleusUnavailableExceptionWhenNucleusFails()
    {
        // Given
        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("asmith", "alice@email.com", true, "Alice", "Smith", "Alice Smith"));
        when(alfrescoClient.getAllUsers()).thenReturn(alfrescoUsers);
        when(nucleusClient.getAllIamUsers())
                .thenThrow(new RuntimeException("API error"));

        // When/Then
        assertThatThrownBy(() -> service.performFullSync())
                .isInstanceOf(NucleusUnavailableException.class)
                .hasMessageContaining("Nucleus unavailable");

        // Verify sync stopped
        verify(userMappingSyncProcessor, never()).syncUserMappings(any(), any(), any());

        // Verify status
        Map<String, Object> status = service.getSyncStatus();
        assertThat(status.get("syncInProgress")).isEqualTo(false);
        assertThat(status.get("lastSyncResult")).asString().startsWith("Failed:");
    }

    @Test
    void shouldThrowSyncInProgressExceptionWhenSyncAlreadyRunning() throws Exception
    {
        // Given
        CountDownLatch syncStarted = new CountDownLatch(1);
        CountDownLatch blockSync = new CountDownLatch(1);

        List<AlfrescoUser> alfrescoUsers = List.of(
                new AlfrescoUser("asmith", "alice@email.com", true, "Alice", "Smith", "Alice Smith"));
        when(alfrescoClient.getAllUsers()).thenAnswer(invocation -> {
            // Block first sync
            syncStarted.countDown();
            blockSync.await(5, TimeUnit.SECONDS);
            return alfrescoUsers;
        });

        // Start first sync in background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> firstSync = executor.submit(() -> service.performFullSync());

        try
        {
            // Wait for first sync to start
            syncStarted.await(1, TimeUnit.SECONDS);

            // When - Try to start second sync
            // Then - Should throw immediately
            assertThatThrownBy(() -> service.performFullSync())
                    .isInstanceOf(SyncInProgressException.class)
                    .hasMessage("Sync already in progress. Please wait for the current sync to complete.");

            Map<String, Object> status = service.getSyncStatus();
            assertThat(status.get("syncInProgress")).isEqualTo(true);
        }
        finally
        {
            blockSync.countDown();
            firstSync.get(5, TimeUnit.SECONDS);
            executor.shutdown();
        }
    }

    @Test
    void shouldReturnNeverSyncedStatusInitially()
    {
        // When
        Map<String, Object> status = service.getSyncStatus();

        // Then
        assertThat(status.get("syncInProgress")).isEqualTo(false);
        assertThat(status.get("lastSyncResult")).isEqualTo("Never Synced");
        assertThat(status.get("alfrescoStatus")).isEqualTo("UNKNOWN");
        assertThat(status.get("nucleusStatus")).isEqualTo("UNKNOWN");
    }
}
