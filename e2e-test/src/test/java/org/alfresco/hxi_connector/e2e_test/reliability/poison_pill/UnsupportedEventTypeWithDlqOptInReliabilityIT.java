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
 * Sister of {@link UnsupportedEventTypeReliabilityIT}: boots the env with the {@code dead-letter-unsupported-types} opt-in enabled and asserts that an unrecognised repo {@code eventType} surfaces on {@code ActiveMQ.DLQ} (instead of the default INFO log + counter + silent ACK).
 *
 * <p>
 * Same trigger as the default-deployment IT (synthetic CloudEvent on {@code alfresco.repo.event2} with an unrecognised {@code type} + sentinel node for liveness); opposite outcome on the DLQ assertion. Both run side-by-side on every CI build so any regression on either path fails loud.
 *
 * <p>
 * Per-class environment lifecycle (own boot, own teardown). Cost: one extra env boot for the opt-in pair.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class UnsupportedEventTypeWithDlqOptInReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[reliability] Booting per-class ReliabilityEnvironment with repo-events dead-letter-unsupported-types opt-in for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder()
                .withRepoEventsDeadLetterUnsupportedTypes()
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

    /**
     * With the opt-in enabled: an unrecognised {@code eventType} surfaces on the DLQ. Same trigger as {@link UnsupportedEventTypeReliabilityIT#shouldLogAndCountUnsupportedEventTypeWithoutDlq}, flipped by {@link ReliabilityEnvironment.Builder#withRepoEventsDeadLetterUnsupportedTypes()}.
     */
    @Test
    void shouldDeadLetterEventWithUnsupportedEventType() throws IOException
    {
        UnsupportedEventTypeReliabilityIT.SyntheticUnknownTypeEvent event =
                UnsupportedEventTypeReliabilityIT.injectSyntheticUnknownTypeEvent(environment);

        RetryUtils.retryWithBackoff(() -> {
            assertThat(environment.jolokia().dlqDepth())
                    .as("opt-in enabled: an unrecognised eventType must surface on the DLQ. Zero here means the EventProcessor never threw UnsupportedEventTypeException, or the repo-events DeadLetterChannel did not catch it")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(event.sentinelNode().id()))
                    .as("liveness: sentinel must flow past the dead-lettered failure")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
