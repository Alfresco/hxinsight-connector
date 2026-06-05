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
 * Positive pair for {@link S3LatencyReliabilityIT}: S3 upload latency under the timeout budget must succeed first try, no DLQ. Without this, the negative test alone could pass even if the timeout fired instantly.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3TolerableLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "s3_tolerable_latency";
    /** Under the 3 s upload timeout; well above a healthy round-trip so the test isn't a no-op. */
    private static final int S3_LATENCY_MS = 1_500;
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
