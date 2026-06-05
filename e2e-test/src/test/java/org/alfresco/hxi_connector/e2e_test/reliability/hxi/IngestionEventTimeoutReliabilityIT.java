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
 * HX Insight stops responding within the response-timeout budget. The HTTP client must surface a timeout, retries must exhaust, and the message must land on the DLQ. Regression guard for the {@code responseTimeoutMs} knob — without it Camel's HTTP component would block the worker thread forever.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class IngestionEventTimeoutReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String SENTINEL_CONTENT = "Timeout sentinel";
    /** Lower number = higher Wiremock priority; the file-based default uses 5. */
    private static final int OVERRIDE_STUB_PRIORITY = 1;
    /** Above the 3 s response timeout so every attempt trips it. */
    private static final int SERVER_DELAY_MS = 6_000;
    private static final int CONVERGENCE_DELAY_MS = 4_000;
    /** Lower bound proving the timeout fired and JMS retried at least once. A count of 1 means the first attempt blocked indefinitely. */
    private static final int MIN_HTTP_ATTEMPTS = 2;

    @Test
    void shouldTimeOutAndDeadLetterWhenHxiResponseExceedsBudget() throws IOException
    {
        installSlowResponseStub();

        @Cleanup
        InputStream content = new ByteArrayInputStream(SENTINEL_CONTENT.getBytes());
        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "ingestion-timeout.txt", content, "text/plain");
        log.info("[reliability] Created node {} with /ingestion-events held for {} ms; expecting HTTP timeout then JMS DLQ", createdNode.id(), SERVER_DELAY_MS);

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(createdNode.id()))
                    .as("HTTP timeout actually fired and JMS retried: expected ≥ %d POSTs to /ingestion-events for this objectId. A count of 1 means the connector blocked indefinitely waiting for HX Insight — the regression the responseTimeoutMs knob exists to prevent",
                            MIN_HTTP_ATTEMPTS)
                    .isGreaterThanOrEqualTo(MIN_HTTP_ATTEMPTS);
            assertThat(environment().jolokia().dlqDepth())
                    .as("bounded redelivery exhausts to DLQ: a slow HX Insight must surface as a parked message on ActiveMQ.DLQ for operator visibility, not a silently dropped event")
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Installs a Wiremock stub that holds the response for {@link #SERVER_DELAY_MS} before answering {@code 202}. Higher-priority than the file-based default stub so the override takes precedence; cleaned up by {@link BaseReliabilityIT#resetBetweenTests()} on the next test boundary.
     */
    private static void installSlowResponseStub()
    {
        stubFor(post(urlEqualTo("/ingestion-events"))
                .atPriority(OVERRIDE_STUB_PRIORITY)
                .willReturn(aResponse()
                        .withStatus(202)
                        .withFixedDelay(SERVER_DELAY_MS)));
    }
}
