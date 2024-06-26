/*-
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
package org.alfresco.hxi_connector.prediction_applier.config;

import static java.util.Optional.ofNullable;

import static org.alfresco.hxi_connector.common.config.properties.Retry.RETRY_ATTEMPTS_DEFAULT;
import static org.alfresco.hxi_connector.common.config.properties.Retry.RETRY_DELAY_MULTIPLIER_DEFAULT;
import static org.alfresco.hxi_connector.common.config.properties.Retry.RETRY_INITIAL_DELAY_DEFAULT;
import static org.alfresco.hxi_connector.common.config.properties.Retry.RETRY_REASONS_BASIC;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.config.properties.Retry;

@Configuration
@PropertySource("classpath:alfresco/module/alfresco-hxinsight-connector-prediction-applier-extension/alfresco-global.properties")
public class AuthConfiguration
{

    @Bean
    public AuthProperties hxInsightAuthProperties(Environment environment)
    {
        AuthProperties authProperties = new AuthProperties();
        AuthProperties.AuthProvider authProvider = new AuthProperties.AuthProvider();
        authProvider.setType(environment.getProperty("hxi.auth.providers.hyland-experience.type"));
        authProvider.setGrantType(environment.getProperty("hxi.auth.providers.hyland-experience.grant-type"));
        authProvider.setClientName(environment.getProperty("hxi.auth.providers.hyland-experience.client-name"));
        authProvider.setClientId(environment.getProperty("hxi.auth.providers.hyland-experience.client-id"));
        authProvider.setClientSecret(environment.getProperty("hxi.auth.providers.hyland-experience.client-secret"));
        authProvider.setScope(ofNullable(environment.getProperty("hxi.auth.providers.hyland-experience.scope")).map(Set::of).orElse(Collections.emptySet()));
        authProvider.setTokenUri(environment.getProperty("hxi.auth.providers.hyland-experience.token-uri"));
        authProvider.setEnvironmentKey(environment.getProperty("hxi.auth.providers.hyland-experience.environment-key"));
        authProperties.setProviders(Map.of("hyland-experience", authProvider));

        Retry retryProperties = new Retry(
                environment.getProperty("hxi.auth.retry.attempts", Integer.class, RETRY_ATTEMPTS_DEFAULT),
                environment.getProperty("hxi.auth.retry.initial-delay", Integer.class, RETRY_INITIAL_DELAY_DEFAULT),
                environment.getProperty("hxi.auth.retry.delay-multiplier", Double.class, RETRY_DELAY_MULTIPLIER_DEFAULT),
                RETRY_REASONS_BASIC);
        authProperties.setRetry(retryProperties);

        return authProperties;
    }

    @Bean
    public DefaultAccessTokenProvider hxInsightAccessTokenProvider(AuthenticationClient hxInsightAuthClient)
    {
        return new DefaultAccessTokenProvider(hxInsightAuthClient);
    }
}
