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

package org.alfresco.hxi_connector.hxi_extension.rest.api;

import java.net.http.HttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.hxi_connector.hxi_extension.service.config.HxInsightClientConfig;
import org.alfresco.hxi_connector.hxi_extension.service.util.AuthService;

@TestConfiguration
public class TestConfig
{
    @Bean
    public ObjectMapper objectMapper()
    {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient()
    {
        return HttpClient.newHttpClient();
    }

    @Bean
    public HxInsightClient hxInsightClient(
            HxInsightClientConfig config,
            AuthService authService,
            ObjectMapper objectMapper,
            HttpClient httpClient)
    {
        return new HxInsightClient(
                config,
                authService,
                objectMapper,
                httpClient);
    }

    @Bean
    public QuestionsEntityResource questionsEntityResource(HxInsightClient hxInsightClient)
    {
        return new QuestionsEntityResource(hxInsightClient);
    }
}
