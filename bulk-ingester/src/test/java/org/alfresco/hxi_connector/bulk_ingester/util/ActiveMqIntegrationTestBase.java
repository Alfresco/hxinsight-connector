/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

package org.alfresco.hxi_connector.bulk_ingester.util;

import java.time.Duration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class ActiveMqIntegrationTestBase
{
    private static final String ACTIVE_MQ_IMAGE = "quay.io/alfresco/alfresco-activemq";
    private static final String ACTIVE_MQ_TAG = DockerTags.getOrDefault("activemq.tag", "5.18.3-jre17-rockylinux8");
    private static final int ACTIVE_MQ_PORT = 61616;
    private static final String BULK_INGESTER_QUEUE = "test.bulk.ingester.queue";

    @Container
    private static final GenericContainer<?> activemq = createAMQContainer();

    public static GenericContainer<?> createAMQContainer()
    {
        return new GenericContainer(DockerImageName.parse(ACTIVE_MQ_IMAGE).withTag(ACTIVE_MQ_TAG))
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1g")
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2))
                .withExposedPorts(ACTIVE_MQ_PORT, 8161, 5672, 61613);
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.activemq.broker-url", () -> "tcp://localhost:" + activemq.getMappedPort(ACTIVE_MQ_PORT));
        registry.add("alfresco.bulk.ingest.endpoint", () -> "activemq:queue:" + BULK_INGESTER_QUEUE);
    }

}
