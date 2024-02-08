package org.alfresco.hxi_connector.live_ingester.domain.usecase.e2e;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.alfresco.hxi_connector.live_ingester.util.ContainerSupport.REQUEST_ID_PLACEHOLDER;

public class PdfOperationsIntegrationTest extends E2ETestBase {

    @Test
    void testPdFOperations() throws IOException, InterruptedException {

        containerSupport.prepareHxInsightToReturnSuccessWithStorageLocation();
        setUpS3();
        WireMock.configureFor(sfs.getHost(), sfs.getPort());

        //given
        containerSupport.prepareSFSToReturnSuccess();

//        WireMock.configureFor(hxInsight.getHost(), hxInsight.getPort());
//        setUpS3();
//        containerSupport.prepareHxInsightToReturnSuccessWithStorageLocation();


        WireMock.configureFor(sfs.getHost(), sfs.getPort());
        //when
        String atsRequest = """
                {
                    "targetReference": "e71dd823-82c7-477c-8490-04cb0e826e66"
                }""";
        containerSupport.raiseATSEvent(atsRequest);


        //then
        containerSupport.expectSFSMessageReceived();



        WireMock.configureFor(hxInsight.getHost(), hxInsight.getPort());

//        setUpS3();
//        containerSupport.prepareHxInsightToReturnSuccessWithStorageLocation();

        String preSignedUrlBody = """
                {
                   "contentType":"application/pdf",
                   "objectId":"e71dd823-82c7-477c-8490-04cb0e826e66"
                    }
                 }""";
        containerSupport.expectHxiPreSignedUrlMessageReceived(preSignedUrlBody);


//        containerSupport.expectS3MessageReceived("test-file.pdf");





//        setUpS3();

//        containerSupport.prepareS3ToReturnSuccess();
////
//        String s3Body = """
//                {
//                   "contentType":"application/pdf",
//                   "objectId":"e71dd823-82c7-477c-8490-04cb0e826e66"
//                    }
//                 }""";
//        containerSupport.expectS3MessageReceived(s3Body);


//        String expectedBody = """
//                {
//                   "objectId" : "e71dd823-82c7-477c-8490-04cb0e826e66",
//                   "eventType" : "create",
//                   "properties" : {
//                      "file:content": {
//                        "path": "/ingestion-base-path/filename1.pdf"
//                        }
//                    }
//                 }""";
//        containerSupport.expectHxIngestMessageReceived(expectedBody);


    }

}
