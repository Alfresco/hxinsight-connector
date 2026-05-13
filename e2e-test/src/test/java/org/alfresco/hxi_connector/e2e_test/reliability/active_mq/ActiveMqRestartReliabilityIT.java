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
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins broker-side restart recovery: graceful stop, abrupt SIGKILL, and restart while a backlog is still draining. In every variant the broker's KahaDB journal preserves the durable subscription across the cycle and the connector's Spring-JMS reconnect picks up where it left off.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    /**
     * Backlog size for the mid-drain test. Small enough to keep wall-time bounded; the slow Wiremock stub stretches per-event drain time so the chaos call provably lands during the drain rather than after it.
     */
    private static final int BACKLOG_SIZE = 5;
    /**
     * Wiremock fixed-delay applied to {@code /ingestion-events} during the mid-drain test. Sized below the connector's HTTP response timeout (configured by the test profile via {@code HYLANDEXPERIENCE_INGESTER_RESPONSETIMEOUTMS=3000}) so each POST returns a successful {@code 202} after the delay rather than getting torn down by a client-side timeout — the drain stays "in progress" via slow successful responses rather than via repeated timeout-and-retry, which would pile in-flight requests on Wiremock's Jetty thread pool and starve the test's own admin queries (observed concretely as a {@code NoHttpResponseException} bubbling out of {@link WiremockCounts#ingestionEventsFor(String)}).
     */
    private static final int SLOW_RESPONSE_DELAY_MS = 2_500;
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /**
     * Convergence step for the mid-drain test's per-event ingestion check. Must comfortably exceed {@link #SLOW_RESPONSE_DELAY_MS} times the worst-case in-flight queue length so a single retry attempt covers the whole drain.
     */
    private static final int BACKLOG_INGESTION_DELAY_MS = 30_000;

    @Test
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
    void shouldDrainBacklogAfterBrokerRestartMidStream() throws IOException
    {
        StubMapping slowStub = installSlowResponseStub();
        try
        {
            log.info("[chaos] detaching connector via Toxiproxy to assemble a {}-event backlog on the durable subscription", BACKLOG_SIZE);
            try
            {
                environment().activemqProxy().disable();
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

            log.info("[chaos] re-enabling Toxiproxy — slow Wiremock responses keep the drain in progress while we restart the broker");
            try
            {
                environment().activemqProxy().enable();
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
            removeStub(slowStub);
        }
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
                    catch (RuntimeException e)
                    {
                        // Wiremock admin can briefly fail to respond while the connector hammers it with the post-restart drain; surface as an AssertionError so RetryUtils retries instead of aborting the test on the first transient infra glitch.
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
