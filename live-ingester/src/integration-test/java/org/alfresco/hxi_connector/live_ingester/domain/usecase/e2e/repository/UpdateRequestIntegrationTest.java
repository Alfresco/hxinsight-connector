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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

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
                        "cm:versionType": null,
                        "cm:versionLabel": "1.0",
                        "cm:taggable": null
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
                        "cm:taggable": null,
                        "cm:description": "Old Description"
                      },
                      "aspectNames": [ "cm:versionable", "cm:thumbnailModification", "cm:author" ]
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "properties": {
                    "cm:title": {"value": "Purchase Order"},
                    "aspectsNames": {"value": ["cm:versionable", "cm:author", "cm:titled"]},
                    "modifiedBy": {"value": "abeecher"}
                  },
                  "removedProperties": ["cm:versionType", "cm:description"]
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testContentUpdateRequest()
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
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 456,
                        "encoding": "UTF-8"
                      }
                    },
                    "resourceBefore": {
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
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e65\\"}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
        containerSupport.expectNoHxIngestMessagesReceived();
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
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "properties": {
                    "cm:title": {"value": "Purchase Order"}
                  }
                }""";
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
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "properties": {
                    "cm:title": {"value": "Summary for year 2024"}
                  }
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    /**
     * This weird situation when we update property from null -> null sometimes happens, and it is probably some defect of our event system
     */
    @Test
    void shouldDoNothingWithUnchangedProperty()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String properties = """
                {
                  "cm:taggable": null
                }
                """;

        String propertiesBefore = """
                {
                  "cm:taggable": null
                }
                """;

        String repoEvent = generatePropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void shouldDeleteProperty()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String properties = """
                {
                  "cm:title": null
                }
                """;

        String propertiesBefore = """
                {
                  "cm:title": "Summary for year 2024"
                }
                """;

        String repoEvent = generatePropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "removedProperties": ["cm:title"]
                }""";
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
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "properties": {
                    "cm:taggable": {"value": ["51d0b636-3c3b-4e33-ba1f-098474f53e8c"]},
                    "cm:categories": {"value": ["a9f57ef6-2acf-4b2a-ae85-82cf552bec58"]}
                  }
                }""";
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
    void testRemovingContentFromNode()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab05a6",
                  "data": {
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65",
                      "name": "purchase-order-scan.pdf",
                      "content": {
                        "sizeInBytes": 0
                      },
                      "properties": {
                        "cm:title": "Purchase Order"
                      }
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      }
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType": "update",
                  "removedProperties": ["cm:content"]
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
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
                {
                  "objectId": "321d84e3-a5fe-431e-92f5-f8e09480305e",
                  "eventType": "update",
                  "properties": {
                    "aspectsNames": {"value": ["cm:preferences", "cm:ownable"]}
                  }
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }
}
