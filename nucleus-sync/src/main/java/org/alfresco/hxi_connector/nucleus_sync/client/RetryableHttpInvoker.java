/*-
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
package org.alfresco.hxi_connector.nucleus_sync.client;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

// Primary Component to call api endpoints with reliability
@Component
public class RetryableHttpInvoker
{
    private final WebClient webClient;
    private final int timeoutInMins;

    @Autowired
    public RetryableHttpInvoker(
            @Value("${http-client.timeout-minutes:5}") int timeoutInMins,
            @Value("${http-client.buffer-size-kilobytes:10240}") int bufferInKB)
    {
        this.timeoutInMins = timeoutInMins;
        this.webClient = WebClient.builder()
                .codecs(
                        clientCodecConfigurer -> clientCodecConfigurer
                                .defaultCodecs()
                                .maxInMemorySize(bufferInKB * 1024))
                .build();
    }

    /**
     * HTTP: Verb GET
     * 
     * @param fullUrl
     *            The Exact URL we want
     * @param headers
     *            Headers including Auth
     * @return The Response as String
     */
    @Retryable(retryFor = {
            WebClientRequestException.class,
            WebClientResponseException.class,
            IOException.class},
            maxAttemptsExpression = "#{${http-client.retry.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.retry.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.retry.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.retry.max-delay-ms:10000}}"))
    public String executeGetRequest(final String fullUrl, final Map<String, String> headers)
    {
        return webClient
                .get()
                .uri(fullUrl)
                .headers(httpHeaders -> {
                    headers.forEach(httpHeaders::set);
                    httpHeaders.set("Content-Type", "application/json");
                })
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMins));
    }

    // HTTP VERB: POST
    @Retryable(retryFor = {
            WebClientRequestException.class,
            WebClientResponseException.class,
            IOException.class},
            maxAttemptsExpression = "#{${http-client.retry.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.retry.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.retry.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.retry.max-delay-ms:10000}}"))
    public String executePostRequest(String fullUrl, final String jsonBody, final Map<String, String> headers)
    {
        return webClient
                .post()
                .uri(fullUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMins));
    }

    // Http Verb : DELETE
    @Retryable(retryFor = {
            WebClientRequestException.class,
            WebClientResponseException.class,
            IOException.class},
            maxAttemptsExpression = "#{${http-client.retry.max-attempts:3}}",
            backoff = @Backoff(
                    delayExpression = "#{${http-client.retry.initial-delay-ms:2000}}",
                    multiplierExpression = "#{${http-client.retry.multiplier:2}}",
                    maxDelayExpression = "#{${http-client.retry.max-delay-ms:10000}}"))
    public String executeDeleteRequest(final String fullUrl, final Map<String, String> headers)
    {
        return webClient
                .delete()
                .uri(fullUrl)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofMinutes(timeoutInMins));
    }

}
