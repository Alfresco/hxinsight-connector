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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class LocalStorageClient
{
    private final URI endpoint;
    private final Region region;
    private final AwsCredentials awsCredentials;
    private final S3Configuration s3Config;
    private final S3Client s3client;

    public LocalStorageClient(LocalStorageConfig.Properties config)
    {
        this.endpoint = URI.create(config.endpoint());
        this.region = Region.of(config.region());
        this.awsCredentials = AwsBasicCredentials.create(config.accessKeyId(), config.secretAccessKey());
        this.s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        this.s3client = S3Client.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .serviceConfiguration(s3Config)
                .build();
    }

    public URL generatePreSignedUploadUrl(String bucketName, String objectKey, String contentType)
    {
        try (S3Presigner s3Presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .serviceConfiguration(s3Config)
                .build())
        {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest preSignPutRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(1))
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(preSignPutRequest).url();
        }
    }

    public List<String> listBucketContent(String bucketName)
    {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Iterable response = s3client.listObjectsV2Paginator(request);

        return response.stream()
                .flatMap(page -> page.contents().stream())
                .map(S3Object::key)
                .toList();
    }

    public InputStream downloadBucketObject(String bucketName, String objectKey)
    {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3client.getObjectAsBytes(getObjectRequest);

        return objectBytes.asInputStream();
    }
}
