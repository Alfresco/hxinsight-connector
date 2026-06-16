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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import org.alfresco.hxi_connector.e2e_test.reliability.harness.ReliabilityEnvironment;

/**
 * Base class for large-scale ingestion mapping tolerance tests.
 *
 * <p>
 * Responsibilities limited to:
 * <ul>
 * <li>Environment lifecycle (start / stop)</li>
 * <li>Stub cleanup after each test</li>
 * <li>Composite "install all" convenience methods for common scenarios</li>
 * </ul>
 *
 * <p>
 * Stub registration is delegated to {@link NucleusStubFactory}, response body building to {@link StubResponseBuilder}, and POST-body extraction/verification to {@link SyncResponseExtractor}.
 * </p>
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseNucleusSyncLargeIngestionIT
{
    /** Must match NUCLEUS_SYSTEM_ID env var on the nucleus-sync container. */
    protected static final String SYSTEM_ID = "-dummy-system-id";
    protected static final String USER_MAPPINGS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    protected static final String GROUPS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    protected static final String GROUP_MEMBERS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/group-members";

    /** Page size for ACS /people API (must match nucleus-sync's {@code alfresco.page-size} config). */
    protected static final int ACS_PAGE_SIZE = 100;

    /** Page size Nucleus IAM uses (must match nucleus-sync's {@code nucleus.page-size} config). */
    protected static final int NUCLEUS_PAGE_SIZE = 1000;

    protected static final int SCENARIO_STUB_PRIORITY = 1;

    /** Total users — configurable via {@code -Dperformance.userCount}. Default 10 000 for fast local runs. */
    protected static final int TOTAL_USER_COUNT = Integer.getInteger("performance.userCount", 10_000);

    /** Total groups — configurable via {@code -Dperformance.groupsCount}. Default 10 000 for fast local runs. */
    protected static final int TOTAL_GROUPS_COUNT = Integer.getInteger("performance.groupsCount", 10_000);

    /** Track stubs we register so we can clean them up in @AfterEach. */
    protected final List<StubMapping> registeredStubs = new ArrayList<>();

    /** Environment built with withStubbedAcs() to route ACS calls to nucleus WireMock. */
    protected ReliabilityEnvironment environment;

    /** Factory for registering WireMock stubs — initialized after environment starts. */
    protected NucleusStubFactory stubs;

    protected WireMock nucleus()
    {
        return environment.nucleusWireMock();
    }

    @BeforeAll
    void startEnvironment() throws IOException, InterruptedException
    {
        log.info("[scale-test] Starting environment with stubbed ACS for large-scale user mapping test");
        environment = ReliabilityEnvironment.builder()
                .withStubbedAcs()
                .build();
        environment.start();
        stubs = new NucleusStubFactory(
                nucleus(), registeredStubs, SYSTEM_ID, ACS_PAGE_SIZE, NUCLEUS_PAGE_SIZE, SCENARIO_STUB_PRIORITY);
        log.info("[scale-test] Environment ready");
    }

    @AfterAll
    void stopEnvironment()
    {
        if (environment != null)
        {
            environment.close();
        }
    }

    @AfterEach
    void cleanupStubs()
    {
        for (StubMapping stub : registeredStubs)
        {
            try
            {
                nucleus().removeStubMapping(stub);
            }
            catch (Exception e)
            {
                log.warn("[scale-test] Failed to remove stub {}", stub.getName(), e);
            }
        }
        registeredStubs.clear();
    }

    // --- Composite convenience methods ---

    /** Install all stubs needed for a full user+group sync with a specific shared group. */
    protected void installAllTotalUsersBasedStubsWithSameGroupId(int totalUsers, String groupId)
    {
        stubs.installNucleusAuthStub();
        stubs.installAcsPeopleStubs(totalUsers);
        stubs.installAcsUserGroupStubWithGroupId(groupId);
        stubs.installNucleusIamUsersStubs(totalUsers);
        stubs.installEmptyMappingsStub();
        stubs.installEmptyGroupsStub();
        stubs.installEmptyGroupMembersStub();
        stubs.installMutationEndpointsWithTracking();
    }

    /** Install all stubs for a full user sync with empty (any) groups. */
    protected void installAllStubsWithAnyGroup()
    {
        stubs.installNucleusAuthStub();
        stubs.installAcsPeopleStubs(TOTAL_USER_COUNT);
        stubs.installAcsUserGroupsStub();
        stubs.installNucleusIamUsersStubs(TOTAL_USER_COUNT);
        stubs.installEmptyMappingsStub();
        stubs.installEmptyGroupsStub();
        stubs.installEmptyGroupMembersStub();
        stubs.installMutationEndpointsWithTracking();
    }
}
