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
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Verifies the bulk-ingester ingress route's explicit dead-letter policy: an unprocessable message must be retried with bounded redelivery, parked on {@code ActiveMQ.DLQ}, counted in the route's Micrometer counter, and the route must remain alive afterwards.
 *
 * <p>
 * The synthetic malformed payload published by this test is a <b>test-only stand-in for any uncaught exception in real traffic</b>. The realistic real-world trigger for this route's dead-letter pathway is a transient downstream failure (HX Insight 5xx, network blip), not external poison-pill input — the producer for the {@code bulk-ingester-events} queue is the bulk-ingester service shipped from this repository, so a malformed payload would in practice mean a producer/consumer schema skew of our own making. Provoking the exception path with malformed JSON is simply the cheapest way to exercise the {@code DeadLetterChannel} configuration end-to-end.
 *
 * <p>
 * Bypasses Toxiproxy by publishing the synthetic message directly to the broker's host-mapped OpenWire port (via {@link DirectQueuePublisher}) so the test exercises the route's exception handler, not its reconnect logic.
 *
 * <p>
 * Asserts the three reliability invariants we expect a robust route to honour:
 * <ol>
 * <li><b>Liveness</b> — a valid sentinel event published <i>after</i> the unprocessable message (via the normal ACS create path) still reaches HX Insight, proving the route did not stop after the failure. The sentinel travels through the repo-events route rather than the bulk-ingester route because the reliability environment does not include a running bulk-ingester service; ACS create still proves the live-ingester JVM and broker connection are healthy.</li>
 * <li><b>Bounded redelivery exhausts to DLQ</b> — the malformed message must end up on {@code ActiveMQ.DLQ}, i.e. {@code dlqDepth() >= 1}. A {@code 0} here means the broker ACK'd the message without anyone processing it, so it vanished.</li>
 * <li><b>No retry storm</b> — DLQ depth equals exactly {@code 1}, not {@code N}. A larger value would mean the route is retrying the same payload tens of times before parking it.</li>
 * </ol>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=BulkIngesterDeadLetterReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class BulkIngesterDeadLetterReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String BULK_INGESTER_QUEUE = "bulk-ingester-events";
    /**
     * Not valid JSON, deliberately. The Camel route's body deserialization throws while unmarshalling, and the route's error handler must dead-letter the original payload. See class javadoc for why "malformed input" is a synthetic test vehicle here rather than a realistic threat model.
     */
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
