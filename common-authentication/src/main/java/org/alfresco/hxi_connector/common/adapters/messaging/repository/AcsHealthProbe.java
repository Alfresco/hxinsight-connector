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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@RequiredArgsConstructor
@Slf4j
public class AcsHealthProbe
{

    private final HttpClient client;
    private final String acsHealthEndpoint;
    private final int retryTimeoutSeconds;
    private final int retryIntervalSeconds;
    private final boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStart() throws InterruptedException
    {
        if (enabled)
        {
            checkAcsAlive();
        }
    }

    void checkAcsAlive() throws InterruptedException
    {
        long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(retryTimeoutSeconds);
        long currentTime;
        do
        {
            try
            {
                log.info("Sending ACS Health Probe request to: {}", acsHealthEndpoint);
                HttpResponse<String> response = client.send(HttpRequest.newBuilder().uri(URI.create(acsHealthEndpoint)).build(), HttpResponse.BodyHandlers.ofString());
                if (isNotErrorCode(response.statusCode()))
                {
                    log.info("ACS is available.");
                    return;
                }
                else
                {
                    sleep();
                }
            }
            catch (IOException e)
            {
                sleep();
            }
            finally
            {
                currentTime = System.currentTimeMillis();
            }
        } while (timeout >= currentTime);

        log.info("ACS health probe failed after {} seconds. ACS is not available.", retryTimeoutSeconds);
        throw new EndpointServerErrorException("ACS is not available.");
    }

    private void sleep() throws InterruptedException
    {
        log.info("ACS is not available. Retrying in {} seconds", retryIntervalSeconds);
        TimeUnit.SECONDS.sleep(retryIntervalSeconds);
    }

    private static boolean isNotErrorCode(int statusCode)
    {
        return statusCode >= 100 && statusCode < 400;
    }
}
