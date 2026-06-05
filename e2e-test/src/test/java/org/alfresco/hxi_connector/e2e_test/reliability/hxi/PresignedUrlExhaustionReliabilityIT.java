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
import static com.github.tomakehurst.wiremock.client.WireMock.setScenarioState;
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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.WiremockCounts;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Persistent failures on the upload leg ({@code /presigned-urls}) must exhaust through {@code @Retryable} + JMS redelivery and land on the DLQ. Then a sentinel must flow end-to-end once the stub recovers.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class PresignedUrlExhaustionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String EXHAUSTION_SCENARIO = "presigned-urls-exhaustion";
    private static final String RECOVERED_STATE = "recovered";
    /** Lower number = higher Wiremock priority; the file-based default uses 5. */
    private static final int SCENARIO_STUB_PRIORITY = 1;
    private static final String PRESIGNED_URLS_BODY_FILE = "presigned-urls.json";
    private static final int CONVERGENCE_DELAY_MS = 1_500;
    /** Lower bound proving the upload-leg retry envelope ran. */
    private static final int MIN_PRESIGNED_URL_POSTS = 3;
    private static final long RECOVERY_SETTLE_MS = 500L;

    @Test
    void shouldDeadLetterPassthroughWhenPresignedUrlExhaustsAndRecoverForSentinel() throws IOException, InterruptedException
    {
        installAlwaysFailWhileStartedThenRecoverStub();

        Node victim;
        @Cleanup
        InputStream content = new ByteArrayInputStream("presigned-url-exhaustion victim".getBytes());
        victim = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "presigned-url-exhaustion.txt", content, "text/plain");
        log.info("[reliability] Created victim node {} — /presigned-urls forced to 500 forever (scenario state STARTED); upload leg should exhaust and DLQ", victim.id());

        final Node finalVictim = victim;
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.presignedUrlRequests())
                    .as("upload-leg retry contract: at least %d POSTs to /presigned-urls expected (in-delivery @Retryable + JMS redelivery, see test profile in LiveIngesterEnvVars). A count below this means the @Retryable / DLC envelope did not run on the upload leg",
                            MIN_PRESIGNED_URL_POSTS)
                    .isGreaterThanOrEqualTo(MIN_PRESIGNED_URL_POSTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("upload-leg DLQ contract: victim event for objectId=%s must surface on the DLQ once the upload-leg retries exhaust. A zero here means exhaustion was silent — the regression class this IT exists to catch (e.g. @Retryable wired without DLC, or DLC misrouted)",
                            finalVictim.id())
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);

        log.info("[reliability] Flipping scenario {} to {} — subsequent /presigned-urls calls return file-based default body", EXHAUSTION_SCENARIO, RECOVERED_STATE);
        setScenarioState(EXHAUSTION_SCENARIO, RECOVERED_STATE);
        Thread.sleep(RECOVERY_SETTLE_MS);

        @Cleanup
        InputStream sentinelContent = new ByteArrayInputStream("presigned-url-exhaustion sentinel".getBytes());
        Node sentinel = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "presigned-url-exhaustion-sentinel.txt", sentinelContent, "text/plain");
        log.info("[reliability] Post-recovery sentinel {} — verifying liveness on the upload leg", sentinel.id());

        RetryUtils.assertWithRetry(() -> assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                .as("liveness on upload leg: post-recovery sentinel must reach HX Insight end-to-end. A zero here means the route is stuck after the upload-leg DLQ event — bounded-failure regression",
                        sentinel.id())
                .isGreaterThanOrEqualTo(1),
                CONVERGENCE_DELAY_MS);
    }

    /**
     * Wiremock scenario: while state is {@code STARTED} every {@code POST /presigned-urls} returns {@code 500}; once state is flipped to {@code RECOVERED} (via {@link com.github.tomakehurst.wiremock.client.WireMock#setScenarioState setScenarioState}) every call returns the file-based default body. Higher-priority than the file-based default stub so the scenario takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installAlwaysFailWhileStartedThenRecoverStub()
    {
        stubFor(post(urlEqualTo("/presigned-urls"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(EXHAUSTION_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500)));
        stubFor(post(urlEqualTo("/presigned-urls"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .inScenario(EXHAUSTION_SCENARIO)
                .whenScenarioStateIs(RECOVERED_STATE)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(PRESIGNED_URLS_BODY_FILE)));
    }
}
