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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotModifiedException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Container-lifecycle primitives for chaos tests. Drives the Docker daemon directly through {@link DockerClientFactory#client()} rather than through Testcontainers' {@link GenericContainer#stop()} / {@link GenericContainer#start()}: Testcontainers' "stop" is "stop + remove" and would erase the broker's KahaDB state on every cycle. Stopping at the Docker layer leaves the container in {@code Exited} state with its filesystem intact; {@code docker start} then resumes the same container, preserving the durable subscription registration and any backlog.
 *
 * <p>
 * Docker re-allocates ephemeral host port mappings on every {@code docker start}, so {@link #startBroker(ReliabilityEnvironment)} also refreshes {@link ReliabilityEnvironment}'s cached host-port observations — without that step, {@link JolokiaProbe} would silently hit an unbound port and report the broker as never recovering.
 */
@UtilityClass
@Slf4j
public final class ProcessChaos
{
    private static final String SIGKILL = "KILL";
    private static final long READINESS_POLL_DELAY_MS = 1_000L;

    /**
     * Graceful container stop ({@code docker stop}). Returns once the daemon has acknowledged the request; pair with {@link #awaitContainerExited} when the test needs to assert the process actually died before continuing.
     */
    public static void gracefulStop(GenericContainer<?> container)
    {
        String id = container.getContainerId();
        log.info("[chaos] graceful stop ({}): {}", id, container.getDockerImageName());
        try
        {
            dockerClient().stopContainerCmd(id).exec();
        }
        catch (NotModifiedException alreadyStopped)
        {
            log.debug("[chaos] container {} was already stopped", id);
        }
    }

    /**
     * Abrupt container kill ({@code docker kill -s KILL}). Models OOM-killer / segfault: no shutdown hooks, no broker flush.
     */
    public static void sigKill(GenericContainer<?> container)
    {
        String id = container.getContainerId();
        log.info("[chaos] SIGKILL ({}): {}", id, container.getDockerImageName());
        try
        {
            dockerClient().killContainerCmd(id).withSignal(SIGKILL).exec();
        }
        catch (NotModifiedException alreadyStopped)
        {
            log.debug("[chaos] container {} was already stopped — kill is a no-op", id);
        }
    }

    /**
     * Resume the broker container and refresh {@link ReliabilityEnvironment}'s cached host-port mappings + {@link JolokiaProbe}. Required because Docker re-allocates ephemeral host ports on every {@code docker start}.
     */
    public static void startBroker(ReliabilityEnvironment env)
    {
        start(env.activemqContainer());
        env.refreshAfterBrokerRestart();
    }

    /**
     * Resume the ACS repository container and refresh {@link ReliabilityEnvironment}'s cached host-port mapping + {@link org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient}. Required because Docker re-allocates ephemeral host ports on every {@code docker start}.
     */
    public static void startRepository(ReliabilityEnvironment env)
    {
        start(env.repositoryContainer());
        env.refreshAfterRepositoryRestart();
    }

    /**
     * Resume a previously stopped or killed container ({@code docker start}). Container ID and on-disk state are preserved; host port bindings may be re-allocated. Use {@link #startBroker(ReliabilityEnvironment)} for the broker-specific wrapper that also refreshes host-port-bound observations.
     */
    public static void start(GenericContainer<?> container)
    {
        String id = container.getContainerId();
        log.info("[chaos] start ({}): {}", id, container.getDockerImageName());
        try
        {
            dockerClient().startContainerCmd(id).exec();
        }
        catch (NotModifiedException alreadyRunning)
        {
            log.debug("[chaos] container {} was already running", id);
        }
    }

    /**
     * Block until the Docker daemon reports the container as not running, or the deadline elapses.
     */
    public static void awaitContainerExited(GenericContainer<?> container, Duration deadline)
    {
        String id = container.getContainerId();
        Instant cutoff = Instant.now().plus(deadline);
        while (Instant.now().isBefore(cutoff))
        {
            InspectContainerResponse.ContainerState state = dockerClient().inspectContainerCmd(id).exec().getState();
            if (Boolean.FALSE.equals(state.getRunning()))
            {
                log.debug("[chaos] container {} reached exited state ({})", id, state.getStatus());
                return;
            }
            sleep(READINESS_POLL_DELAY_MS);
        }
        throw new IllegalStateException("[chaos] container %s did not exit within %s — chaos test cannot make assertions on a still-running container".formatted(id, deadline));
    }

    /**
     * Poll {@link JolokiaProbe#brokerHealthy()} until true or {@code deadlineMs} elapses.
     */
    public static void awaitBrokerReadiness(ReliabilityEnvironment env, long deadlineMs)
    {
        log.info("[chaos] waiting for broker readiness (≤ {} ms)", deadlineMs);
        awaitUntil("broker did not become healthy within %d ms — restart did not produce a usable broker"
                .formatted(deadlineMs), deadlineMs, () -> safe(env.jolokia()::brokerHealthy));
        log.info("[chaos] broker is healthy");
    }

    /**
     * Poll the ACS root path ({@code /alfresco}) until it returns a non-error status code or {@code deadlineMs} elapses. Mirrors the {@code Wait.forHttp("/alfresco")} startup strategy used by {@code AlfrescoRepositoryContainer}; Testcontainers' wait strategy only fires on the initial {@code start()}, so chaos tests that drive {@code docker stop} + {@code docker start} need to re-poll explicitly.
     */
    public static void awaitAcsReadiness(ReliabilityEnvironment env, long deadlineMs)
    {
        String probeUrl = env.repositoryBaseUrl() + "/alfresco";
        log.info("[chaos] waiting for ACS readiness at {} (≤ {} ms)", probeUrl, deadlineMs);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        awaitUntil("ACS did not become reachable at %s within %d ms — repository restart did not produce a usable instance"
                .formatted(probeUrl, deadlineMs), deadlineMs, () -> probeHttp(client, probeUrl));
        log.info("[chaos] ACS is reachable");
    }

    /**
     * Cheap GET-and-classify-status probe. Anything that comes back with a status code below 500 counts as "the upstream answered" — readiness is "the process is up enough to respond", not "the response is meaningful". Tomcat answers {@code /alfresco} with a 302 once the webapp is mounted, which is what {@link #awaitAcsReadiness} relies on.
     */
    private static boolean probeHttp(HttpClient client, String url)
    {
        try
        {
            HttpResponse<Void> response = client.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(2)).GET().build(),
                    HttpResponse.BodyHandlers.discarding());
            return response.statusCode() < 500;
        }
        catch (IOException | InterruptedException e)
        {
            if (e instanceof InterruptedException)
            {
                Thread.currentThread().interrupt();
            }
            log.debug("[chaos] HTTP probe at {} failed (treating as not-ready): {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * Poll until the broker is healthy <i>and</i> the live-ingester has (re)registered its consumer on {@link ReliabilityEnvironment#REPO_EVENT_TOPIC}, or {@code deadlineMs} elapses. Subscriber count is the cheapest end-to-end proof the connector is back online — Spring Boot actuator readiness alone does not assert the JMS subscription is up.
     */
    public static void awaitConnectorReadiness(ReliabilityEnvironment env, long deadlineMs)
    {
        log.info("[chaos] waiting for connector readiness (broker healthy + repo subscription registered, ≤ {} ms)", deadlineMs);
        awaitUntil("connector did not recover within %d ms — broker healthy and/or repo subscription on %s never came back"
                .formatted(deadlineMs, ReliabilityEnvironment.REPO_EVENT_TOPIC),
                deadlineMs,
                () -> safe(() -> env.jolokia().brokerHealthy()
                        && env.jolokia().topicSubscriberCount(ReliabilityEnvironment.REPO_EVENT_TOPIC) >= 1));
        log.info("[chaos] connector is ready");
    }

    private static void awaitUntil(String message, long deadlineMs, BooleanSupplier condition)
    {
        Instant cutoff = Instant.now().plusMillis(deadlineMs);
        do
        {
            if (condition.getAsBoolean())
            {
                return;
            }
            sleep(READINESS_POLL_DELAY_MS);
        }
        while (Instant.now().isBefore(cutoff));
        throw new IllegalStateException("[chaos] " + message);
    }

    /**
     * Wraps a probe call so transient probe-side errors (e.g. broker MBean transiently absent right after restart) are treated as "not ready yet" instead of aborting the readiness wait.
     */
    private static boolean safe(BooleanSupplier probe)
    {
        try
        {
            return probe.getAsBoolean();
        }
        catch (RuntimeException e)
        {
            log.debug("[chaos] readiness probe threw — treating as not-ready: {}", e.getMessage());
            return false;
        }
    }

    private static DockerClient dockerClient()
    {
        return DockerClientFactory.lazyClient();
    }

    private static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[chaos] interrupted while polling container state", e);
        }
    }
}
