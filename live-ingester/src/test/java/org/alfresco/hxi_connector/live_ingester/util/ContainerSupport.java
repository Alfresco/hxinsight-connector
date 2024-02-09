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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.alfresco.hxi_connector.live_ingester.util.RetryUtils.retryWithBackoff;

import java.util.HashMap;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.wiremock.integrations.testcontainers.WireMockContainer;

@Slf4j
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport
{
    public static final String HX_INSIGHT_INGEST_ENDPOINT = "/ingest";
    private static final int HX_INSIGHT_SUCCESS_CODE = 201;
    public static final String REPO_EVENT_TOPIC = "repo.event.topic";
    public static final String BULK_INGESTER_QUEUE = "bulk.ingester.queue";
    public static final String ATS_QUEUE = "ats.queue";
    public static final String REQUEST_ID_PLACEHOLDER = "_REQUEST_ID_";

    private static ContainerSupport instance;

    private Session session;
    private MessageProducer repoEventProducer;
    private MessageProducer bulkIngesterEventProducer;
    private MessageConsumer atsConsumer;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMockContainer hxInsight, String brokerUrl)
    {
        WireMock.configureFor(hxInsight.getHost(), hxInsight.getPort());

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
    }

    public static ContainerSupport getInstance(WireMockContainer hxInsight, String brokerUrl)
    {
        if (instance == null)
        {
            instance = new ContainerSupport(hxInsight, brokerUrl);
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
    public void raiseBulkIngesterEvent(String bulkIngesterEvent)
    {
        bulkIngesterEventProducer.send(session.createTextMessage(bulkIngesterEvent));
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
}
