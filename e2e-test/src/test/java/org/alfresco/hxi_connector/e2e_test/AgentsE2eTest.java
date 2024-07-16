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

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.concatJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getHxInsightRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;

import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;

@Testcontainers
@SuppressWarnings("PMD.FieldNamingConventions")
public class AgentsE2eTest
{
    static final Network network = Network.newNetwork();
    @Container
    static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq);

    @BeforeAll
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @Test
    void shouldReturnAgents()
    {
        // given: contained in wiremock file - get-agents.json.
        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("application/json")
                .when().get(repository.getBaseUrl() + "/alfresco/api/-default-/private/hxi/versions/1/agents")
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
        Map<String, String> expected0 = Map.of("name", "HR Policy Agent", "description", "This agent is responsible for HR policy predictions", "id", "61254576-62a3-453f-8cd8-19e2f6554f29");
        Map<String, String> expected1 = Map.of("name", "Knowledge Base Agent", "description", "Very smart about product knowledge", "id", "b999ee14-3974-41b2-bef8-70ab38c9e642");
        List<Map<String, Map<String, String>>> expected = List.of(Map.of("entry", expected0), Map.of("entry", expected1));
        assertEquals(expected, response.jsonPath().get("list.entries"));
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network)
                .withJavaOpts(concatJavaOpts(getMinimalRepoJavaOpts(postgres, activemq),
                        getHxInsightRepoJavaOpts(hxInsightMock))
                );
        // @formatter:on
    }
}
