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
 * Positive pair for {@link AcsLatencyReliabilityIT}: ACS latency under the timeout budget must succeed first try, no DLQ. Without this, the negative test alone could pass even if the timeout fired instantly.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsTolerableLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "acs_tolerable_latency";
    /** Under the 3 s ACS response timeout; well above a healthy round-trip so the test isn't a no-op. */
    private static final int ACS_LATENCY_MS = 1_500;
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldDeliverEventWhenAcsLatencyStaysWithinTimeoutBudget() throws IOException
    {
        log.info("[reliability] Injecting {} ms downstream latency on toxic-acs (under the {} ms test-profile responseTimeoutMs)", ACS_LATENCY_MS, 3_000);
        try
        {
            environment().acsProxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, ACS_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install ACS latency toxic — chaos cannot start", e);
        }

        Node tolerable;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("ACS-latency in-budget regression guard".getBytes());
            tolerable = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "acs-latency-tolerable.txt", content, "text/plain");
            log.info("[reliability] Published node {} with {} ms ACS latency in place — expecting first-attempt success, no DLQ", tolerable.id(), ACS_LATENCY_MS);

            final Node finalTolerable = tolerable;
            RetryUtils.assertWithRetry(() -> {
                assertThat(WiremockCounts.ingestionEventsFor(finalTolerable.id()))
                        .as("in-budget ACS latency must NOT prevent ingestion — a zero here means the response timeout fired prematurely or the route stalled on a slow-but-tolerable ACS request")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment().jolokia().dlqDepth())
                        .as("in-budget ACS latency must NOT produce a DLQ entry — non-zero here means the timeout machinery is mis-tuned (firing under the configured budget) or some downstream path is mis-classifying a slow-but-successful response as a failure")
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
            environment().acsProxy().toxics().get(LATENCY_TOXIC_NAME).remove();
        }
        catch (IOException e)
        {
            log.warn("[reliability] Could not remove ACS latency toxic during cleanup — BaseReliabilityIT reset will catch it next test", e);
        }
    }
}
