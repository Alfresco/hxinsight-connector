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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
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
 * HX Insight ingestion is hard-down (every POST returns 500). Asserts the HTTP retry budget actually runs and the message ends up on {@code ActiveMQ.DLQ}. Complement to {@link IdempotentIngestionReliabilityIT} — retries help, but not forever.
 *
 * <p>
 * Post-failure liveness is not asserted: the stub fails {@code /ingestion-events} for the whole test, so any sentinel would dead-letter too. The next test's precondition check will catch a structural regression.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IngestionEventRetryExhaustionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Ingestion-event exhaustion sentinel";
    /** Lower number = higher Wiremock priority; the file-based default uses 5. */
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /** Lower bound proving the HTTP retry budget actually ran. Tolerant to changes in the retry-attempts knob. */
    private static final int MIN_HTTP_ATTEMPTS = 2;

    @Test
    void shouldExhaustHttpRetriesAndDeadLetterEventOnPersistentHxiFailure() throws IOException
    {
        installAlwaysFailStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "ingestion-exhaustion.txt", content, "text/plain");
        log.info("[reliability] Created node {} with /ingestion-events forced to 500; expecting HTTP retries to exhaust then JMS DLQ to receive the parked message", createdNode.id());

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                    .as("HTTP retry budget exercised: connector must attempt ≥ %d POSTs to /ingestion-events for this objectId before exhausting and falling through to the JMS handler. A count of 1 means the connector treated the first 500 as fatal",
                            MIN_HTTP_ATTEMPTS)
                    .isGreaterThanOrEqualTo(MIN_HTTP_ATTEMPTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: a persistently-failing /ingestion-events path must move the original event to ActiveMQ.DLQ so an operator can see it. A zero here means the route silently dropped the failure")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }

    /** Always-fail stub for {@code /ingestion-events}. Cleared by {@link BaseReliabilityIT#resetBetweenTests()}. */
    private static void installAlwaysFailStub()
    {
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse().withStatus(500)));
    }
}
