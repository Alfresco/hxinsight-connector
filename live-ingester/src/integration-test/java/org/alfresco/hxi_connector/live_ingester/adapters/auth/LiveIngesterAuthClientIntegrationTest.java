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
package org.alfresco.hxi_connector.live_ingester.adapters.auth;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.HXI_AUTH_PROVIDER;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.adapters.auth.DefaultAuthenticationClientTest;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.AuthProperties;
import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;

@SpringBootTest(classes = {LiveIngesterAuthClientIntegrationTest.LiveIngesterAuthClientIntegrationTestConfig.class, LiveIngesterAuthClient.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
@SuppressWarnings("PMD.TestClassWithoutTestCases")
class LiveIngesterAuthClientIntegrationTest extends DefaultAuthenticationClientTest
{
    @TestConfiguration
    public static class LiveIngesterAuthClientIntegrationTestConfig
    {
        @Bean
        public AuthProperties authorizationProperties()
        {
            AuthProperties authProperties = new AuthProperties();
            AuthProperties.AuthProvider hXauthProvider = AuthUtils.createAuthProvider(hxAuthMock.getBaseUrl() + AuthUtils.TOKEN_PATH);
            authProperties.setProviders(Map.of(HXI_AUTH_PROVIDER, hXauthProvider));
            authProperties.setRetry(
                    new org.alfresco.hxi_connector.common.config.properties.Retry(RETRY_ATTEMPTS, RETRY_DELAY_MS, 1,
                            Collections.emptySet()));
            return authProperties;
        }
    }
}
