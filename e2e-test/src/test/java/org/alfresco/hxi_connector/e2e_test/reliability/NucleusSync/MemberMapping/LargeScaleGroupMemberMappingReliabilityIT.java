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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.MemberMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncLargeIngestionIT;

@Slf4j
public class LargeScaleGroupMemberMappingReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{
    /**
     * Total number of users mapped on BOTH ACS and Nucleus sides — also the number of memberships the test will expect against the shared group. Tune this single knob to scale.
     */
    private static final int TOTAL_USERS = 100000;

    /** Synthetic group every user belongs to — produces {@value #TOTAL_USERS} memberships. */
    private static final String SHARED_GROUP_ID = "groupShared";

    @Test
    void shouldMapAllGroupMembershipsFromBothSides()
    {
        log.info("[scale-test] Starting group-member mapping test: {} users → 1 shared group ({})",
                TOTAL_USERS, SHARED_GROUP_ID);

        // 1. Install all stubs
        installAllStubs();

        long startNanos = System.nanoTime();

        // 2. Trigger sync — both ACS and Nucleus calls hit stubs
        log.info("[scale-test] Triggering synchronization...");
        environment.nucleusSyncClient().startSynchronization();

        // 3. Wait for the membership phase to fire at least one POST /group-members
        RetryUtils.assertWithRetry(() -> {
            int acsPeopleRequests = nucleus().find(getRequestedFor(
                    urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))).size();
            assertThat(acsPeopleRequests)
                    .as("Expected ACS /people to be called")
                    .isGreaterThanOrEqualTo(1);

            int nucleusUserRequests = nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))).size();
            assertThat(nucleusUserRequests)
                    .as("Expected Nucleus /api/users to be called")
                    .isGreaterThanOrEqualTo(1);

            assertThat(nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH))))
                    .as("Expected POST /group-members to be called at least once")
                    .isNotEmpty();
        }, 120, 5000);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[scale-test] Sync completed in {} ms — {} memberships at {} memberships/sec",
                elapsedMs, TOTAL_USERS, TOTAL_USERS * 1000L / Math.max(1, elapsedMs));

        // 4. Verify every (group, user) pair was POSTed. Pairs come in batched array bodies, so
        // we parse all POST /group-members bodies and dedupe by (externalGroupId, memberExternalUserId).
        List<LoggedRequest> memberPosts = nucleus().find(postRequestedFor(urlPathEqualTo(GROUP_MEMBERS_PATH)));
        Set<String> createdMemberships = extractMemberships(memberPosts);

        log.info("[scale-test] Total POST /group-members requests: {}, unique memberships created: {}",
                memberPosts.size(), createdMemberships.size());

        assertThat(createdMemberships.size())
                .as("Expected all %d memberships against group '%s' to be mapped, but only %d were mapped "
                        + "(%d dropped).",
                        TOTAL_USERS, SHARED_GROUP_ID, createdMemberships.size(), TOTAL_USERS - createdMemberships.size())
                .isEqualTo(TOTAL_USERS);

        // Spot-check specific memberships to ensure correctness
        assertThat(createdMemberships)
                .as("First user (user0) should be a member of %s", SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user0"));
        assertThat(createdMemberships)
                .as("Last user (user%d) should be a member of %s", TOTAL_USERS - 1, SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user" + (TOTAL_USERS - 1)));
        assertThat(createdMemberships)
                .as("Middle user (user%d) should be a member of %s", TOTAL_USERS / 2, SHARED_GROUP_ID)
                .contains(membershipKey(SHARED_GROUP_ID, "user" + (TOTAL_USERS / 2)));

        log.info("[scale-test] ✓ All {} memberships against {} successfully mapped — no memberships dropped!",
                TOTAL_USERS, SHARED_GROUP_ID);
    }

    private void installAllStubs()
    {
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USERS);
        installAcsUserGroupStubWithGroupId(SHARED_GROUP_ID);
        installNucleusIamUsersStubs(TOTAL_USERS);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }
}
