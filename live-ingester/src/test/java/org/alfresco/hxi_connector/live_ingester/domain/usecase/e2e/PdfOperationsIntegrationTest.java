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

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

public class PdfOperationsIntegrationTest extends E2ETestBase
{

    @Test
    void testPdFOperations()
    {

        // given
        containerSupport.prepareSFSToReturnSuccess("test-file.pdf");
        containerSupport.prepareHxIToReturnSuccessWithStorageLocation();
        containerSupport.prepareHxIToReturnSuccessAfterReceivingFileLocation();

        // when
        String atsBody = """
                {
                    "targetReference": "e71dd823-82c7-477c-8490-04cb0e826e66"
                }""";
        containerSupport.raiseTransformationCompletedATSEvent(atsBody);

        // then
        containerSupport.expectSFSMessageReceived();

        String preSignedUrlBody = """
                {
                   "contentType":"application/pdf",
                   "objectId":"e71dd823-82c7-477c-8490-04cb0e826e66"
                    }
                 }""";
        containerSupport.expectHxIStorageLocationMessageReceived(preSignedUrlBody);

        containerSupport.expectFileUploadedToS3("test-file.pdf");

//        this should be uncommented after feature implementation of ACS-6381

//        String hxiBody = """
//                {
//                   "objectId" : "e71dd823-82c7-477c-8490-04cb0e826e66",
//                   "eventType" : "update",
//                   "properties" : {
//                      "file:content": {
//                        "path": "/ingestion-base-path/test-file.pdf"
//                        }
//                    }
//                 }""";
//        containerSupport.expectHxIMessageWithFileLocationReceived(hxiBody);

    }
}
