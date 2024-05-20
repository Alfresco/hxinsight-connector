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
package org.alfresco.hxi_connector.prediction_applier.util;

import static java.net.HttpURLConnection.HTTP_CREATED;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.UUID;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

@Slf4j
@Getter
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport
{
    private static final int HTTP_OK_STATUS = 200;
    private static final int HTTP_ACCEPTED_STATUS = 202;
    public static final String HX_INSIGHT_PREDICTION_BATCHES_ENDPOINT = "/prediction-batches";
    public static final String REPOSITORY_PREDICTION_ENDPOINT_TEMPLATE = "/alfresco/api/-default-/private/hxi/versions/1/nodes/%s/predictions";
    private static ContainerSupport instance;
    private final Session session;
    private final WireMock hxInsightMock;
    private final WireMock repositoryMock;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMock hxInsightMock, String brokerUrl, WireMock repositoryMock)
    {
        configureFor(hxInsightMock);
        this.hxInsightMock = hxInsightMock;
        configureFor(repositoryMock);
        this.repositoryMock = repositoryMock;

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public static ContainerSupport getInstance(WireMock hxInsightMock, String brokerUrl, WireMock repositoryMock)
    {
        if (instance == null)
        {
            instance = new ContainerSupport(hxInsightMock, brokerUrl, repositoryMock);
        }
        return instance;
    }

    public static void removeInstance()
    {
        instance = null;
    }

    public void prepareHxInsightToReturnPredictionBatch(String nodeId, String predictedValue)
    {
        String batchId = UUID.randomUUID().toString();

        configureFor(hxInsightMock);
        givenThat(get(urlPathEqualTo(HX_INSIGHT_PREDICTION_BATCHES_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK_STATUS)
                        .withBody("""
                                [
                                    {
                                        "_id": "%s",
                                        "modelId": "a992cd6c-f2f6-415f-ba38-235b703b3cf5",
                                        "fieldConfigurationId": "663b8b3821092e4a9593567a",
                                        "field": "cm:description",
                                        "enrichmentType": "AUTOCORRECT",
                                        "threshold": 0.6,
                                        "dateCreated": "2024-05-09T07:31:29.954Z",
                                        "isSuperseded": false,
                                        "status": "APPROVED",
                                        "currentPage": 0,
                                        "primaryGrouping": {
                                            "key": "type",
                                            "value": "butterfly"
                                        }
                                    }
                                ]
                                """.formatted(batchId))));
        givenThat(get(urlPathEqualTo(HX_INSIGHT_PREDICTION_BATCHES_ENDPOINT + "/" + batchId))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK_STATUS)
                        .withBody("""
                                [
                                   {
                                     "objectId": "%s",
                                     "modelId": "97f33039-2d09-4206-94ab-8b50f2cd2569",
                                     "prediction": [
                                       {
                                         "field": "cm:description",
                                         "confidence": 0.98,
                                         "value": "%s",
                                       }
                                     ],
                                     "enrichmentType": "AUTOCORRECT"
                                   }
                                ]
                                """.formatted(nodeId, predictedValue))));
    }

    public void expectRepositoryRequestReceived(String nodeId, String predictedValue)
    {
        String expectedBody = """
                {
                    "property": "cm:description",
                    "predictionDateTime": "2024-04-12T10:31:12.477+0000",
                    "confidenceLevel": 0.98,
                    "modelId": "97f33039-2d09-4206-94ab-8b50f2cd2569",
                    "predictionValue": "%s",
                    "updateType": "AUTOCORRECT"
                }
                """.formatted(predictedValue);
        String responseBody = """
                {
                    "entry": {
                        "id": "prediction-id",
                        "property": "cm:description",
                        "predictionDateTime": "2024-04-12T10:31:12.477+0000",
                        "confidenceLevel": 0.98,
                        "modelId": "97f33039-2d09-4206-94ab-8b50f2cd2569",
                        "predictionValue": "%s",
                        "previousValue": "Old value",
                        "updateType": "AUTOCORRECT"
                    }
                }
                """.formatted(predictedValue);
        ;

        configureFor(repositoryMock);
        String repositoryUrl = REPOSITORY_PREDICTION_ENDPOINT_TEMPLATE.formatted(nodeId);
        givenThat(post(urlPathEqualTo(repositoryUrl))
                .willReturn(aResponse()
                        .withStatus(HTTP_CREATED)
                        .withBody(responseBody)));

        // retryWithBackoff(() -> getRepositoryMock().verifyThat(postRequestedFor(urlPathEqualTo(repositoryUrl))
        // .withHeader(CONTENT_TYPE, equalTo("application/json"))
        // .withRequestBody(equalToJson(expectedBody))));
    }

    private void resetWireMock()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        hxInsightMock.resetRequests();
        hxInsightMock.resetMappings();
    }
}
