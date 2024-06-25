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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryNodesClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

@Slf4j
@Testcontainers
@SuppressWarnings({"PMD.FieldNamingConventions"})
public class CreateNodeE2eTest
{
    private static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final int INITIAL_DELAY_MS = 300;
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";

    private static final Network network = Network.newNetwork();
    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final GenericContainer<?> sfs = DockerContainers.createSfsContainerWithin(network);
    @Container
    private static final GenericContainer<?> transformCore = DockerContainers.createTransformCoreAioContainerWithin(network);
    @Container
    private static final GenericContainer<?> transformRouter = DockerContainers.createTransformRouterContainerWithin(network);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer();
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final LocalStackContainer awsMock = DockerContainers.createLocalStackContainerWithin(network);
    @Container
    private static final GenericContainer<?> liveIngester = createLiveIngesterContainer();

    RepositoryNodesClient repositoryNodesClient = new RepositoryNodesClient(repository.getBaseUrl(), "admin", "admin");
    AwsS3Client awsS3Client = new AwsS3Client(awsMock.getHost(), awsMock.getFirstMappedPort(), BUCKET_NAME);

    @BeforeAll
    @SneakyThrows
    public static void beforeAll()
    {
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());
        awsMock.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingImageFile() throws IOException
    {
        // given
        File imageFile = new File("src/test/resources/images/quick.jpg");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryNodesClient.createNodeWithContent(PARENT_ID, imageFile);

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isEqualTo(initialBucketContent.size() + 1);

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id())));
        }, INITIAL_DELAY_MS);
    }

    @Test
    @SneakyThrows
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingTextFile() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryNodesClient.createNodeWithContent(PARENT_ID, "dummy.txt", fileContent, "text/plain");

        // then
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isEqualTo(initialBucketContent.size() + 1);

            S3Object s3Object = new ArrayList<>(CollectionUtils.disjunction(initialBucketContent, actualBucketContent)).get(0);
            String actualPdfContent = getPdfContent(s3Object.key());
            assertThat(actualPdfContent).isEqualToIgnoringWhitespace(DUMMY_CONTENT);

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id())));
        }, INITIAL_DELAY_MS);
    }

    @SneakyThrows
    private String getPdfContent(String objectKey)
    {
        @Cleanup
        InputStream pdfContent = awsS3Client.getS3ObjectContent(objectKey);
        @Cleanup
        PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(pdfContent));
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(document);
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // @formatter:off
        return DockerContainers.createExtendedRepositoryContainerWithin(network, true)
            .withJavaOpts("""
            -Ddb.driver=org.postgresql.Driver
            -Ddb.username=%s
            -Ddb.password=%s
            -Ddb.url=jdbc:postgresql://%s:5432/%s
            -Dmessaging.broker.url="failover:(nio://%s:61616)?timeout=3000&jms.useCompression=true"
            -Ddeployment.method=DOCKER_COMPOSE
            -Dtransform.service.enabled=true
            -Dtransform.service.url=http://transform-router:8095
            -Dsfs.url=http://shared-file-store:8099/
            -DlocalTransform.core-aio.url=http://transform-core-aio:8090/
            -Dalfresco-pdf-renderer.url=http://transform-core-aio:8090/
            -Djodconverter.url=http://transform-core-aio:8090/
            -Dimg.url=http://transform-core-aio:8090/
            -Dtika.url=http://transform-core-aio:8090/
            -Dtransform.misc.url=http://transform-core-aio:8090/
            -Dcsrf.filter.enabled=false
            -Dalfresco.restApi.basicAuthScheme=true
            -Xms1500m -Xmx1500m
            """.formatted(
                postgres.getUsername(),
                postgres.getPassword(),
                postgres.getNetworkAliases().stream().findFirst().get(),
                postgres.getDatabaseName(),
                activemq.getNetworkAliases().stream().findFirst().get())
            .replace("\n", " "));
        // @formatter:on
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        return DockerContainers.createLiveIngesterContainerWithin(network)
                .withEnv("HYLAND-EXPERIENCE_INSIGHT_BASE-URL",
                        "http://%s:8080".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLAND-EXPERIENCE_TOKEN-URI",
                        "http://%s:8080/token".formatted(hxInsightMock.getNetworkAliases().stream().findFirst().get()))
                .withEnv("AUTH_PROVIDERS_HYLAND-EXPERIENCE_CLIENT-ID", "dummy-client-key");
    }
}
