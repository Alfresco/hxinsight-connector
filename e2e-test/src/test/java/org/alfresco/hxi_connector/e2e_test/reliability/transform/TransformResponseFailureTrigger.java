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

import java.io.IOException;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.DirectQueuePublisher;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Sentinels;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/** Publishes a synthetic {@code status=400} transform-response plus a normal sentinel. Shared by the default-path and opt-out transform-response-failure ITs. */
@Slf4j
final class TransformResponseFailureTrigger
{
    static final String TRANSFORM_RESPONSE_QUEUE = "org.alfresco.hxinsight-connector.transform.response";

    private TransformResponseFailureTrigger()
    {}

    static SyntheticFailure inject(ReliabilityEnvironment environment) throws IOException
    {
        String droppedNodeRef = UUID.randomUUID().toString();
        String droppedTargetReference = UUID.randomUUID().toString();
        String responsePayload = buildTransformResponsePayload(droppedTargetReference, droppedNodeRef, 400, "Transform unsupported");
        log.info("[reliability] Publishing synthetic transform-response status=400 nodeRef={} targetReference={} to queue {}",
                droppedNodeRef, droppedTargetReference, TRANSFORM_RESPONSE_QUEUE);
        DirectQueuePublisher.publishTextMessage(
                environment.activemqDirectBrokerUrl(),
                TRANSFORM_RESPONSE_QUEUE,
                responsePayload);

        Node sentinelNode = Sentinels.create(environment, "post-transform-failure-sentinel.txt", "post-transform-failure-sentinel");
        return new SyntheticFailure(droppedNodeRef, sentinelNode);
    }

    /**
     * Mirrors the on-the-wire {@code TransformResponse} shape: {@code clientData} is serialised as a stringified JSON field, matching {@code ClientDataSerializer}'s output.
     */
    private static String buildTransformResponsePayload(String targetReference, String nodeRef, int status, String errorDetails)
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

    record SyntheticFailure(String droppedNodeRef, Node sentinelNode)
    {}
}
