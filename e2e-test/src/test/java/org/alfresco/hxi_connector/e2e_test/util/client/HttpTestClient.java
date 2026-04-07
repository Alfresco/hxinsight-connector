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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.SneakyThrows;

import org.alfresco.hxi_connector.e2e_test.util.client.model.User;

public final class HttpTestClient
{
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private HttpTestClient()
    {}

    public static SimpleResponse get(String url, User user)
    {
        return get(url, user.username(), user.password());
    }

    public static SimpleResponse postJson(String url, User user, String body)
    {
        return postJson(url, user.username(), user.password(), body);
    }

    @SneakyThrows
    private static SimpleResponse get(String url, String username, String password)
    {
        HttpRequest request = requestBuilder(url, username, password)
                .GET()
                .build();
        return send(request);
    }

    @SneakyThrows
    private static SimpleResponse postJson(String url, String username, String password, String body)
    {
        HttpRequest request = requestBuilder(url, username, password)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    @SneakyThrows
    private static SimpleResponse send(HttpRequest request)
    {
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new SimpleResponse(response.statusCode(), response.body());
    }

    private static HttpRequest.Builder requestBuilder(String url, String username, String password)
    {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", basicAuthorizationHeader(username, password))
                .header("Accept", "application/json");
    }

    private static String basicAuthorizationHeader(String username, String password)
    {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public static final class SimpleResponse
    {
        private final int statusCode;
        private final String body;

        private SimpleResponse(int statusCode, String body)
        {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode()
        {
            return statusCode;
        }

        public JsonPathWrapper jsonPath()
        {
            return new JsonPathWrapper(body);
        }

        public ResponseBody body()
        {
            return new ResponseBody(body);
        }
    }

    public static final class ResponseBody
    {
        private final String body;

        private ResponseBody(String body)
        {
            this.body = body;
        }

        public String asString()
        {
            return body;
        }
    }

    public static final class JsonPathWrapper
    {
        private final ReadContext context;

        private JsonPathWrapper(String body)
        {
            this.context = JsonPath.parse(body);
        }

        public <T> T get(String path)
        {
            return context.read("$." + path);
        }
    }
}
