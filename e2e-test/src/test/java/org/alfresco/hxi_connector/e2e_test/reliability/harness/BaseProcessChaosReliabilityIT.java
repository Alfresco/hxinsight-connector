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
 * Per-class environment owner for chaos tests that intentionally tear down the broker or live-ingester containers. Subclasses must not extend {@link BaseReliabilityIT}, which shares one environment across the whole Failsafe JVM — a chaos teardown there would cascade-fail every other reliability test.
 *
 * <p>
 * Provides the lifecycle (one {@link ReliabilityEnvironment} per class, started in {@code @BeforeAll}, closed in {@code @AfterAll}) and the small set of helpers shared across every chaos test: publish a node through the repository client, wait for it to land at HX Insight.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.SignatureDeclareThrowsException"})
public abstract class BaseProcessChaosReliabilityIT
{
    protected static final String PARENT_ID = "-my-";
    /**
     * Convergence retry step for end-to-end ingestion checks. Generous enough to absorb the connector's two-event-per-create flow plus the bounded redelivery window without flapping.
     */
    protected static final int INGESTION_DELAY_MS = 5_000;
    /**
     * Wall-clock deadline for connector recovery after a chaos cycle. Allows for cold ActiveMQ start-up, KahaDB recovery on unclean shutdown, and Spring-JMS reconnect ticks.
     */
    protected static final long RECOVERY_DEADLINE_MS = 180_000L;
    /**
     * Wall-clock deadline for ACS to become reachable again after a {@code docker stop} + {@code docker start}. Generous on purpose: the repository's Spring boot can take 1–2 minutes on a cold JVM even with the database state preserved.
     */
    protected static final long ACS_READY_DEADLINE_MS = 240_000L;
    /**
     * Hard cap on time the daemon is allowed to take to surface the container as exited after a stop or kill request.
     */
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

    /**
     * Publish a fresh content node through the repository client. Returns the created {@link Node} so the caller can correlate the resulting POST at the HX Insight Wiremock.
     */
    protected final Node createNode(String name, String content) throws IOException
    {
        @Cleanup
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        return environment().repositoryClient().createNodeWithContent(PARENT_ID, name, stream, "text/plain");
    }

    /**
     * Wait for at least one POST to {@code /ingestion-events} carrying the given {@code objectId}. The {@code label} is only used in the assertion message to make triage one log scan rather than archaeology.
     */
    protected final void assertIngestionFor(String nodeId, String label)
    {
        RetryUtils.retryWithBackoff(() -> assertThat(WiremockCounts.ingestionEventsFor(nodeId))
                .as("[%s] connector did not deliver event for objectId=%s — restart left the consumer detached, durable subscription was lost, or event silently dropped",
                        label, nodeId)
                .isGreaterThanOrEqualTo(1), INGESTION_DELAY_MS);
    }
}
