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
package org.alfresco.hxi_connector.e2e_test.reliability.active_mq;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.USER_AGENT;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getAppInfoRegex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Cleanup;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * No-chaos baseline. Proves the Toxiproxy harness is wired end-to-end before any chaos scenarios apply.
 */
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class ActiveMqReliabilityIT extends BaseReliabilityIT
{
    private static final int DELAY_MS = 500;
    private static final String PARENT_ID = "-my-";
    private static final String DUMMY_CONTENT = "Reliability baseline content";

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void shouldDeliverIngestionEventsEndToEndThroughToxiproxyBaseline() throws IOException
    {
        @Cleanup
        InputStream fileContent = new ByteArrayInputStream(DUMMY_CONTENT.getBytes());

        Node createdNode = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "baseline.txt", fileContent, "text/plain");

        RetryUtils.assertWithRetry(() -> {
            verify(exactly(1), postRequestedFor(urlEqualTo("/presigned-urls")));

            verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/ingestion-events"))
                    .withRequestBody(containing("\"objectId\":\"" + createdNode.id() + "\"")
                            .and(containing("\"eventType\":\"createOrUpdate\""))
                            .and(containing("\"sourceTimestamp\"")))
                    .withHeader(USER_AGENT, matching(getAppInfoRegex())));
        }, DELAY_MS);
    }
}
