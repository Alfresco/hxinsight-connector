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
                        "cm:versionType": "MAJOR"
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE" ],
                    "resourceDeniedAuthorities": [ ]
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e01",
                    "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611227656423,
                    "properties": {
                      "cm:autoVersion": {"type": "boolean", "value": true},
                      "createdAt": {"value": "2024-03-02T11:14:15.695Z", "annotation": "dateCreated"},
                      "modifiedAt" : {"value" : "2024-03-06T11:14:15.695Z", "annotation" : "dateModified"},
                      "cm:versionType": {"type": "string", "value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable"], "annotation": "aspects"},
                      "cm:name": {
                        "value": "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "type": {"value": "cm:content", "annotation": "type"},
                      "createdBy": {"value": "admin", "annotation": "createdBy"},
                      "modifiedBy": {"value": "admin", "annotation": "modifiedBy"},
                       "ancestors" : {
                            "value" : {
                              "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                              "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549" ]
                            },
                            "annotation" : "hierarchy"
                          },
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "purchase-order-scan.pdf",
                            "size": 531152,
                            "content-type": "application/pdf"
                          }
                        }
                      },
                      "ALLOW_ACCESS": {"type": "string", "value": ["GROUP_EVERYONE"]}
                    }
                  }
                ]""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-82c7-477c-8490-04cb0e826e01",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e01\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1611227656423}",
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
                        "cm:versionType": "MAJOR"
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable", "cm:classifiable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE" ],
                    "resourceDeniedAuthorities": [ ]
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e03",
                    "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611227656423,
                    "properties": {
                      "cm:autoVersion": {"type": "boolean", "value": true},
                      "createdAt": {"value": "2024-03-02T11:14:15.695Z", "annotation": "dateCreated"},
                      "modifiedAt": {"value": "2024-03-06T11:14:15.695Z", "annotation": "dateModified"},
                      "cm:versionType": {"type": "string", "value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable", "cm:classifiable"], "annotation": "aspects"},
                      "cm:name": {
                        "value": "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "type": {"value": "cm:content", "annotation": "type"},
                      "createdBy": {"value": "admin", "annotation": "createdBy"},
                      "modifiedBy": {"value": "admin", "annotation": "modifiedBy"},
                      "ancestors" : {
                            "value" : {
                              "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                              "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549" ]
                            },
                            "annotation" : "hierarchy"
                          },
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "purchase-order-scan.pdf",
                            "size": 531152,
                            "content-type": "application/pdf"
                          }
                        }
                      },
                      "ALLOW_ACCESS": {"type": "string", "value": ["GROUP_EVERYONE"]}
                    }
                  }
                ]""";
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
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f04",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223504",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e04",
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
                    "resourceDeniedAuthorities": [ ]
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
                        "cm:description": null
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
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e05",
                    "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611656982995,
                    "properties": {
                      "cm:title": {"type": "string", "value": "Purchase Order"},
                      "aspectsNames": {"value" : [ "cm:versionable", "cm:auditable" ], "annotation" : "aspects"},
                      "modifiedBy": {"value": "abeecher", "annotation": "modifiedBy"},
                      "createdAt" : {
                        "value" : "2024-03-02T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2024-03-06T10:29:42.529Z",
                        "annotation" : "dateModified"
                      },
                      "cm:versionLabel" : {
                        "type": "string",
                        "value" : "1.0"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "cm:name" : {
                        "value" : "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "ancestors" : {
                            "value" : {
                              "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                              "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549"]
                            },
                            "annotation" : "hierarchy"
                          },
                      "type": {"value": "cm:content", "annotation": "type"},
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
    void testUpdateRequestWithAspectInDeniedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
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
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0507",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482807",
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
                        "cm:description": null
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
                [
                  {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e07",
                    "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1611656982995,
                    "properties": {
                      "cm:title": {"type": "string", "value": "Purchase Order"},
                      "aspectsNames": {"value" : [ "cm:versionable", "cm:author", "cm:titled", "cm:classifiable" ], "annotation": "aspects"},
                      "modifiedBy": {"value": "abeecher", "annotation": "modifiedBy"},
                      "createdAt" : {
                        "value" : "2024-03-02T11:14:15.695Z",
                        "annotation" : "dateCreated"
                      },
                      "modifiedAt" : {
                        "value" : "2024-03-06T10:29:42.529Z",
                        "annotation" : "dateModified"
                      },
                      "cm:versionLabel" : {
                        "type": "string",
                        "value" : "1.0"
                      },
                      "createdBy" : {
                        "value" : "admin",
                        "annotation" : "createdBy"
                      },
                      "ALLOW_ACCESS" : {
                        "type": "string",
                        "value" : [ "GROUP_EVERYONE" ]
                      },
                      "cm:name" : {
                        "value" : "purchase-order-scan.pdf",
                        "annotation" : "name"
                      },
                      "ancestors" : {
                            "value" : {
                              "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                              "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549" ]
                            },
                            "annotation" : "hierarchy"
                          },
                      "type": {"value": "cm:content", "annotation": "type"},
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
    void testUpdateRequestWithAtLeastOneAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInDeniedFilter()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0508",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482808",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e08",
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
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f10",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223510",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e10",
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
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f11",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223511",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e11",
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
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0512",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482812",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e12",
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

    @Test
    void testUpdateRequestWithAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorModifiedToFitAllowedFilter()
    {
        // given node updated from being denied to be allowed
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0513",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482813",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e13",
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
                        "cm:versionLabel": "1.0"
                      },
                      "aspectNames": [ "cm:versionable", "cm:auditable" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceBefore": {
                      "@type": "NodeResource",
                      "primaryHierarchy": [ "11111111-1111-1111-1111-111111111111", "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "modifiedAt": "2024-03-02T11:14:25.223Z",
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
                      "aspectNames": [ "cm:versionable" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then send a create event
        String expectedBody = """
                [
                  {
                     "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e13",
                     "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                     "eventType" : "createOrUpdate",
                     "sourceTimestamp": 1611656982995,
                     "properties" : {
                       "cm:title" : {
                         "type": "string",
                         "value" : "Purchase Order"
                       },
                       "createdAt" : {
                         "value" : "2024-03-02T11:14:15.695Z",
                         "annotation" : "dateCreated"
                       },
                       "modifiedAt" : {
                         "value" : "2024-03-06T10:29:42.529Z",
                         "annotation" : "dateModified"
                       },
                       "cm:versionLabel" : {
                         "type": "string",
                         "value" : "1.0"
                       },
                       "createdBy" : {
                         "value" : "admin",
                         "annotation" : "createdBy"
                       },
                       "ALLOW_ACCESS" : {
                         "type": "string",
                         "value" : [ "GROUP_EVERYONE" ]
                       },
                       "cm:name" : {
                         "value" : "purchase-order-scan.pdf",
                         "annotation" : "name"
                       },
                       "aspectsNames" : {
                         "value" : [ "cm:versionable", "cm:auditable" ],
                         "annotation" : "aspects"
                       },
                       "modifiedBy" : {
                         "value" : "abeecher",
                         "annotation" : "modifiedBy"
                       },
                       "ancestors" : {
                             "value" : {
                               "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                               "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549" ]
                             },
                             "annotation" : "hierarchy"
                           },
                       "type": {"value": "cm:content", "annotation": "type"},
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
    void testUpdateRequestWithAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorModifiedToFitDeniedFilter()
    {
        // given node updated from being allowed to be denied
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0514",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482814",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e14",
                      "primaryHierarchy": [ "11111111-1111-1111-1111-111111111111", "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
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
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
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

        // then send a delete event
        String expectedBody = """
                  [
                    {
                      "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e14",
                      "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                      "sourceTimestamp": 1611656982995,
                      "eventType" : "delete"
                    }
                  ]
                """;
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdateRequestWithAspectModifiedToFitDeniedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given node updated from being denied to be allowed
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0515",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482815",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e15",
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
                      "aspectNames": [ "cm:versionable", "cm:auditable", "sc:secured" ],
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

        // then send a delete event
        String expectedBody = """
                  [
                    {
                      "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e15",
                      "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                      "sourceTimestamp" : 1611656982995,
                      "eventType" : "delete"
                    }
                  ]
                """;
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testUpdateRequestWithAspectModifiedToFitAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilter()
    {
        // given node updated from being allowed to be denied
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0516",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482816",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e16",
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
                        "cm:title": "Purchase Order"
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
                        "cm:title": null
                      },
                      "aspectNames": [ "cm:versionable", "sc:secured" ]
                    }
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then send a create event
        String expectedBody = """
                [
                  {
                     "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e16",
                     "sourceId" : "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                     "eventType" : "createOrUpdate",
                     "sourceTimestamp": 1611656982995,
                     "properties" : {
                       "cm:title" : {
                         "type": "string",
                         "value" : "Purchase Order"
                       },
                       "createdAt" : {
                         "value" : "2024-03-02T11:14:15.695Z",
                         "annotation" : "dateCreated"
                       },
                       "modifiedAt" : {
                         "value" : "2024-03-06T10:29:42.529Z",
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
                       "cm:name" : {
                         "value" : "purchase-order-scan.pdf",
                         "annotation" : "name"
                       },
                       "aspectsNames" : {
                         "value" : [ "cm:versionable", "cm:auditable" ],
                         "annotation" : "aspects"
                       },
                       "modifiedBy" : {
                         "value" : "abeecher",
                         "annotation" : "modifiedBy"
                       },
                       "ancestors" : {
                             "value" : {
                               "primaryParentId" : "5f355d16-f824-4173-bf4b-b1ec37ef5549",
                               "primaryAncestorIds" : [ "93f7edf5-e4d8-4749-9b4c-e45097e2e19d","5f355d16-f824-4173-bf4b-b1ec37ef5549" ]
                             },
                             "annotation" : "hierarchy"
                           },
                       "type": {"value": "cm:content", "annotation": "type"},
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
    void testUpdateRequestWhenSingleFilterDeniedNodeBeforeAndAnotherSingleFilterDeniesCurrentNode()
    {
        // given
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Updated",
                  "id": "ae5dac3c-25d0-438d-b148-2084d1ab0517",
                  "primaryHierarchy": [ "11111111-1111-1111-1111-111111111111", "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-26T10:29:42.99524Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeUpdated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "b5b1ebfe-45fc-4f86-b71b-421996482817",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e17",
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
                      "aspectNames": [ "cm:versionable", "cm:thumbnailModification", "sc:secured" ]
                    }
                  }
                }""";
        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }

    @Test
    void testCreateRequestWithAspectInAllowedFilterAndTypeInAllowedFilterAndAncestorInAllowedFilterAndFilteredNodeIdInDenied()
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
                      "id": "11111111-1111-1111-1111-111111111111",
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
        containerSupport.expectNoHxIngestMessagesReceived();
    }
}
