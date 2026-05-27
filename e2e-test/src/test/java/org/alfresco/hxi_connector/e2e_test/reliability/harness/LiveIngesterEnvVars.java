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
 * Builds the live-ingester container's env-var map from a {@link ReliabilityEnvironmentSpec}. Routes everything through Toxiproxy, shrinks retry/timeout budgets so tests settle sub-second, and surfaces opt-in toggles from the spec.
 */
@SuppressWarnings("PMD.LongVariable")
final class LiveIngesterEnvVars
{
    private LiveIngesterEnvVars()
    {}

    static Map<String, String> forSpec(ReliabilityEnvironmentSpec spec)
    {
        Map<String, String> env = new LinkedHashMap<>();

        // Comment out this line to see DEBUG-level logs
        // Please note that DEBUG logs have massive impact on reliability/throughput tests.
        env.put("LOGGING_LEVEL_ORG_ALFRESCO", "INFO");

        env.put("SPRING_ACTIVEMQ_BROKERURL", "nio://" + TOXIC_ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT);
        // ACS REST routed through Toxiproxy so chaos tests can inject faults; the test JVM keeps using the host port.
        env.put("ALFRESCO_REPOSITORY_BASE_URL", "http://" + TOXIC_ACS_ALIAS + ":" + REPOSITORY_PORT + "/alfresco");
        // HX Insight traffic routed through Toxiproxy; stub administration stays on the WireMock host port.
        env.put("HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT);
        env.put("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT + "/token");
        // 3 s response timeout — well below the 6 s ACS latency toxic, so slow ACS surfaces as a timeout fast enough for the DLQ contract.
        env.put("ALFRESCO_REPOSITORY_RESPONSETIMEOUTMS", "3000");
        // Tight retry budget keeps the suite deterministic. Production default is 10 attempts with exponential backoff.
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_ATTEMPTS", "2");
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_INITIALDELAY", "200");
        env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_DELAYMULTIPLIER", "1");
        // Tight HXI HTTP profile. Production default is 10 attempts with exponential backoff and a 30 s response timeout.
        env.put("HYLANDEXPERIENCE_INGESTER_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_INGESTER_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_INGESTER_RESPONSETIMEOUTMS", "3000");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_STORAGE_LOCATION_RESPONSETIMEOUTMS", "3000");
        // Upload PUT: same tight retry profile plus a 3 s per-PUT timeout so latency chaos can trip it.
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_ATTEMPTS", "2");
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_INITIALDELAY", "200");
        env.put("HYLANDEXPERIENCE_STORAGE_UPLOAD_RESPONSETIMEOUTMS", "3000");

        if (spec.withTransformTopology())
        {
            // Only the connector's SFS read goes through Toxiproxy; transform-core-aio writes the real alias.
            env.put("ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL",
                    "http://" + TOXIC_SFS_ALIAS + ":" + TOXIC_SFS_LISTEN_PORT);
            // Force text/plain into ATS (target=application/pdf); catch-all keeps every other MIME on passthrough.
            env.put("JAVA_TOOL_OPTIONS",
                    "-agentlib:jdwp=transport=dt_socket,address=*:5007,server=y,suspend=n"
                            + " -Dalfresco.transform.mime-type.mapping.[text/plain]=application/pdf"
                            + " -Dalfresco.transform.mime-type.mapping.[*]=*");
            // Bound the route's broad-Exception retry budget. Production default is finite; tests tighten it further.
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS", "2");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_INITIALDELAY", "200");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_DELAYMULTIPLIER", "1");
            // Tight DLC redelivery profile so transform-response chaos settles sub-second.
            env.put("ALFRESCO_TRANSFORM_RESPONSE_MAXIMUMREDELIVERIES", "1");
            env.put("ALFRESCO_TRANSFORM_RESPONSE_REDELIVERYDELAYMS", "200");
        }

        if (spec.withTransformResponseDeadLetterDisabled())
        {
            env.put("ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED", "false");
        }

        if (spec.withTransformResponseThrowFailedTransformsDisabled())
        {
            env.put("ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS", "false");
        }

        if (spec.withRepoEventsDeadLetterUnsupportedTypesDisabled())
        {
            env.put("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES", "false");
        }

        return env;
    }
}
