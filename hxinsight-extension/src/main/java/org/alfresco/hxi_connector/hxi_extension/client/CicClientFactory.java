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
package org.alfresco.hxi_connector.hxi_extension.client;

import java.io.IOException;
import java.time.Duration;

import org.hyland.sdk.cic.agent.AgentHttpClient;
import org.hyland.sdk.cic.agent.AgentService;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.BackoffStrategy;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.qna.QnaHttpClient;
import org.hyland.sdk.cic.qna.QnaService;

import org.alfresco.hxi_connector.common.config.properties.Retry;

public final class CicClientFactory
{

    private CicClientFactory()
    {}

    public static AgentService createAgentService(
            AuthenticationHttpClient.Builder authBuilder, String environmentKey, String baseUrl)
    {
        AgentHttpClient httpClient = AgentHttpClient.from(baseUrl, authBuilder)
                .hxpEnvironment(environmentKey)
                .build();
        return new AgentService(httpClient);
    }

    public static QnaService createQnaService(
            AuthenticationHttpClient.Builder authBuilder, String environmentKey, String baseUrl)
    {
        QnaHttpClient httpClient = QnaHttpClient.from(baseUrl, authBuilder)
                .hxpEnvironment(environmentKey)
                .build();
        return new QnaService(httpClient);
    }

    public static AuthenticationHttpClient.Builder buildAuth(
            String tokenUri, String clientId, String clientSecret, String scope, Retry retry)
    {
        AuthenticationHttpClient.Builder builder = AuthenticationHttpClient.from(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .retryPolicy(buildRetryPolicy(retry));
        if (scope != null && !scope.isBlank())
        {
            for (String s : scope.split("\\s+"))
            {
                builder.scope(s);
            }
        }
        return builder;
    }

    static RetryPolicy buildRetryPolicy(Retry retry)
    {
        long initialDelayMs = Math.max(1, retry.initialDelay());
        return RetryPolicy.builder()
                .maxAttempts(retry.attempts())
                .backoffStrategy(BackoffStrategy.exponentialDelay(Duration.ofMillis(initialDelayMs), Duration.ofSeconds(20))) // cap max delay to 10 minutes
                .retryCondition(context -> {
                    Throwable cause = context.exception();
                    while (cause != null)
                    {
                        if (cause instanceof IOException)
                        {
                            return true;
                        }
                        if (retry.reasons() != null && retry.reasons().contains(cause.getClass()))
                        {
                            return true;
                        }
                        cause = cause.getCause();
                    }
                    return false;
                })
                .build();
    }
}
