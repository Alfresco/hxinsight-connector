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
 * Positive counterpart to {@link S3NetworkPartitionReliabilityIT}: pins that brief, transient S3 failures <i>shorter</i> than the connector's upload-path retry budget recover via Spring Retry (within a single delivery) and/or JMS redelivery (across deliveries) — the upload PUT eventually completes, the rendition's content envelope reaches HX Insight, and {@code dlqDepth() == 0}.
 *
 * <p>
 * Two flavours of transient S3 fault are pinned, each in its own test, both expected to recover and both expected to engage the in-delivery {@code @Retryable} layer on {@code HttpFileUploader.upload()}:
 * <ul>
 * <li><b>Connect refused</b> ({@link #shouldRecoverWhenS3ConnectRefusedResolvesWithinRetryBudget}) — Toxiproxy {@code disable()} drops the listener entirely; the connector's PUT attempts surface as {@code HttpHostConnectException}, which the route's {@code wrapErrorIfNecessary} folds into {@code EndpointServerErrorException} so {@code @Retryable} (configured for that type) engages.</li>
 * <li><b>Mid-flight TCP reset (RST)</b> ({@link #shouldRecoverWhenS3PeerResetResolvesWithinRetryBudget}) — Toxiproxy {@code reset_peer} toxic tears the connection down with an RST partway through; surfaces as {@code SocketException("Connection reset")} and is wrapped to {@code EndpointServerErrorException} the same way. Folds in the matrix's connection-reset row by exercising the "connection-aborted-while-streaming-bytes" code path that {@code disable()} alone does not reach.</li>
 * </ul>
 *
 * <p>
 * Without these guards, a regression that silently disables retries on the upload path (annotation removed, exception type re-classified, retry-reasons mis-wired, …) would still pass {@link S3NetworkPartitionReliabilityIT} — that asserts DLQ, and DLQ would still happen, just without the retries that should have prevented it. Each test here asserts (a) the rendition's content envelope reached HXI within the retry budget (positive recovery signal), (b) {@code dlqDepth() == 0} (no message parked), and (c) the {@code live_ingester_retry_attempts_total{exception=EndpointServerErrorException}} counter incremented (in-delivery {@code @Retryable} actually fired, decoupled from JMS-broker-side redelivery that would otherwise mask the contract).
 *
 * <p>
 * The chaos window is sized by signal, not by clock: the harness leaves the toxic in place until the connector's retry counter increments by ≥ 1 — proving {@code @Retryable} actually saw the failure — then removes the toxic so the next in-delivery attempt succeeds. A hard cap of {@value #MAX_CHAOS_MS} ms guards against the connector never observing the fault (in which case the trailing {@code assertInDeliveryRetryFired} fails fast with {@code delta == 0}, surfacing the regression rather than hanging the suite). This eliminates the timing fragility of a fixed sleep: under load, the connector's event-read → first-PUT latency drifts upward, and a clock-sized window can leave the upload's first attempt to land entirely after the toxic is removed — first-try success, {@code @Retryable} never fires, false-pass.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test -am verify -Preliability-tests -Dit.test=S3TransientBlipReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3TransientBlipReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /**
     * Hard cap on how long the chaos toxic is left in place. Generous (covers the worst-case event-read → presigned-URL → first-PUT latency under load — a few times the empirical ~315 ms steady-state) but bounded so the suite cannot hang if the connector never reaches the upload PUT (in which case {@link #assertInDeliveryRetryFired} fails fast with {@code delta == 0}, which is the actual regression signal). Sized comfortably under the JMS-redelivery total budget so the redelivered upload's first attempt still succeeds against the restored proxy.
     */
    private static final int MAX_CHAOS_MS = 3_000;
    /**
     * Polling step while waiting for the connector's retry counter to increment. Small enough that the toxic is removed promptly after {@code @Retryable} fires (so the next in-delivery attempt sees a healthy proxy); not so small that we hammer the actuator endpoint.
     */
    private static final int RETRY_SIGNAL_POLL_MS = 25;
    /**
     * Per-attempt step for the convergence retry loop. Generous so the default 15-attempt cap on {@link RetryUtils#retryWithBackoff} comfortably absorbs JMS redelivery + downstream {@code /presigned-urls} + upload PUT + {@code /ingestion-events} round-trips after the proxy is restored.
     */
    private static final int CONVERGENCE_DELAY_MS = 2_000;
    private static final String RESET_PEER_TOXIC_NAME = "s3-reset-peer";
    /**
     * Counter populated by {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.RetryMetricsRecorder RetryMetricsRecorder} on every Spring {@code @Retryable} {@code onError}. Tagged by the underlying application-level exception class (the listener walks past Camel/concurrent framework wrappers). Reading the delta across the chaos window proves the in-delivery retry layer fired, decoupled from JMS-broker-side DLC redelivery.
     */
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
     * Block until the connector's {@code live_ingester_retry_attempts_total{exception=EndpointServerErrorException}} counter has incremented by ≥ {@link #MIN_RETRY_DELTA} relative to {@code retryCounterBefore}, or {@link #MAX_CHAOS_MS} ms elapse — whichever comes first. Returning on the counter increment means {@code @Retryable} has already seen one failed attempt and is about to schedule the next; the caller can then drop the toxic so the retry lands on a healthy proxy and recovers. Hitting the cap returns silently — the trailing {@link #assertInDeliveryRetryFired} reports the regression with the existing message rather than a duplicate one here.
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
     * Asserts the connector fully recovered the upload after the chaos window. Waits for {@link WiremockCounts#contentEventsFor(String)} to reach one for the victim node — the rendition's content envelope ({@code cm:content.file.id}) only appears in the {@code /ingestion-events} POST <i>after</i> the S3 PUT actually succeeds, so this is a positive end-to-end signal that the upload path landed. Pinning on the metadata-only {@code ingestionEventsFor(...)} would race: ACS publishes the metadata POST <i>before</i> the upload runs, so a 1 there can be observed while the upload is still in-flight (or already exhausted into the DLQ). After the content-event signal, asserts {@code dlqDepth() == 0} to pin "no DLQ entry from this scenario" once the upload completed.
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
     * Pins the in-delivery retry contract: the {@code @Retryable} annotation on {@code HttpFileUploader.upload()} must engage during the chaos window. Reads {@code live_ingester_retry_attempts_total{exception=EndpointServerErrorException}} via the actuator metrics endpoint and asserts a delta of ≥ 1 against the pre-chaos baseline. This is the only signal that distinguishes Spring's in-delivery retry from JMS-broker-side DLC redelivery — both can produce additional PUTs at the boundary, but only {@code @Retryable} increments this counter. A delta of 0 means {@code @Retryable} did not fire and any recovery observed at the boundary was JMS-redelivery substituting for the missing in-delivery retry.
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
