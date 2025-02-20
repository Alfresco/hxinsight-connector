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
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

public class CreateRequestIntegrationTest extends E2ETestBase
{
    @Test
    void testCreateRequest()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2021-01-21T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2021-01-26T10:29:42.529Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:title": "Purchase Order",
                        "cm:versionType": "MAJOR",
                        "cm:versionLabel": "1.0"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE" ],
                    "resourceDeniedAuthorities": []
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/create-or-update-document.yml");
        containerSupport.expectHxIngestMessageReceived(request.body());

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-82c7-477c-8490-04cb0e826e65",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e65\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1611656982995}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
}
