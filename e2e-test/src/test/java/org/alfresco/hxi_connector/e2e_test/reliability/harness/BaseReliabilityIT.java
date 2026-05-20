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
package org.alfresco.hxi_connector.e2e_test.reliability.harness;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.client.WireMock;
import eu.rekawek.toxiproxy.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;

/**
 * Common base for every reliability IT that opts into the shared {@link ReliabilityEnvironment} model. Boots one environment per Failsafe JVM via {@link SharedReliabilityEnvironmentExtension}, then resets per-test state in {@link #resetBetweenTests()} so each test sees a clean baseline.
 *
 * <p>
 * Per-test reset surface (kept tight on purpose):
 * <ul>
 * <li>{@link WireMock#reset()} — clears mock state and recorded interactions so {@link WiremockCounts} starts at zero.</li>
 * <li>Removes every Toxiproxy toxic on {@link ReliabilityEnvironment#activemqProxy()}, {@link ReliabilityEnvironment#acsProxy()}, and {@link ReliabilityEnvironment#hxiProxy()} and re-enables all three proxies. Tests that injected a toxic and forgot to clean up no longer corrupt their successors.</li>
 * <li>Purges {@code ActiveMQ.DLQ} via {@link JolokiaProbe#purgeQueue(String)} so {@code dlqDepth() == 0} assertions are not contaminated by predecessors (notably the poison-pill / dead-letter regression-guard tests, which intentionally leave {@code 1} message on the DLQ).</li>
 * <li>Asserts {@link JolokiaProbe#brokerHealthy()} and that the {@code alfresco.repo.event2} subscription is still registered — fail-fast precondition. A failure here means the previous test left the environment in a bad shape; the message says so explicitly so triage is one log scan, not archaeology.</li>
 * <li>Waits for the live-ingester ↔ HX Insight HTTP boundary to be idle for {@value #IDLE_WINDOW_MS} ms before yielding to the next test, with a hard cap of {@value #IDLE_MAX_WAIT_MS} ms. After {@link WireMock#reset()} the default {@code 200}/{@code 202} stubs are back, so any retry that the previous test left mid-backoff naturally ACKs once it fires; the idle wait gives those retries time to fire and complete. Without this, a chaos test that asserts on the first DLQ entry and exits while parent-folder retries are still mid-backoff can leak those retries into the next test's chaos window — observed concretely as {@link org.alfresco.hxi_connector.e2e_test.reliability.hxi.HxiTransientBlipReliabilityIT} flaking when run after {@link org.alfresco.hxi_connector.e2e_test.reliability.hxi.IngestionEventRetryExhaustionReliabilityIT}.</li>
 * </ul>
 *
 * <p>
 * Tests that intentionally tear down infrastructure (process-level chaos that stops the broker container) must <b>not</b> extend this class — they need their own per-class environment lifecycle. Keep them as standalone {@code @TestInstance(PER_CLASS)} ITs that build a {@link ReliabilityEnvironment} in {@code @BeforeAll} and close it in {@code @AfterAll}.
 */
@Slf4j
@ExtendWith(SharedReliabilityEnvironmentExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;
    private static final String DLQ_QUEUE = "ActiveMQ.DLQ";
    /**
     * Convergence retry step for the per-test post-condition checks. Short on purpose — these are precondition assertions, not steady-state convergence assertions.
     */
    private static final int PRECONDITION_DELAY_MS = 500;
    /**
     * Window of HTTP-boundary inactivity that must elapse before the next test is allowed to start. Sized above the longest single-attempt backoff in the test profile (Spring Retry initial delay 200 ms; ActiveMQ broker default 1 s) so a stragger retry that the previous test left mid-backoff has time to fire against the now-clean WireMock stubs and ACK before we yield.
     */
    private static final long IDLE_WINDOW_MS = 1_500;
    /**
     * Hard cap on the idle wait. Reached only when the broker keeps producing requests faster than the window — the wait ends with a warning so a misbehaving suite produces visible diagnostics rather than a hung test boundary.
     */
    private static final long IDLE_MAX_WAIT_MS = 10_000;
    /**
     * Polling step for the idle loop. Small enough that the post-idle purge fires promptly; not so small that we spam the WireMock admin endpoint.
     */
    private static final long IDLE_POLL_MS = 50;
    /**
     * Higher-priority override (file-based default stub runs at the implicit Wiremock priority of {@code 5}) that swaps the presigned-URL host from {@code aws-mock} to {@code toxic-s3} so connector-side uploads land on the Toxiproxy listener rather than going to Localstack directly. Re-installed on every test boundary because {@link WireMock#reset()} clears it. Kept above the {@code 1}-priority used by per-test scenarios (e.g. {@link PresignedUrlRetryReliabilityIT}) so those scenarios can still win when needed.
     */
    private static final int TOXIC_S3_OVERRIDE_PRIORITY = 3;
    /**
     * Inline body that mirrors {@code __files/presigned-urls.json} but with the {@code toxic-s3} host. Localstack ignores presigned-URL signatures by default (community edition default), so reusing the static signature is safe even though we changed the host. Using a fixed UUID and a templated dummy filename matches the original file shape so the connector path is byte-equivalent to the non-reliability tests.
     */
    private static final String TOXIC_S3_PRESIGNED_URLS_BODY = """
            [
              {
                "id": "12341234-1234-1234-1234-123412341234",
                "url": "http://toxic-s3:4566/test-hxinsight-bucket/dummy-{{ now format='HHmmssSSS' }}-{{ randomValue length=7 type='ALPHABETIC' }}.bin?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240109T154539Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Credential=test%2F20240109%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Expires=60&X-Amz-Signature=31cf819142af9946947f6447d18f624bbad7f69d78ab72959a6cbb0048550f3c"
              }
            ]
            """;

    @BeforeEach
    final void resetBetweenTests()
    {
        ReliabilityEnvironment env = environment();

        log.debug("[reliability] Per-test reset: WireMock + toxics + DLQ purge + healthy precondition");
        // Re-anchor the static WireMock client at the shared mock before reset(). A preceding chaos class
        // (BaseProcessChaosReliabilityIT subclass) may have mutated the static to point at its own per-class
        // Wiremock container, which is now stopped — without this, reset() would hit a closed port.
        WireMock.configureFor(env.hxInsightMock().getHost(), env.hxInsightMock().getPort());
        WireMock.reset();
        installToxicS3PresignedUrlOverride();
        clearToxicsAndEnableProxy(env.activemqProxy());
        clearToxicsAndEnableProxy(env.acsProxy());
        clearToxicsAndEnableProxy(env.hxiProxy());
        clearToxicsAndEnableProxy(env.s3Proxy());
        env.jolokia().purgeQueue(DLQ_QUEUE);
        waitForBrokerIdle();
        env.jolokia().purgeQueue(DLQ_QUEUE);

        RetryUtils.assertWithRetry(() -> {
            assertThat(env.jolokia().brokerHealthy())
                    .as("preceding test left the broker unhealthy — env reset cannot recover, fix the offending test")
                    .isTrue();
            assertThat(env.jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("preceding test left the live-ingester without a subscriber on %s — env reset cannot recover, fix the offending test", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
            assertThat(env.jolokia().dlqDepth())
                    .as("DLQ purge did not drain to zero — broker is in an unexpected state; fix the offending test or extend purge logic")
                    .isZero();
        }, PRECONDITION_DELAY_MS);
    }

    /**
     * Accessor for the shared {@link ReliabilityEnvironment}. Resolved through the extension on every call so the indirection survives a hypothetical future migration to a non-static cache.
     */
    protected final ReliabilityEnvironment environment()
    {
        return SharedReliabilityEnvironmentExtension.sharedEnvironment();
    }

    /**
     * Re-installs the higher-priority {@code POST /presigned-urls} override after every {@link WireMock#reset()} so the URL the connector receives points at {@code toxic-s3} rather than the file-based default's {@code aws-mock}. Without this, content uploads would bypass the Toxiproxy S3 listener and S3-side chaos tests would have no effect on the upload PUT.
     */
    private static void installToxicS3PresignedUrlOverride()
    {
        stubFor(post(urlEqualTo("/presigned-urls"))
                .atPriority(TOXIC_S3_OVERRIDE_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TOXIC_S3_PRESIGNED_URLS_BODY)));
    }

    /**
     * Block until the live-ingester ↔ HX Insight HTTP boundary has been idle for {@value #IDLE_WINDOW_MS} ms or {@value #IDLE_MAX_WAIT_MS} ms total — whichever comes first. Idleness is measured against the connector-facing WireMock paths exercised by every ingestion event ({@link WiremockCounts#PRESIGNED_URLS_PATH} and {@link WiremockCounts#INGESTION_EVENTS_PATH}), so a leaked retry from the previous test reliably surfaces as the request count growing while we poll. Hitting the hard cap logs at {@code WARN} but does not fail the precondition; the dlqDepth assertion that follows is the actual contract.
     */
    private static void waitForBrokerIdle()
    {
        long lastRequestCount = -1;
        long lastChangeNanos = System.nanoTime();
        long startNanos = System.nanoTime();
        long idleWindowNanos = TimeUnit.MILLISECONDS.toNanos(IDLE_WINDOW_MS);
        long maxWaitNanos = TimeUnit.MILLISECONDS.toNanos(IDLE_MAX_WAIT_MS);
        while (System.nanoTime() - startNanos < maxWaitNanos)
        {
            int currentCount = WiremockCounts.presignedUrlRequests() + WiremockCounts.ingestionEvents();
            if (currentCount != lastRequestCount)
            {
                lastRequestCount = currentCount;
                lastChangeNanos = System.nanoTime();
            }
            else if (System.nanoTime() - lastChangeNanos >= idleWindowNanos)
            {
                return;
            }
            try
            {
                Thread.sleep(IDLE_POLL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("[reliability] Broker did not go idle within {} ms — proceeding anyway. Last observed request count was {}; expect downstream tests to flake if a stragger retry from the previous test walks into the next chaos window.",
                IDLE_MAX_WAIT_MS, lastRequestCount);
    }

    private static void clearToxicsAndEnableProxy(Proxy proxy)
    {
        try
        {
            proxy.toxics().getAll().forEach(toxic -> {
                try
                {
                    toxic.remove();
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("[reliability] Failed to remove toxic " + toxic.getName() + " between tests", e);
                }
            });
            if (!proxy.isEnabled())
            {
                proxy.enable();
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Failed to clear toxics / re-enable proxy between tests", e);
        }
    }
}
