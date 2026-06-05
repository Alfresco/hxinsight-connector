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
import java.util.concurrent.TimeUnit;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Brief S3 faults (connect-refused, mid-flight RST) shorter than the upload retry budget must recover via Spring Retry and/or JMS redelivery: the rendition envelope reaches HX Insight, DLQ stays empty, and {@code @Retryable} actually fires.
 *
 * <p>
 * The chaos window is sized by signal, not by clock: the toxic stays in place until the connector's retry counter increments, then is removed so the next attempt sees a healthy proxy. A hard cap stops the suite hanging if {@code @Retryable} never engages.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3TransientBlipReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /** Hard cap on toxic duration. If the connector never reaches the upload PUT, {@link #assertInDeliveryRetryFired} surfaces the regression. */
    private static final int MAX_CHAOS_MS = 3_000;
    private static final int RETRY_SIGNAL_POLL_MS = 25;
    private static final int CONVERGENCE_DELAY_MS = 2_000;
    private static final String RESET_PEER_TOXIC_NAME = "s3-reset-peer";
    /** Counter incremented on every Spring {@code @Retryable onError}, tagged by the underlying exception class. */
    private static final String RETRY_COUNTER = "live_ingester_retry_attempts_total";
    private static final String RETRY_EXCEPTION_TAG = "EndpointServerErrorException";
    private static final double MIN_RETRY_DELTA = 1.0;

    @Test
    void shouldRecoverWhenS3ConnectRefusedResolvesWithinRetryBudget() throws IOException
    {
        log.info("[reliability] Disabling toxic-s3 until the connector's @Retryable observes the connect-refused fault");
        double retryCounterBefore = environment().actuatorMetrics()
                .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG);
        environment().s3Proxy().disable();

        Node victim;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("S3 transient-blip victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "s3-blip-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-blip node {} published — waiting for @Retryable to fire", victim.id());

            waitForRetrySignal(retryCounterBefore, "connect-refused");
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-s3 proxy");
            environment().s3Proxy().enable();
        }

        assertRecovers(victim, "connect-refused");
        assertInDeliveryRetryFired(retryCounterBefore, "connect-refused");
    }

    @Test
    void shouldRecoverWhenS3PeerResetResolvesWithinRetryBudget() throws IOException
    {
        log.info("[reliability] Adding reset_peer toxic on toxic-s3 until the connector's @Retryable observes the RST");
        double retryCounterBefore = environment().actuatorMetrics()
                .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG);
        environment().s3Proxy().toxics().resetPeer(RESET_PEER_TOXIC_NAME, DOWNSTREAM, 0);

        Node victim;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("S3 reset-peer victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "s3-reset-peer-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-RST node {} published — waiting for @Retryable to fire", victim.id());

            waitForRetrySignal(retryCounterBefore, "RST");
        }
        finally
        {
            log.info("[reliability] Removing reset_peer toxic");
            environment().s3Proxy().toxics().get(RESET_PEER_TOXIC_NAME).remove();
        }

        assertRecovers(victim, "RST");
        assertInDeliveryRetryFired(retryCounterBefore, "RST");
    }

    /**
     * Wait until the retry counter increments (proving {@code @Retryable} fired) or the chaos cap elapses. Returns silently on cap — the trailing assertion surfaces the regression.
     */
    private void waitForRetrySignal(double retryCounterBefore, String faultLabel)
    {
        long start = System.nanoTime();
        long maxWaitNanos = TimeUnit.MILLISECONDS.toNanos(MAX_CHAOS_MS);
        while (System.nanoTime() - start < maxWaitNanos)
        {
            double delta = environment().actuatorMetrics()
                    .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG) - retryCounterBefore;
            if (delta >= MIN_RETRY_DELTA)
            {
                log.info("[reliability] @Retryable fired for {} fault — counter delta {}; releasing toxic", faultLabel, delta);
                return;
            }
            try
            {
                Thread.sleep(RETRY_SIGNAL_POLL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("[reliability] @Retryable did not fire within {} ms for {} fault — releasing toxic anyway, expect assertInDeliveryRetryFired to surface the regression", MAX_CHAOS_MS, faultLabel);
    }

    /**
     * Positive recovery signal: the rendition's content envelope only appears in the {@code /ingestion-events} POST after the S3 PUT succeeds, so this proves the upload landed. Then asserts the DLQ stayed empty.
     */
    private void assertRecovers(Node victim, String faultLabel)
    {
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.contentEventsFor(victim.id()))
                    .as("brief S3 fault (%s) within retry budget must NOT prevent rendition upload — a zero here means Spring Retry / JMS redelivery did not recover the S3 PUT for objectId=%s (the connector emits the content envelope in /ingestion-events only after a successful upload)",
                            faultLabel, victim.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("brief S3 fault (%s) within retry budget must NOT produce a DLQ entry — non-zero here means the retry budget was insufficient or the recovery path silently bypassed retries",
                            faultLabel)
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * The in-delivery retry counter must increment during the chaos window. A delta of zero means {@code @Retryable} did not fire — any recovery seen above was JMS redelivery substituting for the missing retry, which is a regression.
     */
    private void assertInDeliveryRetryFired(double retryCounterBefore, String faultLabel)
    {
        RetryUtils.assertWithRetry(() -> {
            double delta = environment().actuatorMetrics()
                    .counterValue(RETRY_COUNTER, "exception", RETRY_EXCEPTION_TAG) - retryCounterBefore;
            assertThat(delta)
                    .as("in-delivery retry contract: %s{exception=%s} must increment by ≥ %.0f during the %s chaos window — a delta of 0 means @Retryable did not fire and any recovery above was JMS-broker-side redelivery substituting for the missing in-delivery retry",
                            RETRY_COUNTER, RETRY_EXCEPTION_TAG, MIN_RETRY_DELTA, faultLabel)
                    .isGreaterThanOrEqualTo(MIN_RETRY_DELTA);
        }, CONVERGENCE_DELAY_MS);
    }
}
