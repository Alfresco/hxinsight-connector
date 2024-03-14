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
package org.alfresco.hxi_connector.live_ingester.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.alfresco.hxi_connector.common.test.util.RetryUtils.retryWithBackoff;
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.BUCKET_NAME;
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.getHxInsightMock;
import static org.alfresco.hxi_connector.live_ingester.util.E2ETestBase.getSfsMock;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.io.IOUtils;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.storage.local.LocalStorageClient;
import org.alfresco.hxi_connector.live_ingester.util.auth.AuthUtils;

@Slf4j
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport
{
    public static final String HX_INSIGHT_INGEST_ENDPOINT = "/ingest";
    private static final int HX_INSIGHT_SUCCESS_CODE = 200;
    public static final String REPO_EVENT_TOPIC = "repo.event.topic";
    public static final String BULK_INGESTER_QUEUE = "bulk.ingester.queue";
    public static final String ATS_QUEUE = "ats.queue";
    public static final String REQUEST_ID_PLACEHOLDER = "_REQUEST_ID_";
    private static ContainerSupport instance;
    public static final String ATS_RESPONSE_QUEUE = "ats.response.queue";
    public static final String SFS_PATH = "/alfresco/api/-default-/private/sfs/versions/1/file/";
    private static final int OK_SUCCESS_CODE = 200;
    private static final String HX_INSIGHT_PRE_SIGNED_URL_PATH = "/pre-signed-url";
    private static final String HX_INSIGHT_RESPONSE_BODY_PATTERN = "{\"%s\": \"%s\"}";
    static final String STORAGE_LOCATION_PROPERTY = "preSignedUrl";
    private static final String OBJECT_KEY = "dummy-file.pdf";
    private static final String OBJECT_CONTENT_TYPE = "application/pdf";
    private final Session session;
    private final MessageProducer repoEventProducer;
    private final MessageProducer bulkIngesterEventProducer;
    private final MessageConsumer atsConsumer;
    private final MessageProducer atsEventProducer;
    private final LocalStorageClient localStorageClient;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMock hxInsightMock, String brokerUrl, LocalStorageClient localStorageClient)
    {
        WireMock.configureFor(hxInsightMock);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic repoTopic = session.createTopic(REPO_EVENT_TOPIC);
        repoEventProducer = session.createProducer(repoTopic);
        Queue bulkIngesterQueue = session.createQueue(BULK_INGESTER_QUEUE);
        bulkIngesterEventProducer = session.createProducer(bulkIngesterQueue);
        Queue atsQueue = session.createQueue(ATS_QUEUE);
        atsConsumer = session.createConsumer(atsQueue);

        Queue atsResponseQueue = session.createQueue(ATS_RESPONSE_QUEUE);
        atsEventProducer = session.createProducer(atsResponseQueue);

        this.localStorageClient = localStorageClient;
    }

    public static ContainerSupport getInstance(WireMock hxInsightMock, String brokerUrl, LocalStorageClient localStorageClient)
    {
        if (instance == null)
        {
            instance = new ContainerSupport(hxInsightMock, brokerUrl, localStorageClient);
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
                        .withStatus(OK_SUCCESS_CODE)));
    }

    @SneakyThrows
    public void raiseRepoEvent(String repoEvent)
    {
        repoEventProducer.send(session.createTextMessage(repoEvent));
    }

    @SneakyThrows
    public void raiseBulkIngesterEvent(String bulkIngesterEvent)
    {
        bulkIngesterEventProducer.send(session.createTextMessage(bulkIngesterEvent));
    }

    @SneakyThrows
    public void expectHxIngestMessageReceived(String expectedBody)
    {
        retryWithBackoff(() -> getHxInsightMock().verifyThat(postRequestedFor(urlPathEqualTo(HX_INSIGHT_INGEST_ENDPOINT))
                .withHeader(AUTHORIZATION, equalTo(AuthUtils.createAuthorizationHeader()))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
        resetWireMock();
    }

    @SneakyThrows
    public void expectNoHxIngestMessagesReceived()
    {
        TimeUnit.MILLISECONDS.sleep(200);
        getHxInsightMock().verifyThat(exactly(0), postRequestedFor(urlPathEqualTo(HX_INSIGHT_INGEST_ENDPOINT)));
        resetWireMock();
    }

    private static void resetWireMock()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        getHxInsightMock().resetRequests();
        getHxInsightMock().resetMappings();
        getSfsMock().resetRequests();
        getSfsMock().resetMappings();
    }

    @SneakyThrows
    public void verifyATSRequestReceived(String expectedBody)
    {
        TextMessage received = retryWithBackoff(() -> {
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
    public void raiseTransformationCompletedATSEvent(String atsEvent)
    {
        atsEventProducer.send(session.createTextMessage(atsEvent));
    }

    @SneakyThrows
    public void prepareSFSToReturnFile(String targetReference, String expectedFile)
    {
        WireMock.configureFor(getSfsMock());

        byte[] fileBytes = Files.readAllBytes(Paths.get("src/integration-test/resources/" + expectedFile));
        givenThat(get(SFS_PATH + targetReference)
                .willReturn(aResponse()
                        .withStatus(OK_SUCCESS_CODE)
                        .withBody(fileBytes)
                        .withHeader("Content-Type", "application/pdf")));

        WireMock.configureFor(getHxInsightMock());
    }

    @SneakyThrows
    public void expectSFSMessageReceived(String targetReference)
    {
        WireMock.configureFor(getSfsMock());

        retryWithBackoff(() -> getSfsMock().verifyThat(exactly(1), getRequestedFor(urlPathEqualTo(SFS_PATH + targetReference))));

        WireMock.configureFor(getHxInsightMock());
    }

    @SneakyThrows
    public URL prepareHxIToReturnStorageLocation()
    {
        URL preSignedUrl = localStorageClient.generatePreSignedUploadUrl(BUCKET_NAME, OBJECT_KEY, OBJECT_CONTENT_TYPE);
        String hxInsightResponse = HX_INSIGHT_RESPONSE_BODY_PATTERN.formatted(STORAGE_LOCATION_PROPERTY, preSignedUrl);
        givenThat(post(HX_INSIGHT_PRE_SIGNED_URL_PATH)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_SUCCESS_CODE)
                        .withBody(hxInsightResponse)));
        return preSignedUrl;
    }

    @SneakyThrows
    public void expectHxIStorageLocationMessageReceived(String expectedBody)
    {
        retryWithBackoff(() -> WireMock.verify(postRequestedFor(urlPathEqualTo(HX_INSIGHT_PRE_SIGNED_URL_PATH))
                .withHeader(AUTHORIZATION, equalTo(AuthUtils.createAuthorizationHeader()))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    @SneakyThrows
    public void expectFileUploadedToS3(String expectedFile)
    {
        List<String> actualBucketContent = localStorageClient.listBucketContent(BUCKET_NAME);
        @Cleanup
        InputStream expectedInputStream = Files.newInputStream(Paths.get("src/integration-test/resources/" + expectedFile));
        @Cleanup
        InputStream bucketFileInputStream = localStorageClient.downloadBucketObject(BUCKET_NAME, OBJECT_KEY);

        assertThat(actualBucketContent).contains(OBJECT_KEY);
        assertTrue(IOUtils.contentEquals(expectedInputStream, bucketFileInputStream));
    }
}
