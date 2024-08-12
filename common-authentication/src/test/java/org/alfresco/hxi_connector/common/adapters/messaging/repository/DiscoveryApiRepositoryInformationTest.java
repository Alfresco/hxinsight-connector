/*-
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
package org.alfresco.hxi_connector.common.adapters.messaging.repository;

import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthService.ALFRESCO_AUTH_PROVIDER;
import static org.alfresco.hxi_connector.common.constant.HttpHeaders.AUTHORIZATION;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@ExtendWith(MockitoExtension.class)
class DiscoveryApiRepositoryInformationTest
{

    private static final String DISCOVERY_ENDPOINT = "http://localhost:8080/alfresco/api/discovery";
    private static final String BEARER_TOKEN = "Bearer token";

    @Mock
    private AuthService authServiceMock;
    @Mock
    private HttpClient httpClientMock;
    @Mock
    private HttpResponse httpResponseMock;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void givenVersionOverrideIsEmptyAndApiReturnsVersion_whenGetRepositoryVersion_thenReturnApiVersion()
            throws IOException, InterruptedException
    {
        // given
        given(authServiceMock.getAuthHeader(ALFRESCO_AUTH_PROVIDER)).willReturn(Map.entry(AUTHORIZATION, BEARER_TOKEN));
        given(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willReturn(httpResponseMock);
        given(httpResponseMock.statusCode()).willReturn(SC_OK);
        given(httpResponseMock.body()).willReturn("""
                {
                    "entry": {
                        "repository": {
                            "id": "eb4c123a-5621-4583-98a2-739379e3a345",
                            "edition": "Enterprise",
                            "version": {
                                "major": "23",
                                "minor": "2",
                                "patch": "2",
                                "hotfix": "0",
                                "schema": 19200,
                                "label": "rc50a6313-blocal",
                                "display": "23.2.2.0 (rc50a6313-blocal) schema 19200"
                            }
                        }
                    }
                }
                """);

        DiscoveryApiRepositoryInformation objectUnderTest = new DiscoveryApiRepositoryInformation(DISCOVERY_ENDPOINT, authServiceMock, objectMapper, httpClientMock);

        // when
        String actualVersion = objectUnderTest.getRepositoryVersion();

        // then
        String expectedApiVersion = "23.2.2";
        assertEquals(expectedApiVersion, actualVersion);
    }

    @Test
    void givenDiscoverApiFails_whenGetRepositoryVersion_thenThrowException()
            throws IOException, InterruptedException
    {
        // given
        given(authServiceMock.getAuthHeader(ALFRESCO_AUTH_PROVIDER)).willReturn(Map.entry(AUTHORIZATION, BEARER_TOKEN));
        given(httpClientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willThrow(new IOException());
        DiscoveryApiRepositoryInformation objectUnderTest = new DiscoveryApiRepositoryInformation(DISCOVERY_ENDPOINT, authServiceMock, objectMapper, httpClientMock);

        // when + then
        assertThrows(EndpointServerErrorException.class, objectUnderTest::getRepositoryVersion);
    }
}
