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
package org.alfresco.hxi_connector.hxi_extension.util.client;

import static java.time.temporal.ChronoUnit.SECONDS;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.CONTENT_TYPE;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;

import org.alfresco.hxi_connector.hxi_extension.rest.api.ConfigEntityResource.HxIConfig;
import org.alfresco.rest.api.model.Aspect;

public class TestRepoClient
{
    private static final int TIMEOUT_SECONDS = 300_000;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication("admin", "admin".toCharArray());
                }
            })
            .connectTimeout(Duration.of(TIMEOUT_SECONDS, SECONDS))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseRepoUrl;
    private final String baseRepoHxiExtensionUrl;

    public TestRepoClient(String host, int port)
    {
        baseRepoUrl = "http://%s:%s/alfresco/api/-default-/public/alfresco/versions/1".formatted(host, port);
        baseRepoHxiExtensionUrl = "http://%s:%s/alfresco/api/-default-/private/hxi/versions/1".formatted(host, port);
    }

    @SneakyThrows
    public HxIConfig getConfig()
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/config/-default-".formatted(baseRepoHxiExtensionUrl)))
                .header(CONTENT_TYPE, "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpStatus.SC_OK)
        {
            throw new IllegalStateException("Call to aspects endpoint returned unexpected response %s".formatted(response.statusCode()));
        }

        Map<String, HxIConfig> parsedResponse = objectMapper.readValue(response.body(), new TypeReference<>() {});

        return parsedResponse.get("entry");
    }

    @SneakyThrows
    public Aspect getAspectById(String aspectId)
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/aspects/%s".formatted(baseRepoUrl, aspectId)))
                .header(CONTENT_TYPE, "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpStatus.SC_OK)
        {
            throw new IllegalStateException("Call to aspects endpoint returned unexpected response %s".formatted(response.statusCode()));
        }

        Map<String, Aspect> parsedResponse = objectMapper.readValue(response.body(), new TypeReference<>() {});

        return parsedResponse.get("entry");
    }
}
