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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.endpoint;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.live_ingester.adapters.storage.endpoint.PreSignedUrlRequester.STORAGE_LOCATION_PROPERTY;

import java.net.URL;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.StorageLocationRequest;
import org.alfresco.hxi_connector.live_ingester.adapters.storage.StorageLocationRequester;
import org.alfresco.hxi_connector.live_ingester.util.DockerTags;

@SpringBootTest(classes = {
        CamelAutoConfiguration.class,
        PreSignedUrlRequester.class})
@ActiveProfiles({"test"})
@Testcontainers
class PreSignedUrlRequesterIntegrationTest
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getOrDefault("wiremock.tag", "3.3.1");

    private static final String HX_INSIGHT_PRE_SIGNED_URL_PATH = "/pre-signed-url";
    private static final String HX_INSIGHT_TEST_USERNAME = "mock";
    private static final String HX_INSIGHT_TEST_PASSWORD = "pass";
    private static final String CAMEL_ENDPOINT_PATTERN = "%s%s?httpMethod=POST&authMethod=Basic&authUsername=%s&authPassword=%s&authenticationPreemptive=true&throwExceptionOnFailure=false";
    private static final String FILE_CONTENT_TYPE = "plain/text";
    private static final String PRE_SIGNED_URL = "http://s3-storage-location";
    private static final String HX_INSIGHT_RESPONSE_BODY = String.format("{\"%s\": \"%s\"}", STORAGE_LOCATION_PROPERTY, PRE_SIGNED_URL);
    private static final int HX_INSIGHT_RESPONSE_CODE = 201;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG));

    @Autowired
    StorageLocationRequester locationRequester;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
    }

    @Test
    void testRequestStorageLocation()
    {
        // given
        String nodeId = "some-node-ref";
        givenThat(post(HX_INSIGHT_PRE_SIGNED_URL_PATH)
                .withBasicAuth(HX_INSIGHT_TEST_USERNAME, HX_INSIGHT_TEST_PASSWORD)
                .willReturn(aResponse()
                        .withStatus(HX_INSIGHT_RESPONSE_CODE)
                        .withBody(HX_INSIGHT_RESPONSE_BODY)));

        // when
        URL actualUrl = locationRequester.requestStorageLocation(new StorageLocationRequest(nodeId, FILE_CONTENT_TYPE));

        // then
        assertThat(actualUrl).asString().isEqualTo(PRE_SIGNED_URL);
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("alfresco.integration.storage.endpoint", PreSignedUrlRequesterIntegrationTest::createEndpointUrl);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static String createEndpointUrl()
    {
        return String.format(
                CAMEL_ENDPOINT_PATTERN,
                wireMockServer.getBaseUrl(), HX_INSIGHT_PRE_SIGNED_URL_PATH, HX_INSIGHT_TEST_USERNAME, HX_INSIGHT_TEST_PASSWORD);
    }
}
