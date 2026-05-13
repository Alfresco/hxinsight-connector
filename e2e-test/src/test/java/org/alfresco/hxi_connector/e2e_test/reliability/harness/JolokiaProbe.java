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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only client for the Jolokia HTTP servlet bundled with the {@code quay.io/alfresco/alfresco-activemq} image. Exposes a small set of broker observations needed by reliability tests: queue depth, DLQ depth, topic subscriber count, and a basic broker-up probe.
 *
 * <p>
 * All operations are synchronous and short-lived; tests are expected to wrap calls in {@code RetryUtils.retryWithBackoff} when steady-state convergence matters.
 *
 * <p>
 * The probe targets the underlying ActiveMQ container directly (not through Toxiproxy), so observations remain available during simulated network partitions on the broker -> live-ingester path.
 */
@Slf4j
public final class JolokiaProbe
{
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "admin"; // pragma: allowlist secret
    private static final String DEFAULT_BROKER_NAME = "localhost";
    /**
     * Jolokia ships with a default {@code jolokia-access.xml} that has {@code <strict-checking/>} enabled and only allows {@code http://localhost*} / {@code http://127.0.0.1*} origins. Java's {@link HttpClient} sends no {@code Origin} header by default, which makes Jolokia respond {@code 403 "Origin null is not allowed to call this agent"}. Sending an explicit, allow-listed {@code Origin} works around this without touching broker config.
     */
    private static final String JOLOKIA_ALLOWED_ORIGIN = "http://localhost";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final URI jolokiaEndpoint;
    private final String authHeader;
    private final String brokerName;

    public JolokiaProbe(String host, int port)
    {
        this(host, port, DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_BROKER_NAME);
    }

    public JolokiaProbe(String host, int port, String user, String password, String brokerName)
    {
        this.http = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();
        this.jolokiaEndpoint = URI.create("http://%s:%d/api/jolokia/".formatted(host, port));
        String credentials = user + ":" + password;
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        this.brokerName = brokerName;
    }

    public int queueDepth(String queueName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s"
                .formatted(brokerName, queueName);
        return readIntAttribute(mbean, "QueueSize");
    }

    /**
     * ActiveMQ only registers the DLQ MBean lazily, the first time a message is dead-lettered. "MBean absent" therefore means "no message was ever DLQ'd", which is logically a depth of zero. This method translates the 404 to {@code 0} so callers can simply assert {@code assertThat(dlqDepth()).isZero()}.
     */
    public int dlqDepth()
    {
        try
        {
            return queueDepth("ActiveMQ.DLQ");
        }
        catch (JolokiaInstanceNotFoundException e)
        {
            log.debug("[jolokia] DLQ MBean not registered yet — treating as empty");
            return 0;
        }
    }

    public int topicSubscriberCount(String topicName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,destinationName=%s"
                .formatted(brokerName, topicName);
        return readIntAttribute(mbean, "ConsumerCount");
    }

    /**
     * Purge a queue (broker-level {@code purge} operation). Used by reliability tests to scrub leftover dead-lettered messages between tests so each test sees a clean DLQ baseline.
     *
     * <p>
     * Tolerates the case where the queue MBean has not been registered yet (e.g. nothing was ever dead-lettered) — same convention as {@link #dlqDepth()}: "MBean absent" is treated as "nothing to purge".
     */
    public void purgeQueue(String queueName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s"
                .formatted(brokerName, queueName);
        try
        {
            // ActiveMQ's purge operation is overloaded (purge() and purge(long)). Jolokia rejects an unqualified name with HTTP 400 and "Signatures found: ),(long)" — disambiguate by writing the no-arg signature explicitly.
            JsonNode response = post(execRequest(mbean, "purge()"));
            int status = response.path("status").asInt();
            if (status == 404)
            {
                log.debug("[jolokia] queue MBean not registered yet — nothing to purge: {}", queueName);
                return;
            }
            if (status != 200)
            {
                throw new IllegalStateException("[jolokia] purge %s failed: status=%d body=%s"
                        .formatted(mbean, status, response));
            }
        }
        catch (JolokiaInstanceNotFoundException e)
        {
            log.debug("[jolokia] queue MBean not registered yet — nothing to purge: {}", queueName);
        }
    }

    public boolean brokerHealthy()
    {
        try
        {
            String mbean = "org.apache.activemq:type=Broker,brokerName=" + brokerName;
            JsonNode response = post(readRequest(mbean, "BrokerName"));
            return response.path("status").asInt() == 200;
        }
        catch (RuntimeException e)
        {
            log.debug("[jolokia] brokerHealthy probe failed: {}", e.getMessage());
            return false;
        }
    }

    private int readIntAttribute(String mbean, String attribute)
    {
        JsonNode response = post(readRequest(mbean, attribute));
        int status = response.path("status").asInt();
        if (status == 404)
        {
            throw new JolokiaInstanceNotFoundException(
                    "[jolokia] MBean not found: %s (attribute %s)".formatted(mbean, attribute));
        }
        if (status != 200)
        {
            throw new IllegalStateException("[jolokia] read %s/%s failed: status=%d body=%s"
                    .formatted(mbean, attribute, status, response));
        }
        return response.path("value").asInt();
    }

    /**
     * Marker exception that lets callers (notably {@link #dlqDepth()}) distinguish "MBean does not exist" from genuine read errors.
     */
    static final class JolokiaInstanceNotFoundException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        JolokiaInstanceNotFoundException(String message)
        {
            super(message);
        }
    }

    private static String readRequest(String mbean, String attribute)
    {
        return """
                {"type":"read","mbean":"%s","attribute":"%s"}"""
                .formatted(mbean, attribute);
    }

    private static String execRequest(String mbean, String operation)
    {
        return """
                {"type":"exec","mbean":"%s","operation":"%s"}"""
                .formatted(mbean, operation);
    }

    private JsonNode post(String body)
    {
        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(jolokiaEndpoint)
                    .timeout(HTTP_TIMEOUT)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .header("Origin", JOLOKIA_ALLOWED_ORIGIN)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200)
            {
                throw new IllegalStateException("[jolokia] HTTP %d: %s".formatted(response.statusCode(), response.body()));
            }
            return OBJECT_MAPPER.readTree(response.body());
        }
        catch (java.io.IOException e)
        {
            throw new IllegalStateException("[jolokia] HTTP call failed", e);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[jolokia] HTTP call interrupted", e);
        }
    }
}
