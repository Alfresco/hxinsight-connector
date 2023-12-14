/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.storage;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Component
public class SignedStorageClient implements StorageClient
{

    @Override
    public StatusLine upload(File file, String contentType, URL preSignedUrl)
    {
        try (InputStream fileInputStream = Files.newInputStream(file.toPath()))
        {
            return this.upload(fileInputStream, contentType, preSignedUrl);
        } catch (IOException e)
        {
            throw new LiveIngesterRuntimeException("Accessing file failed", e);
        }
    }

    @Override
    public StatusLine upload(InputStream inputStream, String contentType, URL preSignedUrl)
    {
        try (CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            HttpPut httpPut = new HttpPut(preSignedUrl.toURI());
            HttpEntity entity = EntityBuilder.create()
                    .setStream(inputStream)
                    .build();
            httpPut.setEntity(entity);
            httpPut.setHeader(CONTENT_TYPE, contentType);
            // TODO If additional metadata will be required uncomment bellow, otherwise removed it
            // metadata.forEach((k, v) -> httpPut.setHeader("x-amz-meta-" + k, v));

            HttpResponse response = httpClient.execute(httpPut);
            return response.getStatusLine();
        } catch (URISyntaxException e)
        {
            throw new LiveIngesterRuntimeException("Pre Signed URL cannot be parsed to URI", e);
        } catch (IOException e)
        {
            throw new LiveIngesterRuntimeException("Calling Pre Signed URL failed", e);
        }
    }
}
