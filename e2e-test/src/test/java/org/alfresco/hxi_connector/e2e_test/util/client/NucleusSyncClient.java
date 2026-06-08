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
package org.alfresco.hxi_connector.e2e_test.util.client;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

/**
 * Nucleus Sync Client Used for Communication with Nucleus Sync Container in E2E Tests. This client can be used to perform operations such as triggering sync, checking sync status, etc. The implementation details will depend on the specific API exposed by the Nucleus Sync container.
 */
@AllArgsConstructor
public class NucleusSyncClient
{
    private final String baseUrl;
    private final int port;
    private static final String SYNC_URL = "http://%s:%s/sync/trigger";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // This is the main Sync Process Executor Call
    @SneakyThrows
    public void startSynchronization()
    {
        // Implementation to trigger synchronization via API call to Nucleus Sync container
        String uri = SYNC_URL.formatted(baseUrl, String.valueOf(port));
        send(
                HttpRequest.newBuilder()
                        .uri(java.net.URI.create(uri))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build());
    }

    @SneakyThrows
    private HttpResponse<String> send(HttpRequest request)
    {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response;
    }
}
