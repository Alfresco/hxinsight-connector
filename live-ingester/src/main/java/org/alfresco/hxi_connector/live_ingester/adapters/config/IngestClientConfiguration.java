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
package org.alfresco.hxi_connector.live_ingester.adapters.config;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_AUTH_PROVIDER;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.BackoffStrategy;
import org.hyland.sdk.cic.http.client.retry.RetryCondition;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.ingest.IngestHttpClient;
import org.hyland.sdk.cic.ingest.IngestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.config.properties.Retry;

@Configuration
@RequiredArgsConstructor
public class IngestClientConfiguration
{
    private final IntegrationProperties integrationProperties;
    private final AuthProperties authProperties;
    private final ApplicationInfoProvider applicationInfoProvider;

    @Bean
    public LiveIngestService ingestService()
    {
        return new LiveIngestService(applicationInfoProvider);
    }

    @EventListener
    public void initializeIngestServiceAfterAcsHealthy(AcsHealthProbe.AcsHealthy acsHealthy)
    {
        String baseUrl = integrationProperties.hylandExperience().insight().ingestion().baseUrl();
        AuthProperties.AuthProvider authProvider = authProperties.getProviders().get(HXP_AUTH_PROVIDER);

        AuthenticationHttpClient.Builder authBuilder = AuthenticationHttpClient.from(authProvider.getTokenUri())
                .clientId(authProvider.getClientId())
                .clientSecret(authProvider.getClientSecret());
        authProvider.getScope().forEach(authBuilder::scope);

        Retry retryConfig = integrationProperties.hylandExperience().ingester().retry();
        long initialDelayMs = Math.max(1, retryConfig.initialDelay());
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxAttempts(retryConfig.attempts())
                .backoffStrategy(BackoffStrategy.exponentialDelay(
                        Duration.ofMillis(initialDelayMs),
                        Duration.ofSeconds(20)))
                .retryCondition(RetryCondition.defaultCondition())
                .build();

        IngestHttpClient ingestHttpClient = IngestHttpClient.from(baseUrl, authBuilder)
                .sourceId(applicationInfoProvider.getSourceId())
                .hxpEnvironment(authProvider.getEnvironmentKey())
                .userAgent(applicationInfoProvider.getUserAgentData())
                .retryPolicy(retryPolicy)
                .build();

        IngestService realIngestService = new IngestService(ingestHttpClient);
        ingestService().setDelegate(realIngestService);
    }
}
