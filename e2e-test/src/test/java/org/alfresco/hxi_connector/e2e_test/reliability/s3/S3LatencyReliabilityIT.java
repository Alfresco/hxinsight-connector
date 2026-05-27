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
package org.alfresco.hxi_connector.e2e_test.reliability.s3;

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
 * S3 upload PUT exceeds the upload response-timeout budget. Asserts the per-PUT timeout fires (route worker freed), retries exhaust, the message lands on the DLQ, and liveness recovers once latency is removed.
 *
 * <p>
 * Production note: {@code hyland-experience.storage.upload.response-timeout-ms} defaults to {@code 0} (no upper bound) — with the default, this scenario would pin the worker thread on the slow PUT forever.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3LatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "s3_huge_latency";
    /** Above the 3 s upload timeout so every PUT trips it. */
    private static final int S3_LATENCY_MS = 6_000;
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldDeadLetterEventWhenS3UploadExceedsTimeoutBudget() throws IOException
    {
        log.info("[reliability] Injecting {} ms downstream latency on toxic-s3", S3_LATENCY_MS);
        try
        {
            environment().s3Proxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, S3_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install S3 latency toxic — chaos cannot start", e);
        }

        Node slow;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("S3-latency timeout regression guard".getBytes());
            slow = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "s3-latency-victim.txt", content, "text/plain");
            log.info("[reliability] Published node {} during S3 latency window — expecting DLQ", slow.id());

            RetryUtils.assertWithRetry(() -> assertThat(environment().jolokia().dlqDepth())
                    .as("S3 slowdown should produce an observable DLQ entry — a zero here means the route is stuck waiting on the slow PUT, contradicting the bounded-retry contract pinned by hyland-experience.storage.upload.response-timeout-ms")
                    .isGreaterThanOrEqualTo(1), CONVERGENCE_DELAY_MS);
        }
        finally
        {
            removeLatencyToxic();
        }

        @Cleanup
        InputStream postContent = new ByteArrayInputStream("post-latency sentinel".getBytes());
        Node sentinel = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "s3-latency-sentinel.txt", postContent, "text/plain");
        log.info("[reliability] Post-latency sentinel {} — verifying liveness", sentinel.id());

        RetryUtils.assertWithRetry(() -> assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                .as("post-recovery sentinel must reach HX Insight — failure here means the route stopped after the latency window")
                .isGreaterThanOrEqualTo(1), CONVERGENCE_DELAY_MS);
    }

    private void removeLatencyToxic()
    {
        try
        {
            environment().s3Proxy().toxics().get(LATENCY_TOXIC_NAME).remove();
        }
        catch (IOException e)
        {
            log.warn("[reliability] Could not remove S3 latency toxic during cleanup — BaseReliabilityIT reset will catch it next test", e);
        }
    }
}
