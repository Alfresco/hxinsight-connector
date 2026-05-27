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
package org.alfresco.hxi_connector.e2e_test.reliability.poison_pill;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Bulk-ingester ingress route DLC. An unprocessable message must be bounded-redelivered, parked on the DLQ exactly once, and the route must stay alive.
 *
 * <p>
 * Malformed JSON is a test-only stand-in for any uncaught exception in the route — the realistic real-world trigger is a downstream failure, not poison-pill input.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class BulkIngesterDeadLetterReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String BULK_INGESTER_QUEUE = "bulk-ingester-events";
    /** Deliberately invalid JSON; the route's unmarshaller throws and the DLC must park the payload. */
    private static final String UNPROCESSABLE_PAYLOAD = "{ this is not valid JSON :: stand-in for any route exception, from BulkIngesterDeadLetterReliabilityIT";
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDeadLetterUnprocessableBulkEventAndKeepRouteAlive() throws IOException, InterruptedException
    {
        log.info("[reliability] Publishing unprocessable payload to queue {} via direct broker connection", BULK_INGESTER_QUEUE);
        DirectQueuePublisher.publishTextMessage(
                environment().activemqDirectBrokerUrl(),
                BULK_INGESTER_QUEUE,
                UNPROCESSABLE_PAYLOAD);

        @Cleanup
        InputStream sentinel = new ByteArrayInputStream("post-unprocessable-sentinel".getBytes());
        Node sentinelNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-unprocessable-sentinel.txt", sentinel, "text/plain");
        log.info("[reliability] Sentinel node {} published — waiting for liveness signal at HX Insight", sentinelNode.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(sentinelNode.id()))
                    .as("liveness: sentinel event published after the unprocessable message must reach HX Insight — failure here means the unprocessable message stopped the route")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: unprocessable message must land on ActiveMQ.DLQ — a zero here means the route ACK'd it and the broker dropped it on the floor")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no retry storm: unprocessable message should be parked exactly once, not redelivered")
                    .isEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
