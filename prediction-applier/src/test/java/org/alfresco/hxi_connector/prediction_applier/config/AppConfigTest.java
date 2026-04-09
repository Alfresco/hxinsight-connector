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
package org.alfresco.hxi_connector.prediction_applier.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.net.http.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.AcsHealthProbe;
import org.alfresco.hxi_connector.common.config.properties.Retry;
import org.alfresco.hxi_connector.common.exception.ValidationException;

@SuppressWarnings("PMD.CloseResource") // HttpClient is not AutoCloseable on Java 17
class AppConfigTest
{

    private final AppConfig appConfig = new AppConfig();

    @Test
    void shouldCreateObjectMapper()
    {
        ObjectMapper objectMapper = appConfig.objectMapper();

        assertThat(objectMapper).isNotNull();
    }

    @Test
    void shouldCreateHttpClient()
    {
        HttpClient httpClient = appConfig.httpClient();
        assertThat(httpClient).isNotNull();
    }

    @Test
    void shouldCreateAcsHealthProbe()
    {
        HttpClient httpClient = HttpClient.newHttpClient();
        RepositoryApiProperties properties = new RepositoryApiProperties(
                "http://localhost:8080",
                "http://localhost:8080/alfresco/api/discovery",
                new Retry(),
                new RepositoryApiProperties.HealthProbe("http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/probes/-live-", 5, 1));

        AcsHealthProbe probe = appConfig.acsHealthProbe(httpClient, properties);

        assertThat(probe).isNotNull();
    }

    @Test
    void shouldFailToCreateRepositoryInformationWithBlankDiscoveryEndpoint()
    {
        HttpClient httpClient = HttpClient.newHttpClient();
        RepositoryApiProperties properties = new RepositoryApiProperties(
                "http://localhost:8080",
                "",
                new Retry(),
                new RepositoryApiProperties.HealthProbe("", 5, 1));

        Throwable throwable = catchThrowable(() -> appConfig.repositoryInformation(
                properties, null, new ObjectMapper(), httpClient, null));

        assertThat(throwable).isInstanceOf(ValidationException.class);
    }
}
