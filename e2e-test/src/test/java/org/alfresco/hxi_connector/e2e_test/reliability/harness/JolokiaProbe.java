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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only Jolokia client for ActiveMQ MBean observations: queue/topic depths, DLQ depth, subscriber count, broker health, and {@code purge}.
 *
 * <p>
 * Talks to the broker container directly so observations stay available during chaos on the Toxiproxy paths.
 */
@Slf4j
@SuppressWarnings("PMD.GuardLogStatement")
public final class JolokiaProbe
{
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "admin"; // pragma: allowlist secret
    private static final String DEFAULT_BROKER_NAME = "localhost";
    /** Default Jolokia access config rejects requests without an allow-listed Origin header. */
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

    /** The DLQ MBean is registered lazily on first dead-letter, so "MBean absent" means depth zero. */
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

    /**
     * Browse every message currently on {@code ActiveMQ.DLQ} via the queue MBean's {@code browse()} operation. Returns each message as a {@link DeadLetteredMessage} carrying the JMS message id and the Jolokia JSON envelope. The envelope holds JMS metadata (message id, JMS headers, {@code OriginalDestination}, {@code BrokerPath}, {@code StringProperties}, etc.) but <strong>not</strong> the message payload: ActiveMQ's {@code OpenTypeSupport} maps each JMS message type to a CompositeData schema that exposes a body field for {@code TextMessage} and (truncated) {@code BytesMessage} only — and our Camel-converted {@code ObjectMessage} payloads land outside that envelope. Operators inspect bodies through a real JMS browser session (Hawtio, the AMQ Web Console, or a replay consumer); the JMX/Jolokia lane is for metadata-level correlation. Tests typically pin {@link DeadLetteredMessage#envelopeContains(String)} on {@code OriginalDestination} to confirm the dead-letter came from the route they
     * triggered. An absent DLQ MBean (no dead-letters yet) yields an empty list.
     */
    public List<DeadLetteredMessage> browseDlq()
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=ActiveMQ.DLQ"
                .formatted(brokerName);
        try
        {
            JsonNode response = post(execRequest(mbean, "browse()"));
            int status = response.path("status").asInt();
            if (status == 404)
            {
                return List.of();
            }
            if (status != 200)
            {
                throw new IllegalStateException("[jolokia] browse %s failed: status=%d body=%s"
                        .formatted(mbean, status, response));
            }
            JsonNode value = response.path("value");
            if (!value.isArray())
            {
                return List.of();
            }
            List<DeadLetteredMessage> messages = new ArrayList<>(value.size());
            for (JsonNode message : value)
            {
                String messageId = message.path("JMSMessageID").asText("");
                messages.add(new DeadLetteredMessage(messageId, message.toString()));
            }
            return List.copyOf(messages);
        }
        catch (JolokiaInstanceNotFoundException e)
        {
            log.debug("[jolokia] DLQ MBean not registered yet — no messages to browse");
            return List.of();
        }
    }

    /** A single dead-lettered message as exposed by the JMX {@code QueueViewMBean.browse()} operation: the JMS message id plus the Jolokia JSON envelope of metadata (headers, {@code OriginalDestination}, {@code BrokerPath}, string/property maps, etc.). The envelope deliberately omits the JMS payload for {@code ObjectMessage}-shaped messages — see {@link JolokiaProbe#browseDlq()} for the reasoning and the production-side body-inspection paths. Tests use {@link #envelopeContains(String)} to pin metadata fields like {@code OriginalDestination}. */
    public record DeadLetteredMessage(String messageId, String envelope)
    {
        /** Does the dead-lettered message's Jolokia envelope (JMS metadata + headers, <strong>not</strong> payload) contain {@code marker} anywhere? Returns {@code false} for null/empty envelopes. */
        public boolean envelopeContains(String marker)
        {
            return envelope != null && !envelope.isEmpty() && envelope.contains(marker);
        }
    }

    public int topicSubscriberCount(String topicName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,destinationName=%s"
                .formatted(brokerName, topicName);
        return readIntAttribute(mbean, "ConsumerCount");
    }

    /** Messages dispatched but not yet ACK'd. Drains to zero in steady state. */
    public int topicInFlightCount(String topicName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,destinationName=%s"
                .formatted(brokerName, topicName);
        return readIntAttribute(mbean, "InFlightCount");
    }

    /** Cumulative messages accepted by the broker on this topic since it started. */
    public int topicEnqueueCount(String topicName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,destinationName=%s"
                .formatted(brokerName, topicName);
        return readIntAttribute(mbean, "EnqueueCount");
    }

    /** Cumulative messages ACK'd by durable subscribers on this topic since the broker started. */
    public int topicDequeueCount(String topicName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,destinationName=%s"
                .formatted(brokerName, topicName);
        return readIntAttribute(mbean, "DequeueCount");
    }

    /** Purge a queue. Tolerates an absent MBean (nothing to purge). */
    public void purgeQueue(String queueName)
    {
        String mbean = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,destinationName=%s"
                .formatted(brokerName, queueName);
        try
        {
            // {@code purge} is overloaded on the MBean; the no-arg signature is explicit.
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

    /** Distinguishes "MBean does not exist" from genuine read errors. */
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
