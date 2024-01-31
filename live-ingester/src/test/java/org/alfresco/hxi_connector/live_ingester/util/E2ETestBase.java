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
package org.alfresco.hxi_connector.live_ingester.util;

import static lombok.AccessLevel.PROTECTED;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REPO_EVENT_TOPIC;

import java.time.Duration;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@SpringBootTest(properties = {
        "logging.level.org.alfresco=DEBUG"
})
@Testcontainers
@DirtiesContext // Forces framework to kill application after tests (i.e. before testcontainers die).
@NoArgsConstructor(access = PROTECTED)
public class E2ETestBase
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getOrDefault("wiremock.tag", "3.3.1");
    private static final String ACTIVE_MQ_IMAGE = "quay.io/alfresco/alfresco-activemq";
    private static final String ACTIVE_MQ_TAG = DockerTags.getOrDefault("activemq.tag", "5.18.3-jre17-rockylinux8");
    private static final int ACTIVE_MQ_PORT = 61616;

    @Container
    private static WireMockContainer hxInsight = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG));
    @Container
    private static GenericContainer activemq = createAMQContainer();

    private static String hxInsightUrl;
    private static String brokerUrl;

    public static GenericContainer createAMQContainer()
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
        hxInsightUrl = hxInsight.getBaseUrl();
        registry.add("hyland-experience.insight.base-url", () -> hxInsightUrl);

        brokerUrl = "tcp://localhost:" + activemq.getMappedPort(ACTIVE_MQ_PORT);
        registry.add("spring.activemq.broker-url", () -> brokerUrl);

        registry.add("integration.alfresco.repository.endpoint", () -> "activemq:topic:" + REPO_EVENT_TOPIC);
    }

    @SneakyThrows
    public ContainerSupport configureContainers()
    {
        WireMock.configureFor(hxInsight.getHost(), hxInsight.getPort());

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        return new ContainerSupport(connection);
    }
}
