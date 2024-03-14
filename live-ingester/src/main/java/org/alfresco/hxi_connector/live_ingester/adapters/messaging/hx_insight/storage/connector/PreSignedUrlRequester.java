/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import static org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationService.setAuthorizationToken;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class PreSignedUrlRequester extends RouteBuilder implements StorageLocationRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + PreSignedUrlRequester.class.getSimpleName();
    static final String ROUTE_ID = "presigned-url-requester";
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    static final String NODE_ID_PROPERTY = "objectId";
    static final String CONTENT_TYPE_PROPERTY = "contentType";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        // @formatter:off
        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Unexpected response. Body: ${body}")
            .process(this::wrapErrorIfNecessary)
            .stop();

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .setBody(simple(null))
            .to(integrationProperties.hylandExperience().storage().location().endpoint())
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .unmarshal()
                .json(JsonLibrary.Jackson, Map.class)
                .process(this::extractUrl)
            .otherwise()
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    @PreAuthorize("hasAuthority('OAUTH2_USER')")
    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.delayMultiplier}"))
    @Override
    public URL requestStorageLocation(StorageLocationRequest storageLocationRequest)
    {
        Map<String, String> request = Map.of(NODE_ID_PROPERTY, storageLocationRequest.nodeId(),
                CONTENT_TYPE_PROPERTY, storageLocationRequest.contentType());

        return camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .withProcessor(exchange -> {
                    exchange.getIn().setBody(request);
                    setAuthorizationToken(exchange);
                })
                .request(URL.class);
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
    private void extractUrl(Exchange exchange)
    {
        exchange.getMessage().setBody(extractStorageLocationUrl(exchange.getIn().getBody(Map.class)), URL.class);
    }

    private URL extractStorageLocationUrl(Map<String, Object> map)
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
    private void throwExceptionOnUnexpectedStatusCode(Exchange exchange)
    {
        int actualStatusCode = exchange.getMessage().getHeader(HTTP_RESPONSE_CODE, Integer.class);
        if (actualStatusCode != EXPECTED_STATUS_CODE)
        {
            log.warn(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(EXPECTED_STATUS_CODE, actualStatusCode));
        }

        ErrorUtils.throwExceptionOnUnexpectedStatusCode(actualStatusCode, EXPECTED_STATUS_CODE);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void wrapErrorIfNecessary(Exchange exchange)
    {
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Set<Class<? extends Throwable>> retryReasons = integrationProperties.hylandExperience().storage().upload().retry().reasons();

        ErrorUtils.wrapErrorIfNecessary(cause, retryReasons);
    }
}
