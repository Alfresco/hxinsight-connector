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

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.util.E2ETestBase;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.HxInsightRequest;
import org.alfresco.hxi_connector.live_ingester.util.insight_api.RequestLoader;

public class RenditionTransferIntegrationTest extends E2ETestBase
{

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testTransferRendition()
    {
        // given
        containerSupport.prepareSFSToReturnFile("e71dd823-82c7-477c-8490-04cb0e826e66", "test-file.pdf");
        containerSupport.prepareHxIToReturnStorageLocation("ecc22bdc-ac8e-4736-a36a-8b9ContentId");
        containerSupport.prepareHxInsightToReturnSuccess();

        // when
        String atsBody = """
                {
                    "targetReference": "e71dd823-82c7-477c-8490-04cb0e826e66",
                    "clientData": "{\\"nodeRef\\":\\"d71dd823-82c7-477c-8490-04cb0e826e65\\",\\"targetMimeType\\":\\"application/pdf\\", \\"timestamp\\": 1611656982995}"
                }""";
        containerSupport.raiseTransformationCompletedATSEvent(atsBody);

        // then
        containerSupport.expectSFSMessageReceived("e71dd823-82c7-477c-8490-04cb0e826e66");

        containerSupport.expectHxIStorageLocationMessageReceived();

        containerSupport.expectFileUploadedToS3("test-file.pdf");

        HxInsightRequest request = RequestLoader.load("/rest/hxinsight/requests/upload-references-document.yml");
        containerSupport.expectHxIngestMessageReceived(request.body());
    }
}
