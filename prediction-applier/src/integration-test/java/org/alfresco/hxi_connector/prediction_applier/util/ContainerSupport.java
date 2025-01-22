/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.apache.camel.Exchange.CONTENT_TYPE;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.HXP_APP_HEADER;
import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.util.RetryUtils.retryWithBackoff;
import static org.alfresco.hxi_connector.prediction_applier.config.AuthConfig.HXAI_ENVIRONMENT_HEADER;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import org.alfresco.hxi_connector.common.test.docker.util.DockerTags;

@Slf4j
@Getter
@SuppressWarnings("PMD.NonThreadSafeSingleton")
public class ContainerSupport
{
    public static final String HXI_PREDICTION_BATCHES_ENDPOINT = "/prediction-batches";
    public static final String REPOSITORY_PREDICTION_ENDPOINT = "/api/-default-/private/hxi/versions/1/nodes/%s/predictions";
    public static final String USER_AGENT_REGEX = "ACS HXI Connector/" + DockerTags.getHxiConnectorTag() + " ACS/" +
            (DockerTags.getRepositoryTag().contains("-") ? DockerTags.getRepositoryTag().split("-")[0] : DockerTags.getRepositoryTag()) + " .*";
    public static final String DISCOVERY_ENDPOINT = "/api/discovery";
    private static ContainerSupport instance;
    private final Session session;
    private final WireMock hxInsightMock;
    private final WireMock repositoryMock;
    private final String repositoryBaseUrl;

    @SneakyThrows
    @SuppressWarnings("PMD.CloseResource")
    private ContainerSupport(WireMock hxInsightMock, String brokerUrl, WireMock repositoryMock, String repositoryBaseUrl)
    {
        configureFor(hxInsightMock);
        this.hxInsightMock = hxInsightMock;
        configureFor(repositoryMock);
        this.repositoryMock = repositoryMock;
        this.repositoryBaseUrl = repositoryBaseUrl;

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public static ContainerSupport getInstance(WireMock hxInsightMock, String brokerUrl, WireMock repositoryMock, String repositoryBaseUrl)
    {
        if (instance == null)
        {
            instance = new ContainerSupport(hxInsightMock, brokerUrl, repositoryMock, repositoryBaseUrl);
        }
        return instance;
    }

    public static void removeInstance()
    {
        instance = null;
    }

    public void prepareHxInsightToReturnPredictionBatch(String batchId, String nodeId, String predictedValue)
    {
        configureFor(hxInsightMock);

        givenThat(get(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(HTTP_NO_CONTENT)));

        givenThat(get(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
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

        givenThat(get(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT + "/" + batchId))
                .willReturn(aResponse()
                        .withStatus(HTTP_NO_CONTENT)));

        givenThat(get(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT + "/" + batchId))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withBody("""
                                [
                                    {
                                        "objectId": "%s",
                                        "modelId": "97f33039-2d09-4206-94ab-8b50f2cd2569",
                                        "prediction": [
                                        {
                                            "field": "cm:description",
                                            "confidence": 0.98,
                                            "value": "%s"
                                        }
                                        ],
                                        "enrichmentType": "AUTOCORRECT"
                                    }
                                ]
                                """.formatted(nodeId, predictedValue))));

        givenThat(put(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT + "/" + batchId))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)));

        configureFor(repositoryMock);

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

        givenThat(post(urlPathEqualTo(REPOSITORY_PREDICTION_ENDPOINT.formatted(nodeId)))
                .willReturn(aResponse()
                        .withStatus(HTTP_CREATED)
                        .withBody(responseBody)));
    }

    public void expectRepositoryRequestReceived(String nodeId, String predictedValue)
    {
        String expectedBody = """
                {
                    "property": "cm:description",
                    "confidenceLevel": 0.98,
                    "modelId": "97f33039-2d09-4206-94ab-8b50f2cd2569",
                    "predictionValue": "%s",
                    "updateType": "AUTOCORRECT"
                }
                """.formatted(predictedValue);

        String repositoryUrl = REPOSITORY_PREDICTION_ENDPOINT.formatted(nodeId);

        retryWithBackoff(() -> getRepositoryMock().verifyThat(postRequestedFor(urlPathEqualTo(repositoryUrl))
                .withHeader(CONTENT_TYPE, equalTo("application/json"))
                .withRequestBody(equalToJson(expectedBody))));
    }

    public void expectBatchStatusWasUpdated(String batchId, String status, int page)
    {
        String expectedBody = """
                {
                    "status": "%s",
                    "currentPage": %d
                }
                """.formatted(status, page);

        retryWithBackoff(() -> getHxInsightMock().verifyThat(putRequestedFor(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT + "/" + batchId))
                .withRequestBody(equalToJson(expectedBody))
                .withHeader(USER_AGENT, matching(USER_AGENT_REGEX))));
    }

    public void expectGetBatchesCalled()
    {
        retryWithBackoff(() -> getHxInsightMock().verifyThat(getRequestedFor(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT))
                .withHeader(USER_AGENT, matching(USER_AGENT_REGEX))
                .withHeader(HXAI_ENVIRONMENT_HEADER, equalTo("hxi-env-key"))
                .withHeader(HXP_APP_HEADER, equalTo("hxai-discovery"))));
    }

    public void prepareRepositoryToReturnDiscovery()
    {
        List<String> versionDetails = extractVersionDetails(DockerTags.getRepositoryTag());

        WireMock.configureFor(getRepositoryMock());
        givenThat(get(urlPathEqualTo(DISCOVERY_ENDPOINT))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withBody("""
                                {
                                    "entry": {
                                        "repository": {
                                            "id": "dummy-repository-id",
                                            "version": {
                                                "major": "%s",
                                                "minor": "%s",
                                                "patch": "%s",
                                                "hotfix": "0",
                                                "schema": 19200
                                            }
                                        }
                                    }
                                }
                                """.formatted(versionDetails.get(0), versionDetails.get(1), versionDetails.get(2)))));
    }

    public static List<String> extractVersionDetails(String version)
    {
        String regex = "(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z]+)\\.?(\\d+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(version);

        if (matcher.find())
        {
            String major = matcher.group(1);
            String minor = matcher.group(2);
            String hotfix = matcher.group(3);

            return List.of(major, minor, hotfix);
        }
        else
        {
            throw new IllegalStateException("No match found for version details in input: " + version);
        }
    }

    public void prepareRepositoryToFailAtDiscovery()
    {
        WireMock.configureFor(getRepositoryMock());
        givenThat(get(urlPathEqualTo(DISCOVERY_ENDPOINT)).willReturn(serviceUnavailable()));
    }

    public void expectDiscoveryEndpointCalled()
    {
        retryWithBackoff(() -> getRepositoryMock().verifyThat(getRequestedFor(urlPathEqualTo(DISCOVERY_ENDPOINT))));
    }

    @SneakyThrows
    public void expectNoPredictionBatchesRequestsReceived()
    {
        TimeUnit.MILLISECONDS.sleep(200);
        hxInsightMock.verifyThat(exactly(0), postRequestedFor(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT)));
        hxInsightMock.verifyThat(exactly(0), getRequestedFor(urlPathEqualTo(HXI_PREDICTION_BATCHES_ENDPOINT)));
        resetWireMock();
    }

    public void resetWireMock()
    {
        WireMock.reset();
        WireMock.resetAllRequests();
        hxInsightMock.resetRequests();
        hxInsightMock.resetMappings();
        repositoryMock.resetRequests();
        repositoryMock.resetMappings();
    }
}
