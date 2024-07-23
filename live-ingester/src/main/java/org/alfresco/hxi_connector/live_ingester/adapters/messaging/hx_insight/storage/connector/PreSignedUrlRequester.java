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

import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.ApplicationInfoProvider.USER_AGENT_DATA;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Component
@Slf4j
@RequiredArgsConstructor
public class PreSignedUrlRequester extends RouteBuilder implements StorageLocationRequester
{
    private static final String LOCAL_ENDPOINT = "direct:" + PreSignedUrlRequester.class.getSimpleName();
    static final String ROUTE_ID = "presigned-url-requester";
    static final String STORAGE_LOCATION_PROPERTY = "url";
    static final String CONTENT_ID_PROPERTY = "id";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;
    private final AuthService authService;
    private final ApplicationInfoProvider applicationInfoProvider;

    @Override
    public void configure()
    {
        // @formatter:off
        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Storage :: Unexpected response while requesting pre-signed URL. Body: ${body}")
            .process(this::wrapErrorIfNecessary)
            .stop();

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .setProperty(USER_AGENT_DATA, applicationInfoProvider::getUserAgentData)
            .process(authService::setHxIAuthorizationHeaders)
            .toD(integrationProperties.hylandExperience().storage().location().endpoint() + ApplicationInfoProvider.USER_AGENT_PARAM)
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .unmarshal()
                .json(JsonLibrary.Jackson, List.class)
                .process(this::extractResponse)
            .otherwise()
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.delayMultiplier}"))
    @Override
    public PreSignedUrlResponse requestStorageLocation()
    {
        return camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .request(PreSignedUrlResponse.class);
    }

    @SuppressWarnings({"unchecked", "PMD.UnusedPrivateMethod"})
    private void extractResponse(Exchange exchange)
    {
        exchange.getMessage()
                .setBody(extractStorageResponse(exchange.getIn().getBody(List.class)), PreSignedUrlResponse.class);
    }

    private PreSignedUrlResponse extractStorageResponse(List<Map<String, Object>> response)
    {
        if (response.isEmpty())
        {
            throw new EndpointServerErrorException("Storage :: Unable to extract list of pre-signed URL responses");
        }
        Map<String, Object> map = response.get(0);
        if (!map.containsKey(STORAGE_LOCATION_PROPERTY))
        {
            throw new EndpointServerErrorException("Storage :: Missing " + STORAGE_LOCATION_PROPERTY + " property in response!");
        }
        URL url;
        try
        {
            url = new URL(String.valueOf(map.get(STORAGE_LOCATION_PROPERTY)));
        }
        catch (MalformedURLException e)
        {
            throw new EndpointServerErrorException("Storage :: Parsing URL from response property failed!", e);
        }
        if (!map.containsKey(CONTENT_ID_PROPERTY))
        {
            throw new EndpointServerErrorException("Storage :: Missing " + CONTENT_ID_PROPERTY + " property in response!");
        }
        String contentId = (String) map.get(CONTENT_ID_PROPERTY);
        return new PreSignedUrlResponse(url, contentId);
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

        ErrorUtils.wrapErrorAndThrowIfNecessary(cause, retryReasons, LiveIngesterRuntimeException.class);
    }

}
