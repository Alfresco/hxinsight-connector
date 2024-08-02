/*-
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
package org.alfresco.hxi_connector.e2e_test;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.alfresco.hxi_connector.e2e_test.util.TestJsonUtils.getSetProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;

@Testcontainers
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.JUnitTestsShouldIncludeAssert"})
public class BulkIngesterE2eTest
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network)
            .withFileSystemBind("./src/test/resources/data/alfresco-dump.sql", "/docker-entrypoint-initdb.d/init-postgres.sql", BindMode.READ_ONLY);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final GenericContainer<?> bulkIngester = DockerContainers.createBulkIngesterContainerWithin(postgres, network)
            .dependsOn(postgres, activemq);
    @Container
    private static final GenericContainer<?> liveIngester = DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, network)
            .withEnv("ALFRESCO_REPOSITORY_VERSION_OVERRIDE", "23.2.1")
            .dependsOn(activemq, hxInsightMock);

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @Test
    void shouldIncludeACLInHxiUpdates()
    {
        // given
        String nodeId = "fa6b38cd-442a-4f77-9d3e-dc212a6b809e";
        String allowAccessFieldName = "ALLOW_ACCESS";
        String denyAccessFieldName = "DENY_ACCESS";

        // when
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events")).withRequestBody(matching(".*\"objectId\":\"%s.*".formatted(nodeId))));

            assertEquals(1, requests.size());

            JsonNode properties = objectMapper.readTree(requests.get(0).getBodyAsString())
                    .get(0)
                    .get("properties");

            assertTrue(properties.has(allowAccessFieldName));
            assertEquals(Set.of("GROUP_EVERYONE", "guest"), getSetProperty(properties, allowAccessFieldName));

            assertTrue(properties.has(denyAccessFieldName));
            assertEquals(Set.of(), getSetProperty(properties, denyAccessFieldName));
        }, 400);
    }
}
