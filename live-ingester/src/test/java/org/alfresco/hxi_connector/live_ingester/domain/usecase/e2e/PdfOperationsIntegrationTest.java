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

import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;

@Slf4j
public class PdfOperationsIntegrationTest extends E2ETestBase
{

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testPdFOperations()
    {

        // given
        containerSupport.prepareSFSToReturnFile("e71dd823-82c7-477c-8490-04cb0e826e66", "test-file.pdf");
        URL url = containerSupport.prepareHxIToReturnStorageLocation();
        containerSupport.prepareHxIToReturnSuccessAfterReceivingFileLocation();

        // when
        String atsBody = """
                {
                    "targetReference": "e71dd823-82c7-477c-8490-04cb0e826e66",
                    "clientData": "{\\"nodeRef\\":\\"f71dd823-82c7-477c-8490-04cb0e826e67\\"}"
                }""";
        containerSupport.raiseTransformationCompletedATSEvent(atsBody);

        // then
        containerSupport.expectSFSMessageReceived("e71dd823-82c7-477c-8490-04cb0e826e66");

        String preSignedUrlBody = """
                {
                   "contentType":"application/pdf",
                   "objectId":"f71dd823-82c7-477c-8490-04cb0e826e67"
                    }
                 }""";
        containerSupport.expectHxIStorageLocationMessageReceived(preSignedUrlBody);

        containerSupport.expectFileUploadedToS3("test-file.pdf");

        String hxiBody = """
                {
                    "objectId" : "f71dd823-82c7-477c-8490-04cb0e826e67",
                    "eventType" : "update",
                    "properties" : {
                        "cm:content": {
                           "path": "%s"
                        }
                    }
                }""".formatted(url);
        containerSupport.expectHxIMessageWithFileLocationReceived(hxiBody);
    }
}
