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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e;

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
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update",
                  "properties" : {
                    "cm:title" : "Purchase Order",
                    "aspectsNames" : [ "cm:versionable", "cm:author", "cm:titled" ],
                    "modifiedByUserWithId" : "abeecher"
                  },
                  "removedProperties" : [ "cm:versionType", "cm:description" ]
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldCreateCustomProperty()
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

        String repoEvent = generateCustomPropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update",
                  "properties" : {
                    "cm:title": "Purchase Order"
                  }
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldUpdateCustomProperty()
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

        String repoEvent = generateCustomPropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update",
                  "properties" : {
                    "cm:title": "Summary for year 2024"
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

        String repoEvent = generateCustomPropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update"
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldDeleteCustomProperty()
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

        String repoEvent = generateCustomPropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update",
                  "removedProperties" : [ "cm:title" ]
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

        String repoEvent = generateCustomPropertiesUpdatedEvent(properties, propertiesBefore);

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "eventType" : "update",
                  "properties" : {
                    "cm:taggable": [ "51d0b636-3c3b-4e33-ba1f-098474f53e8c" ],
                    "cm:categories": [ "a9f57ef6-2acf-4b2a-ae85-82cf552bec58" ]
                  }
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    private String generateCustomPropertiesUpdatedEvent(String properties, String propertiesBefore)
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
}
