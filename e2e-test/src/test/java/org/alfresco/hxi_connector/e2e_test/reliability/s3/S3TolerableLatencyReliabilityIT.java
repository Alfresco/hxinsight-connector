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
 * Positive counterpart to {@link S3LatencyReliabilityIT}: pins that an S3 upload PUT slower than a healthy round-trip but <i>under</i> the connector's {@code hyland-experience.storage.upload.response-timeout-ms} budget completes successfully — ingestion event reaches HX Insight, {@code dlqDepth() == 0}.
 *
 * <p>
 * Without this guard, a regression that trips the upload response timeout early (wrong unit on the property, mis-wired Camel HTTP option, accidental zeroing in a future refactor) would still pass {@link S3LatencyReliabilityIT} (the symmetric negative): both the over-budget and under-budget cases would DLQ, so the negative test alone cannot tell "the timeout setting is working" from "the timeout is permanently broken to fire instantly". This row separates the two.
 *
 * <p>
 * Toxiproxy injects {@value #S3_LATENCY_MS} ms downstream latency on the {@code toxic-s3} listener; the connector is configured with a {@code 3 s} upload response timeout (test profile, see {@link ReliabilityEnvironment}). Each PUT attempt should complete in ~{@value #S3_LATENCY_MS} ms — comfortably below the timeout — so the very first attempt succeeds, no retry / JMS redelivery is needed, and the message reaches HX Insight without touching the DLQ.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=S3TolerableLatencyReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3TolerableLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "s3_tolerable_latency";
    /**
     * Downstream latency Toxiproxy injects on the live-ingester ↔ S3 upload path. Comfortably under the connector's {@code STORAGE_UPLOAD_RESPONSETIMEOUTMS=3000} test profile so each PUT attempt completes inside the per-request budget rather than tripping the timeout. Picked well above a healthy Localstack round-trip (~tens of ms) so the test cannot pass by the latency being a no-op.
     */
    private static final int S3_LATENCY_MS = 1_500;
    /**
     * Per-attempt step for the convergence retry loop. Sized to comfortably absorb one slow upload PUT round-trip (~{@value #S3_LATENCY_MS} ms) plus the downstream HX Insight POSTs.
     */
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldDeliverEventWhenS3UploadLatencyStaysWithinTimeoutBudget() throws IOException
    {
        log.info("[reliability] Injecting {} ms downstream latency on toxic-s3 (under the {} ms test-profile responseTimeoutMs)", S3_LATENCY_MS, 3_000);
        try
        {
            environment().s3Proxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, S3_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install S3 latency toxic — chaos cannot start", e);
        }

        Node tolerable;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("S3-latency in-budget regression guard".getBytes());
            tolerable = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "s3-latency-tolerable.txt", content, "text/plain");
            log.info("[reliability] Published node {} with {} ms S3 latency in place — expecting first-attempt success, no DLQ", tolerable.id(), S3_LATENCY_MS);

            final Node finalTolerable = tolerable;
            RetryUtils.assertWithRetry(() -> {
                assertThat(WiremockCounts.ingestionEventsFor(finalTolerable.id()))
                        .as("in-budget S3 latency must NOT prevent ingestion — a zero here means the upload response timeout fired prematurely or the route stalled on a slow-but-tolerable S3 PUT")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment().jolokia().dlqDepth())
                        .as("in-budget S3 latency must NOT produce a DLQ entry — non-zero here means the timeout machinery is mis-tuned (firing under the configured budget) or some downstream path is mis-classifying a slow-but-successful response as a failure")
                        .isZero();
            }, CONVERGENCE_DELAY_MS);
        }
        finally
        {
            removeLatencyToxic();
        }
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
