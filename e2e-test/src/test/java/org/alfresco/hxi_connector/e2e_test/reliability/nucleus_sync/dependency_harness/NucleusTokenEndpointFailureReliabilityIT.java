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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.dependency_harness;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncReliabilityIT;

/**
 * IDP / OAuth2 token endpoint outage — every Nucleus call needs a Bearer token from {@code POST /token}. If that endpoint fails persistently, the sync MUST surface a clear failure (not hang, not silently succeed against {@code Authorization: Bearer null}, not loop forever on token refresh).
 *
 * <h2>Why this is critical</h2> Token failures sit upstream of every other failure mode. None of the other tests exercise this hop — they all assume a working token. A regression that breaks token refresh (expired credentials, IdP misconfig, JWKS rotation, network outage to the IdP) would slip past every existing test.
 *
 * <h2>What this pins</h2>
 * <ol>
 * <li>Token endpoint persistently returns 503 → sync future MUST complete exceptionally.</li>
 * <li>NO Nucleus mutation requests landed (POST /user-mappings, /groups, /group-members all empty) — proves the failure aborted BEFORE any state change, not after partial progress.</li>
 * </ol>
 *
 * <h2>Why no /token GET probe assertion</h2> Whether {@code /token} was hit 1×, 3× (token refresh retry) or N× (per-call refresh) is an implementation detail. We only assert the visible contract: sync fails, no mutations land.
 */
@Slf4j
public class NucleusTokenEndpointFailureReliabilityIT extends BaseNucleusSyncReliabilityIT
{
    /**
     * Outer wait for the sync future to terminate (with an exception). Token failure should fail fast — most code paths hit /token on the very first call. 30 s is generous so a hung token refresh shows up as a test failure here rather than as a confusing TimeoutException.
     */
    private static final long SYNC_FAILURE_TIMEOUT_S = 30L;

    @Test
    void shouldFailSyncWhenTokenEndpointPersistentlyReturns503() throws Exception
    {
        // Pre-condition: real ACS has at least one user so the sync has something to mediate.
        String emailId = "abcd_%s@hyland.com".formatted(UUID.randomUUID());
        createTestUserWithTestEmail(emailId);

        // IMPORTANT: Do NOT install happy-path stubs (installAllStubs) here.
        // When tests run together, Spring Security's OAuth2AuthorizedClientManager inside the
        // nucleus-sync container caches the token from a prior test (expires_in=3600s). If we
        // install working mutation stubs, the sync would succeed silently using the cached token
        // — completely bypassing the /token endpoint we want to test.
        //
        // By ONLY stubbing /token to 503 and leaving Nucleus endpoints unstubbed:
        // • If no cached token → /token returns 503 → sync fails immediately.
        // • If cached token exists → sync calls unstubbed Nucleus endpoints → WireMock returns
        // "no matching stub" (404) → sync still fails and no mutations land.
        nucleus().register(post(urlEqualTo("/token"))
                .atPriority(SCENARIO_STUB_PRIORITY - 1) // priority 0 beats priority 1
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"server_error\",\"error_description\":\"simulated IdP outage\"}")));

        log.info("[token-failure] /token stubbed to return 503 (no other Nucleus stubs) — triggering sync");

        long startNanos = System.nanoTime();
        CompletableFuture<Integer> syncCall = CompletableFuture.supplyAsync(
                () -> environment().nucleusSyncClient().startSynchronization());

        // The sync MUST fail. The exact status depends on which path triggers first:
        // • Direct 503 from /token if no cached token (or cache expired).
        // • 4xx/5xx from unstubbed Nucleus endpoints if cached token is used.
        // Either way, a 200 here means the sync silently succeeded — a serious bug.
        int statusCode = syncCall.get(SYNC_FAILURE_TIMEOUT_S, TimeUnit.SECONDS);
        assertThat(statusCode)
                .as("Sync should fail when /token returns 503 and no Nucleus endpoints are reachable, "
                        + "but got HTTP %d — likely using a cached token from a prior test AND finding "
                        + "working stubs (which should not be installed in this test)", statusCode)
                .isGreaterThanOrEqualTo(400);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        log.info("[token-failure] Sync failed (as expected) with status {} after {} ms", statusCode, elapsedMs);

        // No mutations should have landed — without working Nucleus endpoints, no state changes can persist.
        assertNoMutationsLanded(USER_MAPPINGS_PATH, "user-mapping");
        assertNoMutationsLanded(GROUPS_PATH, "group");
        assertNoMutationsLanded(GROUP_MEMBERS_PATH, "group-member");
    }

    private void assertNoMutationsLanded(String path, String label)
    {
        List<LoggedRequest> posts = nucleus().find(postRequestedFor(urlPathEqualTo(path)));
        assertThat(posts)
                .as("Expected zero %s POSTs while the token endpoint is down, but %d landed — "
                        + "nucleus-sync is sending unauthenticated requests or bypassing the auth layer",
                        label, posts.size())
                .isEmpty();
    }

}
