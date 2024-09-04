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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

@ExtendWith(MockitoExtension.class)
class AcsHealthProbeTest
{

    private static final String ACS_HEALTH_ENDPOINT = "http://localhost:8080/alfresco";
    private static final int RETRY_TIMEOUT_SECONDS = 3;
    private static final int RETRY_INTERVAL_SECONDS = 1;

    @Mock
    private HttpClient clientMock;
    @Mock
    private HttpResponse<String> responseMock;

    private AcsHealthProbe objectUnderTest;

    @BeforeEach
    void setUp()
    {
        objectUnderTest = new AcsHealthProbe(clientMock, ACS_HEALTH_ENDPOINT, RETRY_TIMEOUT_SECONDS, RETRY_INTERVAL_SECONDS, true);
    }

    @Test
    void givenAcsIsAlive_whenProbeSent_thenReturnSuccessfully() throws Exception
    {
        given(clientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willReturn(responseMock);
        given(responseMock.statusCode()).willReturn(200);

        // when
        objectUnderTest.checkAcsAlive();

        // then
        then(clientMock).should(times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void givenAcsCallThrowsException_whenProbeSent_thenThrowEndpointServerErrorException() throws Exception
    {
        given(clientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willThrow(new IOException());

        // when + then
        assertThrows(EndpointServerErrorException.class, objectUnderTest::checkAcsAlive);
        then(clientMock).should(times(RETRY_TIMEOUT_SECONDS / RETRY_INTERVAL_SECONDS)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void givenAcsIsNotAvailableWithinTimeout_whenProbeSent_throwEndpointServerErrorException() throws Exception
    {
        given(clientMock.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).willReturn(responseMock);
        given(responseMock.statusCode()).willReturn(500);

        // when + then
        assertThrows(EndpointServerErrorException.class, objectUnderTest::checkAcsAlive);
        then(clientMock).should(times(RETRY_TIMEOUT_SECONDS / RETRY_INTERVAL_SECONDS)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}
