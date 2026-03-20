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
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;
import static org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient.ADMIN_USER;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
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

/**
 * E2E tests verifying that real ATS (Alfresco Transform Service) transformations work correctly with the mime-type mapping configured in application.yml:
 *
 * <pre>
 * [text/*]        → application/pdf
 * [image/*]       → image/jpeg
 * [application/*] → application/pdf
 * [audio/*]       → "" (no content)
 * [*]             → * (passthrough)
 * </pre>
 *
 * These E2E tests use real Docker containers for transform-router, transform-core-aio and shared-file-store. This means the tests will fail if the configured ATS do not support the required transformations.
 * <p>
 * Transform requests from the live-ingester go through ACS's transform pipeline (matching the docker-compose setup): ACS consumes from {@code acs-repo-transform-request}, checks the transform capability registry, uploads source content to SFS, and forwards enriched requests to the transform-router. This ensures old/broken transform versions are detected by ACS's config check.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.JUnitTestsShouldIncludeAssert"})
public class ATSTransformE2eTest
{
    private static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final int DELAY_MS = 1000;
    private static final String PARENT_ID = "-my-";

    private static final Network network = Network.newNetwork();
    @Container
    private static final PostgreSQLContainer<?> postgres = DockerContainers.createPostgresContainerWithin(network);
    @Container
    private static final GenericContainer<?> activemq = DockerContainers.createActiveMqContainerWithin(network);
    @Container
    private static final GenericContainer<?> sfs = DockerContainers.createSfsContainerWithin(network);
    @Container
    private static final GenericContainer<?> transformCore = DockerContainers.createTransformCoreAioContainerWithin(network)
            // Override default 1024m — LibreOffice (used for text→pdf) needs significantly more memory,
            // matching the 3000m configured in docker-compose.yml
            .withEnv("JAVA_OPTS", "-Xms256m -Xmx3000m")
            .dependsOn(activemq, sfs);
    @Container
    private static final GenericContainer<?> transformRouter = DockerContainers.createTransformRouterContainerWithin(network)
            .dependsOn(activemq, transformCore);
    @Container
    private static final WireMockContainer hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
            .withFileSystemBind("src/test/resources/wiremock/hxinsight", "/home/wiremock", BindMode.READ_ONLY);
    @Container
    private static final LocalStackContainer awsMock = DockerContainers.createLocalStackContainerWithin(network);
    @Container
    private static final AlfrescoRepositoryContainer repository = createRepositoryContainer()
            .dependsOn(postgres, activemq, transformRouter, sfs);
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

    /**
     * Verifies that a text/plain file is transformed to application/pdf by the ATS. This test will fail if the ATS version does not support text/plain → application/pdf transformation.
     * <p>
     * Mapping: [text/*] → application/pdf
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenTextFile_whenCreatedInRepo_thenATSTransformsToPdfAndContentUploadedToS3()
    {
        // given
        File textFile = new File("src/test/resources/test-files/All cats are gray.txt");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, textFile);

        // then - verify the full ATS round-trip: request → real ATS transform → SFS → S3 upload → ingestion event
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isGreaterThan(initialBucketContent.size());

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }

    /**
     * Verifies that an image/png file is transformed to image/jpeg by the ATS.
     * <p>
     * Mapping: [image/*] → image/jpeg
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenPngImage_whenCreatedInRepo_thenATSTransformsToJpegAndContentUploadedToS3()
    {
        // given
        File pngFile = new File("src/test/resources/test-files/Bird.png");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, pngFile);

        // then - verify the full ATS round-trip for image transformation
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isGreaterThan(initialBucketContent.size());

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }

    /**
     * Verifies that an application/pdf file is passed through directly (source matches target type).
     * <p>
     * Mapping: [application/*] → application/pdf, so application/pdf → application/pdf is a passthrough.
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenPdfFile_whenCreatedInRepo_thenPassthroughDirectlyToS3()
    {
        // given
        File pdfFile = new File("src/test/resources/test-files/Alfresco Control Center.pdf");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, pdfFile);

        // then - verify passthrough: PDF source matches PDF target, no ATS transform needed
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isGreaterThan(initialBucketContent.size());

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }

    /**
     * Verifies that a .docx file is transformed to application/pdf by the ATS.
     * <p>
     * Mapping: [application/*] → application/pdf
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenDocxFile_whenCreatedInRepo_thenATSTransformsToPdfAndContentUploadedToS3()
    {
        // given
        File docxFile = new File("src/test/resources/test-files/Alfresco Content Services 7.4.docx");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, docxFile);

        // then - verify the full ATS round-trip: docx → pdf via LibreOffice
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isGreaterThan(initialBucketContent.size());

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }

    /**
     * Verifies that an audio file results in no content being uploaded (empty mapping).
     * <p>
     * Mapping: [audio/*] → "" (no content ingestion)
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenAudioFile_whenCreatedInRepo_thenNoContentUploadedToS3()
    {
        // given
        File audioFile = new File("src/test/resources/test-files/Test audio.mp3");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        repositoryClient.createNodeWithContent(PARENT_ID, audioFile);

        // then - verify no content was uploaded to S3 (no presigned-urls call for content)
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isEqualTo(initialBucketContent.size());

            // Metadata event should still be sent, but no presigned-urls request (no content upload)
            WireMock.verify(exactly(0), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(1), postRequestedFor(urlEqualTo("/ingestion-events")));
        }, DELAY_MS);
    }

    /**
     * Verifies that a video file is passed through directly via the catch-all mapping.
     * <p>
     * Mapping: [*] → * (passthrough) — no video-specific mapping exists, so the catch-all applies.
     */
    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    final void givenVideoFile_whenCreatedInRepo_thenPassthroughDirectlyToS3()
    {
        // given
        File videoFile = new File("src/test/resources/test-files/Test video.mov");
        List<S3Object> initialBucketContent = awsS3Client.listS3Content();

        // when
        Node createdNode = repositoryClient.createNodeWithContent(PARENT_ID, videoFile);

        // then - verify passthrough via catch-all [*] -> * mapping: no ATS transform needed
        RetryUtils.retryWithBackoff(() -> {
            List<S3Object> actualBucketContent = awsS3Client.listS3Content();
            assertThat(actualBucketContent.size()).isGreaterThan(initialBucketContent.size());

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()).and(containing("sourceTimestamp")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }

    private static AlfrescoRepositoryContainer createRepositoryContainer()
    {
        // Enable ACS transform service with remote transform config only (no local transform URLs).
        // Local transforms bypass SFS (sending content directly to the t-engine via HTTP) and fail
        // in the test environment. Remote transforms go through SFS + transform-router, matching
        // the production pipeline and ensuring ACS checks the transform capability registry.
        // This means old/broken transform versions will be detected by ACS's config check.
        String repoJavaOpts = DockerContainers.concatJavaOpts(
                DockerContainers.getMinimalRepoJavaOpts(postgres, activemq)
                        .replace("-Dtransform.service.enabled=false", "-Dtransform.service.enabled=true"),
                "-Dtransform.service.url=http://transform-router:8095",
                "-Dsfs.url=http://shared-file-store:8099");
        return DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(repoJavaOpts);
    }

    private static GenericContainer<?> createLiveIngesterContainer()
    {
        // The mime-type mapping must be set explicitly via system properties — the default application.yml
        // does not include it, so without these properties everything defaults to passthrough.
        //
        // The live-ingester sends transform requests to the default acs-repo-transform-request queue.
        // ACS processes these requests: it checks the transform capability registry, uploads source
        // content to SFS, and forwards enriched requests to the transform-router. This mirrors the
        // docker-compose setup.
        return DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network)
                .withEnv("JAVA_TOOL_OPTIONS",
                        "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n"
                                + " -Dalfresco.transform.mime-type.mapping.[text/*]=application/pdf"
                                + " -Dalfresco.transform.mime-type.mapping.[image/*]=image/jpeg"
                                + " -Dalfresco.transform.mime-type.mapping.[application/*]=application/pdf"
                                + " -Dalfresco.transform.mime-type.mapping.[audio/*]="
                                + " -Dalfresco.transform.mime-type.mapping.[*]=*");
    }
}
