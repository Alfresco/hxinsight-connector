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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * The connector must retry a transient 5xx on {@code /presigned-urls} and the downstream chain must still complete after recovery. Asserts the retry counter increments (proving {@code @Retryable} fired), the HTTP journal shows ≥ 2 attempts, and the ingestion-event POSTs land per the natural baseline.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class PresignedUrlRetryReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Presigned-URL retry sentinel";
    private static final String FLAKE_SCENARIO = "presigned-urls-flake-once";
    private static final String RECOVERED_STATE = "recovered";
    /** Lower number = higher Wiremock priority; the file-based default uses 5. */
    private static final int SCENARIO_STUB_PRIORITY = 1;
    private static final String PRESIGNED_URLS_BODY_FILE = "presigned-urls.json";
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /** Initial 500 + at least one recovery attempt. */
    private static final int MIN_PRESIGNED_URL_POSTS = 2;
    /** Metadata + content POST per node. */
    private static final int MIN_INGESTION_EVENTS = 2;
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
