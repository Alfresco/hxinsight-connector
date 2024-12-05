/*-
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import org.junit.jupiter.api.Test;

public class OpenApiRequestValidationTest
{

    private final OpenApiInteractionValidator classUnderTest = OpenApiInteractionValidator.createForSpecificationUrl("http://hxai-data-platform-dev-swagger-ui.s3-website-us-east-1.amazonaws.com/docs/insight-ingestion-api-swagger.json")
            .build();

    @Test
    void testRequestToPresignedUrls()
    {
        Request request = SimpleRequest.Builder.post("/v1/presigned-urls")
                .withHeader("authorization", "string")
                .withHeader("content-type", "application/json")
                .withHeader("hxp-environment", "string")
                .withHeader("user-agent", "string")
                .withHeader("count", "string")
                .build();

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEqualTo(Collections.emptyList());
    }

    @Test
    void testRequestToIngestionEvents()
    {
        Request request = SimpleRequest.Builder.post("/v1/ingestion-events")
                .withHeader("authorization", "string")
                .withHeader("content-type", "application/json")
                .withHeader("hxp-environment", "string")
                .withHeader("user-agent", "string")
                .withBody(expectedBody)
                .build();

        // assertThat(classUnderTest.validateRequest(request).getMessages()).isEqualTo(expectedBody);
        assertThat(classUnderTest.validateRequest(request).getMessages()).isEqualTo(Collections.emptyList());
    }

    String expectedBody = """
            [
              {
                "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                "sourceId" : "alfresco-dummy-source-id-0a63de491876",
                "eventType": "create",
                "sourceTimestamp": 1611227656423,
                "properties": {
                  "cm:autoVersion": {"value": true},
                  "createdAt": {"value": 1611227655695},
                  "modifiedAt": {"value" : 1611227655695},
                  "cm:versionType": {"value": "MAJOR"},
                  "aspectsNames": {"value": ["cm:versionable", "cm:auditable"]},
                  "cm:name": {
                    "value": "purchase-order-scan.doc",
                    "annotation" : "name"
                  },
                  "type": {"value": "cm:content"},
                  "createdBy": {"value": "admin"},
                  "modifiedBy": {"value": "admin"},
                  "cm:content": {
                    "file": {
                      "content-metadata": {
                        "size": 531152,
                        "name": "purchase-order-scan.doc",
                        "content-type": "application/msword"
                      }
                    }
                  },
                  "ALLOW_ACCESS": {"value": ["GROUP_EVERYONE"]},
                  "DENY_ACCESS": {"value": []}
                }
              }
            ]""";

}
