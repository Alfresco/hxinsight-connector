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
package org.alfresco.hxi_connector.e2e_test.reliability.hxi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import eu.rekawek.toxiproxy.Proxy;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ToxicPlanner;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ToxicPlans;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Composed-chaos guard: slow {@code /ingestion-events} ({@value #HXI_DELAY_MS} ms, above the 3 s response timeout) overlapping with continuous {@code toxic-activemq} flap. Pins that Spring Retry on the publish path and the JMS-side dead-letter channel compose correctly when both axes degrade together.
 *
 * <p>
 * Each axis is covered alone by {@link IngestionEventTimeoutReliabilityIT} (slow HXI exhausts to DLQ) and {@link org.alfresco.hxi_connector.e2e_test.reliability.active_mq.ActiveMqFlappingReliabilityIT ActiveMqFlappingReliabilityIT} (flap preserves liveness + subscription). Production sees both together; this test guards the interaction.
 *
 * <p>
 * Asserts after the storm clears:
 * <ul>
 * <li>Liveness sentinel reaches HX Insight (route not wedged).</li>
 * <li>Each chaos node produces ≥ 1 {@code POST /ingestion-events} for its objectId — the strong "no silent loss" check (catches even a single dropped event, which a coarse total-POSTs lower bound cannot).</li>
 * <li>Total {@code POST /ingestion-events} ≤ {@value #UPPER_BOUND_TOTAL_POSTS} (no infinite-retry loop).</li>
 * <li>{@code dlqDepth() ≤ CHAOS_NODE_COUNT} — one DLQ entry per submitted JMS message is the absolute ceiling; anything higher is a redelivery storm or an exception-classifier regression.</li>
 * <li>Topic subscriber count = 1 (subscription neither lost nor leaked).</li>
 * <li>Broker healthy.</li>
 * </ul>
 *
 * <p>
 * The DLQ is purged between tests by {@link BaseReliabilityIT#resetBetweenTests()} so any DLQ traffic produced here does not leak into the next test.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=HxiSlowDuringAmqFlapReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class HxiSlowDuringAmqFlapReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    /**
     * Chaos batch size. Each create produces 2 events (Created + Updated), so 20 messages flow through the broker during chaos.
     */
    private static final int CHAOS_NODE_COUNT = 10;
    /**
     * Wiremock fixed-delay (ms). Above the connector's 3 s {@code responseTimeoutMs} so every attempt during chaos trips the HTTP timeout.
     */
    private static final int HXI_DELAY_MS = 5_000;
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /**
     * Chaos window length. One full Spring-Retry-then-DLC-kicks-in cycle takes ~6.4 s (2 attempts × 3 s timeout + 200 ms backoff + 200 ms redelivery delay), so {@value #CHAOS_DURATION_MS} ms gives time for at least one such cycle to complete <i>inside</i> the chaos window.
     */
    private static final long CHAOS_DURATION_MS = 8_000L;
    private static final long SETTLE_AFTER_CHAOS_MS = 3_000L;

    private static final int CONVERGENCE_TOTAL_MS = 15_000;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    /**
     * Upper bound on total {@code POST /ingestion-events}. By design the connector emits 2 POSTs per repo event (one metadata-only via {@code handleMetadataPropertiesChange}, one with content envelope via {@code handleContentChange}); under composed chaos each can also be retried (Spring Retry × JMS redelivery × broker-flap-induced redeliveries). Worst-case math: {@value #CHAOS_NODE_COUNT} chaos creates × 2 POSTs/event × at-most-4 retry attempts × ~2 flap-induced redeliveries + 2 baseline + 2 liveness POSTs ≈ {@value #UPPER_BOUND_TOTAL_POSTS}. Above this is a runaway-retry regression, not a precision count.
     */
    private static final int UPPER_BOUND_TOTAL_POSTS = 200;

    /**
     * Wall-clock timeline (relative to the start of the test method, approximate):
     *
     * <pre>
     *   t = 0.0 s     Pre-chaos baseline sentinel created.
     *   t ≈ 0.5 s     Baseline POST observed at WireMock — happy-path harness check passes.
     *
     *   t ≈ 0.5 s     Slow stub installed: POST /ingestion-events now held {@value #HXI_DELAY_MS} ms.
     *                 ToxicPlanner started: continuous random disable / enable on toxic-activemq.
     *
     *   t ≈ 0.5–3.5 s {@value #CHAOS_NODE_COUNT} chaos node creates submitted via RepositoryClient
     *                 (~300 ms per create through ACS REST).
     *
     *   t ≈ 3.5–8.5 s {@value #CHAOS_DURATION_MS} ms chaos window — flap interleaves with
     *                 in-flight publish retries; each connector attempt either trips the
     *                 3 s response timeout or is killed mid-flight by a broker disconnect.
     *                 Window is sized to exceed one full Spring-Retry exhaustion cycle
     *                 (~6.4 s) so the JMS-side DLC actually engages before chaos clears.
     *
     *   t ≈ 8.5 s     Planner stopped; slow stub removed; HXI is fast again.
     *   t ≈ 8.5–11.5 s {@value #SETTLE_AFTER_CHAOS_MS} ms settle — connector finishes its
     *                 last reconnect; pending Spring Retry attempts succeed; JMS
     *                 redeliveries of unacked messages drain through the now-fast HXI.
     *
     *   t ≈ 11.5 s    Post-chaos liveness sentinel created.
     *   t ≈ 11.5 s + ≤ {@value #CONVERGENCE_TOTAL_MS} ms
     *                 Convergence loop ({@value #CONVERGENCE_DELAY_MS} ms step) — waits
     *                 for the chaos batch redeliveries + liveness sentinel to all settle.
     *
     *   Total in-test work ≈ 18 s; remaining wall-time is shared-env reset + per-test reset.
     * </pre>
     */
    @Test
    void shouldComposeRetryBudgetsAndPreserveLivenessUnderHxiSlowAndAmqFlap() throws IOException, InterruptedException
    {
        Node baseline = createSentinel("composed-baseline.txt", "baseline".getBytes());
        log.info("[reliability] Pre-chaos baseline {} — waiting for one ingestion-event POST", baseline.id());
        RetryUtils.assertWithRetry(
                () -> assertThat(WiremockCounts.ingestionEventsFor(baseline.id())).isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);

        StubMapping slowStub = installSlowIngestionStub();
        log.info("[reliability] Slow stub installed: POST /ingestion-events held for {} ms", HXI_DELAY_MS);

        ToxicPlanner<Proxy> amqPlanner = new ToxicPlanner<>(
                environment().activemqProxy(),
                ToxicPlans.disableAndEnableProxyContinuously());
        log.info("[reliability] Starting toxic-activemq flap planner for ~{} ms", CHAOS_DURATION_MS);
        amqPlanner.start();

        List<Node> chaosNodes = new ArrayList<>(CHAOS_NODE_COUNT);
        try
        {
            log.info("[reliability] Submitting {} chaos events under composed slow-HXI + AMQ-flap chaos", CHAOS_NODE_COUNT);
            for (int i = 0; i < CHAOS_NODE_COUNT; i++)
            {
                Node chaosNode = createSentinel("composed-chaos-" + i + ".txt", ("chaos-" + i).getBytes());
                chaosNodes.add(chaosNode);
                log.info("[reliability] Chaos event {} / {}: node id {}", i + 1, CHAOS_NODE_COUNT, chaosNode.id());
            }
            Thread.sleep(CHAOS_DURATION_MS);
        }
        finally
        {
            log.info("[reliability] Stopping AMQ flap planner");
            amqPlanner.stop();
            // Belt-and-braces: the planner's own finally re-enables the proxy, but a forced shutdown can skip it.
            if (!environment().activemqProxy().isEnabled())
            {
                environment().activemqProxy().enable();
            }
            removeStub(slowStub);
            log.info("[reliability] Slow stub removed; settling {} ms before liveness sentinel", SETTLE_AFTER_CHAOS_MS);
            Thread.sleep(SETTLE_AFTER_CHAOS_MS);
        }

        Node liveness = createSentinel("composed-liveness.txt", "liveness".getBytes());
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
                        .as("chaos node %s must produce ≥ 1 POST /ingestion-events — zero means the connector silently dropped it (broker lost the message during a disconnect, or a filter regression discarded it)",
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
                    .as("subscriber count on %s must be 1 — zero = subscription lost; >1 = leaked across reconnects",
                            REPO_EVENT_TOPIC)
                    .isEqualTo(1);
            assertThat(environment().jolokia().brokerHealthy())
                    .as("broker must be healthy after the flap planner stops")
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

    private static StubMapping installSlowIngestionStub()
    {
        return stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(202)
                        .withFixedDelay(HXI_DELAY_MS)));
    }
}
