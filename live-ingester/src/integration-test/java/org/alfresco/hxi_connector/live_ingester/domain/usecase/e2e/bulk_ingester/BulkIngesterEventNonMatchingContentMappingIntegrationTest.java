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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SpringBootTest(properties = {"logging.level.org.alfresco=DEBUG"})
public class BulkIngesterEventNonMatchingContentMappingIntegrationTest extends E2ETestBase
{
    private static final long TIMESTAMP = 1_308_061_016L;

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "text/html", "text/richtext"})
    void givenExactAndWildcardMimeTypeMappingForContentConfigured_whenContentWithNotMatchingTypeIngested_thenProcessWithoutTransformRequest(
            String sourceMimeType)
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
                    "createdAt": "2011-06-14T02:16:56.000Z",
                    "modifiedAt": "2011-06-15T02:16:56.000Z",
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
                    "sourceTimestamp" : %s,
                    "properties" : {
                      "type": {"value": "cm:content", "annotation": "type"},
                      "createdBy": {"value": "admin", "annotation": "createdBy"},
                      "modifiedBy": {"value": "hr_user", "annotation": "modifiedBy"},
                      "aspectsNames": {"value": ["cm:indexControl", "cm:auditable"], "annotation": "aspects"},
                      "createdAt": {"value": "2011-06-14T02:16:56.000Z", "type": "datetime", "annotation": "dateCreated"},
                      "modifiedAt": {"value": "2011-06-15T02:16:56.000Z", "type": "datetime", "annotation": "dateModified"},
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
        containerSupport.verifyNoATSRequestReceived(200);
    }
}
