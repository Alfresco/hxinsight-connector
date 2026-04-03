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

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;

import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Bucket;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

public class AwsS3Client
{
    private final XmlMapper xmlMapper = new XmlMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String bucketName;

    public AwsS3Client(String host, Integer port, String bucketName)
    {
        this.baseUrl = "http://%s:%s".formatted(host, port);
        this.bucketName = bucketName;
    }

    @SneakyThrows
    public List<S3Object> listS3Content()
    {
        HttpResponse<InputStream> response = executeGet("%s/%s".formatted(baseUrl, bucketName));
        S3Bucket s3Bucket = xmlMapper.readValue(response.body(), S3Bucket.class);

        return s3Bucket.content();
    }

    @SneakyThrows
    public InputStream getS3ObjectContent(String objectKey)
    {
        return executeGet("%s/%s/%s".formatted(baseUrl, bucketName, objectKey)).body();
    }

    @SneakyThrows
    private HttpResponse<InputStream> executeGet(String uri)
    {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 400)
        {
            throw new IllegalStateException("Request to %s failed with status %s".formatted(uri, response.statusCode()));
        }
        return response;
    }
}
