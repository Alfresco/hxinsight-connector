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
package org.alfresco.hxi_connector.nucleus_sync.services.sync.orchestration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncSchedulerService
{
    private final SyncOrchestrationService syncOrchestrationService;
    private static final Logger logger = LoggerFactory.getLogger(SyncSchedulerService.class);

    @Value("${sync.enabled}")
    private boolean syncEnabled;

    @Scheduled(cron = "${sync.cron.expression}")
    public void scheduledSync()
    {
        if (!syncEnabled)
        {
            return;
        }

        logger.info("Starting scheduled sync...");
        try
        {
            String result = syncOrchestrationService.performFullSync();
            logger.info("Scheduled sync completed: " + result);
        }
        catch (Exception e)
        {
            logger.error("Scheduled sync failed: {}", e.getMessage(), e);
        }
    }
}
