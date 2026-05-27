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
package org.alfresco.hxi_connector.e2e_test.reliability.active_mq;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeStub;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Broker restart recovery: graceful stop, SIGKILL, and restart mid-drain. The durable subscription replays the backlog after restart in every variant.
 *
 * <p>
 * Method order is fixed: the mid-drain test runs first because it is most sensitive to residual broker state from earlier tests in the class.
 */
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    private static final int BACKLOG_SIZE = 5;
    /** Slow stub delay on {@code /ingestion-events} during the mid-drain test. Kept below the connector's HTTP response timeout so each POST returns 202, keeping the drain in progress without piling timeouts. */
    private static final int SLOW_RESPONSE_DELAY_MS = 2_500;
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /** Retry budget for the per-event ingestion check during the mid-drain test. */
    private static final int BACKLOG_INGESTION_DELAY_MS = 30_000;
    private static final int BACKLOG_LANDED_POLL_MS = 200;
    private static final int BACKLOG_LANDED_STABLE_MS = 1_500;
    private static final long BACKLOG_LANDED_DEADLINE_MS = 30_000L;

    @Test
    @Order(2)
    void shouldRecoverConnectorAfterBrokerGracefulStopAndRestart() throws IOException
    {
        Node baseline = createNode("amq-restart-graceful-baseline.txt", "Graceful stop baseline");
        assertIngestionFor(baseline.id(), "graceful baseline");

        log.info("[chaos] graceful stop on the broker container");
        ProcessChaos.gracefulStop(environment().activemqContainer());
        ProcessChaos.awaitContainerExited(environment().activemqContainer(), STOP_DEADLINE);

        ProcessChaos.startBroker(environment());
        ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

        Node sentinel = createNode("amq-restart-graceful-sentinel.txt", "Graceful stop sentinel");
        assertIngestionFor(sentinel.id(), "graceful sentinel");
    }

    @Test
    @Order(3)
    void shouldRecoverConnectorAfterBrokerSigKillAndRestart() throws IOException
    {
        Node baseline = createNode("amq-restart-kill-baseline.txt", "SIGKILL baseline");
        assertIngestionFor(baseline.id(), "kill baseline");

        log.info("[chaos] SIGKILL on the broker container");
        ProcessChaos.sigKill(environment().activemqContainer());
        ProcessChaos.awaitContainerExited(environment().activemqContainer(), STOP_DEADLINE);

        ProcessChaos.startBroker(environment());
        ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

        Node sentinel = createNode("amq-restart-kill-sentinel.txt", "SIGKILL sentinel");
        assertIngestionFor(sentinel.id(), "kill sentinel");
    }

    @Test
    @Order(1)
    void shouldDrainBacklogAfterBrokerRestartMidStream() throws IOException
    {
        StubMapping slowStub = installSlowResponseStub();
        boolean toxiproxyDisabled = false;
        try
        {
            int enqueueBaseline = environment().jolokia().topicEnqueueCount(ReliabilityEnvironment.REPO_EVENT_TOPIC);
            log.info("[chaos] detaching connector via Toxiproxy to assemble a {}-event backlog on the durable subscription (broker enqueue baseline={})", BACKLOG_SIZE, enqueueBaseline);
            try
            {
                environment().activemqProxy().disable();
                toxiproxyDisabled = true;
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[chaos] could not disable Toxiproxy — backlog assembly cannot start", e);
            }

            List<Node> backlog = new ArrayList<>(BACKLOG_SIZE);
            for (int i = 0; i < BACKLOG_SIZE; i++)
            {
                backlog.add(createNode("amq-backlog-" + i + ".txt", "Backlog event #" + i));
            }

            awaitBacklogLandedOnBroker(enqueueBaseline);

            log.info("[chaos] re-enabling Toxiproxy — slow Wiremock responses keep the drain in progress while we restart the broker");
            try
            {
                environment().activemqProxy().enable();
                toxiproxyDisabled = false;
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[chaos] could not re-enable Toxiproxy — drain cannot start", e);
            }

            log.info("[chaos] graceful stop on the broker container while the slow drain is in progress");
            ProcessChaos.gracefulStop(environment().activemqContainer());
            ProcessChaos.awaitContainerExited(environment().activemqContainer(), STOP_DEADLINE);

            ProcessChaos.startBroker(environment());
            ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

            for (Node node : backlog)
            {
                assertBacklogIngestionFor(node.id());
            }
        }
        finally
        {
            // Any failure above must not leave Toxiproxy detached — subsequent test methods would lose the broker.
            if (toxiproxyDisabled)
            {
                try
                {
                    environment().activemqProxy().enable();
                    log.warn("[chaos] cleanup re-enabled Toxiproxy after a failure escaped the chaos block");
                }
                catch (IOException restoreFailure)
                {
                    log.error("[chaos] cleanup could not re-enable Toxiproxy after a failure — subsequent tests will fail to reach the broker", restoreFailure);
                }
            }
            removeStub(slowStub);
        }
    }

    /**
     * Wait until the broker's enqueue count has grown by at least {@link #BACKLOG_SIZE} and has been stable for {@link #BACKLOG_LANDED_STABLE_MS} ms. ACS publishes events asynchronously, so {@code createNode} returning is not proof the event reached the broker — stopping the broker before the publisher finishes flushing would lose the in-flight event.
     */
    private void awaitBacklogLandedOnBroker(int enqueueBaseline)
    {
        long deadline = System.currentTimeMillis() + BACKLOG_LANDED_DEADLINE_MS;
        int observed = enqueueBaseline;
        int stablePrev = -1;
        long stableSince = 0;
        while (System.currentTimeMillis() < deadline)
        {
            observed = environment().jolokia().topicEnqueueCount(ReliabilityEnvironment.REPO_EVENT_TOPIC);
            int delta = observed - enqueueBaseline;
            if (observed != stablePrev)
            {
                stablePrev = observed;
                stableSince = System.currentTimeMillis();
            }
            if (delta >= BACKLOG_SIZE && System.currentTimeMillis() - stableSince >= BACKLOG_LANDED_STABLE_MS)
            {
                log.info("[chaos] broker enqueue stable at {} (delta {} ≥ backlog {}, no growth for {} ms) — backlog confirmed landed in KahaDB", observed, delta, BACKLOG_SIZE, BACKLOG_LANDED_STABLE_MS);
                return;
            }
            try
            {
                Thread.sleep(BACKLOG_LANDED_POLL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("[chaos] interrupted while waiting for backlog to land on broker", e);
            }
        }
        throw new IllegalStateException("[chaos] broker enqueue did not stabilise within %d ms (baseline=%d, last observed=%d, delta=%d, expected ≥ %d) — ACS outbox flush stalled or the broker is unreachable; cannot proceed with the restart-mid-stream chaos because any event still in flight from ACS would be silently lost when the broker stops"
                .formatted(BACKLOG_LANDED_DEADLINE_MS, enqueueBaseline, observed, observed - enqueueBaseline, BACKLOG_SIZE));
    }

    private static StubMapping installSlowResponseStub()
    {
        return stubFor(post(urlEqualTo(WiremockCounts.INGESTION_EVENTS_PATH))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(202)
                        .withFixedDelay(SLOW_RESPONSE_DELAY_MS)));
    }

    private void assertBacklogIngestionFor(String nodeId)
    {
        RetryUtils.retryWithBackoff(
                () -> {
                    int count;
                    try
                    {
                        count = WiremockCounts.ingestionEventsFor(nodeId);
                    }
                    catch (Exception e)
                    {
                        // The Wiremock admin can briefly fail to respond during the post-restart drain. Surface as AssertionError so RetryUtils keeps retrying instead of aborting.
                        throw new AssertionError("[chaos] transient Wiremock admin failure while polling for backlog event %s: %s".formatted(nodeId, e.getMessage()), e);
                    }
                    assertThat(count)
                            .as("connector did not deliver backlog event for objectId=%s after broker restart — durable-subscription replay is incomplete or the route is stuck",
                                    nodeId)
                            .isGreaterThanOrEqualTo(1);
                },
                BACKLOG_INGESTION_DELAY_MS);
    }
}
