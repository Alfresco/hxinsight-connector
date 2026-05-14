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

import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.ACTIVEMQ_PORT;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.REPOSITORY_PORT;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_ACS_ALIAS;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_ACTIVEMQ_ALIAS;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_HXI_ALIAS;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_HXI_LISTEN_PORT;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_SFS_ALIAS;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.TOXIC_SFS_LISTEN_PORT;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the live-ingester container's environment-variable map from a {@link ReliabilityEnvironmentSpec}. Pure function: same spec ⇒ same map. Aliases / ports below must stay aligned with the Toxiproxy listener wiring in {@link ReliabilityEnvironment#start()} — both refer to the same logical endpoints in the reliability network topology.
 *
 * <p>
 * The map encodes three distinct concerns, kept together because they all configure one container:
 * <ul>
 * <li>Routing (point the live-ingester at Toxiproxy listeners instead of upstream aliases so chaos tests can inject failures without disturbing the test JVM's own admin connections).</li>
 * <li>Test-only fast retry / timeout profiles (the route shape — bounded retries, DLQ, metric — matches production; the budgets are shrunk so reliability ITs settle sub-second).</li>
 * <li>Reliability-fix opt-in toggles surfaced via the spec (e.g. transform-response DLC, throw-on-failed-transforms).</li>
 * </ul>
 */
@SuppressWarnings("PMD.LongVariable")
final class LiveIngesterEnvVars
{
    private LiveIngesterEnvVars()
    {}

    /**
     * Returns an ordered map of environment variables to apply to the live-ingester container. Insertion order is preserved (LinkedHashMap) so the resulting container env reads top-down in the same logical groupings as historical inline {@code .withEnv(...)} chains.
     */
    static Map<String, String> forSpec(ReliabilityEnvironmentSpec spec)
    {
        Map<String, String> env = new LinkedHashMap<>();

        env.put("SPRING_ACTIVEMQ_BROKERURL", "nio://" + TOXIC_ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT);
        // Route ACS REST through Toxiproxy so chaos tests can inject latency / partition without
        // disturbing the test JVM's direct RepositoryClient (which keeps using the host port).
        env.put("ALFRESCO_REPOSITORY_BASE_URL", "http://" + TOXIC_ACS_ALIAS + ":" + REPOSITORY_PORT + "/alfresco");
        // Route HX Insight ingestion + auth-token traffic through Toxiproxy. The test JVM keeps talking
        // to the WireMock host port directly for stub management; only the connector ↔ HXI path is
        // proxied so chaos tests can inject latency / partition without disturbing stub administration.
        env.put("HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT);
        env.put("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT + "/token");
        // Test-only opt-in to the connector's per-request response timeout for ACS content downloads
        // (production default is 0 — see live-ingester.md). 3s is comfortably below
        // ACS_LATENCY_INJECTION_DELAY used by the latency chaos test (6s) so a slow ACS surfaces as
        // a SocketTimeoutException quickly enough for the bounded JMS budget to drive the message
        // to the DLQ within test wall-time.
        env.put("ALFRESCO_REPOSITORY_RESPONSETIMEOUTMS", "3000");
        // Tight content-download retry budget mirrors the JMS-side test profile: production defaults
        // are 10 attempts with exponential backoff; the chaos suite verifies the *mechanism*
        // (bounded retries, no infinite loop), not the production budget.
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_ATTEMPTS", "2");
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_INITIALDELAY", "200");
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_DELAYMULTIPLIER", "1");
        env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DURABLE", "true");
        // Reliability ITs assert DLQ inventory + metric, so opt in to the route-level DLC for both
        // JMS-fed routes here (production defaults to off since ACS-11592 to preserve master parity
        // for the BulkIngesterE2eTest topology — operators enable per route via these same env vars).
        env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERENABLED", "true");
        env.put("ALFRESCO_BULKINGESTER_DEADLETTERENABLED", "true");
        // Test-only fast DLC profile: production defaults are 6 redeliveries with exponential backoff
        // (1s -> 32s, ~63s before parking). Reliability ITs verify the *mechanism*, not the production
        // retry budget, so we shrink the policy to keep them deterministic and quick (~0.4s before parking).
        // The shape of the route — explicit DLC, bounded redeliveries, DLQ + metric — is identical.
        env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_MAXIMUMREDELIVERIES", "1");
        env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_REDELIVERYDELAYMS", "200");
        env.put("ALFRESCO_BULKINGESTER_MAXIMUMREDELIVERIES", "1");
        env.put("ALFRESCO_BULKINGESTER_REDELIVERYDELAYMS", "200");
        // Test-only fast HX Insight HTTP profile: production defaults are 10 attempts with exponential
        // backoff (0.5s -> 256s, ~8.5min worst-case before parking) and a 30s response timeout.
        // Reliability ITs verify the *mechanism* (retries happen, retries exhaust cleanly, slow responses
        // fail loud), not the production retry budget. Tight values here keep the suite deterministic
        // and quick (~0.6s before retry exhaustion, 3s response timeout) while the route shape stays
        // identical: explicit retry policy, bounded attempts, exhaustion -> JMS DLQ + metric.
        // The 3s response timeout is generous enough that Wiremock burst responses (e.g. during
        // ActiveMqLatencyJitterReliabilityIT's 20-event flood) never trip it spuriously,
        // while still leaving comfortable headroom below the longer Wiremock delays used by the
        // ingestion-event timeout regression guard.
        env.put("HYLANDEXPERIENCE_INGESTER_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_INGESTER_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_INGESTER_RESPONSETIMEOUTMS", "3000");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RESPONSETIMEOUTMS", "3000");
        // Upload path (PUT to S3 presigned URL): same tight test-only retry profile and an explicit
        // 3s per-PUT response timeout so the latency chaos test (S3_LATENCY_INJECTION_DELAY) can trip
        // the timeout deterministically. Production default for the timeout is 0 (opt-in) — see
        // live-ingester.md.
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RESPONSETIMEOUTMS", "3000");

        if (spec.withTransformTopology())
        {
            // Connector's SFS read goes through Toxiproxy; transform-core-aio's writes keep using the real
            // shared-file-store alias so chaos on toxic-sfs doesn't disturb them.
            env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL",
                    "http://" + TOXIC_SFS_ALIAS + ":" + TOXIC_SFS_LISTEN_PORT);
            // Force text/plain into ATS (target=application/pdf); catch-all [*]=* keeps every other MIME on
            // passthrough. JVM-startup config so injected via JAVA_TOOL_OPTIONS — repeats the agentlib flag
            // from DockerContainers.createLiveIngesterContainerWithin since this overrides it.
            env.put("JAVA_TOOL_OPTIONS",
                    "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n"
                            + " -Dalfresco.transform.mime-type.mapping.[text/plain]=application/pdf"
                            + " -Dalfresco.transform.mime-type.mapping.[*]=*");
            // Bound the route's broad-Exception retry budget for fast IT cycles. Production default is
            // unbounded (-1, legacy) — paired with the dead-letter-channel opt-in this leaves the DLC inert
            // because the broad-Exception onException keeps retrying forever and never reaches the DLC's
            // bounded redelivery policy. ITs that boot the DLC opt-in (withTransformResponseDeadLetterEnabled)
            // therefore must override this to a finite value; we use 2 attempts at 200 ms so the IT settles
            // sub-second. The 201-with-download-failure path is bounded separately by
            // ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_ATTEMPTS.
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS", "2");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_INITIALDELAY", "200");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_DELAYMULTIPLIER", "1");
        }

        if (spec.withTransformResponseDeadLetterEnabled())
        {
            // Opt-in route-level deadLetterChannel on transform-response (operator doc:
            // docs/live-ingester.md#transform-response-dead-letter-channel-recommended). Without this,
            // post-201 failures (e.g. SFS download failure after retry exhaustion) silently ACK. Test-only
            // fast profile (1 attempt, 200 ms gap) so the IT settles sub-second; production defaults
            // are 6 redeliveries with exponential 1s -> 32s. Route shape — bounded retries, DLQ, metric —
            // is identical to production.
            env.put("ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED", "true");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_MAXIMUMREDELIVERIES", "1");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_REDELIVERYDELAYMS", "200");
        }

        if (spec.withTransformResponseThrowFailedTransforms())
        {
            // Opt-in to surfacing ATS-reported transform failures (status=400 on the response queue) as
            // FailedTransformResponseException instead of the by-design silent ACK. Pairs with the DLC
            // opt-in above so the exception flows through the route's error handler all the way to the DLQ
            // + Micrometer counter — without that, the exception just exhausts the retry budget and the
            // message is ACK'd anyway (with ERROR logs).
            env.put("ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS", "true");
        }

        if (spec.withRepoEventsDeadLetterUnsupportedTypes())
        {
            // Opt-in to dead-lettering repo events whose eventType matches no dispatch predicate. By default
            // EventProcessor logs INFO + increments live_ingester_repo_events_unhandled_total{type=...} and
            // ACKs the message (preserving forward-compat with new ACS event types — they don't flood the DLQ).
            // The opt-in re-throws UnsupportedEventTypeException so the existing repo-events DeadLetterChannel
            // routes the message to ActiveMQ.DLQ with a live_ingester_repo_events_dlq_total increment.
            env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES", "true");
        }

        return env;
    }
}
