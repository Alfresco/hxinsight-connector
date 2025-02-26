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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.bulk_ingester;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SpringBootTest(properties = {"alfresco.transform.mime-type.mapping.[text/*]=application/pdf",
        "logging.level.org.alfresco=DEBUG"})
public class BulkIngesterEventMatchingContentMappingIntegrationTest extends E2ETestBase
{
    private static final long TIMESTAMP = 1_308_061_016L;

    @ParameterizedTest
    @CsvSource({
            "image/gif,image/png", "image/bmp,image/png", "image/png,image/png", "image/raw,image/png",
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
                  "nodeId": "37be157c-741c-4e51-b781-20d36e4e335a",
                  "timestamp": %s,
                  "contentInfo": {
                    "contentSize": 330,
                    "encoding": "ISO-8859-1",
                    "mimetype": "%s"
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
                }""".formatted(TIMESTAMP, sourceMimeType);
        containerSupport.raiseBulkIngesterEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId" : "37be157c-741c-4e51-b781-20d36e4e335a",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType" : "createOrUpdate",
                    "sourceTimestamp": %s,
                    "properties" : {
                      "type": {"value": "cm:content", "annotation": "type"},
                      "createdBy": {"value": "admin", "annotation": "createdBy"},
                      "modifiedBy": {"value": "hr_user", "annotation": "modifiedBy"},
                      "aspectsNames": {"value": ["cm:indexControl", "cm:auditable"], "annotation": "aspects"},
                      "createdAt": {"value": 1308061016, "annotation": "dateCreated"},
                      "cm:name": {
                        "value": "dashboard.xml",
                        "annotation" : "name"
                      },
                      "cm:isContentIndexed": {"type": "boolean", "value": true},
                      "cm:isIndexed": {"type": "boolean", "value": false},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "dashboard.xml",
                            "size": 330,
                            "content-type": "%s"
                          }
                        }
                      }
                    }
                  }
                ]""".formatted(TIMESTAMP, sourceMimeType);
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/37be157c-741c-4e51-b781-20d36e4e335a",
                    "targetMediaType": "%s",
                    "clientData": "{\\"nodeRef\\":\\"37be157c-741c-4e51-b781-20d36e4e335a\\",\\"targetMimeType\\":\\"%s\\",\\"retryAttempt\\":0,\\"timestamp\\":%s}",
                    "transformOptions": {
                        "timeout": "20000",
                        "resizeWidth": "3840",
                        "resizeHeight": "3840",
                        "allowEnlargement": "false"
                    },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER, expectedTargetMimeType, expectedTargetMimeType, TIMESTAMP);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }

    @ParameterizedTest
    @CsvSource({
            "application/msword,application/pdf", "application/pdf,application/pdf",
            "text/plain,application/pdf", "text/html,application/pdf"
    })
    void givenMappingForNonImage_whenContentWithMatchingTypeIngested_thenProcessWithTransformRequest(
            String sourceMimeType, String expectedTargetMimeType)
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "nodeId": "37be157c-741c-4e51-b781-20d36e4e335a",
                  "timestamp": %s,
                  "contentInfo": {
                    "contentSize": 330,
                    "encoding": "ISO-8859-1",
                    "mimetype": "%s"
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
                }""".formatted(TIMESTAMP, sourceMimeType);
        containerSupport.raiseBulkIngesterEvent(repoEvent);

        // then
        String expectedBody = """
                [
                  {
                    "objectId" : "37be157c-741c-4e51-b781-20d36e4e335a",
                    "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                    "eventType" : "createOrUpdate",
                    "sourceTimestamp": %s,
                    "properties" : {
                      "type": {"value": "cm:content", "annotation": "type"},
                      "createdBy": {"value": "admin", "annotation": "createdBy"},
                      "modifiedBy": {"value": "hr_user", "annotation": "modifiedBy"},
                      "aspectsNames": {"value": ["cm:indexControl", "cm:auditable"], "annotation": "aspects"},
                      "createdAt": {"value": 1308061016, "annotation": "dateCreated"},
                      "cm:name": {
                        "value": "dashboard.xml",
                        "annotation" : "name"
                      },
                      "cm:isContentIndexed": {"type": "boolean", "value": true},
                      "cm:isIndexed": {"type": "boolean", "value": false},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "dashboard.xml",
                            "size": 330,
                            "content-type": "%s"
                          }
                        }
                      }
                    }
                  }
                ]""".formatted(TIMESTAMP, sourceMimeType);
        containerSupport.expectHxIngestMessageReceived(expectedBody);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/37be157c-741c-4e51-b781-20d36e4e335a",
                    "targetMediaType": "%s",
                    "clientData": "{\\"nodeRef\\":\\"37be157c-741c-4e51-b781-20d36e4e335a\\",\\"targetMimeType\\":\\"%s\\",\\"retryAttempt\\":0,\\"timestamp\\":%s}",
                    "transformOptions": {
                        "timeout": "20000"
                    },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER, expectedTargetMimeType, expectedTargetMimeType, TIMESTAMP);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);
    }
}
