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
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins two producer-side invariants of the live-ingester's HX Insight ingestion path: that a transient 5xx on {@code POST /ingestion-events} is retried, and that the retried request body is bit-identical to the original. Both invariants are necessary for HX Insight to recognise a redelivered event as a replay rather than a brand-new event.
 *
 * <p>
 * Failure is injected at the HX Insight HTTP boundary by overriding the file-based Wiremock stub for {@code /ingestion-events} with a higher-priority scenario stub: the first matching POST returns {@code 500}, every subsequent POST returns {@code 202}.
 *
 * <p>
 * Two assertions, one per test:
 * <ol>
 * <li><b>Retry happens.</b> The journal of POSTs to {@code /ingestion-events} for the new node must reach the natural-flow baseline plus at least one redelivery of the forced-500 attempt. A count stuck at the natural baseline means the connector observed the {@code 500} and gave up.</li>
 * <li><b>Retried body is byte-identical to the original.</b> Captured request bodies are grouped by exact equality; at least one body must appear at least twice. A mutation on retry (e.g. fresh {@code sourceTimestamp}, regenerated header) would make every body unique and HX Insight would see brand-new events instead of replays.</li>
 * </ol>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=IdempotentIngestionReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IdempotentIngestionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Idempotent ingestion sentinel";
    /**
     * Wiremock scenario name shared by the two stub variants (initial 500, then recovered 202). Held as a constant so the two stubs cannot drift apart.
     */
    private static final String FLAKE_SCENARIO = "ingestion-events-flake-once";
    private static final String RECOVERED_STATE = "recovered";
    /**
     * Override priority for the scenario-backed stubs. Lower number = higher priority in Wiremock; the file-based default {@code post-ingestion-events.json} runs at the implicit default of {@code 5}, so any value below that wins. Picked {@code 1} to make the override unambiguous to a future reader.
     */
    private static final int SCENARIO_STUB_PRIORITY = 1;
    /**
     * Step delay for the convergence retry loop. The HX Insight ingester's retry policy (see {@code hyland-experience.ingester.retry} in {@code application.yml}) defaults to a 500 ms initial backoff, so a single forced retry resolves within a couple of seconds in the steady state.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Lower bound on observed POSTs once the connector has retried the forced 500 once. Derived as: baseline natural flow (≥ 2 events per create — see {@link ActiveMqReliabilityIT#shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline}) + one redelivery of the failed attempt. Encoded as a constant so the rationale stays attached to the literal.
     */
    private static final int MIN_POSTS_AFTER_RETRY = 3;

    @Test
    void shouldRetryWhenHxiReturnsTransient500() throws IOException
    {
        installFailOnceThenSucceedStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "idempotent-retry.txt", content, "text/plain");
        log.info("[reliability] Created node {} with forced 500-once on /ingestion-events; expecting at least one retry", createdNode.id());

        RetryUtils.retryWithBackoff(() -> assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                .as("at-least-once retry: connector must redeliver after a 5xx on /ingestion-events. The natural ≥ 2-event flow plus one retry should push the journal to ≥ %d POSTs for this objectId; a count of 2 means the connector observed the 500 and stopped",
                        MIN_POSTS_AFTER_RETRY)
                .isGreaterThanOrEqualTo(MIN_POSTS_AFTER_RETRY),
                CONVERGENCE_DELAY_MS);
    }

    @Test
    void shouldEmitIdenticalBodyAcrossRetries() throws IOException
    {
        installFailOnceThenSucceedStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "idempotent-equality.txt", content, "text/plain");
        log.info("[reliability] Created node {} with forced 500-once on /ingestion-events; expecting at least one body to appear bit-identical across retries", createdNode.id());

        RetryUtils.retryWithBackoff(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing("\"objectId\":\"" + createdNode.id() + "\"")));

            assertThat(requests)
                    .as("at-least-once retry must have happened before payload-equality is meaningful — fail this test as a precondition rather than asserting an empty grouping")
                    .hasSizeGreaterThanOrEqualTo(MIN_POSTS_AFTER_RETRY);

            Map<String, Long> bodyCounts = requests.stream()
                    .collect(Collectors.groupingBy(LoggedRequest::getBodyAsString, Collectors.counting()));

            assertThat(bodyCounts.values())
                    .as("payload stability across retries: at least one POST body must appear bit-identical at least twice — a mutation on retry (fresh sourceTimestamp, regenerated idempotency id, etc.) would make every body unique and defeat HX Insight's exact-replay safety. Bodies seen: %s",
                            bodyCounts)
                    .anyMatch(count -> count >= 2L);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Installs a Wiremock scenario that returns {@code 500} for the first matching {@code POST /ingestion-events} and {@code 202} for every subsequent call. Higher-priority than the file-based default stub so the scenario takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installFailOnceThenSucceedStub()
    {
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo(RECOVERED_STATE));
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(RECOVERED_STATE)
                .willReturn(aResponse().withStatus(202)));
    }
}
