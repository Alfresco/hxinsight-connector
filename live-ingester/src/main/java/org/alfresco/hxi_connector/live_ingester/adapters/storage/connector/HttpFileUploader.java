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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.connector;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.FileUploader;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Component
@RequiredArgsConstructor
public class HttpFileUploader extends RouteBuilder implements FileUploader
{
    private static final String LOCAL_ENDPOINT = "direct:" + HttpFileUploader.class.getSimpleName();
    static final String ROUTE_ID = HttpFileUploader.class.getSimpleName();
    private static final String STORAGE_LOCATION = "storageLocation";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;

    @Override
    public void configure()
    {
        onException(Exception.class)
                .log(LoggingLevel.ERROR, log, "Unexpected response. Body: ${body}")
                .stop();

        from(LOCAL_ENDPOINT)
                .id(ROUTE_ID)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
                .toD("${headers." + STORAGE_LOCATION + "}")
                .choice()
                .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(HttpFileUploader::throwUnexpectedStatusCodeException)
                .endChoice()
                .end();
    }

    @Override
    public void upload(FileUploadRequest fileUploadRequest)
    {
        Map<String, Object> headers = Map.of(STORAGE_LOCATION, fileUploadRequest.storageLocation().toString(),
                Exchange.CONTENT_TYPE, fileUploadRequest.contentType());

        camelContext.createProducerTemplate().sendBodyAndHeaders(LOCAL_ENDPOINT, fileUploadRequest.inputStream(), headers);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static void throwUnexpectedStatusCodeException(Exchange exchange)
    {
        throw new LiveIngesterRuntimeException("Unexpected response status code - expecting: " + EXPECTED_STATUS_CODE + ", received: " + exchange.getMessage().getHeader(HTTP_RESPONSE_CODE, Integer.class));
    }
}
