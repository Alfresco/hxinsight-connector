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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Two ingestion invariants: a transient 5xx on {@code POST /ingestion-events} must be retried, and the retried body must be byte-identical to the original. Both are required for HX Insight to dedupe replays.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IdempotentIngestionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Idempotent ingestion sentinel";
    private static final String FLAKE_SCENARIO = "ingestion-events-flake-once";
    private static final String RECOVERED_STATE = "recovered";
    /** Lower number = higher Wiremock priority; the file-based default uses 5. */
    private static final int SCENARIO_STUB_PRIORITY = 1;
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /** Natural flow (≥ 2 POSTs per create) + one redelivery of the failed attempt. */
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

        RetryUtils.assertWithRetry(() -> assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
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

        RetryUtils.assertWithRetry(() -> {
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
