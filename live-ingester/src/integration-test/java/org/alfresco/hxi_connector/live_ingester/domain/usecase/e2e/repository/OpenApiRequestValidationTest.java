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

import static com.atlassian.oai.validator.schema.SchemaValidator.ADDITIONAL_PROPERTIES_KEY;
import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

public class OpenApiRequestValidationTest
{

    private static final String OPEN_API_SPECIFICATION_URL = "http://hxai-data-platform-dev-swagger-ui.s3-website-us-east-1.amazonaws.com/docs/insight-ingestion-api-swagger.json";
    private static OpenApiInteractionValidator openApiInteractionValidator;

    @BeforeAll
    static void setUp()
    {
        openApiInteractionValidator = OpenApiInteractionValidator
                .createForSpecificationUrl(OPEN_API_SPECIFICATION_URL)
                .withLevelResolver(LevelResolver.create().withLevel(ADDITIONAL_PROPERTIES_KEY, ValidationReport.Level.IGNORE).build())
                .build();
    }

    @Test
    void testRequestToPresignedUrls()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/rest/hxinsight/requests/get-presigned-urls.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(openApiInteractionValidator.validateRequest(request).getMessages()).isEmpty();
    }

    @SneakyThrows
    @Test
    void testUploadReferencesRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/rest/hxinsight/requests/upload-references-document.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(openApiInteractionValidator.validateRequest(request).getMessages()).isEmpty();
    }

    @SneakyThrows
    @Test
    void testCreateOrUpdateRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/rest/hxinsight/requests/create-or-update-document.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(openApiInteractionValidator.validateRequest(request).getMessages()).isEmpty();
    }

    @Test
    void testDeleteRequestToIngestionEvents()
    {
        HxInsightRequest hxInsightRequest = RequestLoader.load("/rest/hxinsight/requests/delete-document.yml");

        Request request = makeRequest(hxInsightRequest);

        assertThat(openApiInteractionValidator.validateRequest(request).getMessages()).isEmpty();
    }

    private static Request makeRequest(HxInsightRequest hxInsightRequest)
    {
        SimpleRequest.Builder builder = SimpleRequest.Builder.post(hxInsightRequest.url());
        hxInsightRequest.headers().forEach(builder::withHeader);
        return builder.withBody(hxInsightRequest.body()).build();
    }
}
