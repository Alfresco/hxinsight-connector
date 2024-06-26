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
package org.alfresco.hxi_connector.prediction_applier.domain.usecase.e2e;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.prediction_applier.domain.usecase.e2e.util.PredictionApplierE2ETestBase;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class PredictionApplicationIntegrationTest extends PredictionApplierE2ETestBase
{
    private static final String NODE_ID = "nodeId";
    private static final String BATCH_ID = "batchId";
    private static final String PREDICTED_VALUE = "New value";

    @Test
    public void testPredictionApplication()
    {
        // given
        containerSupport.prepareHxInsightToReturnPredictionBatch(BATCH_ID, NODE_ID, PREDICTED_VALUE);

        // when
        triggerPredictionsCollection();

        // then
        containerSupport.expectBatchStatusWasUpdated(BATCH_ID, "IN_PROGRESS", 1);
        containerSupport.expectBatchStatusWasUpdated(BATCH_ID, "COMPLETE", 2);

        containerSupport.expectRepositoryRequestReceived(NODE_ID, PREDICTED_VALUE);
    }
}
