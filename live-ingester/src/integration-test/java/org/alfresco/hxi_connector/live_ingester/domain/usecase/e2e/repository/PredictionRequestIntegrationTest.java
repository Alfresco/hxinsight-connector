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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class PredictionRequestIntegrationTest extends E2ETestBase
{
    private static final long TIMESTAMP = 1_611_656_982_995L;

    @Test
    void testPredictionNodeCreateRequest()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f61",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223581",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "12341234-1234-1234-1234-123412341234",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "prediction1",
                      "nodeType": "hxi:prediction",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2021-01-21T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2021-01-21T11:14:15.695Z",
                      "properties": {
                        "hxi:predictionDateTime": "2021-01-21T11:14:15.695Z",
                        "hxi:confidenceLevel": 0.9,
                        "hxi:predictionValue": "predicted value"
                      },
                      "isFolder": false,
                      "isFile": false,
                      "primaryAssocQName": "cm:description"
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE" ],
                    "resourceDeniedAuthorities": []
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testPredictionApplyRequest()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "23452345-2345-2345-2345-234523452345",
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
                        "cm:title": "Purchase Order New",
                        "cm:versionType": "MAJOR",
                        "cm:versionLabel": "1.0",
                        "hxi:latestPredictionDateTime": "2024-05-08T17:00:42.529Z"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled", "hxi:predictionApplied" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2021-01-21T11:14:25.223Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "properties": {
                        "cm:title": "Purchase Order Old",
                        "hxi:latestPredictionDateTime": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testNewerPredictionApplyRequest()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "23452345-2345-2345-2345-234523452345",
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
                        "cm:title": "Purchase Order New",
                        "cm:versionType": "MAJOR",
                        "cm:versionLabel": "1.0",
                        "hxi:latestPredictionDateTime": "2024-05-09T19:00:00.000Z"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled", "hxi:predictionApplied" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2021-01-21T11:14:25.223Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "properties": {
                        "cm:title": "Purchase Order Old",
                        "hxi:latestPredictionDateTime": "2024-05-02T08:00:00.000Z"
                      }
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testUpdateRequestWithPredictionPreviouslyApplied()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "34563456-3456-3456-3456-345634563456",
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
                        "cm:versionType": null,
                        "cm:description": null,
                        "cm:versionLabel": "1.0",
                        "hxi:latestPredictionDateTime": "2024-05-08T17:00:42.529Z"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled", "hxi:predictionApplied" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2021-01-21T11:14:25.223Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "properties": {
                        "cm:title": null,
                        "cm:versionType": "MAJOR",
                        "cm:description": "Old Description"
                      },
                      "aspectNames": [ "cm:versionable", "cm:thumbnailModification", "cm:author", "hxi:predictionApplied" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "34563456-3456-3456-3456-345634563456",
                    "eventType": "update",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "timestamp" : %s,
                    "properties": {
                       "createdAt" : {
                         "value" : 1611227655695
                       },
                       "cm:versionLabel" : {
                         "value" : "1.0"
                       },
                       "createdBy" : {
                         "value" : "admin"
                       },
                       "ALLOW_ACCESS" : {
                         "value" : [ "GROUP_EVERYONE" ]
                       },
                       "cm:name" : {
                         "value" : "purchase-order-scan.pdf"
                       },
                       "hxi:latestPredictionDateTime" : {
                         "value" : "2024-05-08T17:00:42.529Z"
                       },
                       "type" : {
                         "value" : "cm:content"
                       },
                       "DENY_ACCESS" : {
                         "value" : [ ]
                       },
                       "cm:content" : {
                         "file" : {
                           "content-metadata" : {
                             "size" : 531152,
                             "name" : "purchase-order-scan.pdf",
                             "content-type" : "application/pdf"
                           }
                         }
                       },
                      "cm:title": {"value": "Purchase Order"},
                      "aspectsNames": {"value": ["cm:versionable", "hxi:predictionApplied", "cm:author", "cm:titled"]},
                      "modifiedBy": {"value": "abeecher"}
                    },
                    "removedProperties": ["cm:versionType", "cm:description"]
                  }
                ]""".formatted(TIMESTAMP);
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testPredictionNodeConfirmedByUserRaisesHxIEventAgainstParent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f61",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223581",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "12341234-1234-1234-1234-123412341234",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "prediction1",
                      "nodeType": "hxi:prediction",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2021-01-21T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2022-02-22T22:22:22.222Z",
                      "properties": {
                        "hxi:predictionDateTime": "2021-01-21T11:11:11.111Z",
                        "hxi:confidenceLevel": 0.9,
                        "hxi:predictionValue": "predicted value",
                        "hxi:previousValue": "previous value",
                        "hxi:reviewStatus": "CONFIRMED"
                      },
                      "isFolder": false,
                      "isFile": false,
                      "primaryAssocQName": "cm:description"
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2021-01-11T11:11:11.111Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "properties": {
                        "hxi:reviewStatus": "UNREVIEWED"
                      }
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE" ],
                    "resourceDeniedAuthorities": []
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "update",
                    "timestamp": %s,
                    "properties": {
                      "cm:description": {"value": "predicted value"}
                    }
                  }
                ]""".formatted(TIMESTAMP);
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }
}
