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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.Cleanup;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.IntegrationCamelTestBase;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local.LocalStorageClient;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local.LocalStorageConfig;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        LocalStorageConfig.class,
        HttpFileUploader.class,
        Application.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
class HttpFileUploaderIntegrationTest extends IntegrationCamelTestBase
{
    private static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final String OBJECT_KEY = "dummy.txt";
    private static final String OBJECT_CONTENT = "Dummy's file dummy content";
    private static final String OBJECT_CONTENT_TYPE = "plain/text";
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;
    private static final String NODE_ID = "node-ref";

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final LocalStackContainer localStackServer = DockerContainers.createLocalStackContainer();

    @Autowired
    LocalStorageClient s3StorageMock;
    @SpyBean
    FileUploader fileUploader;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException
    {
        localStackServer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
    }

    @Test
    void testUpload() throws IOException
    {
        // given
        List<String> initialBucketContent = s3StorageMock.listBucketContent(BUCKET_NAME);

        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(OBJECT_CONTENT.getBytes());
        File fileToUpload = new File(fileContent);
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);

        FileUploadRequest fileUploadRequest = new FileUploadRequest(fileToUpload, OBJECT_CONTENT_TYPE, preSignedUrl);

        // when
        fileUploader.upload(fileUploadRequest, NODE_ID);

        // then
        List<String> actualBucketContent = s3StorageMock.listBucketContent(BUCKET_NAME);
        assertThat(initialBucketContent).doesNotContain(OBJECT_KEY);
        assertThat(actualBucketContent).contains(OBJECT_KEY);
        assertThat(differencesBetween(actualBucketContent, initialBucketContent)).containsExactly(OBJECT_KEY);
    }

    @Test
    void testUpload_serverError_doRetry() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(OBJECT_CONTENT.getBytes());
        File fileToUpload = new File(fileContent);
        URL url = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        URL preSignedUrl = new URL(url.getProtocol(), url.getHost(), 0, url.getFile());

        FileUploadRequest fileUploadRequest = new FileUploadRequest(fileToUpload, OBJECT_CONTENT_TYPE, preSignedUrl);

        // when
        Throwable thrown = catchThrowable(() -> fileUploader.upload(fileUploadRequest, NODE_ID));

        // then
        then(fileUploader).should(times(RETRY_ATTEMPTS)).upload(any(), any());
        assertThat(thrown)
                .cause().isInstanceOf(EndpointServerErrorException.class)
                .rootCause().isInstanceOf(HttpHostConnectException.class)
                .hasMessageContaining(preSignedUrl.getHost(), preSignedUrl.getPort());
    }

    @Test
    void testUpload_clientError_dontRetry() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(OBJECT_CONTENT.getBytes());
        File fileToUpload = new File(fileContent);
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl("invalid-bucket", OBJECT_KEY, OBJECT_CONTENT_TYPE);

        FileUploadRequest fileUploadRequest = new FileUploadRequest(fileToUpload, OBJECT_CONTENT_TYPE, preSignedUrl);

        // when
        Throwable thrown = catchThrowable(() -> fileUploader.upload(fileUploadRequest, NODE_ID));

        // then
        then(fileUploader).should(times(1)).upload(any(), any());
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @Test
    @SuppressWarnings({"ThrowableNotThrown", "ResultOfMethodCallIgnored"})
    void testLoggingOnError() throws IOException
    {
        // given
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(OBJECT_CONTENT.getBytes());
        File fileToUpload = new File(fileContent);
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl("invalid-bucket", OBJECT_KEY, OBJECT_CONTENT_TYPE);

        FileUploadRequest fileUploadRequest = new FileUploadRequest(fileToUpload, OBJECT_CONTENT_TYPE, preSignedUrl);
        ListAppender<ILoggingEvent> logEntries = LoggingUtils.createLogsListAppender(HttpFileUploader.class);

        // when
        catchThrowable(() -> fileUploader.upload(fileUploadRequest, NODE_ID));

        // then
        List<String> logs = logEntries.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(logs)
                .isNotEmpty()
                .last().asString()
                .contains("<Code>NoSuchBucket</Code>", "<BucketName>invalid-bucket</BucketName>")
                .doesNotContain("Authorization=");
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("local.aws.endpoint", localStackServer.getEndpointOverride(S3)::toString);
        registry.add("local.aws.region", localStackServer::getRegion);
        registry.add("local.aws.access-key-id", localStackServer::getAccessKey);
        registry.add("local.aws.secret-access-key", localStackServer::getSecretKey);
        registry.add("hyland-experience.storage.upload.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("hyland-experience.storage.upload.retry.initial-delay", () -> RETRY_DELAY_MS);
    }

    private static List<String> differencesBetween(List<String> firstList, List<String> secondList)
    {
        List<String> difference = new ArrayList<>(firstList);
        difference.removeAll(secondList);
        return difference;
    }
}
