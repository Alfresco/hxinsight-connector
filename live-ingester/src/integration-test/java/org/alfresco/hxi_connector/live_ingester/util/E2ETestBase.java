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
package org.alfresco.hxi_connector.live_ingester.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.http.HttpHeaders.HOST;
import static org.apache.http.entity.ContentType.APPLICATION_FORM_URLENCODED;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static software.amazon.awssdk.http.HttpStatusCode.OK;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.ATS_QUEUE;
import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.ATS_RESPONSE_QUEUE;
import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.BULK_INGESTER_QUEUE;
import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REPO_EVENT_TOPIC;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local.LocalStorageClient;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local.LocalStorageConfig;
import org.alfresco.hxi_connector.live_ingester.util.auth.AuthUtils;

@SpringBootTest(properties = "logging.level.org.alfresco=DEBUG")
@Import(LocalStorageConfig.class)
@ActiveProfiles("test")
@DirtiesContext // Forces framework to kill application after tests (i.e. before testcontainers die).
@Testcontainers
@NoArgsConstructor(access = PROTECTED)
@SuppressWarnings("PMD.FieldNamingConventions")
public class E2ETestBase
{
    public static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static String brokerUrl;
    private static WireMock hxAuthMock;
    private static WireMock hxInsightMock;
    private static WireMock sfsMock;
    protected ContainerSupport containerSupport;

    @Autowired
    private LocalStorageClient localStorageClient;

    @Container
    static final GenericContainer<?> activemqBroker = DockerContainers.createActiveMqContainer();
    @Container
    static final WireMockContainer hxAuthServer = DockerContainers.createWireMockContainer();
    @Container
    static final WireMockContainer hxInsightServer = DockerContainers.createWireMockContainer();
    @Container
    static final WireMockContainer sfsServer = DockerContainers.createWireMockContainer();
    @Container
    static final LocalStackContainer localStackServer = DockerContainers.createLocalStackContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, hxAuthServer.getBaseUrl());

        brokerUrl = "tcp://localhost:" + activemqBroker.getFirstMappedPort();
        registry.add("spring.activemq.broker-url", () -> brokerUrl);

        registry.add("hyland-experience.insight.base-url", hxInsightServer::getBaseUrl);

        registry.add("alfresco.repository.endpoint", () -> "activemq:topic:" + REPO_EVENT_TOPIC);
        registry.add("alfresco.bulk-ingester.endpoint", () -> "activemq:queue:" + BULK_INGESTER_QUEUE);
        registry.add("alfresco.transform.request.endpoint", () -> "activemq:queue:" + ATS_QUEUE + "?jmsMessageType=Text");
        registry.add("alfresco.transform.response.endpoint", () -> "activemq:queue:" + ATS_RESPONSE_QUEUE);
        registry.add("alfresco.transform.shared-file-store.host", () -> "http://" + sfsServer.getHost());
        registry.add("alfresco.transform.shared-file-store.port", sfsServer::getPort);

        registry.add("local.aws.endpoint", localStackServer.getEndpointOverride(S3)::toString);
        registry.add("local.aws.region", localStackServer::getRegion);
        registry.add("local.aws.access-key-id", localStackServer::getAccessKey);
        registry.add("local.aws.secret-access-key", localStackServer::getSecretKey);
    }

    @BeforeAll
    @SneakyThrows
    public static void beforeAll()
    {
        localStackServer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
        hxAuthMock = new WireMock(hxAuthServer.getHost(), hxAuthServer.getPort());
        hxInsightMock = new WireMock(hxInsightServer.getHost(), hxInsightServer.getPort());
        sfsMock = new WireMock(sfsServer.getHost(), sfsServer.getPort());
        WireMock.configureFor(hxAuthMock);
        WireMock.givenThat(post(AuthUtils.TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(OK)
                        .withBody(AuthUtils.createAuthResponseBody())));
    }

    @BeforeEach
    @SneakyThrows
    public void setUp()
    {
        containerSupport = ContainerSupport.getInstance(hxInsightMock, brokerUrl, localStorageClient);
    }

    @AfterEach
    public void reset()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        hxInsightMock.resetRequests();
        hxInsightMock.resetMappings();
        sfsMock.resetRequests();
        sfsMock.resetMappings();
        containerSupport.clearATSQueue();
    }

    @AfterAll
    public static void tearDown()
    {
        WireMock.configureFor(hxAuthMock);
        String expectedAuthRequestBody = AuthUtils.createAuthRequestBody();
        WireMock.verify(postRequestedFor(urlPathEqualTo(AuthUtils.TOKEN_PATH))
                .withHeader(HOST, new EqualToPattern(hxAuthServer.getHost() + ":" + hxAuthServer.getPort()))
                .withHeader(Exchange.CONTENT_TYPE, new EqualToPattern(APPLICATION_FORM_URLENCODED.getMimeType()))
                .withHeader(Exchange.CONTENT_LENGTH, new EqualToPattern(String.valueOf(expectedAuthRequestBody.getBytes(UTF_8).length)))
                .withRequestBody(new EqualToPattern(expectedAuthRequestBody)));
        ContainerSupport.removeInstance();
    }

    public static WireMock getHxInsightMock()
    {
        return hxInsightMock;
    }

    public static WireMock getSfsMock()
    {
        return sfsMock;
    }
}
