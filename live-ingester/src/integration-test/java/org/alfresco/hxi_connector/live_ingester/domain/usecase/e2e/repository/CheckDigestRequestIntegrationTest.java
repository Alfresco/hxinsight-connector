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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;

public class CheckDigestRequestIntegrationTest extends E2ETestBase
{
    @Test
    void testCheckDigestRequest()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String sourceId = "alfresco-dummy-source-id-0a63de491876";
        String objectId = "368818d9-dddd-4b8b-8eab-e050253d7f61";
        String digest = "fake-digest-identifier";
        String url = String.format("/v1/check-digest/%s/%s/%s", sourceId, objectId, digest);

        HxInsightRequest request = new HxInsightRequest(url, null, null);

        String repoEvent = """
                {
                    "sourceId": "%s",
                    "objectId": "%s",
                    "digest": "%s"
                }
                """.formatted(sourceId, objectId, digest);
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectHxIngestMessageReceived(request.body());

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-82c7-477c-8490-04cb0e826e65",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e65\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1611227656423}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
}
