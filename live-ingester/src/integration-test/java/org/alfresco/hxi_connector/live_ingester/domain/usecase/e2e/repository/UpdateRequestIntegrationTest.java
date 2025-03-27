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

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class UpdateRequestIntegrationTest extends E2ETestBase
{
    @Test
    void testUpdateRequest()
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
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
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
                        "cm:versionLabel": "1.0",
                        "cm:description": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ],
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
                        "cm:versionLabel": "1.0",
                        "cm:description": "Old Description"
                      },
                      "aspectNames": [ "cm:versionable", "cm:thumbnailModification", "cm:author" ]
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/create-or-update-document.yml");
        containerSupport.expectHxIngestMessageReceived(request.body());
    }

    @Test
    void testContentUpdateRequest()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // Repo event showing the content was updated, but the content metadata stayed the same.
        String repoEvent = """
                {
                  "type": "org.alfresco.event.node.Updated",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2021-01-21T11:14:15.695Z",
                      "modifiedAt": "2021-01-21T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ],
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 123,
                        "encoding": "UTF-8"
                      }
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 123,
                        "encoding": "UTF-8"
                      }
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
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
        containerSupport.expectHxIngestMessageReceived(
                """
                        [
                          {
                            "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                            "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                            "eventType": "createOrUpdate",
                            "sourceTimestamp": 1611656982995,
                            "properties": {
                              "createdAt" : {
                                "value" : "2021-01-21T11:14:15.695Z",
                                "annotation" : "dateCreated"
                              },
                              "modifiedAt" : {
                                "value" : "2021-01-21T11:14:15.695Z",
                                "annotation" : "dateModified"
                              },
                              "createdBy" : {
                                "value" : "admin",
                                "annotation" : "createdBy"
                              },
                              "cm:name" : {
                                "value" : "purchase-order-scan.pdf",
                                "annotation" : "name"
                              },
                              "ALLOW_ACCESS" : {
                                "type": "string",
                                "value" : [ "GROUP_EVERYONE" ]
                              },
                              "aspectsNames" : {
                                "value" : [ "cm:versionable", "cm:author", "cm:titled" ],
                                "annotation" : "aspects"
                              },
                              "modifiedBy" : {
                                "value" : "abeecher",
                                "annotation" : "modifiedBy"
                              },
                              "type" : {
                                "value" : "cm:content",
                                "annotation" : "type"
                              },
                              "cm:content" : {
                                "file" : {
                                  "content-metadata" : {
                                    "size" : 123,
                                    "name" : "purchase-order-scan.pdf",
                                    "content-type" : "application/pdf"
                                  }
                                }
                              }
                            }
                          }
                        ]""");
    }

    @Test
    void shouldCreateProperty()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String properties = """
                {
                  "cm:title": "Purchase Order"
                }
                """;

        String propertiesBefore = """
                {
                  "cm:title": null
                }
                """;

        String repoEvent = generatePropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611656982995,
                    "properties": {
                      "cm:title": {"type": "string", "value": "Purchase Order"},
                      "createdAt" : {
                        "value" : "2021-01-21T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2021-01-26T10:29:42.529Z",
                        "annotation" : "dateModified"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "cm:name" : {
                        "value" : "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "aspectsNames" : {
                        "value" : [ "cm:versionable", "cm:author", "cm:titled" ],
                        "annotation" : "aspects"
                      },
                      "modifiedBy" : {
                        "value" : "abeecher",
                        "annotation" : "modifiedBy"
                      },
                      "type" : {
                        "value" : "cm:content",
                        "annotation" : "type"
                      },
                      "cm:content" : {
                        "file" : {
                          "content-metadata" : {
                            "size" : 531152,
                            "name" : "purchase-order-scan.pdf",
                            "content-type" : "application/pdf"
                          }
                        }
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldUpdateProperty()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String properties = """
                {
                  "cm:title": "Summary for year 2024"
                }
                """;

        String propertiesBefore = """
                {
                  "cm:title": "Summary"
                }
                """;

        String repoEvent = generatePropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611656982995,
                    "properties": {
                      "cm:title": {"type": "string", "value": "Summary for year 2024"},
                      "createdAt" : {
                        "value" : "2021-01-21T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2021-01-26T10:29:42.529Z",
                        "annotation" : "dateModified"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "cm:name" : {
                        "value" : "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "aspectsNames" : {
                        "value" : [ "cm:versionable", "cm:author", "cm:titled" ],
                        "annotation" : "aspects"
                      },
                      "modifiedBy" : {
                        "value" : "abeecher",
                        "annotation" : "modifiedBy"
                      },
                      "type" : {
                        "value" : "cm:content",
                        "annotation" : "type"
                      },
                      "cm:content" : {
                        "file" : {
                          "content-metadata" : {
                            "size" : 531152,
                            "name" : "purchase-order-scan.pdf",
                            "content-type" : "application/pdf"
                          }
                        }
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldMapTagsAndCategoriesToJustIds()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String properties = """
                {
                  "cm:taggable": [
                    {
                      "storeRef": {
                        "protocol": "workspace",
                        "identifier": "SpacesStore"
                      },
                      "id": "51d0b636-3c3b-4e33-ba1f-098474f53e8c"
                    }
                  ],
                  "cm:categories": [
                    {
                      "storeRef": {
                        "protocol": "workspace",
                        "identifier": "SpacesStore"
                      },
                      "id": "a9f57ef6-2acf-4b2a-ae85-82cf552bec58"
                    }
                  ]
                }
                """;

        String propertiesBefore = """
                {
                  "cm:taggable": null,
                  "cm:categories": null
                }
                """;

        String repoEvent = generatePropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611656982995,
                    "properties": {
                      "cm:taggable": {"type": "string", "value": ["51d0b636-3c3b-4e33-ba1f-098474f53e8c"]},
                      "cm:categories": {"type": "string", "value": ["a9f57ef6-2acf-4b2a-ae85-82cf552bec58"]},
                      "createdAt" : {
                        "value" : "2021-01-21T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2021-01-26T10:29:42.529Z",
                        "annotation" : "dateModified"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "cm:name" : {
                        "value" : "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "aspectsNames" : {
                        "value" : [ "cm:versionable", "cm:author", "cm:titled" ],
                        "annotation" : "aspects"
                      },
                      "modifiedBy" : {
                        "value" : "abeecher",
                        "annotation" : "modifiedBy"
                      },
                      "type" : {
                        "value" : "cm:content",
                        "annotation" : "type"
                      },
                      "cm:content" : {
                        "file" : {
                          "content-metadata" : {
                            "size" : 531152,
                            "name" : "purchase-order-scan.pdf",
                            "content-type" : "application/pdf"
                          }
                        }
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    private String generatePropertiesUpdatedEvent(String properties, String propertiesBefore)
    {
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
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
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
                      "properties": %s,
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "properties": %s
                    }
                  }
                }""";

        return repoEvent.formatted(properties, propertiesBefore);
    }

    @Test
    void testLogInEvent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "621573f5-0fb4-46dd-ab1a-88f83c0e1f2b",
                  "source": "/6cac945d-0919-47cc-ade7-8645e65c4371",
                  "time": "2024-01-09T11:14:33.615Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "a7a1ef25-2398-4fb9-8178-f3a6ff6d5ed0",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "321d84e3-a5fe-431e-92f5-f8e09480305e",
                      "name": "321d84e3-a5fe-431e-92f5-f8e09480305e",
                      "nodeType": "cm:person",
                      "createdByUser": null,
                      "createdAt": null,
                      "modifiedByUser": null,
                      "modifiedAt": null,
                      "content": null,
                      "properties": {
                        "cm:homeFolderProvider": "bootstrapHomeFolderProvider",
                        "cm:homeFolder": {"storeRef": {"protocol": "workspace", "identifier": "SpacesStore"}, "id": "7f1fa040-e840-40c6-a8a0-da457aca2473"},
                        "sys:cascadeCRC": 1040368885,
                        "cm:lastName": ""
                      },
                      "aspectNames": [ "cm:preferences", "cm:ownable" ],
                      "isFolder": false,
                      "isFile": false
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "properties": {
                        "cm:preferenceValues": null
                      },
                      "aspectNames": [ "cm:ownable" ]
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "321d84e3-a5fe-431e-92f5-f8e09480305e",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1704798873615,
                    "properties": {
                      "aspectsNames": {"value": ["cm:preferences", "cm:ownable"], "annotation": "aspects"},
                      "cm:homeFolderProvider" : {
                        "type": "string",
                        "value" : "bootstrapHomeFolderProvider"
                      },
                      "cm:homeFolder" : {
                        "value" : {
                          "storeRef" : {
                            "type" : "object",
                            "value" : {
                              "protocol" : {
                                "type" : "string",
                                "value" : "workspace"
                              },
                              "identifier" : {
                                "type" : "string",
                                "value" : "SpacesStore"
                              }
                            }
                          },
                          "id" : {
                            "type" : "string",
                            "value" : "7f1fa040-e840-40c6-a8a0-da457aca2473"
                          }
                        },
                        "type" : "object"
                      },
                      "sys:cascadeCRC" : {
                        "type": "integer",
                        "value" : 1040368885
                      },
                      "cm:lastName" : {
                        "type": "string",
                        "value" : ""
                      },
                      "cm:name" : {
                        "value" : "321d84e3-a5fe-431e-92f5-f8e09480305e",
                        "annotation" : "name"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "type" : {
                        "value" : "cm:person",
                        "annotation" : "type"
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdateFolderName()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "type": "org.alfresco.event.node.Updated",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482881",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "82c7d723-1dd8-477c-8490-04cb0e826e65",
                      "name": "New Folder",
                      "nodeType": "cm:folder",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2021-01-21T11:14:15.695Z",
                      "modifiedAt": "2021-01-21T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled" ]
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "name": "Old Folder"
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "82c7d723-1dd8-477c-8490-04cb0e826e65",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp" : 1611656982995,
                    "properties": {
                      "cm:name": {
                        "value": "New Folder",
                        "annotation" : "name"
                      },
                      "createdAt" : {
                        "value" : "2021-01-21T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2021-01-21T11:14:15.695Z",
                        "annotation" : "dateModified"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "aspectsNames" : {
                        "value" : [ "cm:versionable", "cm:author", "cm:titled" ],
                        "annotation" : "aspects"
                      },
                      "modifiedBy" : {
                        "value" : "abeecher",
                        "annotation" : "modifiedBy"
                      },
                      "type" : {
                        "value" : "cm:folder",
                        "annotation" : "type"
                      }
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdatePermissions()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.permission.Updated",
                  "id": "158c0396-0f68-48fc-9836-941a8ce30e4f",
                  "source": "/9b11c3e9-4d71-42d8-a616-089dcbd85034",
                  "time": "2024-07-31T10:34:15.416Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/permissionUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "e19ad6f9-6c55-42f8-8140-5f3905e34a50",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "9f3380e3-b9b1-4b01-b1c6-ef1f717a9abb",
                      "primaryHierarchy": [
                        "8d893085-2e62-43a1-9947-f772d2f7ec5b",
                        "1bb5693c-08bc-41ce-a4f3-5d75a828d570"
                      ],
                      "name": "test",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-07-30T11:37:39.182Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-07-31T10:31:48.541Z",
                      "content": {
                        "mimeType": "text/plain",
                        "sizeInBytes": 0,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:title": "",
                        "app:editInline": true,
                        "cm:categories": [],
                        "cm:description": "",
                        "cm:author": ""
                      },
                      "localizedProperties": {
                        "cm:title": {
                          "en": ""
                        },
                        "cm:description": {
                          "en": ""
                        }
                      },
                      "aspectNames": [
                        "cm:generalclassifiable",
                        "cm:titled",
                        "app:inlineeditable",
                        "cm:author",
                        "cm:auditable",
                        "cm:taggable"
                      ],
                      "primaryAssocQName": "cm:test",
                      "secondaryParents": [],
                      "isFile": true,
                      "isFolder": false
                    },
                    "resourceReaderAuthorities": [
                      "GROUP_EVERYONE",
                      "abeecher"
                    ],
                    "resourceDeniedAuthorities": [ ]
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [{
                  "objectId" : "9f3380e3-b9b1-4b01-b1c6-ef1f717a9abb",
                  "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                  "eventType" : "createOrUpdate",
                  "sourceTimestamp" : 1722422055416,
                  "properties" : {
                    "ALLOW_ACCESS" : {
                      "type": "string",
                      "value" : [ "GROUP_EVERYONE", "abeecher" ]
                    },
                    "cm:title" : {
                      "type": "string",
                      "value" : ""
                    },
                    "app:editInline" : {
                      "type": "boolean",
                      "value" : true
                    },
                    "aspectsNames" : {
                      "value" : [ "cm:generalclassifiable", "app:inlineeditable", "cm:author", "cm:titled", "cm:auditable", "cm:taggable" ], "annotation" : "aspects"
                    },
                    "type" : {
                      "value" : "cm:content",
                      "annotation" : "type"
                    },
                    "cm:description" : {
                      "type": "string",
                      "value" : ""
                    },
                    "createdAt" : {
                      "value" : "2024-07-30T11:37:39.182Z",
                      "annotation" : "dateCreated"
                    },
                    "modifiedAt" : {
                      "value" : "2024-07-31T10:31:48.541Z",
                      "annotation" : "dateModified"
                    },
                    "createdBy" : {
                      "value" : "admin",
                      "annotation" : "createdBy"
                    },
                    "cm:name" : {
                      "value" : "test",
                      "annotation" : "name"
                    },
                    "cm:author" : {
                      "type": "string",
                      "value" : ""
                    },
                    "modifiedBy" : {
                      "value" : "admin",
                      "annotation" : "modifiedBy"
                    },
                    "cm:content" : {
                      "file" : {
                        "content-metadata" : {
                          "size" : 0,
                          "name" : "test",
                          "content-type" : "text/plain"
                        }
                      }
                    }
                  }
                }]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }
}
