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
package org.alfresco.hxi_connector.live_ingester.adapters.auth;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.http.common.HttpMethods.POST;
import static org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED;
import static org.apache.hc.core5.http.HttpHeaders.HOST;

import static org.alfresco.hxi_connector.common.util.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.net.URI;
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
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.util.ErrorUtils;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Component
@RequiredArgsConstructor
@Slf4j
public class HxAuthenticationClient extends RouteBuilder implements AuthenticationClient
{
    private static final String LOCAL_ENDPOINT = "direct:" + HxAuthenticationClient.class.getSimpleName();
    private static final String ROUTE_ID = "authentication-requester";
    private static final String AUTH_URL_HEADER = "hxAuthUri";
    private static final String BODY_PATTERN = "grant_type=%s&client_id=%s&client_secret=%s&scope=%s";
    public static final int EXPECTED_STATUS_CODE = 200;

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
            .toD("${headers." + AUTH_URL_HEADER + "}?throwExceptionOnFailure=false")
            .choice()
            .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .unmarshal()
                .json(JsonLibrary.Jackson, AuthenticationResult.class)
                .log(LoggingLevel.DEBUG, log, "Authentication :: success")
            .otherwise()
                .log(LoggingLevel.ERROR, log, "Authentication :: failure")
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.authentication.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.authentication.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.authentication.retry.delayMultiplier}"))
    @Override
    public AuthenticationResult authenticate(String tokenUri, ClientRegistration clientRegistration)
    {
        String body = createEncodedBody(clientRegistration);
        int contentLength = body.getBytes(UTF_8).length;

        return camelContext.createFluentProducerTemplate()
                .to(LOCAL_ENDPOINT)
                .withProcessor(exchange -> {
                    exchange.getIn().setHeaders(Map.of(
                            AUTH_URL_HEADER, tokenUri,
                            Exchange.HTTP_METHOD, POST.name(),
                            HOST, new URI(tokenUri).getHost(),
                            Exchange.CONTENT_TYPE, APPLICATION_FORM_URLENCODED.getMimeType(),
                            Exchange.CONTENT_LENGTH, contentLength));
                    exchange.getIn().setBody(body);
                })
                .request(AuthenticationResult.class);
    }

    private String createEncodedBody(ClientRegistration clientRegistration)
    {
        return BODY_PATTERN.formatted(
                encode(clientRegistration.getAuthorizationGrantType().getValue(), UTF_8),
                encode(clientRegistration.getClientId(), UTF_8),
                encode(clientRegistration.getClientSecret(), UTF_8),
                encode(String.join(",", clientRegistration.getScopes()), UTF_8));
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

        ErrorUtils.wrapErrorIfNecessary(cause, retryReasons, LiveIngesterRuntimeException.class);
    }
}
