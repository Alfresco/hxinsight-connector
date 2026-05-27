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
 * Opt-out from the default in-app DLC: when {@code dead-letter-enabled=false} an SFS outage still surfaces on the DLQ — but via the legacy broker-rollback path (transactional consumer rolls back, broker exhausts {@code maxRedeliveries=1}, the exchange lands on {@code ActiveMQ.DLQ}). The {@code live_ingester_transform_response_dlq_total} counter does not fire on this path.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class SfsOutageOptOutReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 5_000;
    private static final String SFS_DOWNLOAD_FAILURE_LOG_FRAGMENT = "Transform :: Unexpected response while downloading rendition";

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class env with transform topology + transform-response DLC disabled for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withTransformTopology()
                .withTransformResponseDeadLetterDisabled()
                .build();
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
    void shouldDeadLetterViaBrokerRollbackWhenSfsUnreachable() throws IOException, InterruptedException
    {
        WireMock.configureFor(environment.hxInsightMock().getHost(), environment.hxInsightMock().getPort());
        log.info("[reliability] Disabling toxic-sfs proxy for sustained outage");
        environment.sfsProxy().disable();
        try
        {
            SfsOutageTrigger.SfsOutageRun run = SfsOutageTrigger.createVictimAndSentinelDuringOutage(environment);
            RetryUtils.assertWithRetry(() -> {
                assertThat(environment.jolokia().dlqDepth())
                        .as("opt-out: SFS-download retries exhaust, the route's broad onException retry budget exhausts, JMS rolls back, the broker exhausts maxRedeliveries=1 and lands the exchange on ActiveMQ.DLQ. The trigger creates two nodes (victim + sentinel) and the connector issues a transform request for both regardless of source mime — both transform-responses fail at the SFS-download leg under outage, so the broker dead-letters both — exactly 2 entries.")
                        .isEqualTo(2);
                assertThat(environment.jolokia().browseDlq())
                        .as("the dead-lettered messages are the two successful transform-responses the broker rolled back — both must trace back to the transform-response queue. (The JMX browse() envelope carries JMS metadata only, not the payload — see JolokiaProbe.browseDlq() for the reasoning and operator-side body-inspection paths.)")
                        .hasSize(2)
                        .allSatisfy(message -> assertThat(message.envelopeContains(TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE))
                                .as("DLQ envelope should record OriginalDestination=queue://%s but was: %s", TransformResponseFailureTrigger.TRANSFORM_RESPONSE_QUEUE, message.envelope())
                                .isTrue());
                assertThat(WiremockCounts.ingestionEventsFor(run.sentinel().id()))
                        .as("liveness: sentinel must reach HX Insight via the catch-all passthrough path")
                        .isGreaterThanOrEqualTo(1);
                assertThat(environment.liveIngesterContainer().getLogs())
                        .as("the SFS download path must actually have been attempted — proves the handler ran rather than being silently bypassed")
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
