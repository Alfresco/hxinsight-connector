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

package org.alfresco.hxi_connector.live_ingester.adapters.config.messaging;

import java.net.http.HttpClient;
import jakarta.jms.ConnectionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.DiscoveryApiRepositoryInformation;
import org.alfresco.hxi_connector.common.adapters.messaging.repository.RepositoryInformation;
import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.PropertyBasedRepositoryInformation;

@Configuration
public class LiveIngesterMessagingConfig
{

    public static final String CONFIGURATION_ERR_MESSAGE = "One of properties: 'alfresco.repository.discovery-endpoint' or 'alfresco.repository.version-override' must be set in the Live Ingester configuration.";

    @Bean
    public PlatformTransactionManager jmsTransactionManager(ConnectionFactory connectionFactory)
    {
        return new JmsTransactionManager(connectionFactory);
    }

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
    public AcsHealthProbe acsHealthProbe(HttpClient httpClient, IntegrationProperties integrationProperties)
    {
        boolean isVersionOverrideBlank = StringUtils.isBlank(integrationProperties.alfresco().repository().versionOverride());
        boolean isDiscoverEndpointBlank = StringUtils.isBlank(integrationProperties.alfresco().repository().discoveryEndpoint());
        if (isVersionOverrideBlank && isDiscoverEndpointBlank)
        {
            throw new IllegalStateException(CONFIGURATION_ERR_MESSAGE);
        }
        return new AcsHealthProbe(httpClient,
                integrationProperties.alfresco().repository().healthProbe().endpoint(),
                integrationProperties.alfresco().repository().healthProbe().timeoutSeconds(),
                integrationProperties.alfresco().repository().healthProbe().intervalSeconds(),
                isVersionOverrideBlank);
    }

    @Bean
    public RepositoryInformation repositoryInformation(AuthService authService, ObjectMapper objectMapper, IntegrationProperties integrationProperties,
            HttpClient httpClient)
    {
        if (StringUtils.isNotBlank(integrationProperties.alfresco().repository().versionOverride()))
        {
            return new PropertyBasedRepositoryInformation(integrationProperties);
        }
        else if (StringUtils.isNotBlank(integrationProperties.alfresco().repository().discoveryEndpoint()))
        {
            return new DiscoveryApiRepositoryInformation(integrationProperties.alfresco().repository().discoveryEndpoint(), authService, objectMapper, httpClient);
        }
        throw new IllegalStateException(CONFIGURATION_ERR_MESSAGE);
    }

    @Bean
    public ApplicationInfoProvider applicationInfoProvider(RepositoryInformation repositoryInformation, IntegrationProperties integrationProperties)
    {
        return new ApplicationInfoProvider(repositoryInformation, integrationProperties.application());
    }

}
