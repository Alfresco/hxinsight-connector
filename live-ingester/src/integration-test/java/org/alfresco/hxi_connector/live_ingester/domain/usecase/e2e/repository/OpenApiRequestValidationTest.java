package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapi4j.operation.validator.model.Request;
import org.openapi4j.operation.validator.model.impl.Body;
import org.openapi4j.operation.validator.model.impl.DefaultRequest;
import org.openapi4j.operation.validator.validation.OperationValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.schema.validator.ValidationData;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openapi4j.operation.validator.model.Request.Method.POST;

public class OpenApiRequestValidationTest {

    private static final String SPECIFICATION_URL = "http://hxai-data-platform-dev-swagger-ui.s3-website-us-east-1.amazonaws.com/docs/insight-ingestion-api-swagger.json";
    private static URL specificationUrl;

    @BeforeAll
    public static void setUp() throws Exception {
        specificationUrl = new URL(SPECIFICATION_URL);
    }

    @Test
    public void testRequestToPresignedUrls() throws Exception {

        OperationValidator operationValidator = loadOperationValidator(specificationUrl, "presignedUrls");

        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/get-presigned-urls-request.yml");

        Request request = makeRequest(hxInsightRequest);

        checkRequest(request, operationValidator);
    }

    @Test
    public void testCreateRequestToIngestionEvents() throws Exception {

        OperationValidator operationValidator = loadOperationValidator(specificationUrl, "ingestionEvents");

        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/create-document-request.yml");

        Request request = makeRequest(hxInsightRequest);

        checkRequest(request, operationValidator);
    }

    @Test
    public void testCreateRequestToIngestionEventsWithoutSourceId() throws Exception {

        OperationValidator operationValidator = loadOperationValidator(specificationUrl, "ingestionEvents");

        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/create-document-request-without-source-id.yml");

        Request request = makeRequest(hxInsightRequest);

        checkRequest(request, operationValidator);
    }

    @Test
    public void testCreateRequestToIngestionEventsWithEmptyProperties() throws Exception {

        OperationValidator operationValidator = loadOperationValidator(specificationUrl, "ingestionEvents");

        HxInsightRequest hxInsightRequest = RequestLoader.load("/expected-hxinsight-requests/create-document-request-empty-properties.yml");

        Request request = makeRequest(hxInsightRequest);

        checkRequest(request, operationValidator);
    }

    protected OperationValidator loadOperationValidator(URL specification, String operationId) throws Exception {
        OpenApi3 openApi = new OpenApi3Parser().parse(specification, true);

        return new OperationValidator(
                openApi,
                openApi.getPathItemByOperationId(operationId),
                openApi.getOperationById(operationId));
    }

    private static Request makeRequest(HxInsightRequest hxInsightRequest) {
        DefaultRequest.Builder builder = new DefaultRequest.Builder(hxInsightRequest.url(), POST);
        hxInsightRequest.headers().forEach(builder::header);
        if (hxInsightRequest.body() != null) {
            builder.body(Body.from(hxInsightRequest.body()));
        }
        return builder.build();
    }

    protected void checkRequest(Request sentRequest, OperationValidator operationValidator) {
        ValidationData<Void> validation = new ValidationData<>();
        operationValidator.validateBody(sentRequest, validation);
        operationValidator.validateHeaders(sentRequest, validation);

        assertTrue(validation.isValid(), validation.results().toString());
        }
    }
