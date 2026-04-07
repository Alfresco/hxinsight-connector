/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class CreateNodeE2eTest
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final int DELAY_MS = 500;
    private static final int MAX_RETRY_ATTEMPTS = 60;
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";
    private static final String PERMISSIONS_PROPERTY = "PERMISSIONS";

    private static final Network network = Network.newNetwork();
    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final LocalStackContainer awsMock = DockerContainers.createLocalStackContainerWithin(network);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq);
    @Container
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer()
            .dependsOn(activemq, hxInsightMock, awsMock, repository);

    private RepositoryClient repositoryClient;
    private AwsS3Client awsS3Client;

    @BeforeAll
    public void beforeAll() throws IOException, InterruptedException
    {
        awsMock.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
        awsS3Client = new AwsS3Client(awsMock.getHost(), awsMock.getFirstMappedPort(), BUCKET_NAME);
        repositoryClient = new RepositoryClient(repository.getBaseUrl(), ADMIN_USER);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testCreateNodeContainingImageFile() throws IOException
    {
        // given
        File imageFile = new File("src/test/resources/images/quick.jpg");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, imageFile);

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isEqualTo(initialBucketContent.size() + 1);

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, MAX_RETRY_ATTEMPTS, DELAY_MS);
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testCreateNodeContainingTextFile() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, "dummy.txt", fileContent, "text/plain");

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isEqualTo(initialBucketContent.size() + 1);

            S3Object s3Object = new ArrayList<>(CollectionUtils.disjunction(initialBucketContent, actualBucketContent)).get(0);
            String actualContent = getTextContent(s3Object.key());
            assertThat(actualContent).isEqualToIgnoringWhitespace(DUMMY_CONTENT);

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, MAX_RETRY_ATTEMPTS, DELAY_MS);
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void testCreateNodeWithDefaultPermissions()
    {
        // when
        Node createdNode = repositoryClient.createNodeWithContent(
                PARENT_ID,
                "test file",
                new ByteArrayInputStream("test file content".getBytes()),
                "text/plain");

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events")));

            assertFalse(requests.isEmpty(), "Expected ingestion events but none were received");

            Optional<LoggedRequest> createNodeEvent = requests.stream()
                    .filter(request -> request.getBodyAsString().contains(createdNode.id()))
                    .findFirst();

            assertTrue(createNodeEvent.isPresent(), "Expected node creation event not found");

            JsonNode properties = objectMapper.readTree(createNodeEvent.get().getBodyAsString())
                    .get(0)
                    .get("properties");

            assertTrue(properties.has(PERMISSIONS_PROPERTY), "Expected properties to contain PERMISSIONS field");

            JsonNode permissionsValue = properties.get(PERMISSIONS_PROPERTY).get("value");

            // Assert the entire permissions structure
            Map<String, Object> expectedPermissions = Map.of(
                    "read", List.of(Map.of("id", "GROUP_EVERYONE", "type", "GROUP")),
                    "deny", List.of(),
                    "principalsType", "effective");

            Map<String, Object> actualPermissions = objectMapper.convertValue(permissionsValue, Map.class);
            assertEquals(expectedPermissions, actualPermissions, "Permissions structure does not match expected format");
        }, MAX_RETRY_ATTEMPTS, DELAY_MS);
    }

    @SneakyThrows
    private String getTextContent(String objectKey)
    {
        @Cleanup
        InputStream content = awsS3Client.getS3ObjectContent(objectKey);
        return new String(content.readAllBytes());
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        return DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(getMinimalRepoJavaOpts(postgres, activemq));
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network);
    }
}
