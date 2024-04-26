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
package org.alfresco.hxi_connector.live_ingester.adapters.config;

import java.util.List;

import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationService;
import org.alfresco.hxi_connector.common.adapters.auth.HxAuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.HxOAuth2AuthenticationProvider;

@Configuration
@EnableMethodSecurity
@EnableRetry
@EnableScheduling
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class SecurityConfig
{

    @Bean
    @DependsOn("liveIngesterHxAuthClient")
    public AuthenticationProvider hxAuthenticationProvider(OAuth2ClientProperties oAuth2ClientProperties, AuthenticationClient liveIngesterHxAuthClient)
    {
        return new HxOAuth2AuthenticationProvider(oAuth2ClientProperties, liveIngesterHxAuthClient);
    }

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> authenticationProviders)
    {
        return new ProviderManager(authenticationProviders);
    }

    @Bean
    @DependsOn("authenticationManager")
    public AuthenticationService authenticationService(OAuth2ClientProperties oAuth2ClientProperties, IntegrationProperties integrationProperties,
            AuthenticationManager authenticationManager, TaskScheduler taskScheduler, CamelContext camelContext)
    {
        return new AuthenticationService(oAuth2ClientProperties, integrationProperties.hylandExperience().authorization(), integrationProperties.hylandExperience()
                .authentication(), authenticationManager, taskScheduler, camelContext);
    }

    @Bean
    public HxAuthenticationClient liveIngesterHxAuthClient(CamelContext camelContext, IntegrationProperties integrationProperties)
    {
        return new HxAuthenticationClient(camelContext, integrationProperties.hylandExperience().authentication().retry());
    }
}
