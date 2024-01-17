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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.storage;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.TransformConfig;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@Component
@RequiredArgsConstructor
public class SharedFileStoreClient extends RouteBuilder implements TransformEngineFileStorage
{
    private static final String LOCAL_ENDPOINT = "direct:" + SharedFileStoreClient.class.getSimpleName();
    private static final String ROUTE_ID = SharedFileStoreClient.class.getSimpleName();
    private static final int EXPECTED_STATUS_CODE = 200;
    private static final String FILE_ID_HEADER = "fileId";

    private final TransformConfig transformConfig;

    private final CamelContext camelContext;

    @Override
    public void configure()
    {
        onException(Exception.class)
                .log(LoggingLevel.ERROR, log, "Unexpected response. Body: ${body}")
                .stop();

        from(LOCAL_ENDPOINT)
                .id(ROUTE_ID)
                .toD(transformConfig.getStorage().getGetFileEndpoint())
                .choice()
                .when(header(HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::bodyAsFile)
                .otherwise()
                .process(this::throwUnexpectedStatusCodeException)
                .endChoice()
                .end();
    }

    @SuppressWarnings({"PMD.UnusedPrivateMethod"})
    private void bodyAsFile(Exchange exchange)
    {
        byte[] bytes = exchange.getIn().getBody(byte[].class);

        exchange.getIn().setBody(new File(bytes), File.class);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void throwUnexpectedStatusCodeException(Exchange exchange)
    {
        throw new LiveIngesterRuntimeException("Unexpected response status code - expecting: " + EXPECTED_STATUS_CODE + ", received: " + exchange.getMessage().getHeader(HTTP_RESPONSE_CODE, Integer.class));
    }

    @Override
    public File downloadFile(String fileId)
    {
        return camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .withHeader(FILE_ID_HEADER, fileId)
                .request(File.class);
    }
}
