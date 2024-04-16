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

package org.alfresco.hxi_connector.bulk_ingester.util.integration;

import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;

@Testcontainers
@DirtiesContext // Kills app before testcontainers (activemq) so there are no errors related to lost connection
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.UnusedPrivateMethod"})
public class ActiveMqIntegrationTestBase
{
    private static final String BULK_INGESTER_QUEUE = "test.bulk.ingester.queue";
    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainer();

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.activemq.broker-url", () -> "tcp://localhost:" + activemq.getFirstMappedPort());
        registry.add("alfresco.bulk.ingest.endpoint", () -> "activemq:queue:" + BULK_INGESTER_QUEUE);
    }

}
