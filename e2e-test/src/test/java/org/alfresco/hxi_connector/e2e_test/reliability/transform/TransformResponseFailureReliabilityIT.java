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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins the default-deployment behaviour when a transform-response message arriving on the connector's response queue carries {@code status == 400} (ATS' "I cannot produce this rendition, ever" signal — unsupported mime mapping, transform-engine config, etc.).
 *
 * <p>
 * The {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response.ATSTransformResponseHandler} consumes from {@code activemq:queue:org.alfresco.hxinsight-connector.transform.response}. The route logs a route-level WARN line for any non-{@code 201} body (including the full payload), then takes the by-design early-return inside its {@code ingestContent} processor: the JMS message is ACK'd, no DLQ entry is produced, no Micrometer counter is incremented. The WARN log is the operator's only signal of the abandonment.
 *
 * <p>
 * This early-return is intentional — retrying a deterministic ATS rejection is pointless and a flood of redeliveries adds no upside. Deployments that want a structured, automation-friendly signal (DLQ inventory, exception-tagged counter) can opt in via {@code ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS=true} (paired with {@code ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED=true}) — see the sister class {@link TransformResponseFailureWithDlqOptInReliabilityIT}.
 *
 * <p>
 * Driven via {@link DirectQueuePublisher} so the test injects a synthetic transform-response without booting the real transform-engine layer (transform-router / transform-core-aio / SFS) — the response handler does not depend on those containers, only on the JMS message shape. A sentinel node published afterwards through the normal ACS create path proves the route stayed alive past the synthetic failure.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class TransformResponseFailureReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final String TRANSFORM_RESPONSE_QUEUE = "org.alfresco.hxinsight-connector.transform.response";
    private static final int CONVERGENCE_DELAY_MS = 1_000;
    /**
     * Substring of the INFO log line emitted by {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response.ATSTransformResponseHandler#recordSilentDrop} on the default-deployment status=400 branch. Pairs with the {@code live_ingester_transform_response_silent_drop_total} Micrometer counter; both signals exist so this IT can distinguish "the handler executed the silent-drop branch as designed" from "the handler was silently disabled and the negative assertions (no DLQ, no rendition POSTs) hold for the wrong reason". Asserting this fragment binds the negative outcomes to the actual code path running.
     */
    private static final String SILENT_DROP_LOG_FRAGMENT = "Transform :: Silently dropped failed transform-response";

    @Test
    void shouldSilentlyDropTransformResponseWith400Status() throws IOException
    {
        SyntheticFailure failure = injectSynthetic400Failure();

        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(failure.sentinelNode().id()))
                    .as("liveness: sentinel must reach HX Insight after the route ACK'd the failed transform-response")
                    .isGreaterThanOrEqualTo(1);
            assertThat(WiremockCounts.ingestionEventsFor(failure.droppedNodeRef()))
                    .as("no rendition path for the 400 transform-response: the response handler returns early, so neither a presigned-URL nor an ingestion-event POST should fire for the dropped clientData.nodeRef")
                    .isZero();
            assertThat(environment().jolokia().dlqDepth())
                    .as("default deployment: 400 transform-response is ACK'd silently after a route-level WARN log. Operators see the WARN line; DLQ stays at zero. Sister IT covers the opt-in path that lands on the DLQ")
                    .isZero();
            assertThat(environment().liveIngesterContainer().getLogs())
                    .as("the response handler's silent-drop branch must actually have executed — without this positive signal, a fully no-op `ingestContent` would still satisfy the negative assertions above. Asserting the `Transform :: Silently dropped failed transform-response` INFO line binds the negative outcomes to the path under test")
                    .contains(SILENT_DROP_LOG_FRAGMENT);
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Shared trigger used by both this IT and {@link TransformResponseFailureWithDlqOptInReliabilityIT}: publishes a synthetic {@code status=400} transform-response onto the connector's response queue, then publishes a sentinel node through the normal ACS path so liveness can be verified independently of the failure-mode under inspection.
     */
    static SyntheticFailure injectSynthetic400Failure(ReliabilityEnvironment environment) throws IOException
    {
        String droppedNodeRef = UUID.randomUUID().toString();
        String droppedTargetReference = UUID.randomUUID().toString();
        String responsePayload = buildTransformResponsePayload(
                droppedTargetReference, droppedNodeRef, 400, "Transform unsupported");
        log.info("[reliability] Publishing synthetic transform-response status=400 nodeRef={} targetReference={} to queue {}",
                droppedNodeRef, droppedTargetReference, TRANSFORM_RESPONSE_QUEUE);
        DirectQueuePublisher.publishTextMessage(
                environment.activemqDirectBrokerUrl(),
                TRANSFORM_RESPONSE_QUEUE,
                responsePayload);

        Node sentinelNode;
        try (InputStream sentinel = new ByteArrayInputStream("post-transform-failure-sentinel".getBytes()))
        {
            sentinelNode = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, "post-transform-failure-sentinel.txt", sentinel, "text/plain");
        }
        log.info("[reliability] Sentinel node {} published — waiting for liveness signal at HX Insight", sentinelNode.id());

        return new SyntheticFailure(droppedNodeRef, sentinelNode);
    }

    private SyntheticFailure injectSynthetic400Failure() throws IOException
    {
        return injectSynthetic400Failure(environment());
    }

    /**
     * Builds the on-the-wire JSON for a {@code TransformResponse}. The connector's {@code ClientDataSerializer} writes {@code clientData} as a stringified JSON field (delegating to {@code RawJsonSerializer}), so the synthetic payload mirrors that shape — a JSON string whose value is itself the escaped {@code ClientData} JSON — for the response handler's Jackson unmarshal step to succeed.
     */
    private static String buildTransformResponsePayload(
            String targetReference, String nodeRef, int status, String errorDetails)
    {
        String clientDataJson = """
                {"nodeRef":"%s","targetMimeType":"application/pdf","retryAttempt":0,"timestamp":1714521600000}\
                """.formatted(nodeRef);
        String clientDataEscaped = clientDataJson.replace("\"", "\\\"");
        return """
                {
                  "targetReference": "%s",
                  "clientData": "%s",
                  "status": %d,
                  "errorDetails": "%s"
                }
                """.formatted(targetReference, clientDataEscaped, status, errorDetails);
    }

    /**
     * Holder for the identifiers produced by the shared trigger so paired ITs can drive their assertions off the same publish without duplicating the setup code.
     */
    record SyntheticFailure(String droppedNodeRef, Node sentinelNode)
    {
    }
}
