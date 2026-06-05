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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.UserMapping;


import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncLargeIngestionIT;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Large-scale user-mapping test — exercises nucleus-sync with 1 million synthetic users on both ACS and Nucleus sides.
 *
 * <h2>Full stub approach</h2>
 * <p>This test stubs BOTH the ACS /people endpoint AND the Nucleus /api/users endpoint with 1 million
 * synthetic users each. The nucleus-sync container's {@code ALFRESCO_BASE_URL} is routed to the nucleus
 * WireMock (via the {@code withStubbedAcs()} builder flag) so ACS calls hit our stubs instead of the real repository.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>Stub ACS /people to return 1M users with emails {@code user0@hyland.com} through {@code user999999@hyland.com}.</li>
 *   <li>Stub Nucleus /api/users to return 1M users with the same emails.</li>
 *   <li>Trigger sync — nucleus-sync fetches both sides and matches by email.</li>
 *   <li>All 1M mappings should be created (perfect intersection), no mappings dropped.</li>
 * </ol>
 *
 * <h2>Performance expectations</h2>
 * <ul>
 *   <li>1M users on both sides: may take several minutes</li>
 *   <li>Memory pressure on WireMock and nucleus-sync — watch heap</li>
 *   <li>Uses chunked stub generation to avoid OOM during stub installation</li>
 * </ul>
 */



@Slf4j
public class LargeScaleUserMappingReliabilityIT extends BaseNucleusSyncLargeIngestionIT {

    /**
     * Tests nucleus-sync's ability to map 1 million users from both ACS and Nucleus sides.
     * Both sides are stubbed with identical synthetic users so we expect all 1M mappings to be created.
     *
     * <p><b>What this proves:</b>
     * <ul>
     *   <li>nucleus-sync can handle 1M users on both sides without OOM or timeout</li>
     *   <li>All 1M mappings are created correctly — no mappings dropped</li>
     *   <li>Pagination works correctly at scale</li>
     * </ul>
     */
    @Test
    void shouldMapOneMillionUsersFromBothSides()
    {
        log.info("[scale-test] Starting full stub test: {} users on both ACS and Nucleus sides", TOTAL_USER_COUNT);
        installAllStubs();
        long startNanos = System.nanoTime();

        // 2. Trigger sync — both ACS and Nucleus calls hit stubs
        log.info("[scale-test] Triggering synchronization for {} users...", TOTAL_USER_COUNT);
        environment.nucleusSyncClient().startSynchronization();

        // 3. Wait for sync to complete and verify mappings
        // For 1M users, allow up to 10 minutes (120 attempts @ 5 second delay)
        RetryUtils.assertWithRetry(() -> {
            // Verify /people was called (proves ACS stub was hit)
            int acsRequests = nucleus().find(getRequestedFor(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))).size();
            assertThat(acsRequests)
                    .as("Expected /people to be called (ACS stub hit)")
                    .isGreaterThanOrEqualTo(1);

            // Verify /api/users was called (proves Nucleus stub was hit)
            int nucleusRequests = nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))).size();
            assertThat(nucleusRequests)
                    .as("Expected /api/users to be called (Nucleus stub hit)")
                    .isGreaterThanOrEqualTo(1);

            // Verify user-mappings POST was called
            List<LoggedRequest> mappingRequests = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
            assertThat(mappingRequests)
                    .as("Expected user-mappings POST to be called")
                    .isNotEmpty();
        }, 120, 5000); // 120 attempts @ 5 second delay = 10 minute timeout for large scale test

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[scale-test] Full 1M sync completed in {} ms ({} users processed at {} users/sec)",
                elapsedMs, TOTAL_USER_COUNT, TOTAL_USER_COUNT * 1000L / Math.max(1, elapsedMs));

        // 4. Verify all mappings were created — extract user IDs from POST requests
        List<LoggedRequest> mappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH)));
        Set<String> mappedUserIds = extractMappedUserIds(mappingPosts);

        log.info("[scale-test] Total mapping POST requests: {}, unique users mapped: {}",
                mappingPosts.size(), mappedUserIds.size());

        // Verify no users were dropped
        assertThat(mappedUserIds.size())
                .as("Expected all %d users to be mapped, but only %d were mapped. " +
                                "Missing count: %d mappings dropped!",
                        TOTAL_USER_COUNT, mappedUserIds.size(), TOTAL_USER_COUNT - mappedUserIds.size())
                .isEqualTo(TOTAL_USER_COUNT);

        // Verify specific users to ensure correctness
        assertThat(mappedUserIds)
                .as("First user (user0) should be mapped")
                .contains("user0");
        assertThat(mappedUserIds)
                .as("Last user (user%d) should be mapped", TOTAL_USER_COUNT - 1)
                .contains("user" + (TOTAL_USER_COUNT - 1));
        assertThat(mappedUserIds)
                .as("Middle user (user%d) should be mapped", TOTAL_USER_COUNT / 2)
                .contains("user" + (TOTAL_USER_COUNT / 2));

        log.info("[scale-test] ✓ All {} users successfully mapped — no mappings dropped!", TOTAL_USER_COUNT);
    }

    private void installAllStubs(){
        // 1. Install all stubs
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USER_COUNT);
        installAcsUserGroupsStub();
        installNucleusIamUsersStubs(TOTAL_USER_COUNT);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }

}
