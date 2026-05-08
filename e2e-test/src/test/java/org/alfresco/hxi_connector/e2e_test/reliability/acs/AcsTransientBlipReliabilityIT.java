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
 * Positive counterpart to {@link AcsShortPartitionReliabilityIT} and {@link AcsLatencyReliabilityIT}: pins that a brief ACS REST blip <i>shorter</i> than the connector's content-download retry budget recovers via {@link org.springframework.retry.annotation.Retryable @Retryable} (within a single delivery) or JMS redelivery (across deliveries) — the event reaches HX Insight and {@code dlqDepth() == 0}.
 *
 * <p>
 * Without this guard, a regression that silently disables {@code @Retryable} on {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.AlfrescoRepositoryContentClient#downloadContent} (annotation removed, exception type re-classified, retry-reasons mis-wired, …) would still pass {@code AcsLatencyReliabilityIT} / {@code AcsShortPartitionReliabilityIT} — those assert DLQ, and DLQ would still happen, just without the retries that should have prevented it. This test fails loudly in that scenario.
 *
 * <p>
 * Timing budget (test profile, see {@link ReliabilityEnvironment}):
 *
 * <pre>
 *   Spring Retry within delivery: 2 attempts × 200 ms backoff       ~200 ms
 *   JMS redelivery delay (REDELIVERYDELAYMS=200)                     200 ms
 *   Spring Retry within redelivery: 2 attempts × 200 ms backoff     ~200 ms
 *   ────────────────────────────────────────────────────────────────────────
 *   Total before DLQ (from "connector reads event")                 ~600 ms
 * </pre>
 *
 * <p>
 * Disabling {@code toxic-acs} for {@value #BLIP_MS} ms means the connector's first content-download attempt (fired ~50 ms after the event lands on the broker) is guaranteed to hit the disabled proxy and surface an {@code IOException} → {@code EndpointServerErrorException} into {@code @Retryable}. The proxy comes back up before the total budget exhausts, so either the in-delivery second attempt or the JMS-redelivered first attempt succeeds. Either path satisfies the operational invariant: a transient ACS blip within budget does not produce a DLQ entry.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=AcsTransientBlipReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsTransientBlipReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /**
     * Duration the {@code toxic-acs} proxy is held disabled. Sized to comfortably cover the in-delivery Spring Retry budget (~200 ms) so the first content-download attempt provably fails, but well under the total ~600 ms budget so the message can recover before DLQ. See class-level Javadoc for the breakdown.
     */
    private static final int BLIP_MS = 300;
    /**
     * Per-attempt step for the convergence retry loop. Generous so the default 15-attempt cap on {@link RetryUtils#retryWithBackoff} comfortably absorbs JMS redelivery + downstream `/presigned-urls` + `/ingestion-events` round-trips after the proxy is re-enabled.
     */
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldRecoverWhenAcsBlipResolvesWithinRetryBudget() throws IOException, InterruptedException
    {
        log.info("[reliability] Disabling toxic-acs for ~{} ms (shorter than total retry budget)", BLIP_MS);
        environment().acsProxy().disable();

        Node victim;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("ACS transient-blip victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "acs-blip-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-blip node {} published — expecting @Retryable / JMS redelivery to recover within budget", victim.id());

            Thread.sleep(BLIP_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-acs proxy");
            environment().acsProxy().enable();
        }

        final Node finalVictim = victim;
        RetryUtils.retryWithBackoff(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(finalVictim.id()))
                    .as("brief ACS blip within retry budget must NOT prevent ingestion — a zero here means @Retryable / JMS redelivery did not recover the event for objectId=%s",
                            finalVictim.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("brief ACS blip within retry budget must NOT produce a DLQ entry — non-zero here means the retry budget was insufficient or the recovery path silently bypassed @Retryable")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }
}
