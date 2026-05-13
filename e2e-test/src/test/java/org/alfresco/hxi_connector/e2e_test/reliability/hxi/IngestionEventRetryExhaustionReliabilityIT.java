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
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Verifies the live-ingester's repo-events route exhausts cleanly when HX Insight ingestion is hard-down: HTTP-level retries on {@code POST /ingestion-events} run to exhaustion, the resulting exception bubbles through the route's error handler, JMS-level redeliveries also exhaust, and the message lands on {@code ActiveMQ.DLQ} where an operator can see it. The complement to {@link IdempotentIngestionReliabilityIT}'s "retries help" story — this one proves retries don't help <i>forever</i>.
 *
 * <p>
 * Failure is injected at the HX Insight HTTP boundary by overriding the file-based Wiremock stub for {@code /ingestion-events} with a higher-priority stub that always returns {@code 500}. The {@code /presigned-urls} default is left intact so the test exercises the ingestion-event path specifically, not the upload chain.
 *
 * <p>
 * Two assertions:
 * <ol>
 * <li><b>HTTP retries actually happened.</b> Total POSTs to {@code /ingestion-events} for the new node must be ≥ {@link #MIN_HTTP_ATTEMPTS} — the connector must have exhausted its HTTP retry budget before falling through to the JMS-level handler. A count of 1 means the connector observed the first 500 and gave up immediately.</li>
 * <li><b>Bounded redelivery exhausts to DLQ.</b> {@code dlqDepth() >= 1} — the exhausted message must land on {@code ActiveMQ.DLQ}. A zero here means the route ACK'd a failure without dead-lettering, which is the silent-drop bug class the {@code live_ingester_repo_events_dlq_total} counter was added to prevent.</li>
 * </ol>
 *
 * <p>
 * Sentinel post-failure liveness is intentionally <b>not</b> asserted here because the stub forces {@code /ingestion-events} to fail for the duration of the test, so a sentinel ACS create would also dead-letter rather than reach HX Insight. The route's structural liveness (broker subscription survives) is checked implicitly by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary; if this scenario broke the route, the next test fails fast with a precondition error pointing back here.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=IngestionEventRetryExhaustionReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IngestionEventRetryExhaustionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Ingestion-event exhaustion sentinel";
    /**
     * Override priority for the always-fail stub. Lower number = higher priority in Wiremock; the file-based default {@code post-ingestion-events.json} runs at the implicit default of {@code 5}, so any value below that wins. Picked {@code 1} to make the override unambiguous to a future reader.
     */
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /**
     * Convergence retry step. With the test fast-retry profile (HTTP: 2 attempts × 200 ms, JMS: 1 redelivery × 200 ms), exhaustion across both layers completes in well under a second per event; this delay leaves slack for OS scheduling and Camel's internal queueing.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Lower bound on observed HTTP attempts proving the connector ran its HTTP retry budget. Test profile is 2 HTTP attempts × 1 JMS redelivery = 4 POSTs per repo event, but using {@code 2} keeps the assertion tolerant to a future change of the retry-attempts knob without losing the "retries did happen" signal.
     */
    private static final int MIN_HTTP_ATTEMPTS = 2;

    @Test
    void shouldExhaustHttpRetriesAndDeadLetterEventOnPersistentHxiFailure() throws IOException
    {
        installAlwaysFailStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "ingestion-exhaustion.txt", content, "text/plain");
        log.info("[reliability] Created node {} with /ingestion-events forced to 500; expecting HTTP retries to exhaust then JMS DLQ to receive the parked message", createdNode.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                    .as("HTTP retry budget exercised: connector must attempt ≥ %d POSTs to /ingestion-events for this objectId before exhausting and falling through to the JMS handler. A count of 1 means the connector treated the first 500 as fatal",
                            MIN_HTTP_ATTEMPTS)
                    .isGreaterThanOrEqualTo(MIN_HTTP_ATTEMPTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: a persistently-failing /ingestion-events path must move the original event to ActiveMQ.DLQ so an operator can see it. A zero here means the route silently dropped the failure")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Installs a Wiremock stub that returns {@code 500} for every {@code POST /ingestion-events}. Higher-priority than the file-based default stub so the override takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installAlwaysFailStub()
    {
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse().withStatus(500)));
    }
}
