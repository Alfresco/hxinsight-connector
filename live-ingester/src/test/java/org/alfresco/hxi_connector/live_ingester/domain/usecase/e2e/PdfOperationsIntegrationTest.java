package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;
import org.junit.jupiter.api.Test;

public class PdfOperationsIntegrationTest extends E2ETestBase {

    @Test
    void testPdFOperations() {

        //given
        containerSupport.prepareSFSToReturnSuccess();
        containerSupport.prepareHxIToReturnSuccessWithStorageLocation();
        containerSupport.prepareHxIToReturnSuccessAfterReceivingFileLocation();

        //when
        String atsBody = """
                {
                    "targetReference": "e71dd823-82c7-477c-8490-04cb0e826e66"
                }""";
        containerSupport.raiseATSEvent(atsBody);

        //then
        containerSupport.expectSFSMessageReceived();

        String preSignedUrlBody = """
                {
                   "contentType":"application/pdf",
                   "objectId":"e71dd823-82c7-477c-8490-04cb0e826e66"
                    }
                 }""";
        containerSupport.expectHxIStorageLocationMessageReceived(preSignedUrlBody);

        containerSupport.expectFileUploadedToS3("src/test/resources/test-file.pdf");

        String hxiBody = """
                {
                   "objectId" : "e71dd823-82c7-477c-8490-04cb0e826e66",
                   "eventType" : "create",
                   "properties" : {
                      "file:content": {
                        "path": "/ingestion-base-path/test-file.pdf"
                        }
                    }
                 }""";
        containerSupport.expectHxIMessageWithFileLocationReceived(hxiBody);

    }
}
