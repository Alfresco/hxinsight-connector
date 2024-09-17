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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.bulk_ingester;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SpringBootTest(properties = {"alfresco.transform.mime-type.mapping.[text/*]=application/pdf",
        "logging.level.org.alfresco=DEBUG"})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class BulkIngesterEventIntegrationTest extends E2ETestBase
{
    @Test
    void shouldIngestOnlyMetadataIfThereIsNoContent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String bulkIngesterEvent = """
                {
                  "nodeId": "5018ff83-ec45-4a11-95c4-681761752aa7",
                  "contentInfo": null,
                  "timestamp": 1707153500,
                  "properties": {
                    "cm:name": "Mexican Spanish",
                    "type": "cm:category",
                    "createdAt": 1707153552,
                    "createdBy": "System",
                    "modifiedBy": "admin",
                    "aspectsNames": [
                      "cm:auditable"
                    ]
                  }
                }""";
        containerSupport.raiseBulkIngesterEvent(bulkIngesterEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId" : "5018ff83-ec45-4a11-95c4-681761752aa7",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType" : "create",
                    "timestamp": 1707153500,
                    "properties" : {
                      "type": {"value": "cm:category"},
                      "createdAt": {"value": 1707153552},
                      "createdBy": {"value": "System"},
                      "modifiedBy": {"value": "admin"},
                      "aspectsNames": {"value": ["cm:auditable"]},
                      "cm:name": {
                        "value": "Mexican Spanish",
                        "annotation" : "name"
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldIngestMetadataAndContent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "nodeId": "37be157c-741c-4e51-b781-20d36e4e335a",
                  "timestamp": 1308061016,
                  "contentInfo": {
                    "contentSize": 330,
                    "encoding": "ISO-8859-1",
                    "mimetype": "text/xml"
                  },
                  "properties": {
                    "cm:name": "dashboard.xml",
                    "cm:isContentIndexed": true,
                    "cm:isIndexed": false,
                    "createdAt": 1308061016,
                    "type": "cm:content",
                    "createdBy": "admin",
                    "modifiedBy": "hr_user",
                    "aspectsNames": [
                      "cm:indexControl",
                      "cm:auditable"
                    ]
                  }
                }""";
        containerSupport.raiseBulkIngesterEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId" : "37be157c-741c-4e51-b781-20d36e4e335a",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType" : "create",
                    "timestamp": 1308061016,
                    "properties" : {
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "hr_user"},
                      "aspectsNames": {"value": ["cm:indexControl", "cm:auditable"]},
                      "createdAt": {"value": 1308061016},
                      "cm:name": {
                        "value": "dashboard.xml",
                        "annotation" : "name"
                      },
                      "cm:isContentIndexed": {"value": true},
                      "cm:isIndexed": {"value": false},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "dashboard.xml",
                            "size": 330,
                            "content-type": "text/xml"
                          }
                        }
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/37be157c-741c-4e51-b781-20d36e4e335a",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"37be157c-741c-4e51-b781-20d36e4e335a\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1308061016}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);

    }
}
