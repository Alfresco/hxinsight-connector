/*-
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

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenApiTckRequestValidationTest
{

    private static final String BASE_URL = "http://localhost:4010";

    @BeforeEach
    void setUp()
    {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void testRequestToPresignedUrls()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/get-presigned-urls.yml");
        assertThat(actualStatusCode).isEqualTo(SC_OK);
    }

    @Test
    void testUploadReferencesRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/upload-references-document.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @Test
    void testCreateOrUpdateRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/create-or-update-document.yml");
        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    @Test
    void testDeleteRequestToIngestionEvents()
    {
        int actualStatusCode = validateRequest("/rest/hxinsight/requests/delete-document.yml");

        assertThat(actualStatusCode).isEqualTo(SC_ACCEPTED);
    }

    private int validateRequest(String yamlPath)
    {
        // given
        HxInsightRequest request = RequestLoader.load(yamlPath);

        // when
        RequestSpecification requestSpec = given().log().all().headers(request.headers());

        if (request.body() != null)
        {
            requestSpec.body(request.body());
        }

        Response response = requestSpec
                .when()
                .post(request.url());

        // then
        if (response.getStatusCode() != SC_OK || response.getStatusCode() != SC_ACCEPTED)
        {
            log.info("Response Status Code: " + response.getStatusCode());
            log.info("Response Headers: " + response.getHeaders().asList());
            log.info("Response Body: " + response.getBody().asPrettyString());
        }

        return response.getStatusCode();
    }
}
