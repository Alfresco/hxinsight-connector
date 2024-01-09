/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.storage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.HxInsightApiConfig;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.StorageLocationRequester;

@Component
@Slf4j
public class PreSignedUrlRequester extends RouteBuilder implements StorageLocationRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + PreSignedUrlRequester.class.getSimpleName();
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    private static final String CONTENT_TYPE_PROPERTY = "contentType";
    private static final String NODE_ID_PROPERTY = "objectId";
    private static final int EXPECTED_STATUS_CODE = 201;

    private final CamelContext camelContext;
    private final ObjectMapper objectMapper;

    private final String url;
    private final String username;
    private final String password;

    @Autowired
    public PreSignedUrlRequester(CamelContext camelContext, ObjectMapper objectMapper, HxInsightApiConfig.Properties config)
    {
        super(camelContext);
        this.camelContext = camelContext;
        this.objectMapper = objectMapper;
        this.url = config.url().storageLocationRequest();
        this.username = config.username();
        this.password = config.password();
    }

    @Override
    public void configure()
    {
        from(LOCAL_ENDPOINT)
                .marshal()
                .json()
                .to(url + "?httpMethod=POST" +
                        "&authMethod=Basic" +
                        "&authUsername=" + username +
                        "&authPassword=" + password +
                        "&authenticationPreemptive=true");
    }

    @Override
    public URL requestStorageLocation(StorageLocationRequest storageLocationRequest)
    {
        Map<String, String> request = Map.of(NODE_ID_PROPERTY, storageLocationRequest.nodeId(),
                CONTENT_TYPE_PROPERTY, storageLocationRequest.contentType());

        Message message = camelContext.createProducerTemplate()
                .send(LOCAL_ENDPOINT, exchange -> exchange.getIn().setBody(request))
                .getMessage();

        return extractUrlFromMessage(message);
    }

    private URL extractUrlFromMessage(Message message)
    {
        String responseBody = message.getBody(String.class);
        int statusCode = message.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        verifyStatusCode(statusCode, responseBody);

        return extractUrlFromResponseBody(responseBody);
    }

    private void verifyStatusCode(int actual, String responseBody)
    {
        if (actual != EXPECTED_STATUS_CODE)
        {
            log.error("Unexpected response body: {}", responseBody);
            throw new LiveIngesterRuntimeException("Unexpected response status code - expecting: " + EXPECTED_STATUS_CODE + ", received: " + actual);
        }
    }

    private URL extractUrlFromResponseBody(String responseBody)
    {
        try
        {
            Map<String, Object> response = objectMapper.readValue(responseBody, new TypeReference<>() {});
            if (response.containsKey(STORAGE_LOCATION_PROPERTY))
            {
                return new URL(String.valueOf(response.get(STORAGE_LOCATION_PROPERTY)));
            }
            else
            {
                log.error("Unexpected response body: {}", responseBody);
                throw new LiveIngesterRuntimeException("Missing " + STORAGE_LOCATION_PROPERTY + " property in response!");
            }
        }
        catch (JsonProcessingException e)
        {
            log.error("Unexpected response body: {}", responseBody);
            throw new LiveIngesterRuntimeException("Parsing JSON response failed!", e);
        }
        catch (MalformedURLException e)
        {
            log.error("Unexpected pre-signed URL in response: {}", responseBody);
            throw new LiveIngesterRuntimeException("Parsing URL from response property failed!", e);
        }
    }
}
