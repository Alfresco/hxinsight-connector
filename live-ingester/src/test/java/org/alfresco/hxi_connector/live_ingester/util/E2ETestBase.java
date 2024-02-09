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
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.*;

import java.time.Duration;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.local.LocalStorageClient;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.local.LocalStorageConfig;

@SpringBootTest(properties = {
        "logging.level.org.alfresco=DEBUG"
}, classes = {
        LocalStorageConfig.class
})
@Testcontainers
@DirtiesContext // Forces framework to kill application after tests (i.e. before testcontainers die).
@NoArgsConstructor(access = PROTECTED)
@ActiveProfiles("test")
public class E2ETestBase
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getOrDefault("wiremock.tag", "3.3.1");
    private static final String ACTIVE_MQ_IMAGE = "quay.io/alfresco/alfresco-activemq";
    private static final String ACTIVE_MQ_TAG = DockerTags.getOrDefault("activemq.tag", "5.18.3-jre17-rockylinux8");
    private static final int ACTIVE_MQ_PORT = 61616;
    private static String hxInsightUrl;
    private static String brokerUrl;
    protected ContainerSupport containerSupport;
    private static final String LOCALSTACK_IMAGE = "localstack/localstack";
    private static final String LOCALSTACK_TAG = DockerTags.getOrDefault("localstack.tag", "3.0.2");
    public static final String BUCKET_NAME = "test-hxinsight-bucket";
    public static WireMock hxInsightMock;
    public static WireMock sfsMock;

    @Autowired
    private LocalStorageClient localStorageClient;

    @Container
    private static WireMockContainer hxInsight = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG))
            .withEnv("WIREMOCK_OPTIONS", "--verbose");

    @Container
    private static GenericContainer<?> activemq = createAMQContainer();

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final WireMockContainer sfs = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG)).withEnv("WIREMOCK_OPTIONS", "--verbose");

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    private static final LocalStackContainer localStackServer = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE).withTag(LOCALSTACK_TAG));

    public static GenericContainer<?> createAMQContainer()
    {
        return new GenericContainer<>(DockerImageName.parse(ACTIVE_MQ_IMAGE).withTag(ACTIVE_MQ_TAG))
                .withEnv("JAVA_OPTS", "-Xms512m -Xmx1g")
                .waitingFor(Wait.forListeningPort())
                .withStartupTimeout(Duration.ofMinutes(2))
                .withExposedPorts(ACTIVE_MQ_PORT, 8161, 5672, 61613);
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry)
    {

        hxInsightMock = new WireMock(hxInsight.getHost(), hxInsight.getPort());
        sfsMock = new WireMock(sfs.getHost(), sfs.getPort());

        hxInsightUrl = hxInsight.getBaseUrl();
        registry.add("hyland-experience.insight.base-url", () -> hxInsightUrl);

        brokerUrl = "tcp://localhost:" + activemq.getMappedPort(ACTIVE_MQ_PORT);
        registry.add("spring.activemq.broker-url", () -> brokerUrl);

        registry.add("alfresco.repository.endpoint", () -> "activemq:topic:" + REPO_EVENT_TOPIC);

        registry.add("alfresco.transform.request.endpoint", () -> "activemq:queue:" + ATS_QUEUE + "?jmsMessageType=Text");

        registry.add("alfresco.transform.response.endpoint", () -> "activemq:queue:" + ATS_RESPONSE_QUEUE);

        registry.add("alfresco.transform.shared-file-store.host", () -> "http://" + sfs.getHost());
        registry.add("alfresco.transform.shared-file-store.port", sfs::getPort);

        registry.add("local.aws.endpoint", localStackServer.getEndpointOverride(S3)::toString);
        registry.add("local.aws.region", localStackServer::getRegion);
        registry.add("local.aws.access-key-id", localStackServer::getAccessKey);
        registry.add("local.aws.secret-access-key", localStackServer::getSecretKey);
    }

    @BeforeAll
    @SneakyThrows
    public static void setUpS3()
    {
        localStackServer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
    }

    @BeforeEach
    @SneakyThrows
    public void setUp()
    {
        containerSupport = ContainerSupport.getInstance(hxInsight, brokerUrl, localStorageClient);
    }

    @AfterEach
    public void reset()
    {
        WireMock.reset();
        containerSupport.clearATSQueue();
    }

    @AfterAll
    public static void tearDown()
    {
        ContainerSupport.removeInstance();
    }
}
