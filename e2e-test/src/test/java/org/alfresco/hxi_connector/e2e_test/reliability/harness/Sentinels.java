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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Creates "sentinel" nodes used by DLC / poison-pill reliability tests to prove liveness after a failure scenario. A sentinel is a plain node that should reach HX Insight regardless of whatever just dead-lettered, so a test can fail loudly if the failure scenario knocked out unrelated paths.
 */
@Slf4j
public final class Sentinels
{
    public static final String PARENT_ID = "-my-";
    /** Catch-all passthrough MIME — bypasses ATS+SFS so the sentinel reaches HX Insight even when the transform pipeline is being chaos-tested. */
    public static final String PASSTHROUGH_MIME_TYPE = "application/octet-stream";

    private Sentinels()
    {}

    /** Default sentinel: text/plain content under the catch-all passthrough so it does not collide with transform-path scenarios. */
    public static Node create(ReliabilityEnvironment environment, String fileName, String content) throws IOException
    {
        return create(environment, fileName, content, "text/plain");
    }

    /** Sentinel with explicit MIME — use {@link #PASSTHROUGH_MIME_TYPE} when the test exercises the transform pipeline and the sentinel must bypass it. */
    public static Node create(ReliabilityEnvironment environment, String fileName, String content, String mimeType) throws IOException
    {
        Node node;
        try (InputStream payload = new ByteArrayInputStream(content.getBytes()))
        {
            node = environment.repositoryClient()
                    .createNodeWithContent(PARENT_ID, fileName, payload, mimeType);
        }
        log.info("[reliability] Sentinel node {} published ({}, mime={}) — waiting for liveness signal at HX Insight",
                node.id(), fileName, mimeType);
        return node;
    }
}
