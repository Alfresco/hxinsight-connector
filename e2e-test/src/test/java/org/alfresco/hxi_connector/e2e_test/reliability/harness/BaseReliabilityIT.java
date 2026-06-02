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
 * Base class for reliability ITs that share one {@link ReliabilityEnvironment} per Failsafe JVM. {@link #resetBetweenTests()} runs before each test and gives the next test a clean broker, clean WireMock, and clean Toxiproxy state.
 *
 * <p>
 * Tests that stop the broker or live-ingester container (process-level chaos) must <b>not</b> extend this class — they own their own environment via {@code @BeforeAll}/{@code @AfterAll}.
 */
@Slf4j
@ExtendWith(SharedReliabilityEnvironmentExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = ReliabilityEnvironment.REPO_EVENT_TOPIC;
    private static final String DLQ_QUEUE = "ActiveMQ.DLQ";

    private static final int PRECONDITION_DELAY_MS = 500;

    /** How long the HTTP boundary must stay quiet (no new WireMock requests) before yielding to the next test. */
    private static final long IDLE_WINDOW_MS = 1_500;
    /** Hard cap on the HTTP idle wait. Logs WARN and falls through if hit. */
    private static final long IDLE_MAX_WAIT_MS = 10_000;
    private static final long IDLE_POLL_MS = 50;

    /** Hard cap on the broker drain wait. Logs WARN and falls through if hit. */
    private static final long TOPIC_IDLE_MAX_WAIT_MS = 15_000;
    private static final long TOPIC_IDLE_POLL_MS = 100;
    /**
     * Consecutive zero-in-flight samples required. A single zero is not enough — the broker briefly shows zero between a NACK and the next redelivery, so we need to see it stay zero for ~300 ms.
     */
    private static final int TOPIC_IDLE_STABLE_SAMPLES = 3;

    /** Short pause after broker checks. Lets the consumer thread finish any last bookkeeping before the next test starts. */
    private static final long FINAL_SETTLE_MS = 250L;

    /**
     * Priority for the toxic-s3 stub override. Below WireMock's default ({@code 5}) so it wins over the file-based default; above per-test scenarios ({@code 1}) so those scenarios can still override it when needed.
     */
    private static final int TOXIC_S3_OVERRIDE_PRIORITY = 3;
    /** Same body as {@code __files/presigned-urls.json} but pointing at {@code toxic-s3} so uploads go through the Toxiproxy listener. */
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

        // Re-anchor WireMock at the shared mock — a process-chaos class may have left the static client
        // pointing at a now-stopped per-class container.
        WireMock.configureFor(env.hxInsightMock().getHost(), env.hxInsightMock().getPort());
        // First reset clears stubs and counts so waitForBrokerIdle has a stable reference to measure against.
        WireMock.reset();
        WireMock nucleusClient = env.nucleusWireMock();

        // Independently Clean the nucleus Mock - It has own host/port and static WireMock Client Above
        nucleusClient.resetMappings(); // Mock Mappings
        nucleusClient.resetRequests(); // Mock Requests
        nucleusClient.resetScenarios(); // Reset any Scenario Added

        installToxicS3PresignedUrlOverride();
        clearToxicsAndEnableProxy(env.activemqProxy());
        clearToxicsAndEnableProxy(env.acsProxy());
        clearToxicsAndEnableProxy(env.hxiProxy());
        clearToxicsAndEnableProxy(env.s3Proxy());
        clearToxicsAndEnableProxy(env.nucleusproxy());

        env.jolokia().purgeQueue(DLQ_QUEUE);
        waitForBrokerIdle();
        waitForTopicFullyDrained(env);
        // Second reset, AFTER the drain has fully quiesced, clears any residual WireMock counts that the
        // previous test's late-arriving HTTP retry parked between the first reset and the idle window
        // closing — without this, that residual POST counts as a 1-from-zero in the next test, breaking
        // strict-equality assertions like ActiveMqRecoveryBurstReliabilityIT's "presigned-urls must be 0".
        WireMock.reset();
        installToxicS3PresignedUrlOverride();
        env.jolokia().purgeQueue(DLQ_QUEUE);

        RetryUtils.assertWithRetry(() -> {
            assertThat(env.jolokia().brokerHealthy())
                    .as("preceding test left the broker unhealthy")
                    .isTrue();
            assertThat(env.jolokia().topicSubscriberCount(REPO_EVENT_TOPIC))
                    .as("preceding test left the live-ingester without a subscriber on %s", REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
            assertThat(env.jolokia().dlqDepth())
                    .as("DLQ purge did not drain to zero")
                    .isZero();
            assertThat(WiremockCounts.presignedUrlRequests() + WiremockCounts.ingestionEvents())
                    .as("post-reset WireMock counts must be zero — non-zero means a stale request slipped past the second reset")
                    .isZero();
        }, PRECONDITION_DELAY_MS);

        try
        {
            Thread.sleep(FINAL_SETTLE_MS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    protected final ReliabilityEnvironment environment()
    {
        return SharedReliabilityEnvironmentExtension.sharedEnvironment();
    }

    /**
     * Re-installs the {@code POST /presigned-urls} override that points uploads at {@code toxic-s3} (so S3 chaos tests work). {@link WireMock#reset()} clears it, so we put it back on every reset.
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
     * Wait until the WireMock request count stops changing for {@value #IDLE_WINDOW_MS} ms. Catches retries the previous test left mid-backoff that fire against the now-clean stubs and ACK.
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
        log.warn("[reliability] HTTP boundary did not go idle within {} ms; last observed request count was {}",
                IDLE_MAX_WAIT_MS, lastRequestCount);
    }

    /**
     * Wait until the broker reports {@code InFlightCount == 0} on the repo-events topic for {@value #TOPIC_IDLE_STABLE_SAMPLES} samples in a row. {@code InFlightCount} is messages dispatched to the consumer but not yet ACK'd. The stability requirement matters: a single zero can land between a NACK and the next redelivery, while we want to know the broker is truly drained.
     *
     * <p>
     * {@code EnqueueCount == DequeueCount} would be a more obvious "fully drained" check but it does not work here — when a message is dead-lettered the topic's dequeue counter does not increment (DLQ is a separate queue), so {@code EnqueueCount} stays permanently ahead of {@code DequeueCount} for any test that produces DLQ entries.
     */
    private static void waitForTopicFullyDrained(ReliabilityEnvironment env)
    {
        long startNanos = System.nanoTime();
        long maxWaitNanos = TimeUnit.MILLISECONDS.toNanos(TOPIC_IDLE_MAX_WAIT_MS);
        int stableSamples = 0;
        int lastInFlight = -1;
        while (System.nanoTime() - startNanos < maxWaitNanos)
        {
            int inFlight = env.jolokia().topicInFlightCount(ReliabilityEnvironment.REPO_EVENT_TOPIC);
            if (inFlight == 0)
            {
                stableSamples++;
                if (stableSamples >= TOPIC_IDLE_STABLE_SAMPLES)
                {
                    return;
                }
            }
            else
            {
                stableSamples = 0;
            }
            lastInFlight = inFlight;
            try
            {
                Thread.sleep(TOPIC_IDLE_POLL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.warn("[reliability] Topic {} did not stay at InFlightCount=0 for {} samples within {} ms; last seen inFlight={}, stableSamples={}",
                ReliabilityEnvironment.REPO_EVENT_TOPIC, TOPIC_IDLE_STABLE_SAMPLES, TOPIC_IDLE_MAX_WAIT_MS, lastInFlight, stableSamples);
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
            proxy.enable();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[reliability] Failed to clear toxics / re-enable proxy between tests", e);
        }
    }
}
