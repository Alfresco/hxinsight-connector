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
package org.alfresco.hxi_connector.prediction_applier.repository;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.List;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.model.repository.Node;
import org.alfresco.hxi_connector.common.test.util.DockerContainers;

@SpringBootTest(classes = {
        NodesClient.class},
        properties = {
                "alfresco.repository.nodes.username=admin",
                "alfresco.repository.nodes.password=admin",
                "logging.level.org.alfresco=DEBUG"})
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
class NodesClientIntegrationTest
{
    private static final String NODE_ID = "node-id";
    private static final String ASPECT = "cm:generalclassifiable";
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;
    private static final String PUT_NODE_RESPONSE = """
            {
                "entry": {
                    "id": "%s",
                    "aspectNames": [
                        "cm:versionable",
                        "%s"
                    ]
                }
            }""".formatted(NODE_ID, ASPECT);

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer repositoryMock = DockerContainers.createWireMockContainer();

    @SpyBean
    NodesClient nodesClient;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(repositoryMock.getHost(), repositoryMock.getPort());
    }

    @Test
    void testUpdateNode()
    {
        // given
        String url = "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + NODE_ID;
        givenThat(put(url)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(PUT_NODE_RESPONSE)));
        Node nodeToUpdate = new Node(NODE_ID, List.of("cm:versionable", ASPECT));

        // when
        Node actualNode = nodesClient.updateNode(nodeToUpdate);

        // then
        WireMock.verify(putRequestedFor(urlPathEqualTo(url))
                .withBasicAuth(new BasicCredentials("admin", "admin"))
                .withRequestBody(containing(ASPECT)));
        assertThat(actualNode)
                .isNotNull()
                .extracting(Node::id).isEqualTo(NODE_ID);
    }

    @Test
    void testUpdateNode_clientError_dontRetry()
    {
        // given
        String nodeId = "non-existing-id";
        givenThat(put("/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId)
                .willReturn(badRequest()));
        Node nodeToUpdate = new Node(nodeId);

        // when
        Throwable thrown = catchThrowable(() -> nodesClient.updateNode(nodeToUpdate));

        // then
        then(nodesClient).should(times(1)).updateNode(any());
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @Test
    void testUpdateNode_serverError_doRetry() throws IOException
    {
        // given
        String nodeId = "server-error";
        givenThat(put("/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId)
                .willReturn(serverError()));
        Node nodeToUpdate = new Node(nodeId);

        // when
        Throwable thrown = catchThrowable(() -> nodesClient.updateNode(nodeToUpdate));

        // then
        then(nodesClient).should(times(RETRY_ATTEMPTS)).updateNode(any());
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("alfresco.repository.nodes.base-url", repositoryMock::getBaseUrl);
        registry.add("alfresco.repository.nodes.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("alfresco.repository.nodes.retry.initial-delay", () -> RETRY_DELAY_MS);
    }
}
