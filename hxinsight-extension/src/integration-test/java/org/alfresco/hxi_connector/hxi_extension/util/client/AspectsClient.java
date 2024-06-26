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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.alfresco.rest.api.model.Aspect;

public class AspectsClient
{
    private static final String ASPECTS_URL = "http://%s:%s/alfresco/api/-default-/public/alfresco/versions/1/aspects/%s";
    private static final String USER = "admin";
    private static final String PASS = "admin";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String host;
    private final int port;
    private final int timeoutSeconds;

    public AspectsClient(String host, int port, int timeoutSeconds)
    {
        this.host = host;
        this.port = port;
        this.timeoutSeconds = timeoutSeconds;
    }

    @SneakyThrows
    public Aspect getAspectById(String aspectId)
    {
        return mapResponse(requestAspect(aspectId));
    }

    private Aspect mapResponse(String responseBody) throws JsonProcessingException
    {
        Map<String, Map<String, Object>> response = objectMapper.readValue(responseBody, new TypeReference<>() {});
        if (MapUtils.isEmpty(response) || !response.containsKey("entry"))
        {
            return null;
        }

        return objectMapper.convertValue(response.get("entry"), Aspect.class);
    }

    private String requestAspect(String aspectId) throws IOException, AuthenticationException
    {
        String aspectsUrl = ASPECTS_URL.formatted(host, port, aspectId);
        HttpGet request = new HttpGet(aspectsUrl);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USER, PASS);
        request.setHeader(new BasicScheme(StandardCharsets.UTF_8).authenticate(credentials, request, null));

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeoutSeconds * 1000)
                .setConnectionRequestTimeout(timeoutSeconds * 1000)
                .setSocketTimeout(timeoutSeconds * 1000)
                .build();

        @Cleanup
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        @Cleanup
        CloseableHttpResponse response = client.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        if (statusCode != HttpStatus.SC_OK)
        {
            throw new IllegalStateException("Call to %s endpoint returned unexpected response %s".formatted(aspectsUrl, statusCode));
        }

        return responseBody;
    }
}
