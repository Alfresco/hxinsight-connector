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
 * Opt-out from both transform-response defaults: with {@code throw-failed-transforms=false} and {@code dead-letter-enabled=false} an ACS-rejected transform request is logged at WARN and silently ACK'd. No DLQ entry, no counter. Sister class {@link TransformPipelineUnreachableReliabilityIT} covers the default DLQ path.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class TransformPipelineUnreachableOptOutReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    private static final String SILENT_DROP_LOG_FRAGMENT = "Transform :: Silently dropped failed transform-response";

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class env with transform-response DLC + throw-failed-transforms disabled for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withTransformResponseDeadLetterDisabled()
                .withTransformResponseThrowFailedTransformsDisabled()
                .build();
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
    void shouldSilentlyDropRenditionWhenAcsRejectsTransformRequest() throws IOException, InterruptedException
    {
        TransformPipelineUnreachableTrigger.StuckRendition rendition = TransformPipelineUnreachableTrigger.createStuckRenditionAndSentinel(environment);

        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(rendition.sentinelNode().id()))
                    .as("liveness: sentinel must reach HX Insight despite the stuck rendition request")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(rendition.stuckNode().id()))
                    .as("only the metadata-only ingestion-event fires for the stuck node — the rendition path short-circuits on the status=400 transform-response from ACS via the by-design early-return. Pinned at exactly 1 because that's the metadata POST count")
                    .isEqualTo(1);
            assertThat(environment.jolokia().queueDepth(TransformPipelineUnreachableTrigger.TRANSFORM_REQUEST_QUEUE))
                    .as("the transform-request queue should drain — ACS's repo-side handler does consume the request even with transform.service.enabled=false")
                    .isZero();
            assertThat(environment.jolokia().dlqDepth())
                    .as("opt-out: 400 transform-response is ACK'd silently after a route-level WARN log. DLQ stays at zero. Sister IT covers the default DLQ path")
                    .isZero();
            assertThat(environment.liveIngesterContainer().getLogs())
                    .as("the silent-drop branch must actually have executed — proves the handler ran rather than being silently bypassed")
                    .contains(SILENT_DROP_LOG_FRAGMENT);
        }, CONVERGENCE_DELAY_MS);
    }
}
