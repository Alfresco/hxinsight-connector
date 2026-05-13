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
 * Poison-pill on the repo events topic: a malformed JSON payload is published directly onto {@code alfresco.repo.event2} and we observe how the live-ingester handles it.
 *
 * <p>
 * Bypasses Toxiproxy by publishing the synthetic message directly to the broker's host-mapped OpenWire port (via {@link DirectTopicPublisher}) so the test exercises the route's exception handler, not its reconnect logic. The repository is not involved — the malformed payload would never originate from a real ACS.
 *
 * <p>
 * Asserts the three reliability invariants we expect a robust route to honour:
 * <ol>
 * <li><b>Liveness</b> — a valid sentinel event published <i>after</i> the poison pill (via the normal ACS create path) still reaches HX Insight, proving the route did not stop after the failure.</li>
 * <li><b>No silent drop</b> — the malformed message must end up on {@code ActiveMQ.DLQ}, i.e. {@code dlqDepth() >= 1}. A {@code 0} here means the broker ACK'd the message without anyone processing it, so it vanished.</li>
 * <li><b>No retry storm</b> — DLQ depth equals exactly {@code 1}, not {@code N}. A larger value would mean the route is retrying the same poisoned payload tens of times before parking it.</li>
 * </ol>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=RepoEventPoisonPillReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class RepoEventPoisonPillReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    /**
     * Not valid JSON, deliberately. The Camel-Jackson body converter throws while unmarshalling, and the route's error handler must dead-letter the original payload.
     */
    private static final String POISON_PAYLOAD = "{ this is not valid JSON :: poison-pill from RepoEventPoisonPillReliabilityIT";
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDeadLetterMalformedRepoEventWithoutSilentDrop() throws IOException, InterruptedException
    {
        log.info("[reliability] Publishing poison-pill payload to topic {} via direct broker connection", REPO_EVENT_TOPIC);
        DirectTopicPublisher.publishTextMessage(
                environment().activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                POISON_PAYLOAD);

        @Cleanup
        InputStream sentinel = new ByteArrayInputStream("post-poison-sentinel".getBytes());
        Node sentinelNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "post-poison-sentinel.txt", sentinel, "text/plain");
        log.info("[reliability] Sentinel node {} published — waiting for liveness signal at HX Insight", sentinelNode.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(sentinelNode.id()))
                    .as("liveness: sentinel event published after the poison pill must reach HX Insight — failure here means the poison pill stopped the route")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no silent drop: malformed message must land on ActiveMQ.DLQ — a zero here means the route ACK'd it and the broker dropped it on the floor")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("no retry storm: poison pill should be parked exactly once, not redelivered")
                    .isEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
