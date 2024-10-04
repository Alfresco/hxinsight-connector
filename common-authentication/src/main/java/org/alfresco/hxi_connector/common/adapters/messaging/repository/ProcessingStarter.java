/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.common.adapters.messaging.repository;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.context.event.EventListener;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe.AcsHealthy;

@Slf4j
@RequiredArgsConstructor
public class ProcessingStarter
{
    private final CamelContext camelContext;

    @EventListener(AcsHealthy.class)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void startProcessing() throws Exception
    {
        log.info("Starting Camel routes: \n\t{}", getRoutesIds());
        camelContext.getRouteController().startAllRoutes();

        while (camelContext.getRouteController().isStartingRoutes())
        {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        log.atInfo().log("All Camel routes started successfully");
    }

    private String getRoutesIds()
    {
        return camelContext.getRoutes().stream()
                .map(route -> route.getId().concat(" - ").concat(route.getEndpoint().toString()))
                .collect(Collectors.joining("\n\t"));
    }
}
