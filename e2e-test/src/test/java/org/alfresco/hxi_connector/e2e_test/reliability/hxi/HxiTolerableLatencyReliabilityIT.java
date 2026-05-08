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
 * Positive counterpart to {@link IngestionEventTimeoutReliabilityIT}: pins that an HX Insight response slower than a healthy round-trip but <i>under</i> the connector's {@code hyland-experience.ingester.response-timeout-ms} budget completes successfully — ingestion event reaches HX Insight, {@code dlqDepth() == 0}.
 *
 * <p>
 * Without this guard, a regression that trips the response timeout early (wrong unit on the property, mis-wired Camel HTTP option, accidental zeroing in a future refactor) would still pass {@link IngestionEventTimeoutReliabilityIT} (the symmetric negative): both the over-budget and under-budget cases would DLQ, so the negative test alone cannot tell "the timeout setting is working" from "the timeout is permanently broken to fire instantly". This row separates the two.
 *
 * <p>
 * Toxiproxy injects {@value #HXI_LATENCY_MS} ms downstream latency on the live-ingester ↔ HXI path; the connector is configured with a {@code 3 s} response timeout (test profile, see {@link ReliabilityEnvironment}). Each {@code /presigned-urls} and {@code /ingestion-events} attempt should complete in ~{@value #HXI_LATENCY_MS} ms — comfortably below the timeout — so the very first attempt of each succeeds, no retry / JMS redelivery is needed, and the message reaches HX Insight without touching the DLQ.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=HxiTolerableLatencyReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class HxiTolerableLatencyReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String LATENCY_TOXIC_NAME = "hxi_tolerable_latency";
    /**
     * Downstream latency Toxiproxy injects on the live-ingester ↔ HXI path. Comfortably under the connector's {@code RESPONSETIMEOUTMS=3000} test profile so each HXI request completes inside the per-request budget rather than tripping the timeout. Picked well above a healthy WireMock round-trip (~tens of ms) so the test cannot pass by the latency being a no-op.
     *
     * <p>
     * Note on additive latency: the publish flow makes more than one HXI call (auth token if not cached, {@code /presigned-urls}, {@code /ingestion-events}). Each one takes the latency hit independently — picking 1500 ms keeps every individual request comfortably under the 3000 ms timeout, while still making the latency observable in wall-time.
     */
    private static final int HXI_LATENCY_MS = 1_500;
    /**
     * Per-attempt step for the convergence retry loop. Sized to comfortably absorb several slow HXI round-trips (~{@value #HXI_LATENCY_MS} ms each) without flapping.
     */
    private static final int CONVERGENCE_DELAY_MS = 4_000;

    @Test
    void shouldDeliverEventWhenHxiLatencyStaysWithinTimeoutBudget() throws IOException
    {
        log.info("[reliability] Injecting {} ms downstream latency on toxic-hxi (under the {} ms test-profile responseTimeoutMs)", HXI_LATENCY_MS, 3_000);
        try
        {
            environment().hxiProxy().toxics().latency(LATENCY_TOXIC_NAME, DOWNSTREAM, HXI_LATENCY_MS);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Could not install HXI latency toxic — chaos cannot start", e);
        }

        Node tolerable;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("HXI-latency in-budget regression guard".getBytes());
            tolerable = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "hxi-latency-tolerable.txt", content, "text/plain");
            log.info("[reliability] Published node {} with {} ms HXI latency in place — expecting first-attempt success, no DLQ", tolerable.id(), HXI_LATENCY_MS);

            final Node finalTolerable = tolerable;
            RetryUtils.retryWithBackoff(() -> {
                assertThat(WiremockCounts.ingestionEventsFor(finalTolerable.id()))
                        .as("in-budget HXI latency must NOT prevent ingestion — a zero here means the response timeout fired prematurely or the route stalled on a slow-but-tolerable HXI request")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment().jolokia().dlqDepth())
                        .as("in-budget HXI latency must NOT produce a DLQ entry — non-zero here means the timeout machinery is mis-tuned (firing under the configured budget) or some downstream path is mis-classifying a slow-but-successful response as a failure")
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
            environment().hxiProxy().toxics().get(LATENCY_TOXIC_NAME).remove();
        }
        catch (IOException e)
        {
            log.warn("[reliability] Could not remove HXI latency toxic during cleanup — BaseReliabilityIT reset will catch it next test", e);
        }
    }
}
