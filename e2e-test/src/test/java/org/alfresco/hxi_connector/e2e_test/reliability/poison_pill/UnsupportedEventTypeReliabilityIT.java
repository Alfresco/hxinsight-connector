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
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Default behaviour for an unrecognised CloudEvent {@code type}: the event lands on the DLQ via the repo-events dead-letter channel with a {@code live_ingester_repo_events_dlq_total} counter increment. Sister class {@link UnsupportedEventTypeOptOutReliabilityIT} covers the legacy log + counter + ACK behaviour.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class UnsupportedEventTypeReliabilityIT extends BaseReliabilityIT
{
    private static final int CONVERGENCE_DELAY_MS = 1_000;

    @Test
    void shouldDeadLetterEventWithUnsupportedEventType() throws IOException
    {
        UnsupportedEventTypeTrigger.SyntheticUnknownTypeEvent event = UnsupportedEventTypeTrigger.inject(environment());

        RetryUtils.assertWithRetry(() -> {
            assertThat(environment().jolokia().dlqDepth())
                    .as("default deployment: an unrecognised eventType must surface on the DLQ exactly once — env caps redeliveries at 1, so any other count means the DLC fired twice or did not fire at all")
                    .isEqualTo(1);
            assertThat(environment().jolokia().browseDlq())
                    .as("the dead-lettered message must trace back to the repo-events topic we published to — pinning by OriginalDestination rules out false positives from unrelated routes. (The JMX browse() envelope carries JMS metadata only, not the payload — see JolokiaProbe.browseDlq() for the reasoning and operator-side body-inspection paths.)")
                    .singleElement()
                    .satisfies(message -> assertThat(message.envelopeContains(UnsupportedEventTypeTrigger.REPO_EVENT_TOPIC))
                            .as("DLQ envelope should record an OriginalDestination derived from %s but was: %s", UnsupportedEventTypeTrigger.REPO_EVENT_TOPIC, message.envelope())
                            .isTrue());
            assertThat(WiremockCounts.ingestionEventsFor(event.sentinelNode().id()))
                    .as("liveness: sentinel must flow past the dead-lettered failure")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
