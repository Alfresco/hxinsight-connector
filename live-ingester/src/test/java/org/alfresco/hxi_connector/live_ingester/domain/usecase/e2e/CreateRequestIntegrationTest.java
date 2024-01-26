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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.ContainerSupport;
import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CreateRequestIntegrationTest extends E2ETestBase
{
    ContainerSupport containerSupport;

    @BeforeEach
    public void setUp()
    {
        containerSupport = super.configureContainers();
    }

    @Test
    void testCreateRequest()
    {
        // given
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String repoEvent = """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "368818d9-dddd-4b8b-8eab-e050253d7f61",
                  "source": "/08d9b620-48de-4247-8f33-360988d3b19b",
                  "time": "2021-01-21T11:14:16.42372Z",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "4004ca99-9d2a-400d-9d80-8f840e223581",
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
                        "id": "admin",
                        "displayName": "Administrator"
                      },
                      "modifiedAt": "2021-01-21T11:14:15.695Z",
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
                    "resourceDeniedAuthorities": []
                  }
                }""";
        containerSupport.raiseRepoEvent(repoEvent);

        // then
        String expectedBody = """
                {
                   "objectId" : "d71dd823-82c7-477c-8490-04cb0e826e65",
                   "eventType" : "create",
                   "properties" : {
                      "cm:autoVersion" : true,
                      "createdAt" : 1611227655695,
                      "isFolder" : false,
                      "cm:versionType" : "MAJOR",
                      "isFile" : true,
                      "aspectsNames" : [ "cm:versionable", "cm:auditable" ],
                      "name" : "purchase-order-scan.pdf",
                      "primaryAssocQName" : null,
                      "type" : "cm:content",
                      "createdByUserWithId" : "admin",
                      "modifiedByUserWithId" : "admin"
                    }
                 }""";
        containerSupport.expectHxIngestMessageReceived(expectedBody);
    }
}
