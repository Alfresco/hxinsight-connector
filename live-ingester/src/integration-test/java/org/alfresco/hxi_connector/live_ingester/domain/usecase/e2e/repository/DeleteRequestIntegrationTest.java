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

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class DeleteRequestIntegrationTest extends E2ETestBase
{
    @Test
    void testDeleteRequest()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Deleted",
                  "id": "df329995-d744-427c-bafb-4a31ba7d50e3",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-27T10:57:02.586606Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeDeleted",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "acb8e25f-a340-48b5-8de8-249ae5bac670",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "d71dd823-82c7-477c-8490-04cb0e826e65"
                    }
                  }
                }""";
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                    "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                    "eventType": "delete"
                }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }

    @Test
    void testDeleteRequestWithNoResource()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Deleted",
                  "id": "df329995-d744-427c-bafb-4a31ba7d50e3",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-27T10:57:02.586606Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeDeleted",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "acb8e25f-a340-48b5-8de8-249ae5bac670"
                  }
                }""";
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        containerSupport.expectNoHxIngestMessagesReceived();
    }
}
