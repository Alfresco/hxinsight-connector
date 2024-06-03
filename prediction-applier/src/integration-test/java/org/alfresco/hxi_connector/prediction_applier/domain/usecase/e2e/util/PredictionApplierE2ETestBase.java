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
package org.alfresco.hxi_connector.prediction_applier.domain.usecase.e2e.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.ALFRESCO_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.HXI_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils.TOKEN_PATH;
import static org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils.createAuthResponseBody;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.adapters.auth.util.AuthUtils;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.prediction_applier.util.ContainerSupport;

@SpringBootTest(properties = {"logging.level.org.alfresco=DEBUG"})
@ActiveProfiles("test")
@DirtiesContext // Forces framework to kill application after tests (i.e. before testcontainers die).
@Testcontainers
@NoArgsConstructor(access = PROTECTED)
@SuppressWarnings("PMD.FieldNamingConventions")
public class PredictionApplierE2ETestBase
{
    private static String brokerUrl;
    private static WireMock hxAuthMock;
    private static WireMock hxInsightMock;
    private static WireMock repositoryMock;
    protected ContainerSupport containerSupport;

    @Container
    static final GenericContainer<?> activemqBroker = DockerContainers.createActiveMqContainer();
    @Container
    static final WireMockContainer hxAuthServer = DockerContainers.createWireMockContainer();
    @Container
    static final WireMockContainer hxInsightServer = DockerContainers.createWireMockContainer();
    @Container
    static final WireMockContainer repositoryServer = DockerContainers.createWireMockContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry)
    {
        AuthUtils.overrideAuthProperties(registry, hxAuthServer.getBaseUrl(), HXI_AUTH_PROVIDER);
        AuthUtils.overrideAuthProperties(registry, hxAuthServer.getBaseUrl(), ALFRESCO_AUTH_PROVIDER);

        brokerUrl = "tcp://localhost:" + activemqBroker.getFirstMappedPort();
        registry.add("spring.activemq.broker-url", () -> brokerUrl);

        registry.add("hyland-experience.insight.predictions.source-base-url", hxInsightServer::getBaseUrl);

        registry.add("alfresco.repository.endpoint", repositoryServer::getBaseUrl);
    }

    @BeforeAll
    @SneakyThrows
    public static void beforeAll()
    {
        hxAuthMock = new WireMock(hxAuthServer.getHost(), hxAuthServer.getPort());
        hxInsightMock = new WireMock(hxInsightServer.getHost(), hxInsightServer.getPort());
        repositoryMock = new WireMock(repositoryServer.getHost(), repositoryServer.getPort());
        WireMock.configureFor(hxAuthMock);
        WireMock.givenThat(post(TOKEN_PATH)
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withBody(createAuthResponseBody())));
    }

    @BeforeEach
    @SneakyThrows
    public void setUp()
    {
        String repositoryBaseUrl = repositoryServer.getBaseUrl();
        containerSupport = ContainerSupport.getInstance(hxInsightMock, brokerUrl, repositoryMock, repositoryBaseUrl);
    }

    @AfterEach
    public void reset()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        hxInsightMock.resetRequests();
        hxInsightMock.resetMappings();
        repositoryMock.resetRequests();
        repositoryMock.resetMappings();
    }

    @AfterAll
    public static void tearDown()
    {
        WireMock.configureFor(hxAuthMock);
        ContainerSupport.removeInstance();
    }
}
