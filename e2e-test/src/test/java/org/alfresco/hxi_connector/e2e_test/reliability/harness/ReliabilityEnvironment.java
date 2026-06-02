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
import java.time.Duration;
import java.util.Optional;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.tomakehurst.wiremock.client.WireMock;
import eu.rekawek.toxiproxy.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.e2e_test.util.client.NucleusSyncClient;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;

import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.*;

/**
 * Composes a full Testcontainers topology for reliability testing, fronting every dependency the live-ingester talks to with a Toxiproxy listener so chaos tests can inject failures on each path independently without disturbing the test JVM's own admin / control connections.
 *
 * <p>
 * Wiring (all proxy listeners live in a single Toxiproxy container — distinct ports keep them from colliding):
 * <ul>
 * <li>{@code activemq} alias -> real ActiveMQ broker (repository publishes here).</li>
 * <li>{@code toxic-activemq:61616} -> Toxiproxy listener proxying to {@code activemq:61616}. Live-ingester consumes from here.</li>
 * <li>{@code repository} alias -> real Alfresco repository (test JVM publishes nodes here over the host port).</li>
 * <li>{@code toxic-acs:8080} -> Toxiproxy listener proxying to {@code repository:8080}. Live-ingester downloads content from here.</li>
 * <li>{@code hxinsight-mock} alias -> WireMock standing in for HX Insight (test JVM administers stubs over the host port).</li>
 * <li>{@code toxic-hxi:8081} -> Toxiproxy listener proxying to {@code hxinsight-mock:8080}. Live-ingester posts {@code /presigned-urls}, {@code /ingestion-events}, and fetches the auth {@code /token} through here.</li>
 * <li>{@code aws-mock} alias -> Localstack S3 (test JVM administers the bucket via the host port).</li>
 * <li>{@code toxic-s3:4566} -> Toxiproxy listener proxying to {@code aws-mock:4566}. Live-ingester PUTs content here against the pre-signed URLs WireMock returns; {@link BaseReliabilityIT} swaps the WireMock stub body so the URLs point at {@code toxic-s3} instead of {@code aws-mock}. Localstack ignores presigned-URL signatures by default, so the host swap is transparent.</li>
 * <li>{@code nucleus-mock:8082 -> ToxiProxy listener proxying to nucleus sync}</li>
 * </ul>
 *
 * <p>
 * Construct via {@link #builder()}. The default build leaves the ATS / SFS containers off (most reliability tests do not exercise the transform path); {@link Builder#withTransformTopology()} boots SFS, transform-router, and transform-core-aio alongside the rest, switches ACS to {@code transform.service.enabled=true} java opts, fronts SFS with a {@code toxic-sfs} Toxiproxy listener, and configures the live-ingester to point its {@code ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL} at {@code toxic-sfs} (transform-core-aio still talks to the real {@code shared-file-store} alias for its writes — only the connector's read path is proxied). Used by the transform-path chaos tests (e.g. {@link SfsOutageReliabilityIT}). Adds ~90 s of env boot due to the heavyweight transform-core-aio image, which is why the toggle is off by default.
 *
 * <p>
 * Reliability-fix opt-in toggles are exposed via the builder so paired IT classes can boot envs with the fix enabled / disabled and assert the matching contract on each. See {@link Builder#withTransformResponseDeadLetterEnabled()}.
 *
 * <p>
 * Internally this is a thin orchestrator over four single-responsibility collaborators: {@link ReliabilityEnvironmentSpec} (immutable opt-in toggles), {@link NetworkTopology} (Docker network + alias / port constants), {@link ContainerComposition} (Testcontainers handles + lifecycle), and {@link ToxiproxyListeners} (Toxiproxy container + per-path proxy handles). The Facade itself owns the boot ordering, the test-side clients ({@link RepositoryClient}, {@link AwsS3Client}, {@link JolokiaProbe}, {@link ActuatorMetricsProbe}), and the host-port refresh after a chaos restart.
 */
@Slf4j
@SuppressWarnings("PMD.LongVariable")
public class ReliabilityEnvironment implements AutoCloseable
{
    public static final String BUCKET_NAME = "test-hxinsight-bucket";
    public static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";

    private final ReliabilityEnvironmentSpec spec;
    private final NetworkTopology topology = new NetworkTopology();
    private final ToxiproxyListeners proxies;
    private final ContainerComposition containers;

    // === Test-side clients ===
    private RepositoryClient repositoryClient;
    private AwsS3Client awsS3Client;
    private JolokiaProbe jolokia;
    private ActuatorMetricsProbe actuatorMetrics;
    private NucleusSyncClient nucleusSyncClient;

    // === Host-port mappings refreshed after a chaos restart of the corresponding container
    // (Docker re-allocates ephemeral host ports on each `docker start`; Testcontainers caches the original allocation). ===
    private int activemqOpenWireHostPort;
    private int repositoryHostPort;

    public static Builder builder()
    {
        return new Builder();
    }

    private ReliabilityEnvironment(ReliabilityEnvironmentSpec spec)
    {
        this.spec = spec;
        this.proxies = new ToxiproxyListeners(topology, spec.withTransformTopology());
        this.containers = new ContainerComposition(spec, topology);
    }

    public void start() throws IOException, InterruptedException
    {
        log.info("[reliability] Starting infrastructure containers (transformTopology={})", spec.withTransformTopology());
        containers.startInfrastructure();
        proxies.start();

        containers.awsMock().execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
        awsS3Client = new AwsS3Client(containers.awsMock().getHost(), containers.awsMock().getFirstMappedPort(), BUCKET_NAME);

        containers.startTransformTopology();
        proxies.createPreRepositoryProxies(spec.withTransformTopology());

        activemqOpenWireHostPort = containers.activemq().getMappedPort(ACTIVEMQ_PORT);
        jolokia = new JolokiaProbe(containers.activemq().getHost(), containers.activemq().getMappedPort(ACTIVEMQ_JOLOKIA_PORT));

        containers.startRepository();
        proxies.createAcsProxy();

        containers.startNucleusSync();
        proxies.createNucleusProxy();

        if (spec.withTransformTopology())
        {
            // Wait for ACS to populate its transform registry before any test fires a transform request —
            // see {@link ContainerComposition#waitForTransformConfigPropagation} for the rationale. 90s ceiling:
            // observed first-GET is 5–25s on a warm ACS image, with headroom for cold-start and the 60s
            // config-poll cron interval.
            containers.waitForTransformConfigPropagation(Duration.ofSeconds(90));
        }

        containers.startLiveIngester();
        log.info("[reliability] Live-ingester JDWP listening — attach IntelliJ to localhost:{} (container 5007)",
                containers.liveIngester().getMappedPort(5007));
        actuatorMetrics = new ActuatorMetricsProbe(containers.liveIngester().getHost(), containers.liveIngester().getMappedPort(8080));

        repositoryHostPort = containers.repository().getMappedPort(REPOSITORY_PORT);
        repositoryClient = new RepositoryClient(containers.repository().getBaseUrl(), RepositoryClient.ADMIN_USER);
        nucleusSyncClient = new NucleusSyncClient(containers.nucleusSync().getHost(), containers.nucleusSync().getMappedPort(NUCLEUS_PORT));
        WireMock.configureFor(containers.hxInsightMock().getHost(), containers.hxInsightMock().getPort());

        log.info("[reliability] Environment ready");
    }

    public Proxy activemqProxy()
    {
        return proxies.activemqProxy();
    }

    /**
     * Toxiproxy proxy in front of ACS REST. Use to inject latency / partition on the live-ingester ↔ ACS path without affecting the test JVM's direct {@link RepositoryClient} (which uses the host port).
     */
    public Proxy acsProxy()
    {
        return proxies.acsProxy();
    }

    /**
     * Toxiproxy proxy in front of the HX Insight WireMock. Use to inject latency / partition on the live-ingester ↔ HXI path (covers both {@code /presigned-urls}, {@code /ingestion-events}, and the auth {@code /token} endpoint) without affecting the test JVM's direct WireMock administration (which uses the host port).
     */
    public Proxy hxiProxy()
    {
        return proxies.hxiProxy();
    }

    /**
     * Toxiproxy proxy in front of Localstack S3. Use to inject latency / partition / reset on the live-ingester ↔ S3 upload path. {@link BaseReliabilityIT} re-installs a higher-priority WireMock stub on every reset so {@code POST /presigned-urls} returns a URL whose host is {@code toxic-s3} (rather than the file-based default {@code aws-mock}); the connector therefore PUTs uploads through this proxy. The test JVM's direct {@link AwsS3Client} continues to use the host port and is unaffected.
     */
    public Proxy s3Proxy()
    {
        return proxies.s3Proxy();
    }

    // Proxy for the Nucleus
    public Proxy nucleusproxy(){return proxies.nucleusProxy();}


    // Mock for the Nucleus
    public WireMockContainer nucleusMock(){
        return containers.nucleusMock();
    }

    // Nucleus Client
    public NucleusSyncClient  nucleusSyncClient(){
        return nucleusSyncClient;
    }


    /**
     * Dedicated WireMock client bound to the {@link #nucleusMock()} host port. Use this instead of the static {@code WireMock.stubFor(...)} when wiring Nucleus stubs — the static client is configured against {@link #hxInsightMock()} in {@link BaseReliabilityIT}, so any stub registered through it would land on the wrong mock and the nucleus-sync container's requests would 404.
     */
    public com.github.tomakehurst.wiremock.client.WireMock nucleusWireMock()
    {
        return new com.github.tomakehurst.wiremock.client.WireMock(
                containers.nucleusMock().getHost(),
                containers.nucleusMock().getPort());
    }


    /**
     * Toxiproxy proxy in front of the Shared File Store. {@code null} unless this environment was built via {@link Builder#withTransformTopology()}. Use to inject latency / partition / reset on the live-ingester ↔ SFS rendition-download path. Transform-core-aio's writes to SFS bypass this proxy (they use the real {@code shared-file-store} alias) so chaos here only affects the connector's read path.
     */
    public Proxy sfsProxy()
    {
        return proxies.sfsProxy();
    }

    /**
     * SFS container handle. {@code null} unless this environment was constructed with the transform topology toggle on. Used by chaos tests that drive container lifecycle directly through the Docker client (e.g. {@code docker stop shared-file-store}).
     */
    public GenericContainer<?> sfsContainer()
    {
        return containers.sfs();
    }

    /**
     * Whether this environment booted the SFS / transform-router / transform-core-aio containers and the {@code toxic-sfs} Toxiproxy listener. Test helpers can guard on this to skip transform-only assertions when running against a minimal env.
     */
    public boolean hasTransformTopology()
    {
        return spec.withTransformTopology();
    }

    public org.testcontainers.containers.ToxiproxyContainer toxiproxyContainer()
    {
        return proxies.container();
    }

    public GenericContainer<?> activemqContainer()
    {
        return containers.activemq();
    }

    /**
     * Live-ingester container handle. Used by chaos tests that drive container lifecycle directly through the Docker client.
     */
    public GenericContainer<?> liveIngesterContainer()
    {
        return containers.liveIngester();
    }

    /**
     * ACS repository container handle. Used by chaos tests that drive container lifecycle directly through the Docker client.
     */
    public AlfrescoRepositoryContainer repositoryContainer()
    {
        return containers.repository();
    }

    /**
     * Current base URL for ACS over its host port. Reads the port refreshed by {@link #refreshAfterRepositoryRestart()} so it stays routable after a chaos restart.
     */
    public String repositoryBaseUrl()
    {
        return "http://%s:%s".formatted(containers.repository().getHost(), repositoryHostPort);
    }

    /**
     * OpenWire URL to the broker on its host-mapped port, bypassing Toxiproxy. Used to publish synthetic messages directly to the broker without involving the repository or a network proxy. Reads the host port refreshed by {@link #refreshAfterBrokerRestart()} so it stays routable after a chaos restart.
     */
    public String activemqDirectBrokerUrl()
    {
        return "tcp://" + containers.activemq().getHost() + ":" + activemqOpenWireHostPort;
    }

    /**
     * Re-query the Docker daemon for the repository container's current host port mapping and rebuild {@link RepositoryClient}. Must be called after a chaos test stops and restarts the ACS container — Docker re-allocates ephemeral host ports on each {@code docker start} and Testcontainers caches the original allocation, so subsequent {@link #repositoryClient()} calls would otherwise hit a stale port.
     */
    public void refreshAfterRepositoryRestart()
    {
        InspectContainerResponse info = DockerClientFactory.lazyClient()
                .inspectContainerCmd(containers.repository().getContainerId())
                .exec();
        repositoryHostPort = readHostPort(info, REPOSITORY_PORT);
        String newBaseUrl = "http://%s:%s".formatted(containers.repository().getHost(), repositoryHostPort);
        repositoryClient = new RepositoryClient(newBaseUrl, RepositoryClient.ADMIN_USER);
        log.info("[reliability] Refreshed repository host port mapping: {} (baseUrl={})", repositoryHostPort, newBaseUrl);
    }

    /**
     * Re-query the Docker daemon for the broker container's current host port mappings and rebuild {@link JolokiaProbe}. Must be called after a chaos test stops and restarts the broker container — Docker re-allocates ephemeral host ports on each {@code docker start} and Testcontainers caches the original allocation.
     */
    public void refreshAfterBrokerRestart()
    {
        InspectContainerResponse info = DockerClientFactory.lazyClient()
                .inspectContainerCmd(containers.activemq().getContainerId())
                .exec();
        activemqOpenWireHostPort = readHostPort(info, ACTIVEMQ_PORT);
        int jolokiaHostPort = readHostPort(info, ACTIVEMQ_JOLOKIA_PORT);
        jolokia = new JolokiaProbe(containers.activemq().getHost(), jolokiaHostPort);
        log.info("[reliability] Refreshed broker host port mappings: openwire={}, jolokia={}",
                activemqOpenWireHostPort, jolokiaHostPort);
    }

    private static int readHostPort(InspectContainerResponse info, int containerPort)
    {
        Ports ports = info.getNetworkSettings().getPorts();
        Ports.Binding[] bindings = ports.getBindings().get(ExposedPort.tcp(containerPort));
        return Optional.ofNullable(bindings)
                .filter(b -> b.length > 0)
                .map(b -> Integer.parseInt(b[0].getHostPortSpec()))
                .orElseThrow(() -> new IllegalStateException("[reliability] no host port binding for container port %d".formatted(containerPort)));
    }

    public RepositoryClient repositoryClient()
    {
        return repositoryClient;
    }

    public AwsS3Client awsS3Client()
    {
        return awsS3Client;
    }

    public WireMockContainer hxInsightMock()
    {
        return containers.hxInsightMock();
    }

    public JolokiaProbe jolokia()
    {
        return jolokia;
    }

    public ActuatorMetricsProbe actuatorMetrics()
    {
        return actuatorMetrics;
    }

    @Override
    public void close()
    {
        containers.close();
        // Toxiproxy stops with the upstream containers since their net is going away anyway.
        try
        {
            if (proxies.container().isRunning())
            {
                proxies.container().stop();
            }
        }
        catch (RuntimeException e)
        {
            log.warn("[reliability] Stopping Toxiproxy container failed", e);
        }
        topology.close();
    }

    /**
     * Fluent builder for {@link ReliabilityEnvironment}. Each opt-in toggle is exposed as a no-arg flip method. Defaults are all {@code false} — calling {@link #build()} on an empty builder gives a minimal env equivalent to the production-default deployment shape.
     */
    public static final class Builder
    {
        private boolean withTransformTopology;
        private boolean withTransformResponseDeadLetterEnabled;
        private boolean withTransformResponseThrowFailedTransforms;
        private boolean withRepoEventsDeadLetterUnsupportedTypes;
        private boolean withStubbedAcs;

        private Builder()
        {}

        /**
         * Boot SFS + transform-router + transform-core-aio alongside the rest, switch ACS to {@code transform.service.enabled=true}, front SFS with a {@code toxic-sfs} Toxiproxy listener, and configure the live-ingester to read renditions from {@code toxic-sfs} (writes by transform-core-aio still go to the real {@code shared-file-store} alias). Adds ~90 s of env boot due to the heavyweight transform-core-aio image — only enable for transform-path chaos tests.
         */
        public Builder withTransformTopology()
        {
            this.withTransformTopology = true;
            return this;
        }

        /**
         * Install the {@code errorHandler(deadLetterChannel(...))} on the {@code transform-response} Camel route so post-201 failures land on {@code ActiveMQ.DLQ} with a {@code live_ingester_transform_response_dlq_total} counter increment instead of silently ACK'ing. Sets {@code ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED=true} plus a tight test-only redelivery profile (1 attempt, 200 ms).
         */
        public Builder withTransformResponseDeadLetterEnabled()
        {
            this.withTransformResponseDeadLetterEnabled = true;
            return this;
        }

        /**
         * Surface ATS-reported transform failures (status=400 transform-responses) as a thrown exception instead of the by-design silent ACK. Sets {@code ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS=true}. Pair with {@link #withTransformResponseDeadLetterEnabled()} for the failed message to land on the DLQ; without the DLC opt-in, the thrown exception just exhausts the retry budget and the message is ACK'd anyway (with ERROR logs).
         */
        public Builder withTransformResponseThrowFailedTransforms()
        {
            this.withTransformResponseThrowFailedTransforms = true;
            return this;
        }

        /**
         * Re-throw {@link org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.UnsupportedEventTypeException UnsupportedEventTypeException} for repo events whose {@code eventType} matches no dispatch predicate, instead of the default INFO log + {@code live_ingester_repo_events_unhandled_total} counter increment + silent ACK. Sets {@code ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES=true} so the existing repo-events {@code DeadLetterChannel} routes the failure to {@code ActiveMQ.DLQ} with a {@code live_ingester_repo_events_dlq_total} increment.
         */
        public Builder withRepoEventsDeadLetterUnsupportedTypes()
        {
            this.withRepoEventsDeadLetterUnsupportedTypes = true;
            return this;
        }

        /**
         * Route nucleus-sync's {@code ALFRESCO_BASE_URL} to the nucleus WireMock instead of the real ACS repository.
         * This allows large-scale user mapping tests to stub both ACS and Nucleus with synthetic users rather than
         * creating millions of real users in ACS. When enabled, the test must install appropriate stubs on the
         * nucleus WireMock for the ACS {@code /people} endpoint.
         */
        public Builder withStubbedAcs()
        {
            this.withStubbedAcs = true;
            return this;
        }

        public ReliabilityEnvironment build()
        {
            return new ReliabilityEnvironment(new ReliabilityEnvironmentSpec(
                    withTransformTopology,
                    withTransformResponseDeadLetterEnabled,
                    withTransformResponseThrowFailedTransforms,
                    withRepoEventsDeadLetterUnsupportedTypes,
                    withStubbedAcs));
        }
    }
}
