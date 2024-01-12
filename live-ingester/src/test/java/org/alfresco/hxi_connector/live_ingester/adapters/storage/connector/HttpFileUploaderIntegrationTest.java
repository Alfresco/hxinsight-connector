/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import lombok.Cleanup;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.local.LocalStorageClient;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.local.LocalStorageConfig;
import org.alfresco.hxi_connector.live_ingester.util.DockerTags;

@SpringBootTest(classes = {
        CamelAutoConfiguration.class,
        LocalStorageConfig.class,
        HttpFileUploader.class})
@ActiveProfiles({"test"})
@Testcontainers
class HttpFileUploaderIntegrationTest
{
    private static final String LOCALSTACK_IMAGE = "localstack/localstack";
    private static final String LOCALSTACK_TAG = DockerTags.getOrDefault("localstack.tag", "3.0.2");
    private static final String BUCKET_NAME = "test-hxinsight-bucket";
    private static final String OBJECT_KEY = "dummy.txt";
    private static final String OBJECT_CONTENT = "Dummy's file dummy content";
    private static final String OBJECT_CONTENT_TYPE = "plain/text";

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final LocalStackContainer localStackServer = new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE).withTag(LOCALSTACK_TAG));

    @Autowired
    LocalStorageClient s3StorageMock;
    @Autowired
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
        InputStream fileInputStream = new ByteArrayInputStream(OBJECT_CONTENT.getBytes());
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        FileUploadRequest fileUploadRequest = new FileUploadRequest(fileInputStream, OBJECT_CONTENT_TYPE, preSignedUrl);

        // when
        fileUploader.upload(fileUploadRequest);

        // then
        List<String> actualBucketContent = s3StorageMock.listBucketContent(BUCKET_NAME);
        assertThat(initialBucketContent).doesNotContain(OBJECT_KEY);
        assertThat(actualBucketContent).contains(OBJECT_KEY);
        assertThat(actualBucketContent.size() - initialBucketContent.size()).isEqualTo(1);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("local.aws.endpoint", localStackServer.getEndpointOverride(S3)::toString);
        registry.add("local.aws.region", localStackServer::getRegion);
        registry.add("local.aws.access-key-id", localStackServer::getAccessKey);
        registry.add("local.aws.secret-access-key", localStackServer::getSecretKey);
    }
}
