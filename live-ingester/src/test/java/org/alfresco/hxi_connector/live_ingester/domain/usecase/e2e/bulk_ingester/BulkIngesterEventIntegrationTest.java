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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.bulk_ingester;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;
import org.junit.jupiter.api.Test;

import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class BulkIngesterEventIntegrationTest extends E2ETestBase
{
    @Test
    void shouldIngestOnlyMetadataIfThereIsNoContent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "nodeId": "5018ff83-ec45-4a11-95c4-681761752aa7",
                  "type": "cm:category",
                  "createdAt": 1707153552,
                  "creatorId": "System",
                  "modifierId": "admin",
                  "aspectNames": [
                    "cm:auditable"
                  ],
                  "contentInfo": null,
                  "customProperties": {
                    "cm:name": "Mexican Spanish"
                  }
                }""";
        containerSupport.raiseBulkIngesterEvent(repoEvent);

        // then
        String expectedBody = """
                {
                   "objectId" : "5018ff83-ec45-4a11-95c4-681761752aa7",
                   "eventType" : "create",
                   "properties" : {
                      "type": "cm:category",
                      "createdAt": 1707153552,
                      "createdByUserWithId" : "System",
                      "modifiedByUserWithId" : "admin",
                      "aspectsNames" : [ "cm:auditable" ],
                      "cm:name" : "Mexican Spanish"
                    }
                 }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void shouldIngestMetadataAndContent()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "nodeId": "37be157c-741c-4e51-b781-20d36e4e335a",
                  "type": "cm:content",
                  "creatorId": "admin",
                  "modifierId": "hr_user",
                  "aspectNames": [
                    "cm:indexControl",
                    "cm:auditable"
                  ],
                  "contentInfo": {
                    "contentSize": 330,
                    "encoding": "ISO-8859-1",
                    "mimetype": "text/xml"
                  },
                  "createdAt": 1308061016,
                  "customProperties": {
                    "cm:name": "dashboard.xml",
                    "cm:isContentIndexed": true,
                    "cm:isIndexed": false
                  }
                }""";
        containerSupport.raiseBulkIngesterEvent(repoEvent);

        // then
        String expectedBody = """
                {
                   "objectId" : "37be157c-741c-4e51-b781-20d36e4e335a",
                   "eventType" : "create",
                   "properties" : {
                      "type": "cm:content",
                      "createdByUserWithId" : "admin",
                      "modifiedByUserWithId" : "hr_user",
                      "aspectsNames" : [ "cm:indexControl", "cm:auditable" ],
                      "createdAt": 1308061016,
                      "cm:name" : "dashboard.xml",
                      "cm:isContentIndexed": true,
                      "cm:isIndexed": false
                    }
                 }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);


        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/37be157c-741c-4e51-b781-20d36e4e335a",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"37be157c-741c-4e51-b781-20d36e4e335a\\"}",
                    "transformOptions": { "timeout":"20000" },
                    "replyQueue": "org.alfresco.hxinsight-connector.transform.response"
                }""".formatted(REQUEST_ID_PLACEHOLDER);
        containerSupport.verifyATSRequestReceived(expectedATSRequest);

    }
}
