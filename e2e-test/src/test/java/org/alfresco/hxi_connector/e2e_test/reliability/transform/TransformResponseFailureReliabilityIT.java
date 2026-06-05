/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.e2e_test.reliability.transform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Default behaviour for a {@code status=400} transform-response: the route throws {@code FailedTransformResponseException}, the DLC catches it, the failed message lands on the DLQ with a {@code live_ingester_transform_response_dlq_total} counter increment. Sister class {@link TransformResponseFailureOptOutReliabilityIT} covers the legacy WARN-log + silent-ACK shape.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class TransformResponseFailureReliabilityIT extends BaseReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDeadLetterTransformResponseWith400Status() throws IOException
    {
        TransformResponseFailureTrigger.SyntheticFailure failure = TransformResponseFailureTrigger.inject(environment());

        RetryUtils.assertWithRetry(() -> {
            assertThat(environment().jolokia().dlqDepth())
                    .as("default deployment: a 400 transform-response must surface on the DLQ via the route's deadLetterChannel exactly once — env caps redeliveries at 1, so any other count means the DLC fired twice or did not fire at all")
                    .isEqualTo(1);
            assertThat(environment().jolokia().browseDlq())
                    .as("the dead-lettered message's OriginalDestination must point back to the transform-response queue we published to — pinning by source rules out a false positive where some unrelated route dead-lettered something. (The JMX browse() envelope carries JMS metadata only, not the payload — see JolokiaProbe.browseDlq() for the reasoning and operator-side body-inspection paths.)")
                    .singleElement()
                    .satisfies(message -> assertThat(message.envelopeContains(TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE))
                            .as("DLQ envelope should record OriginalDestination=queue://%s but was: %s", TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE, message.envelope())
                            .isTrue());
            assertThat(WiremockCounts.ingestionEventsFor(failure.sentinelNode().id()))
                    .as("liveness: sentinel must flow past the dead-lettered failure")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(failure.droppedNodeRef()))
                    .as("no rendition path for the failed transform-response — neither a presigned-URL nor an ingestion-event POST should fire for the dropped clientData.nodeRef")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }
}
