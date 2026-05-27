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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Sentinels;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Shared trigger for SFS-outage scenarios: with {@code toxic-sfs} already disabled by the caller, creates a {@code text/plain} victim (forced through the transform path) plus a passthrough sentinel, then waits long enough for the failure cascade (transform round-trip + Spring {@code @Retryable} exhaustion + JMS redelivery) to settle.
 */
@Slf4j
final class SfsOutageTrigger
{
    /** Transform round-trip (~5 s) + retry budget + JMS redelivery — long enough for any DLQ outcome to settle. */
    static final int SFS_OUTAGE_SETTLE_SECONDS = 10;

    private SfsOutageTrigger()
    {}

    static SfsOutageRun createVictimAndSentinelDuringOutage(ReliabilityEnvironment environment) throws IOException, InterruptedException
    {
        Node victim;
        try (InputStream victimContent = new ByteArrayInputStream("sfs-outage victim".getBytes()))
        {
            victim = environment.repositoryClient()
                    .createNodeWithContent(Sentinels.PARENT_ID, "sfs-outage-victim.txt", victimContent, "text/plain");
        }
        log.info("[reliability] Victim node {} published — transform path expected to round-trip but rendition download from toxic-sfs to fail", victim.id());

        Node sentinel = Sentinels.create(environment, "sfs-outage-sentinel.bin", "sfs-outage sentinel", Sentinels.PASSTHROUGH_MIME_TYPE);

        Thread.sleep(Duration.ofSeconds(SFS_OUTAGE_SETTLE_SECONDS).toMillis());

        return new SfsOutageRun(victim, sentinel);
    }

    record SfsOutageRun(Node victim, Node sentinel)
    {}
}
