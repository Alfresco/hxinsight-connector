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
                    "resourceDeniedAuthorities": [ ]
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/create-or-update-document-with-ancestors.yml");
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

    @Test
    void testCreateThumbnail()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "b1f04bf2-f670-44d6-be11-4aaa6b94ce6f",
                  "source": "/dc1273e0-9b1c-4309-9273-e09b1c330935",
                  "time": "2025-03-27T11:27:59.28Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "5866c934-f6ca-4403-a6c9-34f6cad403f4",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "2f794000-7b44-4bd7-b940-007b44ebd755",
                      "primaryHierarchy": [ "9cde9dd0-e9b3-4cf1-9e9d-d0e9b33cf1fc","8f2105b4-daaf-4874-9e8a-2152569d109b","b4cff62a-664d-4d45-9302-98723eac1319","1536141f-5b48-499d-b614-1f5b48499dca","a0f105b8-8f5a-45e1-b105-b88f5ac5e179","5a796cd6-5522-4ec9-b96c-d655227ec9a5" ],
                      "name": "doclib",
                      "nodeType": "cm:thumbnail",
                      "createdByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "createdAt": "2025-03-27T11:27:59.232Z",
                      "modifiedByUser": {
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2025-03-27T11:27:59.232Z",
                      "content": {
                        "mimeType": "image/png",
                        "sizeInBytes": 218,
                        "encoding": "UTF-8"
                      },
                      "properties": {
                        "rn:contentHashCode": 1993082629,
                        "cm:contentPropertyName": {
                          "namespaceURI": "http://www.alfresco.org/model/content/1.0",
                          "localName": "content",
                          "prefixString": "content"
                        },
                        "cm:isContentIndexed": true,
                        "cm:thumbnailName": "doclib",
                        "cm:isIndexed": false
                      },
                      "aspectNames": [ "cm:indexControl","rn:hiddenRendition","cm:auditable","rn:rendition2" ],
                      "primaryAssocQName": "cm:doclib",
                      "secondaryParents": [ ],
                      "isFile": true,
                      "isFolder": false
                    },
                    "resourceReaderAuthorities": [ "GROUP_EVERYONE","GROUP_site_swsdp_SiteContributor","GROUP_site_swsdp_SiteCollaborator","GROUP_site_swsdp_SiteManager","GROUP_site_swsdp_SiteConsumer" ],
                    "resourceDeniedAuthorities": [ ]
                  },
                  "extensionAttributes": {
                    "path": "/Company Home/Sites/swsdp/documentLibrary/quick95.doc/doclib",
                    "clientId": null
                  }
                }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/create-thumbnail.yml");
        containerSupport.expectHxIngestMessageReceived(request.body());

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/2f794000-7b44-4bd7-b940-007b44ebd755",
                    "targetMediaType": "image/png",
                    "clientData": "{\\"nodeRef\\":\\"2f794000-7b44-4bd7-b940-007b44ebd755\\",\\"targetMimeType\\":\\"image/png\\",\\"retryAttempt\\":0,\\"timestamp\\":1743074879280}",
                    "transformOptions": { "resizeWidth": "3840", "resizeHeight": "3840", "allowEnlargement": "false", "timeout": "20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
    @Test
    void testCreateRequestWithAncestorsProperty()
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
                          "name": "purchase-order-scan.pdf",
                          "nodeType": "cm:content",
                          "primaryHierarchy": ["5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d"],
                          "createdAt": "2021-01-21T11:14:15.695Z",
                          "createdByUser": {
                            "id": "admin",
                            "displayName": "Administrator"
                          },
                          "modifiedAt": "2021-01-26T10:29:42.529Z",
                          "modifiedByUser": {
                            "id": "abeecher",
                           "displayName": "Alice Beecher"
                          },
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
                          "aspectNames": ["cm:versionable", "cm:author", "cm:titled"],
                          "isFile": true,
                          "isFolder": false
                        },
                        "resourceReaderAuthorities": ["GROUP_EVERYONE"],
                        "resourceDeniedAuthorities": []
                      }
                    }""\";
    
        // when
        containerSupport.raiseRepoEvent(repoEvent);
        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/ancestors/create-request-with-ancestors.yml");
        containerSupport.expectHxIngestMessageReceived(request.body());
    
        String expectedATSRequest = ""\"
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-82c7-477c-8490-04cb0e826e65",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\\\"nodeRef\\\\":\\\\"d71dd823-82c7-477c-8490-04cb0e826e65\\\\",\\\\"targetMimeType\\\\":\\\\"application/pdf\\\\",\\\\"retryAttempt\\\\":0,\\\\"timestamp\\\\":1611656982995}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""\".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
    
    @Test
    void testCreateRequestWithEmptyAncestorsProperty()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();
    
        String repoEvent = ""\"
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
                  "name": "purchase-order-scan.pdf",
                  "nodeType": "cm:content",
                  "primaryHierarchy": [],
                  "createdAt": "2021-01-21T11:14:15.695Z",
                  "createdByUser": {
                    "id": "admin",
                    "displayName": "Administrator"
                  },
                  "modifiedAt": "2021-01-26T10:29:42.529Z",
                  "modifiedByUser": {
                    "id": "abeecher",
                    "displayName": "Alice Beecher"
                  },
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
                  "aspectNames": ["cm:versionable", "cm:author", "cm:titled"],
                  "isFile": true,
                  "isFolder": false
                },
                "resourceReaderAuthorities": ["GROUP_EVERYONE"],
                "resourceDeniedAuthorities": []
              }
            }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/ancestors/create-request-without-ancestors.yml");
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

    @Test
    void testCreateRequestWithoutPrimaryHierarchy()
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
                  "name": "purchase-order-scan.pdf",
                  "nodeType": "cm:content",
                  "createdAt": "2021-01-21T11:14:15.695Z",
                  "createdByUser": {
                    "id": "admin",
                    "displayName": "Administrator"
                  },
                  "modifiedAt": "2021-01-26T10:29:42.529Z",
                  "modifiedByUser": {
                    "id": "abeecher",
                    "displayName": "Alice Beecher"
                  },
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
                  "aspectNames": ["cm:versionable", "cm:author", "cm:titled"],
                  "isFile": true,
                  "isFolder": false
                },
                "resourceReaderAuthorities": ["GROUP_EVERYONE"],
                "resourceDeniedAuthorities": []
              }
            }""";

        // when
        containerSupport.raiseRepoEvent(repoEvent);
        // then
        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/ancestors/create-request-without-ancestors.yml");
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
