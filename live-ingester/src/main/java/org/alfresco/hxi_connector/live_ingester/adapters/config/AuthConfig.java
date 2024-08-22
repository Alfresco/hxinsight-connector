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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import org.alfresco.hxi_connector.common.adapters.auth.AccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationClient;
import org.alfresco.hxi_connector.common.adapters.auth.DefaultAccessTokenProvider;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;

@Configuration
@EnableRetry
@EnableConfigurationProperties
public class AuthConfig
{

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        return security
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/actuator/health/**").permitAll())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
                .build();
    }

    @Bean
    public AccessTokenProvider defaultAccessTokenProvider(AuthenticationClient liveIngesterAuthClient)
    {
        return new DefaultAccessTokenProvider(liveIngesterAuthClient);
    }

    @Bean
    @ConfigurationProperties(prefix = "auth")
    public AuthProperties authorizationProperties()
    {
        return new AuthProperties();
    }

    @Bean
    public AuthService authService(AuthProperties authProperties, AccessTokenProvider defaultAccessTokenProvider)
    {
        return new AuthService(authProperties, defaultAccessTokenProvider);
    }
}
