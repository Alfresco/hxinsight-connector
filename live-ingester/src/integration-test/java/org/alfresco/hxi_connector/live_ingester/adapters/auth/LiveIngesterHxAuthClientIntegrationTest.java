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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.adapters.auth.HxAuthenticationClientTest;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.SecurityConfig;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        SecurityConfig.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
@SuppressWarnings("PMD.TestClassWithoutTestCases")
class LiveIngesterHxAuthClientIntegrationTest extends HxAuthenticationClientTest
{}
