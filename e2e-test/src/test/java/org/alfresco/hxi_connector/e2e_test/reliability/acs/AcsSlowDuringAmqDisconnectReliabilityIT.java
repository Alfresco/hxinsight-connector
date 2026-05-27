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
package org.alfresco.hxi_connector.e2e_test.reliability.acs;

import static eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Composed chaos: slow ACS download (over the response timeout) overlapping with a sustained AMQ disconnect that builds a backlog on the durable subscription. Asserts the ACS retry budget, JMS redelivery budget, and durable-subscription replay compose correctly when both axes degrade together.
 *
 * <p>
 * Asserts after the storm clears: liveness, no silent loss (each chaos node produces ≥ 1 POST), bounded DLQ, healthy broker, 1-subscriber durable subscription.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsSlowDuringAmqDisconnectReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;
    private static final String LATENCY_TOXIC_NAME = "acs_huge_latency";

    /** Latency above the 3 s response timeout so every content-download attempt trips it. */
    private static final int ACS_LATENCY_MS = 6_000;
    private static final int CHAOS_NODE_COUNT = 10;
    private static final long AMQ_DISCONNECT_DURATION_MS = 8_000L;
    /** ACS stays slow after AMQ recovers, so backlog drain replays through a still-slow ACS — this is the composed overlap. */
    private static final long ACS_OVERLAP_AFTER_AMQ_RECOVERY_MS = 5_000L;
    private static final long SETTLE_AFTER_CHAOS_MS = 5_000L;

    private static final int CONVERGENCE_TOTAL_MS = 30_000;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    /** Safety bound to catch a runaway-retry regression. */
    private static final int UPPER_BOUND_TOTAL_POSTS = 200;

    @Test
    void shouldComposeRetryBudgetsAndPreserveLivenessUnderAcsSlowAndAmqDisconnect() throws IOException, InterruptedException
    {
        Node baseline = createSentinel("composed-acs-amq-baseline.txt", "baseline".getBytes());
        log.info("[reliability] Pre-chaos baseline {} — waiting for one ingestion-event POST", baseline.id());
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEventsFor(baseline.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        installAcsLatencyToxic();
        log.info("[reliability] ACS latency toxic installed: {} ms downstream", ACS_LATENCY_MS);

        log.info("[reliability] Disabling Toxiproxy in front of ActiveMQ for {} ms", AMQ_DISCONNECT_DURATION_MS);
        environment().activemqProxy().disable();

        List<Node> chaosNodes = new ArrayList<>(CHAOS_NODE_COUNT);
        try
        {
            log.info("[reliability] Submitting {} chaos events while AMQ is disconnected (events queue on durable subscription)", CHAOS_NODE_COUNT);
            for (int i = 0; i < CHAOS_NODE_COUNT; i++)
            {
                Node chaosNode = createSentinel("composed-acs-amq-chaos-" + i + ".txt", ("chaos-" + i).getBytes());
                chaosNodes.add(chaosNode);
                log.info("[reliability] Chaos event {} / {}: node id {}", i + 1, CHAOS_NODE_COUNT, chaosNode.id());
            }
            Thread.sleep(AMQ_DISCONNECT_DURATION_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling Toxiproxy — backlog drain begins through still-slow ACS");
            if (!environment().activemqProxy().isEnabled())
            {
                environment().activemqProxy().enable();
            }
        }

        log.info("[reliability] Holding ACS latency for {} ms after AMQ recovery so the drain composes both chaos sources", ACS_OVERLAP_AFTER_AMQ_RECOVERY_MS);
        Thread.sleep(ACS_OVERLAP_AFTER_AMQ_RECOVERY_MS);

        removeAcsLatencyToxic();
        log.info("[reliability] ACS latency removed; settling {} ms before liveness sentinel", SETTLE_AFTER_CHAOS_MS);
        Thread.sleep(SETTLE_AFTER_CHAOS_MS);

        Node liveness = createSentinel("composed-acs-amq-liveness.txt", "liveness".getBytes());
        log.info("[reliability] Post-chaos liveness sentinel {} — waiting for ingestion-event POST", liveness.id());

        RetryUtils.assertWithRetry(() -> {
            int totalPosts = WiremockCounts.ingestionEvents();
            int livenessPosts = WiremockCounts.ingestionEventsFor(liveness.id());
            log.info("[reliability] Convergence check: total POSTs={}, liveness POSTs={}, dlqDepth={}",
                    totalPosts, livenessPosts, environment().jolokia().dlqDepth());

            assertThat(livenessPosts)
                    .as("liveness sentinel %s must reach HX Insight — zero means the storm wedged the route", liveness.id())
                    .isGreaterThanOrEqualTo(1);
            for (Node chaosNode : chaosNodes)
            {
                assertThat(WiremockCounts.ingestionEventsFor(chaosNode.id()))
                        .as("chaos node %s must produce ≥ 1 POST /ingestion-events — zero means the durable subscription dropped the message during AMQ disconnect, or the connector silently filtered it after recovery (metadata POST runs ahead of content download, so even an event whose content download exhausts retries should still produce one metadata POST)",
                                chaosNode.id())
                        .isGreaterThanOrEqualTo(1);
            }
            assertThat(totalPosts)
                    .as("total POSTs > %d — points to an infinite-retry regression", UPPER_BOUND_TOTAL_POSTS)
                    .isLessThanOrEqualTo(UPPER_BOUND_TOTAL_POSTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("DLQ depth bounded by submitted JMS messages — depth above %d means the broker is being flooded by something other than legitimate retry exhaustion (e.g. a redelivery storm or an exception-classifier regression)",
                            CHAOS_NODE_COUNT)
                    .isLessThanOrEqualTo(CHAOS_NODE_COUNT);
            assertThat(environment().jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("subscriber count on %s must be 1 — zero = subscription lost during disconnect; >1 = leaked across reconnect",
                            REPO_EVENT_TOPIC)
                    .isEqualTo(1);
            assertThat(environment().jolokia().brokerHealthy())
                    .as("broker must be healthy after the chaos clears")
                    .isTrue();
        }, CONVERGENCE_TOTAL_MS / CONVERGENCE_DELAY_MS, CONVERGENCE_DELAY_MS);
    }

    private Node createSentinel(String name, byte[] body) throws IOException
    {
        @Cleanup
        InputStream content = new ByteArrayInputStream(body);
        return environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, name, content, "text/plain");
    }

    private void installAcsLatencyToxic()
    {
        try
        {
            environment().acsProxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, ACS_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install ACS latency toxic — chaos cannot start", e);
        }
    }

    private void removeAcsLatencyToxic()
    {
        try
        {
            environment().acsProxy().toxics().get(LATENCY_TOXIC_NAME).remove();
        }
        catch (IOException e)
        {
            log.warn("[reliability] Could not remove ACS latency toxic during cleanup — BaseReliabilityIT reset will catch it next test", e);
        }
    }
}
