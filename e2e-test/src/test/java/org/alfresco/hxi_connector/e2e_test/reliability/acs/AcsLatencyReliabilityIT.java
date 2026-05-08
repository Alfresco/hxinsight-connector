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
package org.alfresco.hxi_connector.e2e_test.reliability.acs;

import static eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM;

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
 * Pins bounded-failure visibility when ACS REST is reachable but pathologically slow. Toxiproxy injects {@value #ACS_LATENCY_MS} ms of downstream latency per byte; the connector is configured with a {@code 3 s} response timeout (test profile, see {@link ReliabilityEnvironment}). The expected end state for an event whose content download fires while the latency is in place:
 *
 * <ul>
 * <li><b>No silent hung thread</b> — the per-request timeout fires (vs. the production default of {@code 0} which leaves Camel HTTP at no upper bound), the route worker thread is released, the route stays alive.</li>
 * <li><b>Bounded retries</b> — the connector retries via {@link org.springframework.retry.annotation.Retryable} (test-tightened to {@code attempts=2, initialDelay=200, multiplier=1}), then hands back to JMS-level redelivery, which exhausts inside the test's wall-time budget.</li>
 * <li><b>Observable failure</b> — the message lands on {@code ActiveMQ.DLQ} (depth becomes {@code >= 1}); no ingestion event for that {@code objectId} reaches HX Insight.</li>
 * <li><b>Liveness</b> — once the latency is removed, a sentinel publish flows end-to-end without operator intervention.</li>
 * </ul>
 *
 * <p>
 * Production note: the {@code alfresco.repository.responseTimeoutMs} setting exercised here defaults to {@code 0} (unset). With the default, this exact scenario would pin the route worker thread on the slow ACS request indefinitely. A positive value is the only way to bound per-request wait — see {@code live-ingester.md}.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=AcsLatencyReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "acs_huge_latency";
    /**
     * Per-byte latency Toxiproxy injects on the live-ingester ↔ ACS path. Comfortably exceeds the connector's {@code RESPONSETIMEOUTMS=3000} test profile so each content-download attempt provably trips the timeout rather than completing late.
     */
    private static final int ACS_LATENCY_MS = 6_000;
    private static final int CONVERGENCE_DELAY_MS = 2_000;
    /**
     * Wall-clock budget for the connector to exhaust HTTP retries + JMS redelivery and surface the failure on the DLQ. Sized for {@code attempts=2 × ~3 s} on each of {@code maximumRedeliveries=1+1} JMS attempts, plus headroom for Camel internal scheduling.
     */
    private static final int DLQ_DEADLINE_MS = 30_000;
    /**
     * Substring from {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.AlfrescoRepositoryContentClient AlfrescoRepositoryContentClient}'s {@code onException(Exception.class).log(ERROR, ...)} chain. Fires once per @{@code Retryable} attempt when the ACS REST GET to {@code /alfresco/api/.../content} fails (timeout, connect-refused, RST, …) — i.e. exactly when the chaos under test is exercised. Asserting a non-zero count of this substring distinguishes "the download path actually ran and failed as designed" from "the download path was silently disabled and the DLQ entry comes from an unrelated regression in the rendition pipeline" — without it, a no-op {@code downloadContent} that returns an empty stream would land a DLQ entry through the rendition POST and satisfy the broad {@code dlqDepth >= 1} assertion. The log line includes the live-ingester-side ACS endpoint URL, so it cannot be invariant with respect to the download path running.
     */
    private static final String ACS_DOWNLOAD_FAILURE_LOG_FRAGMENT = "Repository :: Unexpected response while downloading content";

    @Test
    void shouldDeadLetterEventWhenAcsResponseExceedsTimeoutBudget() throws IOException
    {
        log.info("[reliability] Injecting {} ms downstream latency on toxic-acs", ACS_LATENCY_MS);
        try
        {
            environment().acsProxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, ACS_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install ACS latency toxic — chaos cannot start", e);
        }

        Node slow;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("ACS-latency timeout regression guard".getBytes());
            slow = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "acs-latency-victim.txt", content, "text/plain");
            log.info("[reliability] Published node {} during ACS latency window — expecting DLQ within {} ms", slow.id(), DLQ_DEADLINE_MS);

            // Asserting on dlqDepth + the ACS-download route's onException log line: the metadata POST runs
            // ahead of the content download and may reach HX Insight even when the content path DLQs, so an
            // ingestion-event count for this objectId is not a clean negative signal here. The log substring
            // pins the DLQ entry to the chaos under test (the ACS download path) rather than to any
            // collateral failure in the rendition pipeline; without it, a regression that silently disables
            // the download path could still satisfy `dlqDepth >= 1` for an unrelated reason.
            RetryUtils.retryWithBackoff(() -> {
                assertThat(environment().jolokia().dlqDepth())
                        .as("ACS slowdown should produce an observable DLQ entry — a zero here means the route is stuck waiting on the slow request, contradicting the bounded-retry contract pinned by alfresco.repository.responseTimeoutMs")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment().liveIngesterContainer().getLogs())
                        .as("the ACS-content-download route must actually have been attempted under the latency chaos — without this positive signal, a regression that silently disables the download (e.g. an early-return in AlfrescoRepositoryContentClient.downloadContent) would still satisfy dlqDepth >= 1 via a collateral failure further down the rendition pipeline (e.g. empty-stream upload). Asserting the route's `Repository :: Unexpected response while downloading content - Endpoint: ...` ERROR log binds the DLQ entry to the chaos under test")
                        .contains(ACS_DOWNLOAD_FAILURE_LOG_FRAGMENT);
            }, CONVERGENCE_DELAY_MS);
        }
        finally
        {
            removeLatencyToxic();
        }

        @Cleanup
        InputStream postContent = new ByteArrayInputStream("post-latency sentinel".getBytes());
        Node sentinel = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "acs-latency-sentinel.txt", postContent, "text/plain");
        log.info("[reliability] Post-latency sentinel {} — verifying liveness", sentinel.id());

        RetryUtils.retryWithBackoff(() -> assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                .as("post-recovery sentinel must reach HX Insight — failure here means the route stopped after the latency window")
                .isGreaterThanOrEqualTo(1), CONVERGENCE_DELAY_MS);
    }

    private void removeLatencyToxic()
    {
        try
        {
            environment().acsProxy().toxics().get(LATENCY_TOXIC_NAME).remove();
        }
        catch (IOException e)
        {
            log.warn("[reliability] Could not remove ACS latency toxic during cleanup — BaseReliabilityIT reset will catch it next test", e);
        }
    }
}
