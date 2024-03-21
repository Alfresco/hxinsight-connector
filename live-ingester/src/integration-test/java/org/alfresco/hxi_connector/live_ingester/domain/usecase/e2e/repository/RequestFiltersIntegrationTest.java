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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SpringBootTest(properties = {"alfresco.filter.aspect.deny[0]=sc:secured",
        "alfresco.filter.aspect.allow[0]=cm:versionable", "alfresco.filter.aspect.allow[1]=cm:auditable",
        "alfresco.filter.type.deny[0]=cm:folder",
        "alfresco.filter.type.allow[0]=cm:content",
        "alfresco.filter.path.allow[0]=5f355d16-f824-4173-bf4b-b1ec37ef5549", "alfresco.filter.path.allow[1]=93f7edf5-e4d8-4749-9b4c-e45097e2e19d",
        "alfresco.filter.path.deny[0]=11111111-1111-1111-1111-111111111111",
        "logging.level.org.alfresco=DEBUG"})
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class RequestFiltersIntegrationTest extends E2ETestBase
{

    @Test
    void testCreateRequestWithAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f01",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223501",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e01",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable" ],
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
        String expectedBody = """
                {
                   "objectId": "d71dd823-82c7-477c-8490-04cb0e826e01",
                   "eventType": "create",
                   "properties": {
                      "cm:autoVersion": {"value": true},
                      "createdAt": {"value": 1709378055695},
                      "cm:versionType": {"value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable"]},
                      "cm:name": {"value": "purchase-order-scan.pdf"},
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "admin"},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "purchase-order-scan.pdf",
                            "size": 531152,
                            "content-type": "application/pdf"
                          }
                        }
                      }
                    }
                 }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-82c7-477c-8490-04cb0e826e01",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e01\\",\\"targetMimeType\\":\\"application/pdf\\"}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }

    @Test
    void testCreateRequestWithAspectInDeniedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f02",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223502",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e02",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable", "sc:secured" ],
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testCreateRequestWithAtLeastOneAspectPresentInAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f03",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223503",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e03",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable", "cm:classifiable" ],
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
        String expectedBody = """
                {
                   "objectId": "d71dd823-82c7-477c-8490-04cb0e826e03",
                   "eventType": "create",
                   "properties": {
                      "cm:autoVersion": {"value": true},
                      "createdAt": {"value": 1709378055695},
                      "cm:versionType": {"value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable", "cm:classifiable"]},
                      "cm:name": {"value": "purchase-order-scan.pdf"},
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "admin"},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "purchase-order-scan.pdf",
                            "size": 531152,
                            "content-type": "application/pdf"
                          }
                        }
                      }
                    }
                 }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testCreateRequestWithAtLeastOneAspectPresentInAllowedFilterAndTypeInAllowedFilterAndAncestorInDeniedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f03",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223503",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e03",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "11111111-1111-1111-1111-111111111111" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable", "cm:classifiable" ],
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testUpdateRequestWithAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0504",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482804",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e04",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2024-03-06T10:29:42.529Z",
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
                      "aspectNames": [ "cm:versionable", "cm:auditable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
                      "aspectNames": [ "cm:versionable" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e04",
                  "eventType": "update",
                  "properties": {
                    "cm:title": {"value": "Purchase Order"},
                    "aspectsNames": {"value" : [ "cm:versionable", "cm:auditable" ]},
                    "modifiedBy": {"value": "abeecher"}
                  },
                  "removedProperties": ["cm:versionType", "cm:description"]
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdateRequestWithAspectInDeniedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0505",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482805",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e05",
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2024-03-06T10:29:42.529Z",
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
                      "aspectNames": [ "cm:versionable", "sc:secured" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
                      "aspectNames": [ "cm:versionable" ]
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testUpdateRequestWithAtLeastOneAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0506",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482806",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e06",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2024-03-06T10:29:42.529Z",
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
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled", "cm:classifiable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
                      "aspectNames": [ "cm:versionable" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e06",
                  "eventType": "update",
                  "properties": {
                    "cm:title": {"value": "Purchase Order"},
                    "aspectsNames": {"value" : [ "cm:versionable", "cm:author", "cm:titled", "cm:classifiable" ]},
                    "modifiedBy": {"value": "abeecher"}
                  },
                  "removedProperties": ["cm:versionType", "cm:description"]
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdateRequestWithAtLeastOneAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInDeniedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0506",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482806",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e06",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "11111111-1111-1111-1111-111111111111" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2024-03-06T10:29:42.529Z",
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
                      "aspectNames": [ "cm:versionable", "cm:author", "cm:titled", "cm:classifiable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
                      "aspectNames": [ "cm:versionable" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testCreateRequestWithEmptyAspectCollectionAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f07",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223507",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e07",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ ],
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testCreateRequestWithNoAspectsInEventAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f08",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223508",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e08",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:content",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testCreateRequestWithAspectInAllowedFilterAndTypeInDeniedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f09",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223509",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e09",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:folder",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2024-03-06T11:14:15.695Z",
                      "content": {
                        "mimeType": "application/pdf",
                        "sizeInBytes": 531152,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "cm:autoVersion": true,
                        "cm:versionType": "MAJOR",
                        "cm:taggable": null
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable" ],
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testUpdateRequestWithAspectInAllowedFilterAndTypeInDeniedFilterAndAncestorInAllowedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0510",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482810",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e10",
                      "name": "purchase-order-scan.pdf",
                      "nodeType": "cm:folder",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2024-03-02T11:14:15.695Z",
                      "modifiedByUser": {
                        "id": "abeecher",
                        "displayName": "Alice Beecher"
                      },
                      "modifiedAt": "2024-03-06T10:29:42.529Z",
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
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }
}
