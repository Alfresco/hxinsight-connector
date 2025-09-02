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
package org.alfresco.hxi_connector.nucleus_sync.controller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.alfresco.hxi_connector.nucleus_sync.services.sync.orchestration.SyncOrchestrationService;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController
{
    private final SyncOrchestrationService syncOrchestrationService;

    @PostMapping("/trigger")
    public Map<String, String> triggerSync()
    {
        try
        {
            String result = syncOrchestrationService.performFullSync();
            return Map.of("status", "success", "message", result);
        }
        catch (Exception e)
        {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @GetMapping("/status")
    public Map<String, Object> getSyncStatus()
    {
        return syncOrchestrationService.getSyncStatus();
    }
}
