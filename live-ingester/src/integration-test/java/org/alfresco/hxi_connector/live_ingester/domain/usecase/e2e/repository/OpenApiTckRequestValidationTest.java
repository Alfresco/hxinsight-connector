/*
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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import static org.apache.hc.core5.http.HttpStatus.SC_ACCEPTED;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

@Slf4j
@SuppressWarnings("PMD.CloseResource") // HttpClient is not AutoCloseable on Java 17
public class OpenApiTckRequestValidationTest
{
    private static final String BASE_URL = "http://localhost:4010";

    private static HttpClient httpClient;

    @BeforeAll
    static void beforeAll()
    {
        httpClient = HttpClient.newHttpClient();
    }

    @AfterAll
    static void afterAll()
    {
        httpClient = null;
    }

    @Test
    void testRequestToPresignedUrls()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/get-presigned-urls.yml");
        assertThat(actualStatusCode).isEqualTo(SC_OK);
    }

    @Test
    void testUploadReferencesRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/upload-references-document.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @Test
    void testCreateOrUpdateRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/create-or-update-document.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @Test
    void testCreateThumbnailEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/create-thumbnail.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @Test
    void testDeleteRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/delete-document.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @SneakyThrows
    private int validateRequest(String yamlPath)
    {
        // given
        HxInsightRequest request = RequestLoader.load(yamlPath);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + request.url()))
                .POST(request.body() != null
                        ? HttpRequest.BodyPublishers.ofString(request.body())
                        : HttpRequest.BodyPublishers.noBody());

        request.headers().forEach(requestBuilder::header);

        // when
        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        // then
        log.info("Response Status Code: {}", response.statusCode());
        log.info("Response Headers: {}", response.headers().map());
        log.info("Response Body: {}", response.body());

        return response.statusCode();
    }
}
