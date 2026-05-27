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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-only client for the live-ingester's Spring Boot Actuator metrics endpoint. Returns the counter/gauge value, or {@code 0.0} when the metric has not been registered yet.
 *
 * <p>
 * The probe talks to the container's mapped port directly, so it stays available while Toxiproxy chaos is on the other paths.
 */
@Slf4j
public final class ActuatorMetricsProbe
{
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final URI metricsBaseUri;

    public ActuatorMetricsProbe(String host, int port)
    {
        this.http = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();
        this.metricsBaseUri = URI.create("http://%s:%d/actuator/metrics/".formatted(host, port));
    }

    public double counterValue(String metricName)
    {
        return counterValue(metricName, null, null);
    }

    public double counterValue(String metricName, String tagKey, String tagValue)
    {
        return readMeasurement(metricName, tagKey, tagValue, "COUNT");
    }

    public double gaugeValue(String metricName)
    {
        return gaugeValue(metricName, null, null);
    }

    public double gaugeValue(String metricName, String tagKey, String tagValue)
    {
        return readMeasurement(metricName, tagKey, tagValue, "VALUE");
    }

    private double readMeasurement(String metricName, String tagKey, String tagValue, String statistic)
    {
        URI uri = buildUri(metricName, tagKey, tagValue);
        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 404)
            {
                log.debug("[actuator] {} returned 404 — treating as {}=0 (metric not yet registered)", uri, statistic);
                return 0.0;
            }
            if (status != 200)
            {
                throw new IllegalStateException("Unexpected status %d from %s: %s".formatted(status, uri, response.body()));
            }
            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode measurements = root.path("measurements");
            for (JsonNode measurement : measurements)
            {
                if (statistic.equals(measurement.path("statistic").asText()))
                {
                    return measurement.path("value").asDouble(0.0);
                }
            }
            return 0.0;
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to read actuator metric %s from %s".formatted(metricName, uri), e);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to read actuator metric %s from %s".formatted(metricName, uri), e);
        }
    }

    private URI buildUri(String metricName, String tagKey, String tagValue)
    {
        URI base = metricsBaseUri.resolve(URLEncoder.encode(metricName, StandardCharsets.UTF_8));
        if (tagKey == null || tagValue == null)
        {
            return base;
        }
        String tagParam = URLEncoder.encode(tagKey + ":" + tagValue, StandardCharsets.UTF_8);
        return URI.create(base + "?tag=" + tagParam);
    }
}
