/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.e2e_test.util.TestJsonUtils.asSet;

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
            .withEnv("ALFRESCO_FILTER_PATH_ALLOW", "dad275aa-affc-487d-a7ed-92cf8e6ce351")
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
    void shouldIncludeBasicProperties()
    {
        // given
        String nodeId = "02acf462-533d-4e1b-9825-05fa934140da";
        String sourceId = "a1f3e7c0-d193-7023-ce1d-0a63de491876";
        String eventType = "createOrUpdate";

        // when
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events")).withRequestBody(matching(".*\"objectId\":\"%s.*".formatted(nodeId))));

            assertEquals(1, requests.size());

            JsonNode event = objectMapper.readTree(requests.get(0).getBodyAsString())
                    .get(0);

            assertEquals(nodeId, event.get("objectId").asText());
            assertEquals(sourceId, event.get("sourceId").asText());
            assertEquals(eventType, event.get("eventType").asText());
            assertTrue(event.has("sourceTimestamp"));
            assertTrue(event.has("properties"));
        }, 15, 200);
    }

    @Test
    void shouldIncludeACLInHxiUpdates()
    {
        // given
        String nodeId = "02acf462-533d-4e1b-9825-05fa934140da";
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
            assertEquals(Set.of("GROUP_EVERYONE"), asSet(properties.get(allowAccessFieldName).get("value")));

            assertFalse(properties.has(denyAccessFieldName));
        }, 15, 200);
    }
}
