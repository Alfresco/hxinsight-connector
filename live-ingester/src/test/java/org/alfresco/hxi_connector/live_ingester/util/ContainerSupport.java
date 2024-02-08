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
package org.alfresco.hxi_connector.live_ingester.util;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.live_ingester.util.RetryUtils.retryWithBackoff;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.local.LocalStorageClient;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.IOUtils;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@Slf4j
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport {
    public static final String HX_INSIGHT_INGEST_ENDPOINT = "/ingest";
    private static final int HX_INSIGHT_SUCCESS_CODE = 201;
    public static final String REPO_EVENT_TOPIC = "repo.event.topic";
    public static final String ATS_QUEUE = "ats.queue";
    public static final String REQUEST_ID_PLACEHOLDER = "_REQUEST_ID_";
    private static ContainerSupport instance;
    private Session session;
    private MessageProducer repoEventProducer;
    private MessageConsumer atsConsumer;

    public static final String ATS_RESPONSE_QUEUE = "ats.response.queue";
    private MessageProducer atsEventProducer;
    public static final String SFS_ENDPOINT = "/alfresco/api/-default-/private/sfs/versions/1/file/e71dd823-82c7-477c-8490-04cb0e826e66";
    private static final int OK_SUCCESS_CODE = 200;
    private static final String HX_INSIGHT_PRE_SIGNED_URL_PATH = "/pre-signed-url";
    private static final String HX_INSIGHT_LOCATION_PATH = "/ingestion-base-path";
    private static final String HX_INSIGHT_RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    private static final String OBJECT_KEY = "dummy-file.pdf";
    private static final String OBJECT_CONTENT_TYPE = "application/pdf";
    LocalStorageClient s3StorageMock;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMockContainer hxInsight, String brokerUrl, LocalStorageClient s3StorageMock) {
        WireMock.configureFor(hxInsight.getHost(), hxInsight.getPort());

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic repoTopic = session.createTopic(REPO_EVENT_TOPIC);
        repoEventProducer = session.createProducer(repoTopic);
        Queue atsQueue = session.createQueue(ATS_QUEUE);
        atsConsumer = session.createConsumer(atsQueue);

        Queue atsResponseQueue = session.createQueue(ATS_RESPONSE_QUEUE);
        atsEventProducer = session.createProducer(atsResponseQueue);

        this.s3StorageMock = s3StorageMock;
    }

    public static ContainerSupport getInstance(WireMockContainer hxInsight, String brokerUrl, LocalStorageClient s3StorageMock) {
        if (instance == null) {
            instance = new ContainerSupport(hxInsight, brokerUrl, s3StorageMock);
        }
        return instance;
    }

    public static void removeInstance() {
        instance = null;
    }

    public void prepareHxInsightToReturnSuccess() {
        givenThat(post(HX_INSIGHT_INGEST_ENDPOINT)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_SUCCESS_CODE)));
    }

    @SneakyThrows
    public void raiseRepoEvent(String repoEvent) {
        repoEventProducer.send(session.createTextMessage(repoEvent));
    }

    @SneakyThrows
    public void expectHxIngestMessageReceived(String expectedBody) {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_INGEST_ENDPOINT))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    @SneakyThrows
    public void verifyATSRequestReceived(String expectedBody) {
        TextMessage received = (TextMessage) retryWithBackoff(() -> {
            TextMessage message = receiveATSTextMessage();
            assertNotNull(message);
            return message;
        });

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> receivedMap = objectMapper.readValue(received.getText(), HashMap.class);
        String requestId = (String) receivedMap.get("requestId");

        Map<String, Object> expectedMap = objectMapper.readValue(expectedBody.replace(REQUEST_ID_PLACEHOLDER, requestId), HashMap.class);

        assertEquals(expectedMap, receivedMap);
    }

    @SneakyThrows
    public void clearATSQueue() {
        while (receiveATSTextMessage() != null) {
            log.debug("Removed message from ATS queue");
        }
    }

    @SneakyThrows
    TextMessage receiveATSTextMessage() {
        return (TextMessage) atsConsumer.receiveNoWait();
    }

    @SneakyThrows
    public void raiseATSEvent(String atsEvent) {
        atsEventProducer.send(session.createTextMessage(atsEvent));
    }

    @SneakyThrows
    public void prepareSFSToReturnSuccess() {

        WireMock.configureFor(sfsMock);

        @Cleanup
        InputStream fileInputStream = new FileInputStream("src/test/resources/test-file.pdf");
        byte[] fileBytes = fileInputStream.readAllBytes();

        givenThat(get(SFS_ENDPOINT)
                .willReturn(aResponse()
                        .withStatus(OK_SUCCESS_CODE)
                        .withBody(fileBytes)
                        .withHeader("Content-Type", "application/pdf"))
        );

        WireMock.configureFor(hxInsightMock);
    }

    @SneakyThrows
    public void expectSFSMessageReceived() {
        WireMock.configureFor(sfsMock);

        retryWithBackoff(() -> WireMock.verify(getRequestedFor(urlPathEqualTo(SFS_ENDPOINT))));

        WireMock.configureFor(hxInsightMock);
    }

    @SneakyThrows
    public void prepareHxIToReturnSuccessWithStorageLocation() {
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(HX_INSIGHT_PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_SUCCESS_CODE)
                        .withBody(hxInsightResponse)));
    }

    @SneakyThrows
    public void expectHxIStorageLocationMessageReceived(String expectedBody) {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_PRE_SIGNED_URL_PATH))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    @SneakyThrows
    public void expectFileUploadedToS3(String expectedFilePath) {

        List<String> actualBucketContent = s3StorageMock.listBucketContent(BUCKET_NAME);
        @Cleanup
        InputStream expectedInputStream = new FileInputStream(expectedFilePath);
        InputStream bucketFileInputStream = s3StorageMock.downloadBucketObject(BUCKET_NAME, OBJECT_KEY);

        assertThat(actualBucketContent).contains(OBJECT_KEY);
        assertTrue(IOUtils.contentEquals(expectedInputStream, bucketFileInputStream));
    }

    @SneakyThrows
    public void prepareHxIToReturnSuccessAfterReceivingFileLocation() {
        givenThat(post(HX_INSIGHT_LOCATION_PATH)
                .willReturn(aResponse()
                        .withStatus(OK_SUCCESS_CODE)));
    }

    @SneakyThrows
    public void expectHxIMessageWithFileLocationReceived(String expectedBody) {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_LOCATION_PATH))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

}
