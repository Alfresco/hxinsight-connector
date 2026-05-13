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

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins ACS-side restart recovery: graceful stop and abrupt SIGKILL of the repository container, followed by {@code docker start} and a sentinel publish. The repository's database state (Postgres) is preserved across the cycle, so the connector should rediscover ACS once it answers HTTP again — no operator intervention required.
 *
 * <p>
 * <b>Contract scope:</b> the post-recovery {@link #assertIngestionFor(String, String) awaitIngestionFor} canary on each method asserts that the connector's <i>metadata pipeline</i> is alive after the ACS restart — i.e. the repo-event consumer reattached, the durable JMS subscription replayed pending events, and the metadata POST to {@code /ingestion-events} fired for the sentinel objectId. It does <b>not</b> verify the content-download path (which runs after the metadata POST, hits {@code AlfrescoRepositoryContentClient.downloadContent} against the live repository, and feeds the rendition pipeline). Adding a {@code WiremockCounts.contentEventsFor(sentinel.id()) >= 1} assertion would extend the contract to that path, but is intentionally out of scope here — the content-download retry contract is held by {@link AcsLatencyReliabilityIT} (in-budget timeout via {@code @Retryable} exhaustion) and {@link AcsTolerableLatencyReliabilityIT} (under-budget tolerance), both of which exercise the route under chaos with the live repository up. This IT's job is the restart-recovery shape: "ACS came back, the connector noticed, events resumed flowing".
 *
 * <p>
 * Note on wall-time: ACS Spring boot from a warm Postgres still takes 1–2 minutes per restart, so this class is intentionally limited to two methods. {@link #ACS_READY_DEADLINE_MS} is generous on purpose to absorb that.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class AcsRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    // Assertion set scope: pre-stop baseline + post-restart sentinel must each produce a metadata `/ingestion-events` POST
    // (via `awaitIngestionFor`). The post-restart assertion is the regression catch — it pins that the JMS consumer
    // reattached, the durable subscription replayed, and the metadata POST fired. Content-download path is NOT covered;
    // see class-level Javadoc for cross-references to the ITs that hold that contract.
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
