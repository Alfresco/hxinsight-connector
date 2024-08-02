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
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;
import static org.alfresco.hxi_connector.e2e_test.util.TestJsonUtils.asSet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

@Slf4j
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.FieldNamingConventions"})
/**
 * End-to-end base tests for creating a node with content. Due to some issues with testcontainers environment, this class is extended by 2 other test classes. One of its children is command docker-compose dependent and is enabled for GitHub Actions only and disabled for maven builds. The other child class works the other way around.
 */
abstract class CreateNodeE2eTestBase
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final int MAX_ATTEMPTS = 5;
    private static final int INITIAL_DELAY_MS = 700;
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Dummy's file dummy content";
    private static final String ALLOW_ACCESS_PROPERTY = "ALLOW_ACCESS";
    private static final String DENY_ACCESS_PROPERTY = "DENY_ACCESS";

    protected RepositoryClient repositoryClient;
    protected AwsS3Client awsS3Client;

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
                    .withRequestBody(containing(createdNode.id()))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, MAX_ATTEMPTS, INITIAL_DELAY_MS);
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
            String actualPdfContent = getPdfContent(s3Object.key());
            assertThat(actualPdfContent).isEqualToIgnoringWhitespace(DUMMY_CONTENT);

            WireMock.verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));
            WireMock.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing(createdNode.id()))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, MAX_ATTEMPTS, INITIAL_DELAY_MS);
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

            assertFalse(requests.isEmpty());

            Optional<LoggedRequest> createNodeEvent = requests.stream()
                    .filter(request -> request.getBodyAsString().contains(createdNode.id()))
                    .findFirst();

            assertTrue(createNodeEvent.isPresent());

            JsonNode properties = objectMapper.readTree(createNodeEvent.get().getBodyAsString())
                    .get(0)
                    .get("properties");

            assertTrue(properties.has(ALLOW_ACCESS_PROPERTY));
            assertEquals(Set.of("GROUP_EVERYONE"), asSet(properties.get(ALLOW_ACCESS_PROPERTY).get("value")));

            assertTrue(properties.has(DENY_ACCESS_PROPERTY));
            assertEquals(Set.of(), asSet(properties.get(DENY_ACCESS_PROPERTY).get("value")));
        }, MAX_ATTEMPTS, INITIAL_DELAY_MS);
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
}
