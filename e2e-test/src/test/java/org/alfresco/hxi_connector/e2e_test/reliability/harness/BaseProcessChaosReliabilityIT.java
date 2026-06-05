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
package org.alfresco.hxi_connector.e2e_test.reliability.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Per-class environment owner for chaos tests that tear down containers. Subclasses must not extend {@link BaseReliabilityIT}, whose environment is shared across the whole Failsafe JVM.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.SignatureDeclareThrowsException"})
public abstract class BaseProcessChaosReliabilityIT
{
    protected static final String PARENT_ID = "-my-";
    protected static final int INGESTION_DELAY_MS = 5_000;
    /** Deadline for connector recovery after a chaos cycle. Covers cold ActiveMQ start-up, KahaDB recovery, and Spring-JMS reconnect. */
    protected static final long RECOVERY_DEADLINE_MS = 180_000L;
    /** Deadline for ACS to be reachable after {@code docker stop} + {@code docker start}. The repository can take 1–2 minutes on a cold JVM. */
    protected static final long ACS_READY_DEADLINE_MS = 240_000L;
    protected static final Duration STOP_DEADLINE = Duration.ofSeconds(20);

    private ReliabilityEnvironment environment;

    @BeforeAll
    final void startEnvironment() throws Exception
    {
        log.info("[chaos] Booting per-class ReliabilityEnvironment for {}", getClass().getSimpleName());
        environment = ReliabilityEnvironment.builder().build();
        environment.start();
    }

    @AfterAll
    final void closeEnvironment()
    {
        if (environment != null)
        {
            log.info("[chaos] Closing per-class ReliabilityEnvironment for {}", getClass().getSimpleName());
            environment.close();
        }
    }

    protected final ReliabilityEnvironment environment()
    {
        return environment;
    }

    /** Publish a fresh content node through the repository client. */
    protected final Node createNode(String name, String content) throws IOException
    {
        @Cleanup
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        return environment().repositoryClient().createNodeWithContent(PARENT_ID, name, stream, "text/plain");
    }

    /** Wait for at least one {@code /ingestion-events} POST carrying the given {@code objectId}. The {@code label} is used only in the assertion message to help triage. */
    protected final void assertIngestionFor(String nodeId, String label)
    {
        RetryUtils.retryWithBackoff(() -> assertThat(WiremockCounts.ingestionEventsFor(nodeId))
                .as("[%s] connector did not deliver event for objectId=%s — restart left the consumer detached, durable subscription was lost, or event silently dropped",
                        label, nodeId)
                .isGreaterThanOrEqualTo(1), INGESTION_DELAY_MS);
    }
}
