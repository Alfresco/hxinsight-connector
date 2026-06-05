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
 * Chaos on the connector's read path to the Shared File Store while the rest of the transform pipeline stays up. Only the live-ingester's SFS download goes through Toxiproxy; transform-core-aio's writes use the real alias. Two tests: a happy-path smoke and the outage case (SFS unreachable → DLQ via the default in-app DLC). Sister class {@link SfsOutageOptOutReliabilityIT} exercises the broker-rollback path used when the DLC is disabled.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class SfsOutageReliabilityIT
{
    /** Convergence retry step — covers transform round-trip + S3 PUT on a warm container. */
    private static final int CONVERGENCE_DELAY_MS = 5_000;
    /** Log fragment from the SFS download error path. Asserting it ran proves the {@code ingestContent} handler reached the download leg under outage (rather than being silently skipped). */
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
                    .createNodeWithContent(Sentinels.PARENT_ID, "transform-smoke.txt", content, "text/plain");
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
     * Sustained SFS outage on the connector's read path. The default in-app DLC copies the failed transform-response to {@code ActiveMQ.DLQ} and increments {@code live_ingester_transform_response_dlq_total}. Sister class {@link SfsOutageOptOutReliabilityIT} covers the legacy broker-rollback path.
     */
    @Test
    void shouldDeadLetterWhenSfsUnreachable() throws IOException, InterruptedException
    {
        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        log.info("[reliability] Disabling toxic-sfs proxy for sustained outage");
        environment.sfsProxy().disable();
        try
        {
            SfsOutageTrigger.SfsOutageRun run = SfsOutageTrigger.createVictimAndSentinelDuringOutage(environment);
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
                        .as("default DLC must copy each failed transform-response to ActiveMQ.DLQ exactly once. The trigger creates two nodes (victim + sentinel) and the connector issues a transform request for both regardless of source mime — the sentinel's octet-stream `Sentinels.PASSTHROUGH_MIME_TYPE` is a contract for the *catch-all forwarder*, not for skipping ATS, so its transform-response also fails at the SFS-download leg under outage. Two failed transform-responses → 2 DLC fires → 2 DLQ entries. Pinned strict because the env caps redeliveries at 1 (rules out broker-side duplication). Sister IT covers the broker-rollback opt-out path.")
                        .isEqualTo(2);
                assertThat(environment.jolokia().browseDlq())
                        .as("the dead-lettered messages are the two successful (status=201) transform-responses that the connector failed to act on (SFS unreachable) — both must trace back to the transform-response queue. (The JMX browse() envelope carries JMS metadata only, not the payload — see JolokiaProbe.browseDlq() for the reasoning and operator-side body-inspection paths.)")
                        .hasSize(2)
                        .allSatisfy(message -> assertThat(message.envelopeContains(TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE))
                                .as("DLQ envelope should record OriginalDestination=queue://%s but was: %s", TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE, message.envelope())
                                .isTrue());
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
}
