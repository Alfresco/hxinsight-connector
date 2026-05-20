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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Pins the paired-outage worst case: both broker and live-ingester are killed and the connector starts <i>before</i> the broker has finished initializing. Models a docker-compose / kubernetes pod restart that brings both processes up unordered. The connector's connect-retry path must absorb the warm-up window without crash-looping.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class PairedRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    @Test
    void shouldRecoverWhenConnectorStartsImmediatelyAfterBrokerRestart() throws IOException
    {
        log.info("[chaos] SIGKILL on both broker and live-ingester to model an unordered pod-level restart");
        ProcessChaos.sigKill(environment().liveIngesterContainer());
        ProcessChaos.sigKill(environment().activemqContainer());
        ProcessChaos.awaitContainerExited(environment().liveIngesterContainer(), STOP_DEADLINE);
        ProcessChaos.awaitContainerExited(environment().activemqContainer(), STOP_DEADLINE);

        log.info("[chaos] starting the broker — host port mappings are about to change");
        ProcessChaos.startBroker(environment());

        log.info("[chaos] starting the live-ingester immediately — exercising the reconnect-retry path through the broker warm-up window");
        ProcessChaos.start(environment().liveIngesterContainer());
        ProcessChaos.awaitConnectorReadiness(environment(), RECOVERY_DEADLINE_MS);

        Node sentinel = createNode("paired-restart-sentinel.txt", "Paired-outage sentinel");
        assertIngestionFor(sentinel.id(), "paired sentinel");
    }
}
