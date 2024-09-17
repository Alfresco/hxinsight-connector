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

import java.net.http.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.DiscoveryApiRepositoryInformation;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ProcessingStarter;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.RepositoryInformation;
import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.common.util.EnsureUtils;

@Configuration
public class AppConfig
{

    @Bean
    @ConfigurationProperties(prefix = "application")
    public Application application()
    {
        return new Application();
    }

    @Bean
    public HttpClient httpClient()
    {
        return HttpClient.newHttpClient();
    }

    @Bean
    @Profile("!test")
    public AcsHealthProbe acsHealthProbe(HttpClient httpClient, RepositoryApiProperties repositoryApiProperties)
    {
        return new AcsHealthProbe(httpClient, repositoryApiProperties.healthProbe().endpoint(), repositoryApiProperties.healthProbe().timeoutSeconds(), repositoryApiProperties.healthProbe().intervalSeconds(), true);
    }

    @Bean
    public RepositoryInformation repositoryInformation(RepositoryApiProperties repositoryApiProperties, AuthService authService, ObjectMapper objectMapper, HttpClient httpClient, AcsHealthProbe acsHealthProbe)
    {
        EnsureUtils.ensureNotBlank(repositoryApiProperties.discoveryEndpoint(), "ACS Discovery API endpoint property must be set");
        return new DiscoveryApiRepositoryInformation(repositoryApiProperties.discoveryEndpoint(), authService, objectMapper, httpClient);
    }

    @Bean
    public ApplicationInfoProvider applicationInfoProvider(RepositoryInformation repositoryInformation, Application applicationProperties)
    {
        return new ApplicationInfoProvider(repositoryInformation, applicationProperties);
    }

    @Bean
    public ProcessingStarter processingStarter(CamelContext camelContext)
    {
        return new ProcessingStarter(camelContext);
    }
}
