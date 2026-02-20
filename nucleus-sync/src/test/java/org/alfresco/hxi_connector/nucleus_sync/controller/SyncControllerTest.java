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
package org.alfresco.hxi_connector.nucleus_sync.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.SyncOrchestrationService;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.AlfrescoUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.NucleusUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@WebMvcTest(SyncController.class)
class SyncControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncOrchestrationService syncOrchestrationService;

    @Test
    void shouldTriggerSyncSuccessfully() throws Exception
    {
        // Given
        when(syncOrchestrationService.performFullSync())
                .thenReturn("Sync completed successfully");

        // When/Then
        mockMvc.perform(post("/sync/trigger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sync completed successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn409WhenSyncAlreadyInProgress() throws Exception
    {
        // Given
        when(syncOrchestrationService.performFullSync())
                .thenThrow(new SyncInProgressException());

        // When/Then
        mockMvc.perform(post("/sync/trigger"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("SYNC_IN_PROGRESS"))
                .andExpect(jsonPath("$.message").value("Sync already in progress. Please wait for the current sync to complete."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn503WhenAlfrescoUnavailable() throws Exception
    {
        // Given
        when(syncOrchestrationService.performFullSync())
                .thenThrow(new AlfrescoUnavailableException("Connection timeout",
                        new RuntimeException()));

        // When/Then
        mockMvc.perform(post("/sync/trigger"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("ALFRESCO_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Alfresco unavailable: Connection timeout"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn503WhenNucleusUnavailable() throws Exception
    {
        // Given
        when(syncOrchestrationService.performFullSync())
                .thenThrow(new NucleusUnavailableException("API error",
                        new RuntimeException()));

        // When/Then
        mockMvc.perform(post("/sync/trigger"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("NUCLEUS_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Nucleus unavailable: API error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnSyncStatus() throws Exception
    {
        // Given
        Map<String, Object> status = Map.of(
                "syncInProgress", false,
                "lastSyncTime", LocalDateTime.now(),
                "lastSyncResult", "Sync completed successfully",
                "alfrescoStatus", "HEALTHY",
                "nucleusStatus", "HEALTHY");
        when(syncOrchestrationService.getSyncStatus()).thenReturn(status);

        // When/Then
        mockMvc.perform(get("/sync/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncInProgress").value(false))
                .andExpect(jsonPath("$.lastSyncResult").value("Sync completed successfully"))
                .andExpect(jsonPath("$.alfrescoStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.nucleusStatus").value("HEALTHY"));
    }
}
