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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;

@Service
public class SyncSchedulerService
{
    private final SyncOrchestrationService syncOrchestrationService;
    private final boolean syncEnabled;
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncSchedulerService.class);

    public SyncSchedulerService(
            @Value("${sync.enabled}") boolean syncEnabled,
            SyncOrchestrationService syncOrchestrationService)
    {
        this.syncEnabled = syncEnabled;
        this.syncOrchestrationService = syncOrchestrationService;
    }

    @Scheduled(cron = "${sync.cron.expression}")
    public void scheduledSync()
    {
        if (!syncEnabled)
        {
            LOGGER.atDebug()
                    .setMessage("Scheduled sync skipped - sync disabled")
                    .log();
            return;
        }

        LOGGER.atInfo()
                .setMessage("Starting scheduled sync...")
                .log();
        try
        {
            String result = syncOrchestrationService.performFullSync();
            LOGGER.atInfo()
                    .setMessage("Scheduled sync completed: {}")
                    .addArgument(result)
                    .log();
        }
        catch (SyncInProgressException e)
        {
            LOGGER.atWarn()
                    .setMessage("Scheduled sync skipped - previous sync still running")
                    .log();
            ;
        }
        catch (SyncException e)
        {
            LOGGER.atWarn()
                    .setMessage("Scheduled sync failed - see previous logs for details")
                    .log();
        }
    }
}
