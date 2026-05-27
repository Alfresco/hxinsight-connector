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
package org.alfresco.hxi_connector.e2e_test.reliability.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;

/**
 * Brief S3 partition on the upload PUT path. The {@code toxic-s3} proxy is disabled outright while {@code /presigned-urls} stays up. Asserts the mid-partition upload lands on the DLQ, the route survives, and the subscription is preserved.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class S3NetworkPartitionReliabilityIT extends BaseReliabilityIT
{
    private static final String PARENT_ID = "-my-";
    private static final int PARTITION_SECONDS = 2;
    private static final long SETTLE_AFTER_RECOVERY_MS = 1_000L;
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @Test
    void shouldDeadLetterMidPartitionUploadAndKeepRouteAlive() throws IOException, InterruptedException
    {
        Node victim;
        log.info("[reliability] Disabling toxic-s3 proxy for ~{} s", PARTITION_SECONDS);
        environment().s3Proxy().disable();
        try
        {
            @Cleanup
            InputStream content = new ByteArrayInputStream("mid-partition s3 victim".getBytes());
            victim = environment().repositoryClient()
                    .createNodeWithContent(PARENT_ID, "s3-partition-victim.txt", content, "text/plain");
            log.info("[reliability] Mid-partition node {} published — upload PUT path expected to bounded-fail and DLQ", victim.id());

            Thread.sleep(Duration.ofSeconds(PARTITION_SECONDS).toMillis());
        }
        finally
        {
            log.info("[reliability] Re-enabling toxic-s3 proxy");
            environment().s3Proxy().enable();
        }

        Thread.sleep(SETTLE_AFTER_RECOVERY_MS);

        @Cleanup
        InputStream postContent = new ByteArrayInputStream("post-partition s3 sentinel".getBytes());
        Node sentinel = environment().repositoryClient()
                .createNodeWithContent(PARENT_ID, "s3-partition-sentinel.txt", postContent, "text/plain");
        log.info("[reliability] Post-partition sentinel {} — verifying liveness + DLQ visibility", sentinel.id());

        final Node finalVictim = victim;
        RetryUtils.assertWithRetry(() -> {
            assertThat(WiremockCounts.ingestionEventsFor(sentinel.id()))
                    .as("liveness: post-partition sentinel must reach HX Insight after the upload chain completes — failure here means the route stopped after the S3 partition")
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("mid-partition upload for objectId=%s must surface on the DLQ — a zero here means the failure was silent (route stuck or message dropped, leaving content un-ingested)",
                            finalVictim.id())
                    .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().topicSubscriberCount(ReliabilityEnvironment.REPO_EVENT_TOPIC))
                    .as("S3 partition must not knock out the JMS subscription — live-ingester should still be subscribed to %s", ReliabilityEnvironment.REPO_EVENT_TOPIC)
                    .isGreaterThanOrEqualTo(1);
        }, CONVERGENCE_DELAY_MS);
    }
}
