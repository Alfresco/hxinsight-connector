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
package org.alfresco.hxi_connector.e2e_test.reliability.hxi;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.*;

/**
 * Pins the connector's behaviour as a <i>passthrough</i> for the CloudEvent {@code time} field — Jackson parses it to an {@code OffsetDateTime}, {@code EventUtils.getEventTimestamp} calls {@code .toInstant().toEpochMilli()}, and the result is set verbatim as {@code sourceTimestamp} on the outbound HX Insight POST. The passthrough method covers value range, ISO-8601 format variants, and timezone offsets; each scenario publishes a synthetic CloudEvent with a fresh {@code objectId} and asserts the corresponding epoch-millis arrives unchanged at Wiremock with {@code dlqDepth() == 0}.
 *
 * <p>
 * The rejection method pins the one documented carve-out: {@code IngestNodeCommand} and {@code DeleteNodeCommand} enforce a {@code sourceTimestamp > 0} guard, so the literal Unix epoch and any pre-1970 date trigger a {@code ValidationException} inside the record constructor (before the metadata POST is built) and dead-letter via the route's error handler. If a future change loosens or removes the guard, this method will fail and force a deliberate update.
 *
 * <p>
 * Gated by the {@code reliability-tests} profile; opt-in with {@code mvn -pl e2e-test failsafe:integration-test failsafe:verify -Preliability-tests -Dit.test=TimestampPassthroughReliabilityIT}.
 */
@Slf4j
@SuppressWarnings({"PMD.FieldNamingConventions", "PMD.TestClassWithoutTestCases"})
public class TimestampPassthroughReliabilityIT extends BaseReliabilityIT
{
    private static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";
    /**
     * Convergence budget for per-scenario assertions. Passthrough scenarios converge in well under a second (one publish, one POST). Rejection scenarios need to wait for the bounded JMS redelivery to exhaust before the message lands on DLQ; with the test-profile JMS DLC (1 redelivery, 200 ms delay — see {@link ReliabilityEnvironment}) this is sub-second too. 2 s is comfortable headroom over realistic env latency for both shapes.
     */
    private static final int CONVERGENCE_DELAY_MS = 2_000;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("passthroughTimestampScenarios")
    void shouldPropagateTimestampVerbatim(String label, String wireFormatTime, long expectedEpochMillis) throws IOException
    {
        String fabricatedNodeId = UUID.randomUUID().toString();
        String fabricatedEventId = UUID.randomUUID().toString();
        String payload = buildMetadataOnlyEventWithTime(fabricatedEventId, fabricatedNodeId, wireFormatTime);
        log.info("[reliability] [passthrough/{}] Publishing CloudEvent id={} with time={} (expected epoch-millis={})", label, fabricatedEventId, wireFormatTime, expectedEpochMillis);
        DirectTopicPublisher.publishTextMessage(
                environment().activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                payload);

        String sourceTimestampMarker = "\"sourceTimestamp\":" + expectedEpochMillis;
        String objectIdMarker = "\"objectId\":\"" + fabricatedNodeId + "\"";

        RetryUtils.assertWithRetry(() -> {
            assertThat(findAll(postRequestedFor(urlEqualTo(WiremockCounts.INGESTION_EVENTS_PATH))
                    .withRequestBody(containing(objectIdMarker))
                    .withRequestBody(containing(sourceTimestampMarker))).size())
                            .as("[passthrough/%s] verbatim propagation: expected a POST to /ingestion-events for objectId=%s carrying sourceTimestamp=%d (derived from time=%s). A zero here means either the timestamp was silently rewritten, the offset was stripped, the value was clamped, or the connector failed to emit the event at all",
                                    label, fabricatedNodeId, expectedEpochMillis, wireFormatTime)
                            .isGreaterThanOrEqualTo(1);
            assertThat(environment().jolokia().dlqDepth())
                    .as("[passthrough/%s] no spurious dead-lettering: a parseable post-epoch timestamp is not a poison pill — DLQ must stay at zero", label)
                    .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("rejectedTimestampScenarios")
    void shouldDeadLetterEventWithNonPositiveTimestamp(String label, String wireFormatTime) throws IOException
    {
        String fabricatedNodeId = UUID.randomUUID().toString();
        String fabricatedEventId = UUID.randomUUID().toString();
        String payload = buildMetadataOnlyEventWithTime(fabricatedEventId, fabricatedNodeId, wireFormatTime);
        log.info("[reliability] [reject/{}] Publishing CloudEvent id={} with time={} (expected: ValidationException → DLQ, no /ingestion-events POST)", label, fabricatedEventId, wireFormatTime);
        DirectTopicPublisher.publishTextMessage(
                environment().activemqDirectBrokerUrl(),
                REPO_EVENT_TOPIC,
                payload);

        String objectIdMarker = "\"objectId\":\"" + fabricatedNodeId + "\"";

        RetryUtils.assertWithRetry(() -> {
            assertThat(environment().jolokia().dlqDepth())
                    .as("[reject/%s] documented contract: the {@code sourceTimestamp > 0} guard in IngestNodeCommand throws ValidationException; the route's error handler must park the message on ActiveMQ.DLQ after bounded redelivery", label)
                    .isGreaterThanOrEqualTo(1);
            assertThat(findAll(postRequestedFor(urlEqualTo(WiremockCounts.INGESTION_EVENTS_PATH))
                    .withRequestBody(containing(objectIdMarker))).size())
                            .as("[reject/%s] no POST escapes: the ValidationException is thrown inside the IngestNodeCommand record constructor, before the metadata POST is built — so /ingestion-events must see no traffic for objectId=%s. A non-zero count here means the guard moved past command construction and the connector now emits the event before rejecting it",
                                    label, fabricatedNodeId)
                            .isZero();
        }, CONVERGENCE_DELAY_MS);
    }

    /**
     * Each scenario carries (label, wire-format ISO string, expected epoch-millis). The expected value is derived from the same ISO string via {@link OffsetDateTime#parse(CharSequence)}{@code .toInstant().toEpochMilli()} — i.e. the exact computation the production code performs in {@code EventUtils.getEventTimestamp} — so the assertion encodes "the connector did what {@code Instant.toEpochMilli()} would do here", not a hand-coded magic number.
     */
    static Stream<Arguments> passthroughTimestampScenarios()
    {
        return Stream.of(
                scenario("today / UTC / millis", "2026-04-29T09:00:00.000Z"),
                scenario("far future (9999)", "9999-12-31T23:59:59.999Z"),
                scenario("positive non-UTC offset (+05:30)", "2026-04-29T15:00:00+05:30"),
                scenario("negative non-UTC offset (-08:00)", "2026-04-29T01:00:00-08:00"),
                scenario("nanosecond precision (truncates to ms)", "2026-04-29T09:00:00.123456789Z"),
                scenario("no fractional seconds", "2026-04-29T09:00:00Z"));
    }

    static Stream<Arguments> rejectedTimestampScenarios()
    {
        return Stream.of(
                Arguments.of("exact Unix epoch (epoch-millis=0)", "1970-01-01T00:00:00Z"),
                Arguments.of("pre-1970 (epoch-millis<0)", "1900-01-01T00:00:00Z"));
    }

    private static Arguments scenario(String label, String wireFormatTime)
    {
        long expectedEpochMillis = OffsetDateTime.parse(wireFormatTime).toInstant().toEpochMilli();
        return Arguments.of(label, wireFormatTime, expectedEpochMillis);
    }

    private static String buildMetadataOnlyEventWithTime(String eventId, String nodeId, String time)
    {
        return """
                {
                  "specversion": "1.0",
                  "type": "org.alfresco.event.node.Created",
                  "id": "%s",
                  "source": "/reliability-it",
                  "time": "%s",
                  "dataschema": "https://api.alfresco.com/schema/event/repo/v1/nodeCreated",
                  "datacontenttype": "application/json",
                  "data": {
                    "eventGroupId": "%s",
                    "resource": {
                      "@type": "NodeResource",
                      "id": "%s",
                      "name": "timestamp-passthrough.txt",
                      "nodeType": "cm:content",
                      "createdAt": "%s",
                      "modifiedAt": "%s",
                      "createdByUser": { "id": "admin", "displayName": "Administrator" },
                      "modifiedByUser": { "id": "admin", "displayName": "Administrator" },
                      "properties": {
                        "cm:title": "timestamp-passthrough"
                      },
                      "aspectNames": [ "cm:titled" ],
                      "primaryHierarchy": [ "-my-" ],
                      "isFolder": false,
                      "isFile": true
                    },
                    "resourceReaderAuthorities": ["GROUP_EVERYONE"],
                    "resourceDeniedAuthorities": []
                  }
                }
                """.formatted(eventId, time, UUID.randomUUID(), nodeId, time, time);
    }
}
