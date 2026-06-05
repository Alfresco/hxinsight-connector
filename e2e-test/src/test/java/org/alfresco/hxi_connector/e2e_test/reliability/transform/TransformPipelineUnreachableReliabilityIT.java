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

import java.io.IOException;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Default behaviour when ACS rejects a transform request (transform service disabled). ACS synthesises a {@code status=400} transform-response; the route throws {@code FailedTransformResponseException}, the DLC catches it, and the failed message lands on the DLQ. Sister class {@link TransformPipelineUnreachableOptOutReliabilityIT} covers the legacy WARN-log + silent-ACK shape.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class TransformPipelineUnreachableReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class env for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder().build();
        environment.liveIngesterContainer().withEnv("JAVA_TOOL_OPTIONS", TransformPipelineUnreachableTrigger.JAVA_TOOL_OPTIONS);
        environment.start();
    }

    @AfterAll
    final void closeEnvironment()
    {
        if (environment != null)
        {
            log.info("[reliability] Closing per-class env for {}", getClass().getSimpleName());
            environment.close();
        }
    }

    @Test
    void shouldDeadLetterRenditionWhenAcsRejectsTransformRequest() throws IOException, InterruptedException
    {
        TransformPipelineUnreachableTrigger.StuckRendition rendition = TransformPipelineUnreachableTrigger.createStuckRenditionAndSentinel(environment);

        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        RetryUtils.assertWithRetry(() -> {
            assertThat(environment.jolokia().dlqDepth())
                    .as("default deployment: the trigger creates two nodes (the stuck one + a post-stuck sentinel) and the connector issues a transform request for both regardless of source mime. With transform.service.enabled=false, ACS synthesises a status=400 transform-response for each, the route throws FailedTransformResponseException on each, and the DLC dead-letters each — exactly 2 entries. Env caps redeliveries at 1, so any other count means the DLC duplicated entries or did not fire at all.")
                    .isEqualTo(2);
            assertThat(environment.jolokia().browseDlq())
                    .as("the dead-lettered messages are ACS's two 400 transform-responses — both must trace back to the transform-response queue, which is what the route consumed from before throwing FailedTransformResponseException. (The JMX browse() envelope carries JMS metadata only, not the payload — see JolokiaProbe.browseDlq() for the reasoning and operator-side body-inspection paths.)")
                    .hasSize(2)
                    .allSatisfy(message -> assertThat(message.envelopeContains(TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE))
                            .as("DLQ envelope should record OriginalDestination=queue://%s but was: %s", TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE, message.envelope())
                            .isTrue());
            assertThat(WiremockCounts.ingestionEventsFor(rendition.sentinelNode().id()))
                    .as("liveness: sentinel must flow past the dead-lettered failure")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(rendition.stuckNode().id()))
                    .as("only the metadata-only ingestion-event fires for the stuck node — the rendition path short-circuits on the status=400 transform-response from ACS via the route's DLC. Pinned at exactly 1 because that's the metadata POST count")
                    .isEqualTo(1);
            assertThat(environment.jolokia().queueDepth(TransformPipelineUnreachableTrigger.TRANSFORM_REQUEST_QUEUE))
                    .as("the transform-request queue should drain — ACS's repo-side handler does consume the request even with transform.service.enabled=false")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }
}
