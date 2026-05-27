/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.ERROR;

import static org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider.USER_AGENT_DATA;
import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.event.Level;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector.model.PreSignedUrlResponse;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LoggingUtils;
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
        String storageRequestEndpoint = buildStorageRequestEndpoint();
        // onException is log-only. Throwing or otherwise mutating the exchange from inside an
        // onException processor triggers Camel's FatalFallbackErrorHandler, which short-circuits
        // the outer transacted route's error handling. Retry classification for transient I/O
        // is delegated to requestStorageLocation()'s catch block, which calls
        // ErrorUtils.wrapErrorAndThrowIfNecessary against the configured retry reasons set so
        // a Jackson parse failure / HttpHostConnectException / NoHttpResponseException / etc.
        // is re-thrown as EndpointServerErrorException for @Retryable to match.
        onException(Exception.class)
            .log(ERROR, log, "Storage :: Unexpected response while requesting pre-signed URL - Endpoint: %s".formatted(storageRequestEndpoint))
            .process(exchange -> LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR));

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .setProperty(USER_AGENT_DATA, applicationInfoProvider::getUserAgentData)
            .process(authService::setHxIAuthorizationHeaders)
            .toD(storageRequestEndpoint)
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

    private String buildStorageRequestEndpoint()
    {
        String base = integrationProperties.hylandExperience().storage().location().endpoint();
        int timeoutMs = integrationProperties.hylandExperience().storage().location().responseTimeoutMs();
        if (timeoutMs <= 0)
        {
            // 0 means "no per-request timeout"; omit the parameter so the Camel HTTP default applies
            // (matches HttpFileUploader#buildUploadEndpoint, AlfrescoRepositoryContentClient#buildContentEndpoint).
            return base + ApplicationInfoProvider.USER_AGENT_PARAM;
        }
        return base + "&httpClient.responseTimeout=" + timeoutMs + ApplicationInfoProvider.USER_AGENT_PARAM;
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.storage.location.retry.delayMultiplier}"))
    @Override
    public PreSignedUrlResponse requestStorageLocation()
    {
        try
        {
            return camelContext.createFluentProducerTemplate()
                    .to(LOCAL_ENDPOINT)
                    .request(PreSignedUrlResponse.class);
        }
        catch (CamelExecutionException e)
        {
            classifyAndRethrow(e);
            throw e;
        }
    }

    /**
     * Translates transient I/O / parse causes (e.g. {@code HttpHostConnectException}, {@code NoHttpResponseException}, {@code JsonEOFException}, {@code MismatchedInputException}) into {@link EndpointServerErrorException} so {@code @Retryable} on {@link #requestStorageLocation()} matches and Spring Retry's listener ({@code RetryMetricsRecorder}) records the attempt under the stable {@code exception=EndpointServerErrorException} tag — independent of which concrete Apache HC / Jackson exception class the underlying Camel HTTP component happened to surface.
     */
    private void classifyAndRethrow(CamelExecutionException e)
    {
        Throwable cause = e.getCause();
        if (cause instanceof Exception causeException)
        {
            ErrorUtils.wrapErrorAndThrowIfNecessary(causeException,
                    integrationProperties.hylandExperience().storage().location().retry().reasons(),
                    LiveIngesterRuntimeException.class);
        }
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

}
