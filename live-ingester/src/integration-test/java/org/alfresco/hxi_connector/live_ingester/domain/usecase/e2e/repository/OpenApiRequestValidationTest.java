package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e.repository;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenApiRequestValidationTest {

    private final OpenApiInteractionValidator classUnderTest =
            OpenApiInteractionValidator.createForSpecificationUrl("http://hxai-data-platform-dev-swagger-ui.s3-website-us-east-1.amazonaws.com/docs/insight-ingestion-api-swagger.json")
                    .build();

    @Test
    void testRequestToPresignedUrls() {
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
    void testRequestToIngestionEvents() {
        Request request = SimpleRequest.Builder.post("/v1/ingestion-events")
                .withHeader("authorization", "string")
                .withHeader("content-type", "application/json")
                .withHeader("hxp-environment", "string")
                .withHeader("user-agent", "string")
                .withBody(expectedBody)
                .build();

        assertThat(classUnderTest.validateRequest(request).getMessages()).isEqualTo(expectedBody);
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
