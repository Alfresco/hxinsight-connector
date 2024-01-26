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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationConfig;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreSignedUrlRequester extends RouteBuilder implements StorageLocationRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + PreSignedUrlRequester.class.getSimpleName();
    private static final String LOCAL_RETRYABLE_ENDPOINT = "direct:retryable-" + PreSignedUrlRequester.class.getSimpleName();
    static final String ROUTE_ID = PreSignedUrlRequester.class.getSimpleName();
    private static final String UNEXPECTED_STATUS_CODE_MESSAGE = "Unexpected response status code - expecting: %d, received: %d";
    private static final int EXPECTED_STATUS_CODE = 201;
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    static final String NODE_ID_PROPERTY = "objectId";
    static final String CONTENT_TYPE_PROPERTY = "contentType";

    private final CamelContext camelContext;
    private final IntegrationConfig.Properties integrationProperties;

    @Override
    @SuppressWarnings("unchecked")
    public void configure()
    {
        List<Class<? extends Throwable>> retryReasons = integrationProperties.storage().retry().reasons();
        // @formatter:off
        if (retryReasons != null && !retryReasons.isEmpty())
        {
            onException(retryReasons.toArray(Class[]::new))
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .logExhaustedMessageBody(true)
                .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
                .maximumRedeliveries(integrationProperties.storage().retry().attempts())
                .redeliveryDelay(integrationProperties.storage().retry().initialDelay())
                .backOffMultiplier(integrationProperties.storage().retry().delayMultiplier())
                .stop();
        }

        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Headers: ${headers}, Body: ${body}")
            .stop();

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .marshal()
            .json()
            .to(LOCAL_RETRYABLE_ENDPOINT)
            .end();

        from(LOCAL_RETRYABLE_ENDPOINT)
            .errorHandler(noErrorHandler())
            .to(integrationProperties.storage().endpoint())
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .unmarshal()
                .json(JsonLibrary.Jackson, Map.class)
                .process(PreSignedUrlRequester::extractUrl)
            .otherwise()
                .process(PreSignedUrlRequester::throwUnexpectedStatusCodeException)
            .endChoice()
            .end();
        // @formatter:on
    }

    @Override
    public URL requestStorageLocation(StorageLocationRequest storageLocationRequest)
    {
        Map<String, String> request = Map.of(NODE_ID_PROPERTY, storageLocationRequest.nodeId(),
                CONTENT_TYPE_PROPERTY, storageLocationRequest.contentType());

        return camelContext.createProducerTemplate().requestBody(LOCAL_ENDPOINT, request, URL.class);
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
    private static void extractUrl(Exchange exchange)
    {
        exchange.getIn().setBody(extractStorageLocationUrl(exchange.getMessage().getBody(Map.class)), URL.class);
    }

    private static URL extractStorageLocationUrl(Map<String, Object> map)
    {
        if (map.containsKey(STORAGE_LOCATION_PROPERTY))
        {
            try
            {
                return new URL(String.valueOf(map.get(STORAGE_LOCATION_PROPERTY)));
            }
            catch (MalformedURLException e)
            {
                throw new EndpointServerErrorException("Parsing URL from response property failed!", e);
            }
        }
        else
        {
            throw new EndpointServerErrorException("Missing " + STORAGE_LOCATION_PROPERTY + " property in response!");
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static void throwUnexpectedStatusCodeException(Exchange exchange)
    {
        int actualStatusCode = exchange.getMessage().getHeader(HTTP_RESPONSE_CODE, Integer.class);
        if (actualStatusCode >= 400 && actualStatusCode <= 499)
        {
            throw new EndpointClientErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(EXPECTED_STATUS_CODE, actualStatusCode));
        }
        else if (actualStatusCode >= 500 && actualStatusCode <= 599)
        {
            throw new EndpointServerErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(EXPECTED_STATUS_CODE, actualStatusCode));
        }
        else if (actualStatusCode != EXPECTED_STATUS_CODE)
        {
            log.warn(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(EXPECTED_STATUS_CODE, actualStatusCode));
        }
    }
}
