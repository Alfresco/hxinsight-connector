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
package org.alfresco.hxi_connector.e2e_test.reliability.poison_pill;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Opt-out from the default: when {@code dead-letter-unsupported-types=false} an unrecognised CloudEvent {@code type} is logged + counted and the JMS message is ACK'd. Operators choose this for forward-compatibility with future ACS event types over DLQ inventory. Sister class {@link UnsupportedEventTypeReliabilityIT} covers the default DLQ path.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases", "PMD.SignatureDeclareThrowsException"})
public class UnsupportedEventTypeOptOutReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class env with dead-letter-unsupported-types disabled for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withRepoEventsDeadLetterUnsupportedTypesDisabled()
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
    void shouldLogAndCountUnsupportedEventTypeWithoutDlq() throws IOException
    {
        UnsupportedEventTypeTrigger.SyntheticUnknownTypeEvent event = UnsupportedEventTypeTrigger.inject(environment);

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(event.sentinelNode().id()))
                    .as("liveness: sentinel published after the unknown-type event must reach HX Insight — failure here means the route stopped on the unsupported event")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment.liveIngesterContainer().getLogs())
                    .as("opt-out path must still emit the always-on INFO log line naming the unsupported eventType")
                    .contains(UnsupportedEventTypeTrigger.UNHANDLED_LOG_FRAGMENT);
            assertThat(environment.jolokia().dlqDepth())
                    .as("opt-out: an unrecognised eventType is logged + counted + ACK'd. DLQ stays at zero so adding a new ACS event type does not flood ActiveMQ.DLQ. The sister IT pins the default DLQ path")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }
}
