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
 * Malformed JSON published directly to the repo events topic. Asserts the route survives, the message lands on the DLQ exactly once (no retry storm), and a sentinel still reaches HX Insight.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class RepoEventPoisonPillReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    /** Deliberately not valid JSON — Camel-Jackson throws while unmarshalling. */
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
