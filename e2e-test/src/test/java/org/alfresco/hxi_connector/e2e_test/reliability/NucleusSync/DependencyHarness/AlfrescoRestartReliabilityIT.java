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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.DependencyHarness;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.BaseProcessChaosReliabilityIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ProcessChaos;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;

/**
 * Reliability of nucleus-sync's Alfresco client across an Alfresco container restart.
 *
 * <p>
 * <b>Why this extends {@link BaseProcessChaosReliabilityIT} rather than {@code BaseReliabilityIT}.</b> The shared-environment base is documented as off-limits for tests that drive {@code docker stop} on infrastructure — its {@code @BeforeEach} reset asserts broker health + DLQ depth + active subscription, and a chaos-restart from a previous test can leave those invariants broken for the next one. By owning the environment per-class we get a clean ACS to break, and tests in other classes are unaffected.
 *
 * <p>
 * <b>What this pins.</b> After a graceful {@code stop} + {@code start} of ACS, a freshly-triggered sync must (a) reach Alfresco (the new container instance) over the existing {@code toxic-acs} listener, (b) progress past the Alfresco-fetch phase into the Nucleus phase, and (c) drive the IAM-users and user-mappings GETs on the Nucleus mock — proving the nucleus-sync HTTP client recovered from any stale pooled TCP connections.
 *
 * <p>
 * <b>Convergence budget.</b> {@link #SETTLE_BEFORE_SYNC_MS} after readiness lets the live-ingester's JMS subscription re-attach so the broker invariants don't trip the next class's preconditions. The {@link #ASSERTION_CONVERGENCE_MS} retry window is generous because the first request after restart commonly burns a retry on a dead pooled Netty connection before establishing a fresh one.
 */

@Slf4j
public class AlfrescoRestartReliabilityIT extends BaseProcessChaosReliabilityIT
{
    /** Must match {@code NUCLEUS_SYSTEM_ID} on the nucleus-sync container. */
    private static final String SYSTEM_ID = "-dummy-system-id";
    private static final String USER_MAPPINGS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    private static final String GROUPS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    private static final String GROUP_MEMBERS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/group-members";

    /**
     * Wait between "ACS is reachable" and "trigger sync". Covers the gap between the HTTP probe in {@link ProcessChaos#awaitAcsReadiness} (which only checks Tomcat is up) and the People API being fully bootstrapped + the in-memory authentication subsystem accepting credentials.
     */
    private static final long SETTLE_BEFORE_SYNC_MS = 3_000L;

    /**
     * Total wall clock allowed for the assertion to converge. After an ACS restart the first call from nucleus-sync may pay one or two stale-Netty-connection retries before reaching a healthy peer.
     */
    private static final int ASSERTION_CONVERGENCE_MS = 30_000;

    private static final int SCENARIO_STUB_PRIORITY = 1;

    private WireMock nucleus()
    {
        return new WireMock(environment().nucleusMock().getHost(), environment().nucleusMock().getPort());
    }

    @BeforeEach
    void resetNucleusStubsAndJournal()
    {
        // Per-class env means stubs from one @Test would survive into the next; reset explicitly.
        WireMock client = nucleus();
        client.resetMappings();
        client.resetRequests();
        client.resetScenarios();
        installAllStubs();
    }

    @Test
    void shouldRecoverAndCompleteSyncAfterAlfrescoGracefulRestart() throws InterruptedException
    {
        log.info("[chaos] Stopping ACS gracefully");
        ProcessChaos.gracefulStop(environment().repositoryContainer());
        ProcessChaos.awaitContainerExited(environment().repositoryContainer(), Duration.ofSeconds(20));

        log.info("[chaos] Starting ACS");
        ProcessChaos.startRepository(environment());
        ProcessChaos.awaitAcsReadiness(environment(), ACS_READY_DEADLINE_MS); // parallel

        // Tomcat /alfresco being reachable ≠ People API being ready. A short settle here avoids a
        // 401 / 503 on the first authenticated call before the security subsystem finishes loading.
        Thread.sleep(SETTLE_BEFORE_SYNC_MS);

        // Create the user via the post-restart RepositoryClient (refreshed by ProcessChaos.startRepository).
        User user = new User("test", "test", "abcd@hyland.com");
        environment().repositoryClient().createUser(user);

        // Trigger sync. The first attempt to ACS may fail because nucleus-sync's Reactor Netty
        // connection pool still holds dead sockets to the pre-restart container; AlfrescoRetryableInvoker
        // covers that. If startSynchronization() still throws, log and rethrow so failure is loud.
        try
        {
            environment().nucleusSyncClient().startSynchronization();
        }
        catch (RuntimeException e)
        {
            log.error("[chaos] startSynchronization() failed post-restart — see nucleus-sync container logs in test output", e);
            throw e;
        }

        // 6. Assert the recovery contract.
        RetryUtils.assertWithRetry(() -> {
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo("/api/users"))))
                    .as("nucleus-sync did not call /api/users after ACS restart — either retries "
                            + "exhausted on stale Netty pool entries (raise HTTP_RETRY_MAX_ATTEMPTS), "
                            + "the post-restart Alfresco people endpoint was still warming up "
                            + "(raise SETTLE_BEFORE_SYNC_MS), or ALFRESCO_BASE_URL isn't routed via toxic-acs")
                    .isNotEmpty();
            assertThat(nucleus().find(getRequestedFor(urlPathEqualTo(USER_MAPPINGS_PATH))))
                    .as("nucleus-sync did not reach the user-mappings GET after ACS restart — sync "
                            + "orchestration aborted inside the Alfresco-fetch phase")
                    .isNotEmpty();
        }, ASSERTION_CONVERGENCE_MS);
    }

    private void installAllStubs()
    {
        installAuthResponse();
        installReturnUserWithSameMail();
        installReturnEmptyMapping();
        installReturnEmptyGroups();
        installReturnEmptyGroupMembers();
        installMutationEndpoints();
    }

    private void installReturnUserWithSameMail()
    {
        nucleus().register(get(urlPathEqualTo("/api/users"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        {
                          "users": [
                            {
                              "userName": "asmith",
                              "userId": "iam-1234",
                              "email": "abcd@hyland.com"
                            }
                          ]
                        }
                        """)));
    }

    private void installAuthResponse()
    {
        nucleus().register(post(urlEqualTo("/token"))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("""
                        {
                          "access_token": "stub-access-token",
                          "expires_in": 3600,
                          "token_type": "Bearer",
                          "scope": "iam.user-data.account system-integrations-config system-integrations-runtime environment_authorization"
                        }
                        """)));
    }

    private void installReturnEmptyMapping()
    {
        nucleus().register(get(urlPathEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{ \"items\": [] }")));
    }

    private void installReturnEmptyGroups()
    {
        nucleus().register(get(urlPathEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{ \"items\": [] }")));
    }

    private void installReturnEmptyGroupMembers()
    {
        nucleus().register(get(urlPathEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY)
                .willReturn(jsonResponse("{ \"items\": [] }")));
    }

    private void installMutationEndpoints()
    {
        nucleus().register(post(urlEqualTo(USER_MAPPINGS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY).willReturn(jsonResponse("{}")));
        nucleus().register(post(urlEqualTo(GROUPS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY).willReturn(jsonResponse("{}")));
        nucleus().register(post(urlEqualTo(GROUP_MEMBERS_PATH))
                .atPriority(SCENARIO_STUB_PRIORITY).willReturn(jsonResponse("{}")));
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder jsonResponse(String body)
    {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(body);
    }
}
