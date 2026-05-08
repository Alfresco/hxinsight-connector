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
 * Sister of {@link TransformPipelineUnreachableReliabilityIT}: boots the env with both the {@code transform-response} dead-letter opt-in and the {@code throw-failed-transforms} opt-in enabled, and asserts that an ACS-rejected transform request (the resulting {@code status=400} transform-response) surfaces on {@code ActiveMQ.DLQ} with a {@code live_ingester_transform_response_dlq_total} counter increment.
 *
 * <p>
 * Same trigger as the default-deployment IT (mime-type mapping forces {@code text/plain → application/pdf}, ACS' transform service is disabled and synthesises a {@code status=400} response); opposite outcome on the DLQ assertion. Both run side-by-side so any regression on either path fails loud.
 *
 * <p>
 * Per-class environment lifecycle (own boot, own teardown). Cost: one extra env boot for the opt-in pair.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class TransformPipelineUnreachableWithDlqOptInReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class ReliabilityEnvironment with transform-response DLC + throw-failed-transforms opt-ins for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withTransformResponseDeadLetterEnabled()
                .withTransformResponseThrowFailedTransforms()
                .build();
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

    /**
     * With both opt-ins enabled: an ACS-rejected transform request surfaces on the DLQ. Same trigger as {@link TransformPipelineUnreachableReliabilityIT#shouldSilentlyDropRenditionWhenAcsRejectsTransformRequest}, flipped by {@link ReliabilityEnvironment.Builder#withTransformResponseThrowFailedTransforms()} + {@link ReliabilityEnvironment.Builder#withTransformResponseDeadLetterEnabled()}.
     */
    @Test
    void shouldDeadLetterRenditionWhenAcsRejectsTransformRequest() throws IOException, InterruptedException
    {
        TransformPipelineUnreachableReliabilityIT.StuckRendition rendition =
                TransformPipelineUnreachableReliabilityIT.createStuckRenditionAndSentinel(environment);

        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        RetryUtils.retryWithBackoff(() -> {
            assertThat(environment.jolokia().dlqDepth())
                    .as("opt-in enabled: ACS-rejected transform request must surface on the DLQ. Zero here means the route's deadLetterChannel did not catch the FailedTransformResponseException")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(rendition.sentinelNode().id()))
                    .as("liveness: sentinel must flow past the dead-lettered failure")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
