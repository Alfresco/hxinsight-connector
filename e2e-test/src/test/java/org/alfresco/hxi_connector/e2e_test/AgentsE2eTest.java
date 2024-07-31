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

import static java.lang.String.format;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.concatJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getHxInsightRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
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
    private static final String AGENT_ID = "61254576-62a3-453f-8cd8-19e2f6554f29";
    private static final String AVATAR_DEFAULT_ID = "-default-";

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

    @Test
    void shouldReturnAgentAvatar() throws IOException
    {
        // given: contained in wiremock file - get-agent-avatar.json.
        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("image/png")
                .when().get(repository.getBaseUrl() + format("/alfresco/api/-default-/private/hxi/versions/1/agents/%s/avatars/%s", AGENT_ID, AVATAR_DEFAULT_ID))
                .then().extract().response();

        // then
        assertEquals(SC_OK, response.statusCode());
        assertArrayEquals(Files.readAllBytes(Paths.get("src/test/resources/wiremock/hxinsight/__files/avatar.png")),
                IOUtils.toByteArray(response.body().asInputStream()));
    }

    @Test
    void shouldReturn404ForAvatarWithNotDefaultId() throws IOException
    {
        // given: contained in wiremock file - get-agent-avatar.json.
        String avatarId = "sample-avatar-id";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("image/png")
                .when().get(repository.getBaseUrl() + format("/alfresco/api/-default-/private/hxi/versions/1/agents/%s/avatars/%s", AGENT_ID, avatarId))
                .then().extract().response();

        // then
        assertEquals(SC_NOT_FOUND, response.statusCode());
        assertTrue(((String) response.jsonPath().get("error.briefSummary")).contains(format("Avatar with id=%s not found", avatarId)));
    }

    @Test
    void shouldNotGetAvatarForNotExistingAgent() throws IOException
    {
        // given: contained in wiremock file - get-agent-avatar.json.
        String nonExistentAgentId = "non-existent-agent-id";

        // when
        Response response = given().auth().preemptive().basic("admin", "admin")
                .contentType("image/png")
                .when().get(repository.getBaseUrl() + format("/alfresco/api/-default-/private/hxi/versions/1/agents/%s/avatars/%s", nonExistentAgentId, AVATAR_DEFAULT_ID))
                .then().extract().response();

        // then
        assertEquals(SC_SERVICE_UNAVAILABLE, response.statusCode());
        assertTrue(((String) response.jsonPath().get("error.briefSummary")).contains(format("Failed to get avatar for agent with id %s", nonExistentAgentId)));
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
