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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.GroupMapping;

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

@Slf4j
public class LargeScaleGroupMappingReliabilityIT extends BaseNucleusSyncLargeIngestionIT {


    private void installAllStubs(){
        installNucleusAuthStub();
        installSingleAcsUserStub();
        installRepositoryGroupStubs(TOTAL_GROUPS_COUNT);
        installNucleusSingleUserStub();
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }


    /**
     * Tests nucleus-sync's ability to map 1 million Groups from ACS to Nucleus Side
     * The Group needs to be linked or part of any User
     *
     * <p><b>What this proves:</b>
     * <ul>
     *   <li>nucleus-sync can handle 1M Group SYNC</li>
     *   <li>All 1M mappings are created correctly — no mappings dropped</li>
     *   <li>Pagination works correctly at scale</li>
     * </ul>
     */
    @Test
    void shouldMapOneMillionUsersFromBothSides()
    {
        log.info("[scale-test] Starting full stub test: {} users on both ACS and Nucleus sides", TOTAL_GROUPS_COUNT);

        // 1. Install all stubs
        installAllStubs();

        long startNanos = System.nanoTime();

        // 2. Trigger sync — both ACS and Nucleus calls hit stubs
        log.info("[scale-test] Triggering synchronization for {} Groups...", TOTAL_GROUPS_COUNT);
        environment.nucleusSyncClient().startSynchronization();

        // 3. Wait for sync to complete and verify mappings
        // For 1M users, allow up to 10 minutes (120 attempts @ 5 second delay)
        RetryUtils.assertWithRetry(() -> {

            // verify Groups call was done for user<number>
            int acsGroupRequests = nucleus().find(getRequestedFor(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people/user1/groups"))).size();
            assertThat(acsGroupRequests)
                    .as("Expected /people to be called (ACS stub hit)")
                    .isGreaterThanOrEqualTo(1);


            // verify group creation request was hit. Groups are POSTed in batched array bodies
            // (one POST can carry many NucleusGroupInput entries), so we only assert at least one
            // POST landed; the exact group-count check happens after the retry loop.
            assertThat(nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH))))
                    .as("Expected POST /groups to be called at least once")
                    .isNotEmpty();

        }, 120, 5000); // 120 attempts @ 5 second delay = 10 minute timeout for large scale test

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[scale-test] Full 1M sync completed in {} ms ({} users processed at {} users/sec)",
                elapsedMs, TOTAL_GROUPS_COUNT, TOTAL_GROUPS_COUNT * 1000L / Math.max(1, elapsedMs));

        // 4. Verify all groups were created — extract group IDs from POST bodies
        List<LoggedRequest> mappingPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUPS_PATH)));
        Set<String> mappedGroupIds = extractGroupIds(mappingPosts);

        log.info("[scale-test] Total group POST requests: {}, unique groups mapped: {}",
                mappingPosts.size(), mappedGroupIds.size());

        // Verify no groups were dropped
        assertThat(mappedGroupIds.size())
                .as("Expected all %d groups to be mapped, but only %d were mapped. " +
                                "Missing count: %d mappings dropped!",
                        TOTAL_GROUPS_COUNT, mappedGroupIds.size(), TOTAL_GROUPS_COUNT - mappedGroupIds.size())
                .isEqualTo(TOTAL_GROUPS_COUNT);

        // Verify specific groups to ensure correctness (entries are emitted as id="group<N>"
        // by buildAcsGroupPageJson)
        assertThat(mappedGroupIds)
                .as("First group (group0) should be mapped")
                .contains("group0");
        assertThat(mappedGroupIds)
                .as("Last group (group%d) should be mapped", TOTAL_GROUPS_COUNT - 1)
                .contains("group" + (TOTAL_GROUPS_COUNT - 1));
        assertThat(mappedGroupIds)
                .as("Middle group (group%d) should be mapped", TOTAL_GROUPS_COUNT / 2)
                .contains("group" + (TOTAL_GROUPS_COUNT / 2));

        log.info("[scale-test] ✓ All {} groups successfully mapped — no mappings dropped!", TOTAL_GROUPS_COUNT);
    }

}
