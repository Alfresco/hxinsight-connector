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
package org.alfresco.hxi_connector.e2e_test.reliability.hxi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Brief HX Insight blip shorter than the publish retry budget must recover via Spring Retry or JMS redelivery: the event reaches HX Insight, DLQ stays empty.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class HxiTransientBlipReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    /** Long enough to fail the first publish attempt, short enough that retries recover before DLQ. */
    private static final int BLIP_MS = 300;
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldRecoverWhenHxiBlipResolvesWithinRetryBudget() throws IOException, InterruptedException
    {
        log.info("[reliability] Disabling toxic-hxi for ~{} ms (shorter than total retry budget)", BLIP_MS);
        environment().hxiProxy().disable();

        Node victim;
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("HXI transient-blip victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "hxi-blip-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-blip node {} published — expecting Spring Retry / JMS redelivery to recover within budget", victim.id());

            Thread.sleep(BLIP_MS);
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-hxi proxy");
            environment().hxiProxy().enable();
        }

        final Node finalVictim = victim;
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(finalVictim.id()))
                    .as("brief HXI blip within retry budget must NOT prevent ingestion — a zero here means Spring Retry / JMS redelivery did not recover the event for objectId=%s",
                            finalVictim.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("brief HXI blip within retry budget must NOT produce a DLQ entry — non-zero here means the retry budget was insufficient or the recovery path silently bypassed retries")
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }
}
