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
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

/**
 * Chaos on the connector's read path to the Shared File Store while the rest of the transform pipeline (ACS, transform-router, transform-core-aio, SFS itself) stays up. The reliability env constructed via {@link ReliabilityEnvironment.Builder#withTransformTopology()} routes only the live-ingester's {@code SharedFileStoreClient.downloadFile} GET through Toxiproxy ({@code toxic-sfs} alias); transform-core-aio's writes to SFS use the real {@code shared-file-store} alias and are unaffected by anything we do with the proxy.
 *
 * <p>
 * Default-deployment pair of {@code SfsOutageWithDlqOptInReliabilityIT}: the env does <i>not</i> opt into the {@code transform-response} dead-letter wiring (operator-doc {@code docs/live-ingester.md#transform-response-dead-letter-channel-recommended}), so post-201 failures during rendition processing silently ACK after retries exhaust. Both classes run together as a paired regression guard until the default is flipped.
 *
 * <p>
 * Two test methods:
 * <ul>
 * <li>{@link #shouldTransformAndUploadWhenSfsAvailable} — happy-path smoke: a {@code text/plain} content node with mime mapping {@code text/plain → application/pdf} survives the full transform round-trip, the rendition lands in S3, HX Insight sees both the metadata and content ingestion-event POSTs.</li>
 * <li>{@link #shouldSilentlyDropWhenSfsUnreachable} — default behaviour: the rendition is silently dropped during a sustained SFS outage. Removed when the dead-letter opt-in default flips.</li>
 * </ul>
 *
 * <p>
 * Per-class environment lifecycle (own boot, own teardown) — the env's transform-path toggle adds ~90 s of cold-start time which would dominate the rest of the reliability suite if hoisted into the shared {@link BaseReliabilityIT} env. The toggle stays opt-in until enough transform-path tests exist to justify a different split.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class SfsOutageReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /** Convergence retry step — covers transform round-trip + S3 PUT on a warm container. */
    private static final int CONVERGENCE_DELAY_MS = 5_000;
    /** Settle window before asserting: transform round-trip (~5 s) + @Retryable + JMS redelivery. */
    private static final int SFS_OUTAGE_SETTLE_SECONDS = 10;
    /**
     * Substring from {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.storage.SharedFileStoreClient SharedFileStoreClient}'s {@code onException(Exception.class).log(ERROR, ...)} chain. Fires once per @{@code Retryable} attempt when {@code SharedFileStoreClient.downloadFile} cannot reach the SFS endpoint — i.e. exactly when the {@code ingestContent} handler reaches the SFS-download leg under outage. Asserting a non-zero count of this substring distinguishes "the handler ran and the SFS download failed as designed" from "the handler was silently disabled and the test passes for the wrong reason" — without it, a no-op {@code ingestContent} satisfies all the negative DLQ / content-event assertions trivially. The log line includes the SFS endpoint URL, so it cannot be invariant with respect to the handler running. Chosen over the response route's outer {@code onException(Exception.class).log("Transform :: Retrying ...")} substring because the outer log only
     * fires after the SFS client's @{@code Retryable} exhausts (~30 s with default {@code retryIngestion.attempts}/backoff), which is beyond the IT's settle window.
     */
    private static final String SFS_DOWNLOAD_FAILURE_LOG_FRAGMENT = "Transform :: Unexpected response while downloading rendition";

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class ReliabilityEnvironment with transform topology for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withTransformTopology()
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

    @Test
    void shouldTransformAndUploadWhenSfsAvailable() throws IOException
    {
        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        List<S3Object> initialBucketContent = environment.awsS3Client().listS3Content();

        Node node;
        try (InputStream content = new ByteArrayInputStream("transform-smoke-content".getBytes()))
        {
            node = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "transform-smoke.txt", content, "text/plain");
        }
        log.info("[reliability] Smoke node {} created with text/plain (mapping forces transform to application/pdf — full ATS round-trip)", node.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(node.id()))
                    .as("smoke: connector should post BOTH the metadata event AND the post-rendition content event for the node — failure here means the transform round-trip did not complete (env wiring broken, ATS containers not up, or connector cannot read from toxic-sfs)")
                    .isGreaterThanOrEqualTo(2);
            assertThat(environment.awsS3Client().listS3Content().size())
                    .as("smoke: rendition must land in the test bucket — confirms the connector read the file from SFS via toxic-sfs and PUT it through the (untoxified) toxic-s3 path")
                    .isGreaterThan(initialBucketContent.size());
            assertThat(environment.jolokia().dlqDepth())
                    .as("smoke: no DLQ traffic on the happy path")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Sustained SFS outage on the connector's read path: the {@code toxic-sfs} Toxiproxy listener is fully disabled before the victim node is created and stays disabled for the whole settle window. ACS, transform-router, and transform-core-aio are unaffected (they reach SFS via the real {@code shared-file-store} alias and bypass Toxiproxy), so the transform itself succeeds and a {@code status=201} response with a valid {@code targetReference} arrives on the connector's transform-response queue. The fault hits when {@code SharedFileStoreClient.downloadFile} tries to GET the rendition.
     *
     * <p>
     * Default behaviour: Spring {@code @Retryable} exhausts after the configured budget ({@code ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_ATTEMPTS=2}) and rethrows {@link org.alfresco.hxi_connector.common.exception.EndpointServerErrorException}; without the opt-in the route falls back to {@code DefaultErrorHandler} / {@code FatalFallbackErrorHandler[null]}, the exchange logs {@code "Exhausted after delivery attempt: 1"} and the JMS message is ACK'd. HX Insight gets only the metadata-only ingestion-event, no post-rendition content event fires, no DLQ entry, no metric.
     *
     * <p>
     * The {@code retry-ingestion.attempts} env var (production default {@code -1} / unbounded; this env caps it at {@code 2}) does <i>not</i> control this path — it governs the {@code retryContentTransformation} republish loop for non-201 responses, which doesn't fire on a 201-with-download-failure. Worth noting: that production default is itself a separate latent gap (unbounded redelivery on the response handler's republish branch), but it's not the bug exercised here.
     *
     * <p>
     * The sentinel uses {@code application/octet-stream}: with the catch-all mapping {@code [*]=*} the source mime maps to itself, so the connector's content-copier path bypasses ATS+SFS entirely and reads source content from ACS REST directly (toxic-acs is not disabled). Liveness via the sentinel proves the JMS subscription, ACS REST path, and S3 upload path are all intact while only the SFS path is degraded.
     */
    @Test
    void shouldSilentlyDropWhenSfsUnreachable() throws IOException, InterruptedException
    {
        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        log.info("[reliability] Disabling toxic-sfs proxy for sustained outage");
        environment.sfsProxy().disable();
        try
        {
            SfsOutageRun run = createVictimAndSentinelDuringOutage();
            RetryUtils.assertWithRetry(() -> {
                assertThat(WiremockCounts.ingestionEventsFor(run.sentinel().id()))
                        .as("liveness: sentinel must reach HX Insight — failure here means SFS chaos has knocked out unrelated paths (catch-all [*]=* should bypass ATS+SFS entirely)")
                        .isGreaterThanOrEqualTo(1);
                assertThat(WiremockCounts.ingestionEventsFor(run.victim().id()))
                        .as("victim must surface in HX Insight via at least one metadata-only ingestion-event (ACS fires both Created and Updated when a node is created with content, and the connector's metadata path runs before the transform request even goes out — neither metadata POST depends on SFS)")
                        .isGreaterThanOrEqualTo(1);
                assertThat(WiremockCounts.contentEventsFor(run.victim().id()))
                        .as("the post-rendition content event (carrying cm:content.file.id) must NOT fire for the victim — SFS is unreachable from the connector, so SharedFileStoreClient.downloadFile exhausts retries and the rendition is never uploaded. A non-zero here would mean the rendition path recovered (proxy was re-enabled too early?) or some other path published a content event without going through SFS")
                        .isZero();
                assertThat(environment.jolokia().dlqDepth())
                        .as("default behaviour: no DLQ entry — the SFS outage is silently ACK'd. Paired with SfsOutageWithDlqOptInReliabilityIT which asserts the opposite when the opt-in is on")
                        .isZero();
                assertThat(environment.jolokia().topicSubscriberCount(ReliabilityEnvironment.REPO_EVENT_TOPIC))
                        .as("SFS partition must not knock out the JMS subscription — live-ingester should still be subscribed to %s", ReliabilityEnvironment.REPO_EVENT_TOPIC)
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment.liveIngesterContainer().getLogs())
                        .as("the SFS download path must actually have been attempted — without this positive signal, a regression that silently disables the rendition handler (e.g. an early-return in ATSTransformResponseHandler.ingestContent) would still satisfy the negative DLQ / content-event assertions above and the IT would pass for the wrong reason. Asserting on the SharedFileStoreClient route's `Transform :: Unexpected response while downloading rendition - Endpoint: ...` ERROR log distinguishes 'handler ran and SFS download failed as designed' from 'handler bypassed entirely'")
                        .contains(SFS_DOWNLOAD_FAILURE_LOG_FRAGMENT);
            }, CONVERGENCE_DELAY_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-sfs proxy");
            environment.sfsProxy().enable();
        }
    }

    /**
     * Shared trigger for the SFS-outage assertion methods: with {@code toxic-sfs} already disabled by the caller, creates a {@code text/plain} victim (forced through the transform path by the mime-type mapping) and a {@code application/octet-stream} sentinel (catch-all passthrough, bypasses ATS+SFS), then waits long enough for the failure cascade (transform round-trip + Spring {@code @Retryable} exhaustion + JMS redelivery + any ACK / DLQ outcome) to settle before assertions are evaluated. The caller is responsible for re-enabling the proxy in a {@code finally} block.
     */
    private SfsOutageRun createVictimAndSentinelDuringOutage() throws IOException, InterruptedException
    {
        Node victim;
        try (InputStream victimContent = new ByteArrayInputStream("sfs-outage victim".getBytes()))
        {
            victim = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "sfs-outage-victim.txt", victimContent, "text/plain");
        }
        log.info("[reliability] Victim node {} published — transform path expected to round-trip but rendition download from toxic-sfs to fail", victim.id());

        Node sentinel;
        try (InputStream sentinelContent = new ByteArrayInputStream("sfs-outage sentinel".getBytes()))
        {
            sentinel = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "sfs-outage-sentinel.bin", sentinelContent, "application/octet-stream");
        }
        log.info("[reliability] Sentinel node {} published with application/octet-stream (catch-all [*]=* path, bypasses ATS+SFS) — verifying liveness", sentinel.id());

        Thread.sleep(Duration.ofSeconds(SFS_OUTAGE_SETTLE_SECONDS).toMillis());

        return new SfsOutageRun(victim, sentinel);
    }

    /**
     * Holder for the identifiers produced by {@link #createVictimAndSentinelDuringOutage()} so both outage tests can drive their assertions off the same trigger run without duplicating the node-creation code.
     */
    private record SfsOutageRun(Node victim, Node sentinel)
    {}
}
