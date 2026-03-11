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

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.AlfrescoUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;

@ExtendWith(MockitoExtension.class)
public class SyncSchedulerServiceTest
{
    @Mock
    private SyncOrchestrationService syncOrchestrationService;

    @InjectMocks
    private SyncSchedulerService schedulerService;

    @Test
    void shouldExecuteScheduledSyncWhenEnabled()
    {
        // Given
        ReflectionTestUtils.setField(schedulerService, "syncEnabled", true);
        when(syncOrchestrationService.performFullSync())
                .thenReturn("Sync completed successfully");

        // When
        schedulerService.scheduledSync();

        // Then
        verify(syncOrchestrationService).performFullSync();
    }

    @Test
    void shouldSkipScheduledSyncWhenDisabled()
    {
        // Given
        ReflectionTestUtils.setField(schedulerService, "syncEnabled", false);

        // When
        schedulerService.scheduledSync();

        // Then
        verify(syncOrchestrationService, never()).performFullSync();
    }

    @Test
    void shouldHandleSyncInProgressExceptionGracefully()
    {
        // Given
        ReflectionTestUtils.setField(schedulerService, "syncEnabled", true);
        when(syncOrchestrationService.performFullSync())
                .thenThrow(new SyncInProgressException());

        // When/Then - Should not propagate exception
        assertThatNoException().isThrownBy(schedulerService::scheduledSync);

        verify(syncOrchestrationService).performFullSync();
    }

    @Test
    void shouldHandleSyncExceptionGracefully()
    {
        // Given
        ReflectionTestUtils.setField(schedulerService, "syncEnabled", true);
        when(syncOrchestrationService.performFullSync())
                .thenThrow(new AlfrescoUnavailableException("Connection failed",
                        new RuntimeException()));

        // When/Then - Should not propagate exception
        assertThatNoException().isThrownBy(schedulerService::scheduledSync);

        verify(syncOrchestrationService).performFullSync();
    }
}
