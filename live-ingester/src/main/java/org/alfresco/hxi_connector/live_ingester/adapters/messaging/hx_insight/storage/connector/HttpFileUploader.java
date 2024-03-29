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

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils.UNEXPECTED_STATUS_CODE_MESSAGE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpMethods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.utils.ErrorUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpFileUploader extends RouteBuilder implements FileUploader
{
    private static final String LOCAL_ENDPOINT = "direct:" + HttpFileUploader.class.getSimpleName();
    static final String ROUTE_ID = "rendition-uploader";
    static final String AMZ_SECURITY_TOKEN = "X-Amz-Security-Token=";
    static final String STORAGE_LOCATION_HEADER = "storageLocation";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final CamelContext camelContext;
    private final IntegrationProperties integrationProperties;

    @Override
    public void configure()
    {
        // @formatter:off
        onException(Exception.class)
            .log(LoggingLevel.ERROR, log, "Upload :: Unexpected response while uploading to S3. Body: ${body}")
            .process(this::wrapErrorIfNecessary)
            .stop();

        from(LOCAL_ENDPOINT)
            .id(ROUTE_ID)
            .noStreamCaching()
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.PUT))
            .toD("${headers." + STORAGE_LOCATION_HEADER + "}&throwExceptionOnFailure=false")
            .choice()
            .when(header(HTTP_RESPONSE_CODE).isNotEqualTo(String.valueOf(EXPECTED_STATUS_CODE)))
                .process(this::throwExceptionOnUnexpectedStatusCode)
            .endChoice()
            .end();
        // @formatter:on
    }

    @Retryable(retryFor = EndpointServerErrorException.class,
            maxAttemptsExpression = "#{@integrationProperties.hylandExperience.storage.upload.retry.attempts}",
            backoff = @Backoff(delayExpression = "#{@integrationProperties.hylandExperience.storage.upload.retry.initialDelay}",
                    multiplierExpression = "#{@integrationProperties.hylandExperience.storage.upload.retry.delayMultiplier}"))
    @Override
    @SuppressWarnings({"PMD.CloseResource", "PMD.PreserveStackTrace"})
    public void upload(FileUploadRequest fileUploadRequest)
    {
        InputStream fileData = fileUploadRequest.file().data();
        try
        {
            Map<String, Object> headers = Map.of(
                    STORAGE_LOCATION_HEADER, wrapRawToken(fileUploadRequest.storageLocation()),
                    Exchange.CONTENT_TYPE, fileUploadRequest.contentType());

            camelContext.createFluentProducerTemplate()
                    .to(LOCAL_ENDPOINT)
                    .withHeaders(headers)
                    .withBody(fileData)
                    .request();
        }
        catch (Exception e)
        {
            try
            {
                fileData.reset();
                throw e;
            }
            catch (IOException ioe)
            {
                log.atDebug().log("Upload :: Stream reset failed due to: {}", ioe.getMessage());
                throw e;
            }
        }
    }

    private String wrapRawToken(URL preSignedUrl)
    {
        String query = preSignedUrl.getQuery();
        if (query != null && query.contains(AMZ_SECURITY_TOKEN))
        {
            String token = StringUtils.substringBetween(query, AMZ_SECURITY_TOKEN, "&");
            if (StringUtils.isEmpty(token))
            {
                token = StringUtils.substringAfter(query, AMZ_SECURITY_TOKEN);
            }

            return preSignedUrl.toString().replace(token, "RAW(%s)".formatted(decode(token, UTF_8)));
        }

        return preSignedUrl.toString();
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
