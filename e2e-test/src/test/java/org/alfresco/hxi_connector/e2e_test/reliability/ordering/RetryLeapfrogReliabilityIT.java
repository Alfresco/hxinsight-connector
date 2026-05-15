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
package org.alfresco.hxi_connector.e2e_test.reliability.ordering;

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
import java.util.UUID;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Pins per-node ordering at HX Insight when the first event for a node hits a transient {@code 5xx}. While the failing POST is still in flight, a second event for the same node must wait on the broker — it must not race past v1 at HX Insight. After the failure clears, the retried v1 POST must land before v2.
 *
 * <p>
 * The single-instance route is {@code transacted()} and reads in order, so the consumer thread is busy with v1 until v1 either succeeds or DLQs. If someone removes {@code transacted()}, adds parallel consumers, or makes the publish leg async, v2 can leak past v1 — and HX Insight ends up with stale data because of last-arrival-wins on digest. This test exists to catch that.
 *
 * <p>
 * How the test works:
 * <ol>
 * <li><b>Hold v1.</b> A Wiremock scenario returns {@code 500} for the first {@code POST /ingestion-events}, but only after a fixed delay; later calls return {@code 202} right away. The delay is shorter than {@code responseTimeoutMs} (3 s in the test profile) so the HTTP client actually waits — it doesn't time out.</li>
 * <li><b>Wait for v1 to start.</b> Poll the Wiremock journal until the v1 POST is recorded. Wiremock logs the request on receipt, before sending the response, so seeing it there means the connector dispatched v1 and the consumer is now parked on the held HTTP read.</li>
 * <li><b>Publish v2.</b> PUT a metadata update on the same node with a unique marker in {@code cm:description}. The Updated event lands on the broker; the connector cannot consume it yet because the thread is busy with v1.</li>
 * <li><b>Strong mid-delay check.</b> Before v1's delay expires, search the Wiremock journal for the marker. It must not be there. If it is, the consumer raced past v1 — that's the regression.</li>
 * </ol>
 *
 * <p>
 * After the convergence retry loop finishes, walk the journal in order and check that every POST after the first marker POST also carries the marker. The mid-delay check proves v2 was queued at the broker while v1 was retrying; the walk proves the in-order contract holds all the way through the retry recovery.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=RetryLeapfrogReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class RetryLeapfrogReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String INGESTION_EVENTS_PATH = "/ingestion-events";
    private static final String FLAKE_SCENARIO = "retry-leapfrog-slow-fail-once";
    private static final String RECOVERED_STATE = "recovered";
    private static final int SCENARIO_STUB_PRIORITY = 1;
    /**
     * How long Wiremock holds the first {@code 500} response. Stays under {@code responseTimeoutMs} (3 s in the test profile) so the HTTP client doesn't time out, and long enough to fit the v2 publish and mid-delay check inside the window. {@code 2_000} leaves ~1 s on each side.
     */
    private static final int FORCED_DELAY_MS = 2_000;
    /**
     * How often we poll Wiremock waiting for v1's POST to show up. Tight enough to exit quickly once v1 lands; not so tight that we hammer the admin endpoint.
     */
    private static final int V1_POST_POLL_DELAY_MS = 50;
    /**
     * Cap on the v1-arrival poll. 30 attempts × 50 ms = 1.5 s — comfortably above typical event-consume latency (~50–200 ms), and well under v1's {@value #FORCED_DELAY_MS} ms hold so we never miss the in-flight window.
     */
    private static final int V1_POST_POLL_ATTEMPTS = 30;
    /**
     * How long we wait after the v2 PUT before checking the journal. Covers ACS → broker propagation (~50 ms) and gives a parallel-consumer regression a fair chance to land a marker POST.
     */
    private static final int MID_DELAY_SETTLE_MS = 500;
    /**
     * Step for the post-recovery convergence loop. Longer than the leftover v1 delay + Spring Retry backoff + v2 publish round-trip so each tick sees a stable journal.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_500;
    /**
     * Minimum POSTs we expect once the scenario plays out: one v1 attempt (delayed-then-retried) plus the v2 Updated. Create natural-flow may push the count higher.
     */
    private static final int MIN_POSTS_AFTER_LEAPFROG = 2;

    @Test
    void shouldDeliverFirstEventBeforeSecondAtHxiDespiteForcedRetry() throws IOException, InterruptedException
    {
        installSlowFailFirstThenSucceedStub();

        Node victim;
        @Cleanup
        InputStream content = new ByteArrayInputStream("retry-leapfrog v1".getBytes());
        victim = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "retry-leapfrog.txt", content, "text/plain");
        log.info("[reliability] Created victim node {} — first POST /ingestion-events held at 500 for {} ms", victim.id(), FORCED_DELAY_MS);

        // 1. Wait until the connector has dispatched v1's POST.
        RetryUtils.assertWithRetry(() -> assertThat(WiremockCounts.ingestionEventsFor(victim.id()))
                .as("v1 POST never reached Wiremock — the rest of the test would be vacuous if the connector hasn't started publishing yet")
                .isGreaterThanOrEqualTo(1),
                V1_POST_POLL_ATTEMPTS, V1_POST_POLL_DELAY_MS);

        // 2. Publish v2 (metadata PUT with marker) while v1 is held in-flight.
        String v2Marker = "retry-leapfrog-v2-" + UUID.randomUUID();
        String updateBody = """
                {
                    "properties": {
                        "cm:description": "%s"
                    }
                }
                """.formatted(v2Marker);
        environment().repositoryClient().updateNodeWithContent(victim.id(), updateBody);
        log.info("[reliability] PUT update on node {} with marker={} while v1 is held — v2 should sit on the broker, not at HX Insight yet",
                victim.id(), v2Marker);

        // 3. Strong mid-delay check: v2 must not have reached HX Insight yet.
        Thread.sleep(MID_DELAY_SETTLE_MS);
        List<LoggedRequest> midDelayMarkerJournal = findAll(postRequestedFor(urlEqualTo(INGESTION_EVENTS_PATH))
                .withRequestBody(containing(v2Marker)));
        assertThat(midDelayMarkerJournal)
                .as("ordering regression: v1 is still held by Wiremock (%d ms delay on the first 500), but v2 (marker=%s) has already reached HX Insight. The consumer raced past v1 — the route should hold the queue until the in-flight delivery finishes",
                        FORCED_DELAY_MS, v2Marker)
                .isEmpty();

        // 4. Convergence: v1 delay expires, retry succeeds, v2 lands.
        // Walk the journal and check no v1-shaped POST follows the first v2 marker.
        RetryUtils.assertWithRetry(() -> {
            List<LoggedRequest> requests = findAll(postRequestedFor(urlEqualTo(INGESTION_EVENTS_PATH))
                    .withRequestBody(containing("\"objectId\":\"" + victim.id() + "\"")));

            assertThat(requests)
                    .as("expected both v1's POSTs and the v2 Updated POST for objectId=%s before checking order",
                            victim.id())
                    .hasSizeGreaterThanOrEqualTo(MIN_POSTS_AFTER_LEAPFROG);

            int firstV2Index = -1;
            for (int i = 0; i < requests.size(); i++)
            {
                if (requests.get(i).getBodyAsString().contains(v2Marker))
                {
                    firstV2Index = i;
                    break;
                }
            }
            assertThat(firstV2Index)
                    .as("no POST with marker=%s for objectId=%s — the metadata PUT did not produce an Updated event in time, or the connector dropped it",
                            v2Marker, victim.id())
                    .isNotNegative();

            for (int i = firstV2Index + 1; i < requests.size(); i++)
            {
                String body = requests.get(i).getBodyAsString();
                assertThat(body)
                        .as("ordering regression: POST #%d for objectId=%s landed after v2 (marker=%s) but does not carry the marker — a redelivered v1 attempt overtook v2 at HX Insight. Body fragment: %s",
                                i, victim.id(), v2Marker, abbreviate(body))
                        .contains(v2Marker);
            }

            assertThat(environment().jolokia().dlqDepth())
                    .as("DLQ should be empty — one forced 500 should fit inside the retry budget. A non-zero count means retries didn't fire or the stub forced too many failures")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Wiremock scenario: first {@code POST /ingestion-events} gets {@code 500} after a fixed delay; every later call returns {@code 202} immediately. Priority is set higher than the file-based default stub so the scenario wins; {@code BaseReliabilityIT.resetBetweenTests()} cleans it up at the next test boundary.
     */
    private static void installSlowFailFirstThenSucceedStub()
    {
        stubFor(post(urlEqualTo(INGESTION_EVENTS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withFixedDelay(FORCED_DELAY_MS))
                .willSetStateTo(RECOVERED_STATE));
        stubFor(post(urlEqualTo(INGESTION_EVENTS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(RECOVERED_STATE)
                .willReturn(aResponse().withStatus(202)));
    }

    private static String abbreviate(String body)
    {
        int maxLen = 240;
        return body.length() <= maxLen ? body : body.substring(0, maxLen) + "…";
    }
}
