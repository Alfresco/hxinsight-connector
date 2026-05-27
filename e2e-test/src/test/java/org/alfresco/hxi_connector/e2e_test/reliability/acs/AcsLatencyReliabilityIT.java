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
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * ACS REST is reachable but slow (latency above the response timeout). The per-request timeout must fire, retries exhaust, the message lands on the DLQ with a log fragment that pins the ACS download path, and a post-recovery sentinel flows end-to-end.
 *
 * <p>
 * The {@code alfresco.repository.responseTimeoutMs} setting defaults to 0 in production; this test requires a positive value to bound the per-request wait.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "acs_huge_latency";
    /** Above the 3 s response timeout so every attempt trips it. */
    private static final int ACS_LATENCY_MS = 6_000;
    private static final int CONVERGENCE_DELAY_MS = 2_000;
    private static final int DLQ_DEADLINE_MS = 30_000;

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

            // The log substring binds the DLQ entry to the ACS download path. A simple "ingestionEventsFor==0" would
            // be wrong because the metadata POST runs ahead of the content download and may still reach HXI.
            RetryUtils.assertWithRetry(() -> {
                assertThat(environment().jolokia().dlqDepth())
                        .as("ACS slowdown should produce an observable DLQ entry — a zero here means the route is stuck waiting on the slow request, contradicting the bounded-retry contract pinned by alfresco.repository.responseTimeoutMs")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment().liveIngesterContainer().getLogs())
                        .as("DLQ entry is not bound to the ACS download path — log fragment <<%s>> missing, regression candidates: early-return in AlfrescoRepositoryContentClient.downloadContent, or collateral failure further down the rendition pipeline (e.g. empty-stream upload)".formatted(ACS_DOWNLOAD_FAILURE_LOG_FRAGMENT))
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

        RetryUtils.assertWithRetry(() -> assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
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
