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
import java.util.Set;

import org.hyland.sdk.cic.agent.AgentHttpClient;
import org.hyland.sdk.cic.agent.AgentService;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.BackoffStrategy;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.qna.QnaHttpClient;
import org.hyland.sdk.cic.qna.QnaService;

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
            String tokenUri, String clientId, String clientSecret, String scope,
            int maxAttempts, long initialDelayMs, double multiplier, long maxDelayMs)
    {
        AuthenticationHttpClient.Builder builder = AuthenticationHttpClient.from(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .retryPolicy(RetryPolicy.builder()
                        .maxAttempts(maxAttempts)
                        .backoffStrategy(BackoffStrategy.exponentialDelay(
                                Duration.ofMillis(Math.max(1, initialDelayMs)),
                                Duration.ofMillis(maxDelayMs),
                                multiplier))
                        .retryCondition(context -> {
                            Throwable cause = context.exception();
                            while (cause != null)
                            {
                                if (cause instanceof IOException)
                                {
                                    return true;
                                }
                                cause = cause.getCause();
                            }
                            return false;
                        })
                        .build());
        for (String s : parseScopes(scope))
        {
            builder.scope(s);
        }
        return builder;
    }

    static Set<String> parseScopes(String scope)
    {
        if (scope == null || scope.isBlank())
        {
            return Set.of();
        }
        return Set.of(scope.strip().split("\\s+"));
    }
}
