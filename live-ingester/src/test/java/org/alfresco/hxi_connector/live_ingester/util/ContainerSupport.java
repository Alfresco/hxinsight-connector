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
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.BUCKET_NAME;
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.hxInsight;
import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.live_ingester.util.RetryUtils.retryWithBackoff;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.apache.commons.lang3.ClassLoaderUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.util.ResourceUtils;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@Slf4j
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport
{
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
    private static final String NODE_ID = "some-node-ref";
    private static final String HX_INSIGHT_PRE_SIGNED_URL_PATH = "/pre-signed-url";
    private static final String HX_INSIGHT_TEST_USERNAME = "mock";
    private static final String HX_INSIGHT_TEST_PASSWORD = "pass";
    private static final String CAMEL_ENDPOINT_PATTERN = "%s%s?httpMethod=POST&authMethod=Basic&authUsername=%s&authPassword=%s&authenticationPreemptive=true&throwExceptionOnFailure=false";
    private static final String FILE_CONTENT_TYPE = "plain/text";
    private static final String HX_INSIGHT_RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    private static final int HX_INSIGHT_RESPONSE_CODE = 201;
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    private static final String OBJECT_KEY = "dummy-file.pdf";
    private static final String OBJECT_CONTENT = "Dummy's file dummy content";
    private static final String OBJECT_CONTENT_TYPE = "application/pdf";

    LocalStorageClient s3StorageMock;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMockContainer hxInsight, String brokerUrl, LocalStorageClient s3StorageMock)
    {
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

    public static ContainerSupport getInstance(WireMockContainer hxInsight, String brokerUrl, LocalStorageClient s3StorageMock)
    {
        if (instance == null)
        {
            instance = new ContainerSupport(hxInsight, brokerUrl, s3StorageMock);
        }
        return instance;
    }

    public static void removeInstance()
    {
        instance = null;
    }

    public void prepareHxInsightToReturnSuccess()
    {
        givenThat(post(HX_INSIGHT_INGEST_ENDPOINT)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_SUCCESS_CODE)));
    }

    @SneakyThrows
    public void raiseRepoEvent(String repoEvent)
    {
        repoEventProducer.send(session.createTextMessage(repoEvent));
    }

    @SneakyThrows
    public void expectHxIngestMessageReceived(String expectedBody)
    {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_INGEST_ENDPOINT))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    @SneakyThrows
    public void verifyATSRequestReceived(String expectedBody)
    {
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
    public void clearATSQueue()
    {
        while (receiveATSTextMessage() != null)
        {
            log.debug("Removed message from ATS queue");
        }
    }

    @SneakyThrows
    TextMessage receiveATSTextMessage()
    {
        return (TextMessage) atsConsumer.receiveNoWait();
    }

    @SneakyThrows
    public void raiseATSEvent(String atsEvent)
    {
        atsEventProducer.send(session.createTextMessage(atsEvent));
    }

    @SneakyThrows
    public void prepareSFSToReturnSuccess()
    {
        //File testFile = new File(ContainerSupport.class.getClassLoader().getResource("test-file.pdf").getFile());
//        File testFile = new File(getClass().getClassLoader().getResource("src/test/resources/test-file.pdf").getFile());

        File testFile = new File("src/test/resources/test-file.pdf");

        @Cleanup
        var stream = new FileInputStream(testFile);
        byte[] bytes = stream.readAllBytes();

        givenThat(get(SFS_ENDPOINT)
                .willReturn(aResponse()
                        .withStatus(OK_SUCCESS_CODE)
                        .withBody(bytes)
                        .withHeader("Content-Type", "application/pdf"))
        );
    }

    @SneakyThrows
    public void expectSFSMessageReceived()
    {
        retryWithBackoff(() -> WireMock.verify(getRequestedFor(urlPathEqualTo(SFS_ENDPOINT))));
    }

    @SneakyThrows
    public void prepareHxInsightToReturnSuccessWithStorageLocation()
    {
        //        String preSignedUrl = "http://s3-storage-location";
        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(HX_INSIGHT_PRE_SIGNED_URL_PATH)
                //                .withBasicAuth(HX_INSIGHT_TEST_USERNAME, HX_INSIGHT_TEST_PASSWORD)
                //                .withRequestBody(new ContainsPattern(NODE_ID))
                //                .withRequestBody(new ContainsPattern(FILE_CONTENT_TYPE))
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody(hxInsightResponse)));
    }

    @SneakyThrows
    public void expectHxiPreSignedUrlMessageReceived(String expectedBody)
    {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_PRE_SIGNED_URL_PATH))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    @SneakyThrows
    public void prepareS3ToReturnSuccess()
    {
        //        URL preSignedUrl = s3StorageMock.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        //        givenThat(post(LOCAL_ENDPOINT)
        //                .willReturn(aResponse()
        //                        .withStatus(OK_SUCCESS_CODE)));

        File testFile = new File(ContainerSupport.class.getClassLoader().getResource("test-file.pdf").getFile());

    }

    @SneakyThrows
    public void expectS3MessageReceived(String expectedFile)
    {
        ////        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(LOCAL_ENDPOINT))
        //                .withHeader("Content-Type", equalTo("application/json"))
        //                .withRequestBody(equalToJson(expectedBody))));

        //        InputStream expectedInputStream = ContainerSupport.class.getClassLoader().getResourceAsStream("test-file.pdf");
        //        InputStream bucketFileInputStream = s3StorageMock.listBucketContent(BUCKET_NAME).contains(OBJECT_KEY);
        //        List<String> actualBucketContent = s3StorageMock.listBucketContent(BUCKET_NAME);
        //        assertThat(actualBucketContent).contains(OBJECT_KEY);
        ////        assertThat(actualBucketContent).cm
        //
        //        assertTrue(IOUtils.contentEquals(expectedInputStream, inputStream2));
    }

}
