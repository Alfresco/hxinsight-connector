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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.ATSTransformRequester.TIMEOUT_KEY;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.model.ATSTransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;

@ExtendWith(MockitoExtension.class)
public class ATSTransformRequesterTest
{
    private static final String NODE_REF = "nodeRef";
    private static final String TARGET_MIME_TYPE = "targetMimeType";
    private static final int TIMEOUT = 123;
    private static final int ATTEMPT = 2;
    private static final String QUEUE_NAME = "queueName";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();

    @Mock
    IntegrationProperties integrationProperties;
    @InjectMocks
    ATSTransformRequester atsTransformRequester;

    @Test
    public void testToTransformRequest_missingConfigurationGivesNoError()
    {
        // given
        setUpIntegrationProperties(null);
        TransformRequest transformRequest = new TransformRequest(NODE_REF, TARGET_MIME_TYPE, TIMESTAMP);

        // when
        ATSTransformRequest atsTransformRequest = atsTransformRequester.toTransformRequest(transformRequest, ATTEMPT);

        // then
        Map<String, String> expectedTransformOptions = Map.of(TIMEOUT_KEY, String.valueOf(TIMEOUT));
        ATSTransformRequest expected = createExpectedATSRequest(expectedTransformOptions);
        assertThat(atsTransformRequest).usingRecursiveComparison().ignoringFields("requestId").isEqualTo(expected);
    }

    @Test
    public void testToTransformRequest_noOptionsForMimeTypeGivesNoError()
    {
        // given
        Map<String, Map<String, String>> transformOptions = Map.of("otherMimeType", Map.of("otherOption", "otherValue"));
        setUpIntegrationProperties(transformOptions);
        TransformRequest transformRequest = new TransformRequest(NODE_REF, TARGET_MIME_TYPE, TIMESTAMP);

        // when
        ATSTransformRequest atsTransformRequest = atsTransformRequester.toTransformRequest(transformRequest, ATTEMPT);

        // then
        Map<String, String> expectedTransformOptions = Map.of(TIMEOUT_KEY, String.valueOf(TIMEOUT));
        ATSTransformRequest expected = createExpectedATSRequest(expectedTransformOptions);
        assertThat(atsTransformRequest).usingRecursiveComparison().ignoringFields("requestId").isEqualTo(expected);
    }

    @Test
    public void testToTransformRequest_withOptionsForMimeType()
    {
        // given
        Map<String, String> mimeTypeOptions = Map.of("option1", "value1", "option2", "value2");
        Map<String, Map<String, String>> transformOptions = Map.of(TARGET_MIME_TYPE, mimeTypeOptions);
        setUpIntegrationProperties(transformOptions);
        TransformRequest transformRequest = new TransformRequest(NODE_REF, TARGET_MIME_TYPE, TIMESTAMP);

        // when
        ATSTransformRequest atsTransformRequest = atsTransformRequester.toTransformRequest(transformRequest, ATTEMPT);

        // then
        Map<String, String> expectedTransformOptions = Map.of(
                TIMEOUT_KEY, String.valueOf(TIMEOUT),
                "option1", "value1",
                "option2", "value2");
        ATSTransformRequest expected = createExpectedATSRequest(expectedTransformOptions);
        assertThat(atsTransformRequest).usingRecursiveComparison().ignoringFields("requestId").isEqualTo(expected);
    }

    void setUpIntegrationProperties(Map<String, Map<String, String>> transformOptions)
    {
        IntegrationProperties.Alfresco alfrescoProperties = mock(IntegrationProperties.Alfresco.class);
        given(integrationProperties.alfresco()).willReturn(alfrescoProperties);
        Transform transformProperties = mock(Transform.class);
        given(alfrescoProperties.transform()).willReturn(transformProperties);

        Transform.Response responseProperties = mock(Transform.Response.class);
        given(transformProperties.response()).willReturn(responseProperties);
        given(responseProperties.queueName()).willReturn(QUEUE_NAME);

        Transform.Request requestProperties = mock(Transform.Request.class);
        given(transformProperties.request()).willReturn(requestProperties);
        given(requestProperties.timeout()).willReturn(TIMEOUT);
        given(requestProperties.options()).willReturn(transformOptions);
    }

    ATSTransformRequest createExpectedATSRequest(Map<String, String> expectedTransformOptions)
    {
        return new ATSTransformRequest(
                NODE_REF,
                TARGET_MIME_TYPE,
                new ClientData(NODE_REF, TARGET_MIME_TYPE, ATTEMPT, TIMESTAMP),
                expectedTransformOptions,
                QUEUE_NAME);
    }
}
