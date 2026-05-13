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
 * Pins the default-deployment behaviour when ACS itself rejects a transform request — simulating an installation where the transform service is not deployed or has been administratively disabled. The reliability environment's ACS already runs with {@code transform.service.enabled=false} (from {@code DockerContainers.getMinimalRepoJavaOpts}), which is exactly that condition.
 *
 * <p>
 * Empirically: ACS does NOT leave the {@code acs-repo-transform-request} queue idle when its transform service is disabled. A repo-side handler still consumes the request and synthesises a structured failure response on the connector's transform-response queue — {@code status=400}, {@code errorDetails="04040040 Transformation failed occurred."}, {@code targetReference=null}. The connector logs a route-level WARN line for the non-201 body, then takes the by-design early-return inside {@code ATSTransformResponseHandler.ingestContent} — JMS message ACK'd, no DLQ entry, no Micrometer counter, no structured per-failure log line.
 *
 * <p>
 * Same code path as {@link TransformResponseFailureReliabilityIT}, exercised end-to-end through ACS instead of via a synthetic message. The two ITs pin the same default-deployment contract from different angles: synthetic-400 IT is fast (no transform containers needed) and acts as a unit-shaped regression guard; this IT exercises the real ACS-rejection path so any future change in ACS' rejection shape would surface here. Operators wanting a structured drop signal (DLQ entry, exception-tagged counter) opt in via {@code ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS=true} paired with {@code ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED=true} — see the sister class {@link TransformPipelineUnreachableWithDlqOptInReliabilityIT}.
 *
 * <p>
 * Owns its own per-class {@link ReliabilityEnvironment} because the connector's mime-type mapping is a JVM-startup config; setting {@code [text/plain]→application/pdf} on the shared env would change every other shared-env test's content behaviour. The mapping is injected via {@code JAVA_TOOL_OPTIONS} on the live-ingester container before {@link ReliabilityEnvironment#start()}; the catch-all {@code [*]→*} keeps every other MIME on full passthrough so the sentinel (using {@code application/octet-stream}) can prove liveness without colliding with the stuck-rendition scenario.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class TransformPipelineUnreachableReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String TRANSFORM_REQUEST_QUEUE = "acs-repo-transform-request";
    /**
     * Convergence retry step for the post-condition checks once both nodes have been created. Generous enough to absorb the connector's metadata POST, ACS's internal transform-rejection round-trip, and the sentinel passthrough flow without flapping.
     */
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Fixed wait between creating the stuck node and the sentinel. The stuck-node assertion is negative (no content POST fires) — a retry-with-backoff would have to time out, so we let the metadata POST land and the ACS-side rejection complete synchronously before moving on.
     */
    private static final long STUCK_OBSERVATION_WAIT_MS = 10_000L;
    /**
     * MIME for the sentinel — falls under the catch-all {@code [*]→*} mapping so the connector takes the passthrough path rather than the transform-required path. {@code text/plain} cannot be reused (it's the MIME we mapped to {@code application/pdf} to force the stuck-rendition scenario).
     */
    private static final String SENTINEL_MIME_TYPE = "application/octet-stream";
    /**
     * Substring of the INFO log line emitted by {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response.ATSTransformResponseHandler#recordSilentDrop} on the default-deployment status=400 branch. Identical to the synthetic-400 IT's signal — both ITs exercise the same silent-drop branch from different angles (synthetic message via direct queue publish vs. ACS-rejected transform-request end-to-end), so they share the same positive log assertion. Without it the negative assertions (no DLQ, metadata-only ingestion event) hold for a fully no-op handler too.
     */
    private static final String SILENT_DROP_LOG_FRAGMENT = "Transform :: Silently dropped failed transform-response";

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class ReliabilityEnvironment for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder().build();
        environment.liveIngesterContainer().withEnv("JAVA_TOOL_OPTIONS",
                "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n"
                        + " -Dalfresco.transform.mime-type.mapping.[text/plain]=application/pdf"
                        + " -Dalfresco.transform.mime-type.mapping.[*]=*");
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
    void shouldSilentlyDropRenditionWhenAcsRejectsTransformRequest() throws IOException, InterruptedException
    {
        StuckRendition rendition = createStuckRenditionAndSentinel(environment);

        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(rendition.sentinelNode().id()))
                    .as("liveness: sentinel must reach HX Insight despite the stuck rendition request")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(rendition.stuckNode().id()))
                    .as("only the metadata-only ingestion-event fires for the stuck node — the rendition path short-circuits on the status=400 transform-response from ACS via the by-design early-return. Pinned at exactly 1 because that's the metadata POST count")
                    .isEqualTo(1);
            assertThat(environment.jolokia().queueDepth(TRANSFORM_REQUEST_QUEUE))
                    .as("the transform-request queue should drain — ACS's repo-side handler does consume the request even with transform.service.enabled=false")
                    .isZero();
            assertThat(environment.jolokia().dlqDepth())
                    .as("default deployment: 400 transform-response is ACK'd silently after a route-level WARN log. DLQ stays at zero. Sister IT covers the opt-in path that lands on the DLQ")
                    .isZero();
            assertThat(environment.liveIngesterContainer().getLogs())
                    .as("the response handler's silent-drop branch must actually have executed for the ACS-rejected transform-request — without this positive signal, a fully no-op `ingestContent` would still satisfy the negative assertions above. Asserting the `Transform :: Silently dropped failed transform-response` INFO line binds the negative outcomes to the path under test")
                    .contains(SILENT_DROP_LOG_FRAGMENT);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Shared trigger used by both this IT and {@link TransformPipelineUnreachableWithDlqOptInReliabilityIT}: creates a {@code text/plain} stuck node (the mime-type mapping forces a transform to {@code application/pdf}, which ACS rejects because its transform service is disabled), waits long enough for ACS to synthesise the {@code status=400} transform-response and for the connector to process it, then publishes a sentinel node on the catch-all passthrough path so liveness can be verified independently of the stuck-rendition outcome.
     */
    static StuckRendition createStuckRenditionAndSentinel(ReliabilityEnvironment environment) throws IOException, InterruptedException
    {
        Node stuckNode;
        try (InputStream stuckContent = new ByteArrayInputStream("text-content-needing-transform".getBytes()))
        {
            stuckNode = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "needs-transform.txt", stuckContent, "text/plain");
        }
        log.info("[reliability] Stuck node {} created with text/plain (mapping forces transform to application/pdf — ACS will reject because transform.service.enabled=false)", stuckNode.id());

        Thread.sleep(STUCK_OBSERVATION_WAIT_MS);

        Node sentinelNode;
        try (InputStream sentinelContent = new ByteArrayInputStream("post-stuck-sentinel".getBytes()))
        {
            sentinelNode = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "post-stuck-sentinel.bin", sentinelContent, SENTINEL_MIME_TYPE);
        }
        log.info("[reliability] Sentinel node {} created with {} (catch-all [*]=* keeps it on passthrough)",
                sentinelNode.id(), SENTINEL_MIME_TYPE);

        return new StuckRendition(stuckNode, sentinelNode);
    }

    /**
     * Holder for the identifiers produced by {@link #createStuckRenditionAndSentinel}. Shared with the paired opt-in IT.
     */
    record StuckRendition(Node stuckNode, Node sentinelNode)
    {
    }
}
