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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SpringBootTest(properties = {"alfresco.transform.mime-type.mapping.[text/*]=application/pdf",
        "logging.level.org.alfresco=DEBUG"})
public class MatchingContentMappingRequestIntegrationTest extends E2ETestBase
{
    @ParameterizedTest
    @CsvSource({"image/gif,image/png", "image/bmp,image/png", "image/png,image/png", "image/raw,image/png",
            "image/jpeg,image/jpeg", "image/heic,image/jpeg", "image/webp,image/jpeg"
    })
    void givenMappingForImage_whenContentWithMatchingTypeIngested_thenProcessWithTransformRequest(
            String sourceMimeType, String expectedTargetMimeType)
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-eaeq-4b8b-8eab-e050253d7f61",
                  "source": "/08d9b620-14de-4247-8f33-360988d3b191",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9f2e-400d-9d80-8f840e223581",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-01c7-477c-8490-04cb0e826e61",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.bmp",
                      "nodeType": "cm:content",
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
                      "content": {
                        "mimeType": "%s",
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
                    "resourceDeniedAuthorities": []
                  }
                }""".formatted(sourceMimeType);
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-01c7-477c-8490-04cb0e826e61",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "create",
                    "sourceTimestamp" : 1611227656423,
                    "properties": {
                      "cm:autoVersion": {"value": true},
                      "createdAt": {"value": 1611227655695},
                      "modifiedAt": {"value" : 1611227655695},
                      "cm:versionType": {"value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable"]},
                      "cm:name": {
                        "value": "purchase-order-scan.bmp",
                        "annotation" : "name"
                      },
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "admin"},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "size": 531152,
                            "name": "purchase-order-scan.bmp",
                            "content-type": "%s"
                          }
                        }
                      },
                      "ALLOW_ACCESS": {"value": ["GROUP_EVERYONE"]},
                      "DENY_ACCESS": {"value": []}
                    }
                  }
                ]""".formatted(sourceMimeType);
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-01c7-477c-8490-04cb0e826e61",
                    "targetMediaType": "%s",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-01c7-477c-8490-04cb0e826e61\\",\\"targetMimeType\\":\\"%s\\",\\"retryAttempt\\":0,\\"timestamp\\":1611227656423}",
                    "transformOptions": {
                        "timeout":"20000",
                        "resizeWidth": "3840",
                        "resizeHeight": "3840",
                        "allowEnlargement": "false"
                    },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER, expectedTargetMimeType, expectedTargetMimeType);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }

    @ParameterizedTest
    @CsvSource({"application/msword,application/pdf", "application/pdf,application/pdf", "text/plain,application/pdf",
            "text/html,application/pdf"
    })
    void givenMappingForNonImage_whenContentWithMatchingTypeIngested_thenProcessWithTransformRequest(
            String sourceMimeType, String expectedTargetMimeType)
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-eaeq-4b8b-8eab-e050253d7f61",
                  "source": "/08d9b620-14de-4247-8f33-360988d3b191",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9f2e-400d-9d80-8f840e223581",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-01c7-477c-8490-04cb0e826e61",
                      "primaryHierarchy": [ "5f355d16-f824-4173-bf4b-b1ec37ef5549", "93f7edf5-e4d8-4749-9b4c-e45097e2e19d" ],
                      "name": "purchase-order-scan.bmp",
                      "nodeType": "cm:content",
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
                      "content": {
                        "mimeType": "%s",
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
                    "resourceDeniedAuthorities": []
                  }
                }""".formatted(sourceMimeType);
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId": "d71dd823-01c7-477c-8490-04cb0e826e61",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType": "create",
                    "sourceTimestamp" : 1611227656423,
                    "properties": {
                      "cm:autoVersion": {"value": true},
                      "createdAt": {"value": 1611227655695},
                      "modifiedAt": {"value": 1611227655695},
                      "cm:versionType": {"value": "MAJOR"},
                      "aspectsNames": {"value": ["cm:versionable", "cm:auditable"]},
                      "cm:name": {
                        "value": "purchase-order-scan.bmp",
                        "annotation" : "name"
                      },
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "admin"},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "size": 531152,
                            "name": "purchase-order-scan.bmp",
                            "content-type": "%s"
                          }
                        }
                      },
                      "ALLOW_ACCESS": {"value": ["GROUP_EVERYONE"]},
                      "DENY_ACCESS": {"value": []}
                    }
                  }
                ]""".formatted(sourceMimeType);
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/d71dd823-01c7-477c-8490-04cb0e826e61",
                    "targetMediaType": "%s",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-01c7-477c-8490-04cb0e826e61\\",\\"targetMimeType\\":\\"%s\\",\\"retryAttempt\\":0,\\"timestamp\\":1611227656423}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER, expectedTargetMimeType, expectedTargetMimeType);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
}
