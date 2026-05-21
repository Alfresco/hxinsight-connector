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
 * Composed-chaos guard: slow ACS download path ({@value #ACS_LATENCY_MS} ms, above the 3 s response timeout) overlapping with a sustained {@code toxic-activemq} disconnect that builds a backlog on the durable subscription. Pins that the ACS-side {@code @Retryable} budget, the JMS-level redelivery budget, and the durable-subscription replay machinery compose correctly when both axes degrade together.
 *
 * <p>
 * Each axis is covered alone by {@link AcsLatencyReliabilityIT} (slow ACS exhausts to DLQ) and {@link org.alfresco.hxi_connector.e2e_test.reliability.active_mq.ActiveMqPartitionReliabilityIT ActiveMqPartitionReliabilityIT} (long AMQ partition preserves the backlog and drains it cleanly). Production sees both together; this test guards the interaction.
 *
 * <p>
 * Asserts after the storm clears:
 * <ul>
 * <li>Liveness sentinel reaches HX Insight (route not wedged).</li>
 * <li>Each chaos node produces ≥ 1 {@code POST /ingestion-events} for its objectId — the strong "no silent loss" check. The metadata POST runs ahead of the content download (see {@code EventProcessor.process}: {@code handleMetadataPropertiesChange} then {@code handleContentChange}), so a chaos event whose content download exhausts retries still surfaces as ≥ 1 metadata POST. Zero here means the durable subscription dropped the message during the AMQ disconnect, or the connector silently filtered it after recovery.</li>
 * <li>Total {@code POST /ingestion-events} ≤ {@value #UPPER_BOUND_TOTAL_POSTS} (no infinite-retry loop).</li>
 * <li>{@code dlqDepth() ≤ CHAOS_NODE_COUNT} — under composed chaos some chaos events legitimately exhaust their content-download retry budget before ACS recovers; one DLQ entry per submitted JMS message is the absolute ceiling. Anything higher is a redelivery storm or an exception-classifier regression.</li>
 * <li>Topic subscriber count = 1 (subscription neither lost during the disconnect nor leaked across reconnect).</li>
 * <li>Broker healthy.</li>
 * </ul>
 *
 * <p>
 * The DLQ is purged between tests by {@link BaseReliabilityIT#resetBetweenTests()} so any DLQ traffic produced here does not leak into the next test.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=AcsSlowDuringAmqDisconnectReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsSlowDuringAmqDisconnectReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;
    private static final String LATENCY_TOXIC_NAME = "acs_huge_latency";

    /**
     * Per-byte latency Toxiproxy injects on the live-ingester ↔ ACS path. Comfortably exceeds the connector's {@code RESPONSETIMEOUTMS=3000} test profile so each content-download attempt provably trips the timeout rather than completing late.
     */
    private static final int ACS_LATENCY_MS = 6_000;
    /**
     * Chaos batch size. Each create produces a repo event that lands on the durable subscription while AMQ is disconnected; events drain after AMQ recovery and hit the still-slow ACS download.
     */
    private static final int CHAOS_NODE_COUNT = 10;
    /**
     * AMQ disconnect window. Long enough to accumulate the full chaos batch on the durable subscription before the connector starts pulling.
     */
    private static final long AMQ_DISCONNECT_DURATION_MS = 8_000L;
    /**
     * How long ACS stays slow after AMQ recovers. This is the composed-chaos overlap: backlog drain happens through a still-slow ACS, so each replayed event hits the in-delivery retry path.
     */
    private static final long ACS_OVERLAP_AFTER_AMQ_RECOVERY_MS = 5_000L;
    /**
     * Settle window after both chaos sources clear. Sized for the longest tail of in-flight content-download retries to either succeed against the now-fast ACS or exhaust to DLQ.
     */
    private static final long SETTLE_AFTER_CHAOS_MS = 5_000L;

    private static final int CONVERGENCE_TOTAL_MS = 30_000;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    /**
     * Upper bound on total {@code POST /ingestion-events} which includes retries and redeliveries based on default connector configuration. Safety check that we don't regress to a runaway-retry state.
     */
    private static final int UPPER_BOUND_TOTAL_POSTS = 200;

    /**
     * Wall-clock timeline (relative to the start of the test method, approximate):
     *
     * <pre>
     *   t = 0.0 s     Pre-chaos baseline sentinel created; verified end-to-end before any chaos.
     *
     *   t = 1.5 s     Install ACS latency toxic ({@value #ACS_LATENCY_MS} ms downstream)
     *                 + {@code
     * activemqProxy().disable()
     * }.
     *
     *   t = 1.5..9.5 s  Submit {@value #CHAOS_NODE_COUNT} chaos creates while AMQ is disconnected;
     *                   the repository's own AMQ connection bypasses Toxiproxy and keeps publishing,
     *                   so messages queue on the connector's durable subscription.
     *
     *   t = 9.5 s     {@code
     * activemqProxy().enable()
     * }. ACS still slow. Backlog drain begins;
     *                 each replayed event hits the slow ACS download path. Metadata POSTs always
     *                 fire (ordered ahead of content download in {@code
     * EventProcessor.process
     * }).
     *
     *   t = 9.5..14.5 s   ACS slow during drain → composed chaos. Some content downloads time out,
     *                     retry, and exhaust to DLQ.
     *
     *   t = 14.5 s    Remove ACS latency toxic. Any in-flight retries from this point hit a fast ACS.
     *
     *   t = 14.5..19.5 s   Settle: any in-flight retries either succeed (content POST fires) or
     *                      exhaust their remaining budget.
     *
     *   t = 19.5 s    Liveness sentinel created.
     *
     *   t = 19.5..49.5 s   Convergence assertions over {@value #CONVERGENCE_TOTAL_MS} ms; allows the
     *                      backlog tail to drain through the now-recovered chain and the liveness
     *                      sentinel to land.
     *
     *   Total in-test work ≈ 30 s; remaining wall-time is shared-env reset + per-test reset.
     * </pre>
     */
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
