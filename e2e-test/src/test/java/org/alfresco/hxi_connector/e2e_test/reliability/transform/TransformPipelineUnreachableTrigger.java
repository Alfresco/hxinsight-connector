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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.Sentinels;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Shared trigger: creates a {@code text/plain} stuck node (the mime-type mapping forces a transform to {@code application/pdf}, which ACS rejects because its transform service is disabled), waits long enough for ACS to synthesise the {@code status=400} transform-response and for the connector to process it, then publishes a sentinel node on the catch-all passthrough so liveness can be verified independently of the stuck-rendition outcome.
 */
@Slf4j
final class TransformPipelineUnreachableTrigger
{
    static final String TRANSFORM_REQUEST_QUEUE = "acs-repo-transform-request";
    static final String JAVA_TOOL_OPTIONS = "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n"
            + " -Dalfresco.transform.mime-type.mapping.[text/plain]=application/pdf"
            + " -Dalfresco.transform.mime-type.mapping.[*]=*";

    /** Fixed wait — the stuck-node assertion is negative (no content POST fires) so a retry-with-backoff would have to time out. */
    private static final long STUCK_OBSERVATION_WAIT_MS = 10_000L;

    private TransformPipelineUnreachableTrigger()
    {}

    static StuckRendition createStuckRenditionAndSentinel(ReliabilityEnvironment environment) throws IOException, InterruptedException
    {
        Node stuckNode;
        try (InputStream stuckContent = new ByteArrayInputStream("text-content-needing-transform".getBytes()))
        {
            stuckNode = environment.repositoryClient()
                    .createNodeWithContent(Sentinels.PARENT_ID, "needs-transform.txt", stuckContent, "text/plain");
        }
        log.info("[reliability] Stuck node {} created with text/plain (mapping forces transform to application/pdf — ACS will reject because transform.service.enabled=false)", stuckNode.id());

        Thread.sleep(STUCK_OBSERVATION_WAIT_MS);

        Node sentinelNode = Sentinels.create(environment, "post-stuck-sentinel.bin", "post-stuck-sentinel", Sentinels.PASSTHROUGH_MIME_TYPE);
        return new StuckRendition(stuckNode, sentinelNode);
    }

    record StuckRendition(Node stuckNode, Node sentinelNode)
    {}
}
