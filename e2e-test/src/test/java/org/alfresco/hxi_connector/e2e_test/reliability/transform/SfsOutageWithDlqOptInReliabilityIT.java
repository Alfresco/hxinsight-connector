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
package org.alfresco.hxi_connector.e2e_test.reliability.transform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Sister of {@link SfsOutageReliabilityIT}: boots the env with the {@code transform-response} dead-letter opt-in enabled (operator-doc {@code docs/live-ingester.md#transform-response-dead-letter-channel-recommended}) and asserts that a sustained SFS outage surfaces the failed transform-response on {@code ActiveMQ.DLQ} with a {@code live_ingester_transform_response_dlq_total} counter increment.
 *
 * <p>
 * Same trigger as the default-deployment pair (proxy disabled, {@code text/plain} victim through ATS, {@code application/octet-stream} sentinel for liveness); opposite outcome on the DLQ assertion. Both run side-by-side on every CI build. Once the opt-in default flips, {@link SfsOutageReliabilityIT} is removed and this class becomes the sole regression guard for the SFS-outage scenario.
 *
 * <p>
 * Per-class environment lifecycle (own boot, own teardown). Cost: ~90 s per env boot, two boots for the SFS-outage scenario.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class SfsOutageWithDlqOptInReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /** Convergence retry step — covers transform round-trip + DLQ park on a warm container. */
    private static final int CONVERGENCE_DELAY_MS = 5_000;
    /** Settle window before asserting: transform round-trip (~5 s) + @Retryable + JMS redelivery + DLQ park. */
    private static final int SFS_OUTAGE_SETTLE_SECONDS = 10;

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class ReliabilityEnvironment with transform topology + transform-response DLC opt-in for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withTransformTopology()
                .withTransformResponseDeadLetterEnabled()
                .build();
        environment.start();
    }

    @AfterAll
    final void closeEnvironment()
    {
        if (environment != null)
        {
            log.info("[reliability] Closing per-class ReliabilityEnvironment for {}", getClass().getSimpleName());
            environment.close();
        }
    }

    /**
     * With the opt-in enabled: a sustained SFS outage surfaces the failed transform-response on the DLQ. Same trigger as {@link SfsOutageReliabilityIT#shouldSilentlyDropWhenSfsUnreachable}, flipped by {@link ReliabilityEnvironment.Builder#withTransformResponseDeadLetterEnabled()}.
     */
    @Test
    void shouldDeadLetterWhenSfsUnreachable() throws IOException, InterruptedException
    {
        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        log.info("[reliability] Disabling toxic-sfs proxy for sustained outage");
        environment.sfsProxy().disable();
        try
        {
            Node victim;
            try (InputStream victimContent = new ByteArrayInputStream("sfs-outage victim".getBytes()))
            {
                victim = environment.repositoryClient()
                        .createNodeWithContent(PARENT_ID, "sfs-outage-victim.txt", victimContent, "text/plain");
            }
            log.info("[reliability] Victim node {} published — transform path expected to round-trip but rendition download from toxic-sfs to fail and the response message to land on the DLQ via the opt-in route handler", victim.id());

            Node sentinel;
            try (InputStream sentinelContent = new ByteArrayInputStream("sfs-outage sentinel".getBytes()))
            {
                sentinel = environment.repositoryClient()
                        .createNodeWithContent(PARENT_ID, "sfs-outage-sentinel.bin", sentinelContent, "application/octet-stream");
            }
            log.info("[reliability] Sentinel node {} published with application/octet-stream (catch-all [*]=* path, bypasses ATS+SFS) — verifying liveness past the dead-lettered failure", sentinel.id());

            Thread.sleep(Duration.ofSeconds(SFS_OUTAGE_SETTLE_SECONDS).toMillis());

            RetryUtils.assertWithRetry(() -> {
                assertThat(environment.jolokia().dlqDepth())
                        .as("opt-in enabled: SFS outage must surface on the DLQ. Zero here means the route's deadLetterChannel did not catch the post-201 download failure")
                        .isGreaterThanOrEqualTo(1);
                assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                        .as("liveness: sentinel must flow past the dead-lettered failure")
                        .isGreaterThanOrEqualTo(1);
            }, CONVERGENCE_DELAY_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-sfs proxy");
            environment.sfsProxy().enable();
        }
    }
}
