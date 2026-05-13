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
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

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
 * Pins that the live-ingester retries a transient 5xx from {@code POST /presigned-urls} and that the downstream ingestion chain still completes after recovery. Without this guard, a single bad response from the storage-location endpoint would orphan content in ACS without a corresponding HX Insight event — silent data loss.
 *
 * <p>
 * Failure is injected at the HX Insight HTTP boundary by overriding the file-based Wiremock stub for {@code /presigned-urls} with a higher-priority scenario stub: the first matching POST returns {@code 500}, every subsequent POST returns {@code 200} with the same body the default stub would have served.
 *
 * <p>
 * Three coupled assertions, single test:
 * <ol>
 * <li><b>In-delivery retry fired.</b> The {@code live_ingester_retry_attempts_total} Micrometer counter (tagged {@code exception=EndpointServerErrorException}, populated by {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.RetryMetricsRecorder RetryMetricsRecorder} on every Spring {@code @Retryable} {@code onError}) must increment by ≥ 1 during the chaos window. This is the only signal that distinguishes Spring Retry's in-delivery {@code @Retryable} attempts from JMS-broker-side redelivery — both can produce additional POSTs at the boundary, but only {@code @Retryable} increments the counter. A delta of 0 means the connector observed the 500 and the {@code @Retryable} mechanism did not engage; any pass on the boundary-POST count alone would be a JMS-redelivery substitute.</li>
 * <li><b>Retry observable at the HTTP boundary.</b> The journal of POSTs to {@code /presigned-urls} for this run must be ≥ 2 — initial 500 plus at least one recovery attempt. A count of 1 means the connector observed the 500 and gave up.</li>
 * <li><b>Chain still completes.</b> After the connector recovers from the 500, it must finish the upload-then-emit chain: ≥ 2 POSTs to {@code /ingestion-events} for the new node, mirroring the natural-flow baseline (one create yields ≥ 2 ingestion-event POSTs end-to-end). A 0 here means {@code /presigned-urls} eventually succeeded but the connector dropped the rest of the chain.</li>
 * </ol>
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=PresignedUrlRetryReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class PresignedUrlRetryReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Presigned-URL retry sentinel";
    /**
     * Wiremock scenario name shared by the two stub variants (initial 500, then recovered 200). Held as a constant so the two stubs cannot drift apart.
     */
    private static final String FLAKE_SCENARIO = "presigned-urls-flake-once";
    private static final String RECOVERED_STATE = "recovered";
    /**
     * Override priority for the scenario-backed stubs. Lower number = higher priority in Wiremock; the file-based default {@code post-presigned-urls.json} runs at the implicit default of {@code 5}, so any value below that wins. Picked {@code 1} to make the override unambiguous to a future reader.
     */
    private static final int SCENARIO_STUB_PRIORITY = 1;
    /**
     * Body file name reused from the file-based default stub so the recovered response is byte-identical to what the connector normally sees. Editing the default {@code presigned-urls.json} also updates this test.
     */
    private static final String PRESIGNED_URLS_BODY_FILE = "presigned-urls.json";
    /**
     * Step delay for the convergence retry loop. The HX Insight storage-location retry policy in the test profile (see {@link ReliabilityEnvironment}) is 2 attempts × 200 ms initial backoff, so a single forced retry resolves within roughly a second.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Lower bound on observed POSTs once the connector has retried the forced 500 once. Initial 500 + at least one recovery attempt.
     */
    private static final int MIN_PRESIGNED_URL_POSTS = 2;
    /**
     * Lower bound on observed ingestion-event POSTs after the chain has converged. Matches the natural-flow baseline (≥ 2 events per create — see {@link ActiveMqReliabilityIT#shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline}).
     */
    private static final int MIN_INGESTION_EVENTS = 2;
    /**
     * Counter increment expected during the chaos window: a single forced 500 on {@code /presigned-urls} fires {@code @Retryable.onError} once before the recovered 200 succeeds. The exception class tag matches what {@code PreSignedUrlRequester.requestStorageLocation} declares as {@code retryFor}.
     */
    private static final String RETRY_COUNTER = "live_ingester_retry_attempts_total";
    private static final String RETRY_EXCEPTION_TAG = "EndpointServerErrorException";
    private static final double MIN_RETRY_DELTA = 1.0;

    @Test
    void shouldRetryWhenPresignedUrlReturnsTransient500() throws IOException
    {
        installFailOnceThenSucceedStub();

        double retryCounterBefore = environment().actuatorMetrics()
                .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG);

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "presigned-url-retry.txt", content, "text/plain");
        log.info("[reliability] Created node {} with forced 500-once on /presigned-urls; expecting retry then full chain completion", createdNode.id());

        RetryUtils.assertWithRetry(() -> {
            double retryCounterDelta = environment().actuatorMetrics()
                    .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG) - retryCounterBefore;
            assertThat(retryCounterDelta)
                    .as("in-delivery retry contract: %s{exception=%s} must increment by ≥ %.0f during the chaos window — a delta of 0 means @Retryable did not fire and any boundary-POST retry observed below was JMS-broker-side redelivery substituting for the missing in-delivery retry",
                            RETRY_COUNTER, RETRY_EXCEPTION_TAG, MIN_RETRY_DELTA)
                    .isGreaterThanOrEqualTo(MIN_RETRY_DELTA);
            assertThat(WiremockCounts.presignedUrlRequests())
                    .as("at-least-once retry: connector must redeliver after a 5xx on /presigned-urls. Expected ≥ %d POSTs (initial 500 + recovery); a count of 1 means the connector observed the 500 and stopped",
                            MIN_PRESIGNED_URL_POSTS)
                    .isGreaterThanOrEqualTo(MIN_PRESIGNED_URL_POSTS);
            assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                    .as("downstream chain after recovery: once /presigned-urls succeeds, the connector must finish the upload-then-emit chain (≥ %d ingestion-event POSTs for this objectId, natural-flow baseline). A 0 here means the chain stalled after recovery",
                            MIN_INGESTION_EVENTS)
                    .isGreaterThanOrEqualTo(MIN_INGESTION_EVENTS);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Installs a Wiremock scenario that returns {@code 500} for the first matching {@code POST /presigned-urls} and {@code 200} with the default {@link #PRESIGNED_URLS_BODY_FILE} body for every subsequent call. Higher-priority than the file-based default stub so the scenario takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installFailOnceThenSucceedStub()
    {
        stubFor(post(urlEqualTo("/presigned-urls"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo(RECOVERED_STATE));
        stubFor(post(urlEqualTo("/presigned-urls"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(FLAKE_SCENARIO)
                .whenScenarioStateIs(RECOVERED_STATE)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(PRESIGNED_URLS_BODY_FILE)));
    }
}
