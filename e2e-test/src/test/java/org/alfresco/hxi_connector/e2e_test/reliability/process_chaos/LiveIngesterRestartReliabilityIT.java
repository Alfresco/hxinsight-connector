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
package org.alfresco.hxi_connector.e2e_test.reliability.process_chaos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins live-ingester restart recovery: graceful stop, abrupt SIGKILL, and restart while a backlog is queued. In every variant the broker holds events on the durable subscription while the connector is down; the restarted connector reattaches and drains the backlog without operator intervention.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class LiveIngesterRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    private static final int BACKLOG_SIZE = 5;

    @Test
    void shouldRecoverConnectorAfterLiveIngesterGracefulStopAndRestart() throws IOException
    {
        Node baseline = createNode("ingester-restart-graceful-baseline.txt", "Graceful stop baseline");
        awaitIngestionFor(baseline.id(), "graceful baseline");

        log.info("[chaos] graceful stop on the live-ingester container");
        ProcessChaos.gracefulStop(environment().liveIngesterContainer());
        ProcessChaos.awaitContainerExited(environment().liveIngesterContainer(), STOP_DEADLINE);

        log.info("[chaos] queueing a {}-event backlog on the broker while the connector is down", BACKLOG_SIZE);
        List<Node> backlog = new ArrayList<>(BACKLOG_SIZE);
        for (int i = 0; i < BACKLOG_SIZE; i++)
        {
            backlog.add(createNode("ingester-graceful-backlog-" + i + ".txt", "Backlog #" + i));
        }

        ProcessChaos.start(environment().liveIngesterContainer());
        ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

        for (Node node : backlog)
        {
            awaitIngestionFor(node.id(), "graceful backlog");
        }
    }

    @Test
    void shouldRecoverConnectorAfterLiveIngesterSigKillAndRestart() throws IOException
    {
        Node baseline = createNode("ingester-restart-kill-baseline.txt", "SIGKILL baseline");
        awaitIngestionFor(baseline.id(), "kill baseline");

        log.info("[chaos] SIGKILL on the live-ingester container");
        ProcessChaos.sigKill(environment().liveIngesterContainer());
        ProcessChaos.awaitContainerExited(environment().liveIngesterContainer(), STOP_DEADLINE);

        log.info("[chaos] queueing a {}-event backlog on the broker while the connector is down", BACKLOG_SIZE);
        List<Node> backlog = new ArrayList<>(BACKLOG_SIZE);
        for (int i = 0; i < BACKLOG_SIZE; i++)
        {
            backlog.add(createNode("ingester-kill-backlog-" + i + ".txt", "Backlog #" + i));
        }

        ProcessChaos.start(environment().liveIngesterContainer());
        ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

        for (Node node : backlog)
        {
            awaitIngestionFor(node.id(), "kill backlog");
        }
    }
}
