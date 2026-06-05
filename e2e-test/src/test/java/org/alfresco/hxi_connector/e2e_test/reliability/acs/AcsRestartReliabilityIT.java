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
package org.alfresco.hxi_connector.e2e_test.reliability.acs;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * ACS repository restart recovery — graceful stop and SIGKILL. Postgres state is preserved, so the connector should reattach once ACS answers HTTP again. Asserts the metadata pipeline resumes post-restart; the content-download path is covered separately by {@link AcsLatencyReliabilityIT} and {@link AcsTolerableLatencyReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    @Test
    void shouldRecoverConnectorAfterAcsGracefulStopAndRestart() throws IOException
    {
        Node baseline = createNode("acs-restart-graceful-baseline.txt", "Graceful stop baseline");
        assertIngestionFor(baseline.id(), "graceful baseline");

        log.info("[chaos] graceful stop on the ACS repository container");
        ProcessChaos.gracefulStop(environment().repositoryContainer());
        ProcessChaos.awaitContainerExited(environment().repositoryContainer(), STOP_DEADLINE);

        ProcessChaos.startRepository(environment());
        ProcessChaos.awaitAcsReadiness(environment(), ACS_READY_DEADLINE_MS);

        Node sentinel = createNode("acs-restart-graceful-sentinel.txt", "Graceful stop sentinel");
        assertIngestionFor(sentinel.id(), "graceful sentinel");
    }

    @Test
    void shouldRecoverConnectorAfterAcsSigKillAndRestart() throws IOException
    {
        Node baseline = createNode("acs-restart-kill-baseline.txt", "SIGKILL baseline");
        assertIngestionFor(baseline.id(), "kill baseline");

        log.info("[chaos] SIGKILL on the ACS repository container");
        ProcessChaos.sigKill(environment().repositoryContainer());
        ProcessChaos.awaitContainerExited(environment().repositoryContainer(), STOP_DEADLINE);

        ProcessChaos.startRepository(environment());
        ProcessChaos.awaitAcsReadiness(environment(), ACS_READY_DEADLINE_MS);

        Node sentinel = createNode("acs-restart-kill-sentinel.txt", "SIGKILL sentinel");
        assertIngestionFor(sentinel.id(), "kill sentinel");
    }
}
