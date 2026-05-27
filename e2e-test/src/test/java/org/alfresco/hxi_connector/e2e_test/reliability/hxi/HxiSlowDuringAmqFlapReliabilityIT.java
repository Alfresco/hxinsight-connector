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
 * Composed chaos: slow {@code /ingestion-events} (over the response timeout) plus continuous AMQ flap, at the same time. Asserts no event is silently lost, the route stays live, and the broker is healthy after the storm.
 *
 * <p>
 * Under the default-on bounded retry-ingestion ({@code attempts=6}) + per-route DLC contract, a chaos node ends in one of two terminal states: either it produces at least one {@code POST /ingestion-events} (the route processed it cleanly), or it exhausts the broker's redelivery budget during the AMQ flap (each prefetch-then-disconnect cycle counts as a redelivery — six of those land the message on {@code ActiveMQ.DLQ} without the connector ever getting a clean processing pass). The dead-letter is the structured fail-stop that replaces silent loss; the aggregate invariant therefore asserts {@code postedChaosNodes + dlqDepth >= CHAOS_NODE_COUNT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class HxiSlowDuringAmqFlapReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;

    private static final int CHAOS_NODE_COUNT = 10;
    /** Slow-stub delay above the connector's 3 s response timeout, so every attempt during chaos times out. */
    private static final int HXI_DELAY_MS = 5_000;
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /** Long enough to fit one full Spring-Retry-then-DLC cycle inside the storm. */
    private static final long CHAOS_DURATION_MS = 8_000L;
    private static final long SETTLE_AFTER_CHAOS_MS = 3_000L;

    private static final int CONVERGENCE_TOTAL_MS = 15_000;
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    /** Safety bound to catch a runaway-retry regression. */
    private static final int UPPER_BOUND_TOTAL_POSTS = 200;

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
            // Failsafe re-enable in case the planner's own finally was skipped on a forced shutdown.
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
            int dlqDepth = environment().jolokia().dlqDepth();
            int chaosNodesPosted = 0;
            for (Node chaosNode : chaosNodes)
            {
                if (WiremockCounts.ingestionEventsFor(chaosNode.id()) >= 1)
                {
                    chaosNodesPosted++;
                }
            }
            int chaosNodesUnposted = CHAOS_NODE_COUNT - chaosNodesPosted;
            log.info("[reliability] Convergence check: total POSTs={}, liveness POSTs={}, chaos posted={}/{}, chaos unposted={}, dlqDepth={}",
                    totalPosts, livenessPosts, chaosNodesPosted, CHAOS_NODE_COUNT, chaosNodesUnposted, dlqDepth);

            assertThat(livenessPosts)
                    .as("liveness sentinel %s must reach HX Insight — zero means the storm wedged the route", liveness.id())
                    .isGreaterThanOrEqualTo(1);
            // Under the default-on bounded retry-ingestion + per-route DLC contract, a chaos node has two valid terminal outcomes:
            // (a) ≥ 1 POST /ingestion-events at wiremock — the route processed the event and the broker ACKed it, OR
            // (b) the broker's prefetch-then-disconnect cycle exhausted maxRedeliveries during the AMQ flap and the message
            // landed on ActiveMQ.DLQ without the connector ever getting a clean processing pass — the dead-letter is
            // the structured fail-stop the contract guarantees in lieu of silent loss.
            // The aggregate invariant: every unposted chaos node is accounted for by a DLQ entry. We cannot attribute DLQ
            // entries to specific nodes without payload inspection (JMX browse() exposes JMS metadata only — see
            // JolokiaProbe.browseDlq()), so we require dlqDepth ≥ unposted-chaos-count. Anything less means an event was
            // truly lost — silent drop, filter regression, or a subscription leak.
            assertThat(dlqDepth)
                    .as("no-silent-loss: %d chaos node(s) produced 0 POST /ingestion-events under composed slow-HXI + AMQ-flap chaos. Under the default-on bounded retry contract these must be accounted for by ≥ %d entries on ActiveMQ.DLQ (broker-redelivery exhaustion is the contract's fail-stop). Observed dlqDepth=%d → an event was silently dropped (broker lost the message, filter regression, or subscription leak).",
                            chaosNodesUnposted, chaosNodesUnposted, dlqDepth)
                    .isGreaterThanOrEqualTo(chaosNodesUnposted);
            assertThat(totalPosts)
                    .as("total POSTs > %d — points to an infinite-retry regression", UPPER_BOUND_TOTAL_POSTS)
                    .isLessThanOrEqualTo(UPPER_BOUND_TOTAL_POSTS);
            assertThat(dlqDepth)
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
