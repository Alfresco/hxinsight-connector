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

import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

public class OpenApiRequestValidationTest
{

    private final OpenApiInteractionValidator classUnderTest = OpenApiInteractionValidator.createForSpecificationUrl("http://hxai-data-platform-dev-swagger-ui.s3-website-us-east-1.amazonaws.com/docs/insight-ingestion-api-swagger.json")
            .build();

    @Test
    void testRequestToPresignedUrls()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/get-presigned-url-request.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEmpty();
    }

    @Test
    void testCreateRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/create-document-request.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEmpty();
    }

    @Test
    void testUpdateRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/update-document-request.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEmpty();
    }

    @Test
    void testDeleteRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/delete-document-request.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEmpty();
    }

    private static Request makeRequest(HxInsightRequest hxInsightRequest)
    {
        SimpleRequest.Builder builder = SimpleRequest.Builder.post(hxInsightRequest.url());
        hxInsightRequest.headers().forEach(builder::withHeader);
        return builder.withBody(hxInsightRequest.body()).build();
    }
}
