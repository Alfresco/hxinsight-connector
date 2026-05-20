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
 * Positive counterpart to {@link AcsLatencyReliabilityIT}: pins that an ACS REST response slower than a healthy round-trip but <i>under</i> the connector's {@code alfresco.repository.responseTimeoutMs} budget completes successfully — ingestion event reaches HX Insight, {@code dlqDepth() == 0}.
 *
 * <p>
 * Without this guard, a regression that trips the response timeout early (wrong unit on the property, mis-wired Camel HTTP option, accidental zeroing in a future refactor) would still pass {@link AcsLatencyReliabilityIT} (the symmetric negative): both the over-budget and under-budget cases would DLQ, so the negative test alone cannot tell "the timeout setting is working" from "the timeout is permanently broken to fire instantly". This row separates the two.
 *
 * <p>
 * Toxiproxy injects {@value #ACS_LATENCY_MS} ms downstream latency on the live-ingester ↔ ACS path; the connector is configured with a {@code 3 s} response timeout (test profile, see {@link ReliabilityEnvironment}). Each content-download attempt should complete in ~{@value #ACS_LATENCY_MS} ms — comfortably below the timeout — so the very first attempt succeeds, no retry / JMS redelivery is needed, and the message reaches HX Insight without touching the DLQ.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=AcsTolerableLatencyReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsTolerableLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "acs_tolerable_latency";
    /**
     * Downstream latency Toxiproxy injects on the live-ingester ↔ ACS path. Comfortably under the connector's {@code RESPONSETIMEOUTMS=3000} test profile so each content-download attempt completes inside the per-request budget rather than tripping the timeout. Picked well above a healthy ACS round-trip (~tens of ms) so the test cannot pass by the latency being a no-op.
     */
    private static final int ACS_LATENCY_MS = 1_500;
    /**
     * Per-attempt step for the convergence retry loop. Sized to comfortably absorb one slow ACS round-trip (~{@value #ACS_LATENCY_MS} ms) plus the downstream HX Insight POSTs.
     */
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
