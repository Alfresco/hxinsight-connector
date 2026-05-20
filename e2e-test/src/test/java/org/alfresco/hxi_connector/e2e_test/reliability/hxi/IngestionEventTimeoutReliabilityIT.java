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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
 * Verifies the live-ingester fails loud and bounded when HX Insight stops responding within {@code hyland-experience.ingester.response-timeout-ms}: the HTTP client must surface a timeout exception (not block forever), the route's error handler must catch it, JMS-level redeliveries must exhaust, and the message must land on {@code ActiveMQ.DLQ} where an operator can see it. Acts as a regression guard for the {@code httpClient.responseTimeout} configuration knob — without it the Camel HTTP component defaults to <b>infinite</b> connect/response timeouts and a slow HX Insight would block the route's worker thread until the JVM is restarted.
 *
 * <p>
 * Failure is injected at the HX Insight HTTP boundary by overriding the file-based Wiremock stub for {@code /ingestion-events} with a higher-priority stub that holds the response for {@link #SERVER_DELAY_MS} before answering {@code 202}. With the test profile {@code responseTimeoutMs = 1_000} (see {@link ReliabilityEnvironment}) the connector aborts each attempt after roughly 1 s; the JMS-level handler then retries once and DLQs.
 *
 * <p>
 * Two assertions:
 * <ol>
 * <li><b>Timeout actually fired (and JMS retried).</b> Total POSTs to {@code /ingestion-events} for the new node must be ≥ {@link #MIN_HTTP_ATTEMPTS}. A count of 1 means the connector blocked indefinitely on the first attempt and the JMS-level retry never engaged — the original infinite-timeout regression.</li>
 * <li><b>Bounded redelivery exhausts to DLQ.</b> {@code dlqDepth() >= 1} — the timed-out message must land on {@code ActiveMQ.DLQ}. A zero here means the route silently lost the message after the timeout.</li>
 * </ol>
 *
 * <p>
 * Wall-time bound: each attempt waits up to {@code responseTimeoutMs} (1 s in the test profile), the natural flow generates ≥ 2 events per create, each event consumes 2 attempts (HTTP + 1 JMS redelivery), so worst-case in-test work is ≤ ~6 s plus convergence overhead. If this test ever exceeds ~30 s of in-test work, the timeout knob is silently being bypassed.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=IngestionEventTimeoutReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IngestionEventTimeoutReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Timeout sentinel";
    /**
     * Override priority for the slow stub. Lower number = higher priority in Wiremock; the file-based default {@code post-ingestion-events.json} runs at the implicit default of {@code 5}, so any value below that wins.
     */
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /**
     * Wiremock fixed-delay (milliseconds). Must comfortably exceed the connector's {@code responseTimeoutMs} (3 s in the test profile, see {@link ReliabilityEnvironment}) so every attempt definitely trips the timeout. {@code 6_000} leaves clear daylight without inflating the test wall-time too much when combined with the bounded JMS retry budget.
     */
    private static final int SERVER_DELAY_MS = 6_000;
    /**
     * Convergence retry step. Must be longer than the {@code responseTimeoutMs} so the connector has time to register at least the first timeout before each assertion poll.
     */
    private static final int CONVERGENCE_DELAY_MS = 4_000;
    /**
     * Lower bound on observed HTTP attempts proving the timeout fired and the JMS layer retried at least once. A count of 1 means the first attempt blocked indefinitely.
     */
    private static final int MIN_HTTP_ATTEMPTS = 2;

    @Test
    void shouldTimeOutAndDeadLetterWhenHxiResponseExceedsBudget() throws IOException
    {
        installSlowResponseStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "ingestion-timeout.txt", content, "text/plain");
        log.info("[reliability] Created node {} with /ingestion-events held for {} ms; expecting HTTP timeout then JMS DLQ", createdNode.id(), SERVER_DELAY_MS);

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                    .as("HTTP timeout actually fired and JMS retried: expected ≥ %d POSTs to /ingestion-events for this objectId. A count of 1 means the connector blocked indefinitely waiting for HX Insight — the regression the responseTimeoutMs knob exists to prevent",
                            MIN_HTTP_ATTEMPTS)
                    .isGreaterThanOrEqualTo(MIN_HTTP_ATTEMPTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: a slow HX Insight must surface as a parked message on ActiveMQ.DLQ for operator visibility, not a silently dropped event")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Installs a Wiremock stub that holds the response for {@link #SERVER_DELAY_MS} before answering {@code 202}. Higher-priority than the file-based default stub so the override takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installSlowResponseStub()
    {
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(202)
                        .withFixedDelay(SERVER_DELAY_MS)));
    }
}
