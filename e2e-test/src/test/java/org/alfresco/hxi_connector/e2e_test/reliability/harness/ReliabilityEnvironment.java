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

import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.ENTERPRISE;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getMinimalRepoJavaOpts;
import static org.alfresco.hxi_connector.common.test.docker.util.DockerContainers.getRepoJavaOptsWithTransforms;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.tomakehurst.wiremock.client.WireMock;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryClient;

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
 * </ul>
 *
 * <p>
 * Construct via {@link #builder()}. The default build leaves the ATS / SFS containers off (most reliability tests do not exercise the transform path); {@link Builder#withTransformTopology()} boots SFS, transform-router, and transform-core-aio alongside the rest, switches ACS to {@code transform.service.enabled=true} java opts, fronts SFS with a {@code toxic-sfs} Toxiproxy listener, and configures the live-ingester to point its {@code ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL} at {@code toxic-sfs} (transform-core-aio still talks to the real {@code shared-file-store} alias for its writes — only the connector's read path is proxied). Used by the transform-path chaos tests (e.g. {@link SfsOutageReliabilityIT}). Adds ~90 s of env boot due to the heavyweight transform-core-aio image, which is why the toggle is off by default.
 *
 * <p>
 * Reliability-fix opt-in toggles are exposed via the builder so paired IT classes can boot envs with the fix enabled / disabled and assert the matching contract on each. See {@link Builder#withTransformResponseDeadLetterEnabled()}.
 */
@Slf4j
public class ReliabilityEnvironment implements AutoCloseable
{
    public static final String BUCKET_NAME = "test-hxinsight-bucket";
    public static final String REPO_EVENT_TOPIC = "alfresco.repo.event2";

    private static final String TOXIPROXY_IMAGE = "ghcr.io/shopify/toxiproxy:2.12.0";
    private static final String ACTIVEMQ_ALIAS = "activemq";
    private static final String TOXIC_ACTIVEMQ_ALIAS = "toxic-activemq";
    private static final String REPOSITORY_ALIAS = "repository";
    private static final String TOXIC_ACS_ALIAS = "toxic-acs";
    private static final String HXI_MOCK_ALIAS = "hxinsight-mock";
    private static final String TOXIC_HXI_ALIAS = "toxic-hxi";
    private static final String LOCALSTACK_ALIAS = "aws-mock";
    private static final String TOXIC_S3_ALIAS = "toxic-s3";
    private static final String SFS_ALIAS = "shared-file-store";
    private static final String TOXIC_SFS_ALIAS = "toxic-sfs";
    private static final int ACTIVEMQ_PORT = 61616;
    private static final int ACTIVEMQ_JOLOKIA_PORT = 8161;
    private static final int REPOSITORY_PORT = 8080;
    private static final int HXI_MOCK_PORT = 8080;
    /**
     * Inside-Toxiproxy listen port for the {@code toxic-sfs} proxy. Matches the SFS upstream port — Toxiproxy listeners can share a port number across distinct aliases (each alias resolves to the same Toxiproxy IP and the listener port selects the proxy), but each listener still needs a unique port within the Toxiproxy container.
     */
    private static final int TOXIC_SFS_LISTEN_PORT = 8099;
    private static final int SFS_PORT = 8099;
    /**
     * Port the {@code toxic-hxi} Toxiproxy listener binds inside the Toxiproxy container. Distinct from {@link #REPOSITORY_PORT} so the ACS and HXI proxies can coexist in the single Toxiproxy container — a Toxiproxy listener cannot share a port with another listener even if the network aliases differ.
     */
    private static final int TOXIC_HXI_LISTEN_PORT = 8081;
    private static final int LOCALSTACK_PORT = 4566;
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    // === Reliability-fix opt-ins ===
    private final boolean withTransformTopology;
    private final boolean withTransformResponseDeadLetterEnabled;
    private final boolean withTransformResponseThrowFailedTransforms;
    private final boolean withRepoEventsDeadLetterUnsupportedTypes;

    // === Containers (sfs / transformCoreAio / transformRouter populated only when withTransformTopology) ===
    private final Network network = Network.newNetwork();
    private final PostgreSQLContainer<?> postgres;
    private final GenericContainer<?> activemq;
    private final ToxiproxyContainer toxiproxy;
    private final WireMockContainer hxInsightMock;
    private final LocalStackContainer awsMock;
    private final AlfrescoRepositoryContainer repository;
    private final GenericContainer<?> liveIngester;
    private final GenericContainer<?> sfs;
    private final GenericContainer<?> transformCoreAio;
    private final GenericContainer<?> transformRouter;

    // === Toxiproxy listeners (created in start(); sfsProxy populated only when withTransformTopology) ===
    private Proxy activemqProxy;
    private Proxy acsProxy;
    private Proxy hxiProxy;
    private Proxy s3Proxy;
    private Proxy sfsProxy;

    // === Test-side clients ===
    private RepositoryClient repositoryClient;
    private AwsS3Client awsS3Client;
    private JolokiaProbe jolokia;
    private ActuatorMetricsProbe actuatorMetrics;

    // === Host-port mappings refreshed after a chaos restart of the corresponding container
    // (Docker re-allocates ephemeral host ports on each `docker start`; Testcontainers caches the original allocation). ===
    private int activemqOpenWireHostPort;
    private int activemqJolokiaHostPort;
    private int repositoryHostPort;

    public static Builder builder()
    {
        return new Builder();
    }

    private ReliabilityEnvironment(Builder b)
    {
        this.withTransformTopology = b.withTransformTopology;
        this.withTransformResponseDeadLetterEnabled = b.withTransformResponseDeadLetterEnabled;
        this.withTransformResponseThrowFailedTransforms = b.withTransformResponseThrowFailedTransforms;
        this.withRepoEventsDeadLetterUnsupportedTypes = b.withRepoEventsDeadLetterUnsupportedTypes;
        postgres = DockerContainers.createPostgresContainerWithin(network);
        activemq = DockerContainers.createActiveMqContainerWithin(network);

        if (withTransformTopology)
        {
            sfs = DockerContainers.createSfsContainerWithin(network);
            // LibreOffice (text/plain → application/pdf path) needs more heap than the default 1024m;
            // matching the docker-compose-minimal.yml + ATSTransformE2eTest sizing.
            transformCoreAio = DockerContainers.createTransformCoreAioContainerWithin(network)
                    .withEnv("JAVA_OPTS", "-Xms256m -Xmx3000m")
                    .dependsOn(activemq, sfs);
            transformRouter = DockerContainers.createTransformRouterContainerWithin(network)
                    .dependsOn(activemq, transformCoreAio);
        }
        else
        {
            sfs = null;
            transformCoreAio = null;
            transformRouter = null;
        }

        // Toxiproxy listener aliases vary with the topology toggle: toxic-sfs is only needed when the live-ingester
        // talks to SFS (transform-path tests). Listeners must each bind a unique port inside Toxiproxy even though
        // the upstream containers can collide on a port number (ACS and HXI both serve 8080) — the alias selects
        // which Toxiproxy listener a connector request hits, the listener port disambiguates within the proxy.
        String[] toxiproxyAliases = withTransformTopology
                ? new String[]{TOXIC_ACTIVEMQ_ALIAS, TOXIC_ACS_ALIAS, TOXIC_HXI_ALIAS, TOXIC_S3_ALIAS, TOXIC_SFS_ALIAS}
                : new String[]{TOXIC_ACTIVEMQ_ALIAS, TOXIC_ACS_ALIAS, TOXIC_HXI_ALIAS, TOXIC_S3_ALIAS};
        toxiproxy = new ToxiproxyContainer(DockerImageName.parse(TOXIPROXY_IMAGE))
                .withNetwork(network)
                .withNetworkAliases(toxiproxyAliases)
                .withStartupTimeout(STARTUP_TIMEOUT);

        hxInsightMock = DockerContainers.createWireMockContainerWithin(network)
                // Pin an explicit alias so the {@code toxic-hxi} Toxiproxy upstream is legible at the call site
                // ({@link DockerContainers#createWireMockContainerWithin} otherwise leaves the container with
                // only Testcontainers' auto-generated alias).
                .withNetworkAliases(HXI_MOCK_ALIAS)
                .withFileSystemBind(
                        "src/test/resources/wiremock/hxinsight",
                        "/home/wiremock",
                        BindMode.READ_ONLY);

        awsMock = DockerContainers.createLocalStackContainerWithin(network);

        // ACS java opts switch with the toggle: transform.service.enabled=true plus transform-router / SFS URLs
        // are needed for the transform-capability registry to populate; otherwise the registry stays empty
        // and ACS short-circuits every transform request with a status=400 transform-response (the path
        // exercised by TransformPipelineUnreachableReliabilityIT, intentionally distinct from the SFS-outage
        // topology in SfsOutageReliabilityIT where real transforms run and only the connector's SFS read is
        // proxied through Toxiproxy).
        String repoJavaOpts = withTransformTopology
                ? getRepoJavaOptsWithTransforms(postgres, activemq)
                : getMinimalRepoJavaOpts(postgres, activemq);
        repository = DockerContainers.createExtendedRepositoryContainerWithin(network, ENTERPRISE)
                .withJavaOpts(repoJavaOpts);

        liveIngester = DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, network)
                .withEnv("SPRING_ACTIVEMQ_BROKERURL", "nio://" + TOXIC_ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT)
                // Route ACS REST through Toxiproxy so chaos tests can inject latency / partition without
                // disturbing the test JVM's direct {@link RepositoryClient} (which keeps using the host port).
                .withEnv("ALFRESCO_REPOSITORY_BASE_URL", "http://" + TOXIC_ACS_ALIAS + ":" + REPOSITORY_PORT + "/alfresco")
                // Route HX Insight ingestion + auth-token traffic through Toxiproxy. The test JVM keeps talking
                // to the WireMock host port directly for stub management; only the connector ↔ HXI path is
                // proxied so chaos tests can inject latency / partition without disturbing stub administration.
                .withEnv("HYLANDEXPERIENCE_INSIGHT_INGESTION_BASEURL", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT)
                .withEnv("AUTH_PROVIDERS_HYLANDEXPERIENCE_TOKENURI", "http://" + TOXIC_HXI_ALIAS + ":" + TOXIC_HXI_LISTEN_PORT + "/token")
                // Test-only opt-in to the connector's per-request response timeout for ACS content downloads
                // (production default is 0 — see live-ingester.md). 3s is comfortably below
                // ACS_LATENCY_INJECTION_DELAY used by the latency chaos test (6s) so a slow ACS surfaces as
                // a SocketTimeoutException quickly enough for the bounded JMS budget to drive the message
                // to the DLQ within test wall-time.
                .withEnv("ALFRESCO_REPOSITORY_RESPONSETIMEOUTMS", "3000")
                // Tight content-download retry budget mirrors the JMS-side test profile: production defaults
                // are 10 attempts with exponential backoff; the chaos suite verifies the *mechanism*
                // (bounded retries, no infinite loop), not the production budget.
                .withEnv("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_ATTEMPTS", "2")
                .withEnv("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_INITIALDELAY", "200")
                .withEnv("ALFRESCO_TRANSFORM_SHAREDFILESTORE_RETRY_DELAYMULTIPLIER", "1")
                .withEnv("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DURABLE", "true")
                // Reliability ITs assert DLQ inventory + metric, so opt in to the route-level DLC for both
                // JMS-fed routes here (production defaults to off since ACS-11592 to preserve master parity
                // for the BulkIngesterE2eTest topology — operators enable per route via these same env vars).
                .withEnv("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERENABLED", "true")
                .withEnv("ALFRESCO_BULKINGESTER_DEADLETTERENABLED", "true")
                // Test-only fast DLC profile: production defaults are 6 redeliveries with exponential backoff
                // (1s -> 32s, ~63s before parking). Reliability ITs verify the *mechanism*, not the production
                // retry budget, so we shrink the policy to keep them deterministic and quick (~0.4s before parking).
                // The shape of the route — explicit DLC, bounded redeliveries, DLQ + metric — is identical.
                .withEnv("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_MAXIMUMREDELIVERIES", "1")
                .withEnv("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_REDELIVERYDELAYMS", "200")
                .withEnv("ALFRESCO_BULKINGESTER_MAXIMUMREDELIVERIES", "1")
                .withEnv("ALFRESCO_BULKINGESTER_REDELIVERYDELAYMS", "200")
                // Test-only fast HX Insight HTTP profile: production defaults are 10 attempts with exponential
                // backoff (0.5s -> 256s, ~8.5min worst-case before parking) and a 30s response timeout.
                // Reliability ITs verify the *mechanism* (retries happen, retries exhaust cleanly, slow responses
                // fail loud), not the production retry budget. Tight values here keep the suite deterministic
                // and quick (~0.6s before retry exhaustion, 3s response timeout) while the route shape stays
                // identical: explicit retry policy, bounded attempts, exhaustion -> JMS DLQ + metric.
                // The 3s response timeout is generous enough that Wiremock burst responses (e.g. during
                // {@code ActiveMqLatencyJitterReliabilityIT}'s 20-event flood) never trip it spuriously,
                // while still leaving comfortable headroom below the longer Wiremock delays used by the
                // ingestion-event timeout regression guard.
                .withEnv("HYLANDEXPERIENCE_INGESTER_RETRY_ATTEMPTS", "2")
                .withEnv("HYLANDEXPERIENCE_INGESTER_RETRY_INITIALDELAY", "200")
                .withEnv("HYLANDEXPERIENCE_INGESTER_RESPONSETIMEOUTMS", "3000")
                .withEnv("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_ATTEMPTS", "2")
                .withEnv("HYLANDEXPERIENCE_STORAGE_LOCATION_RETRY_INITIALDELAY", "200")
                .withEnv("HYLANDEXPERIENCE_STORAGE_LOCATION_RESPONSETIMEOUTMS", "3000")
                // Upload path (PUT to S3 presigned URL): same tight test-only retry profile and an explicit
                // 3s per-PUT response timeout so the latency chaos test (S3_LATENCY_INJECTION_DELAY) can trip
                // the timeout deterministically. Production default for the timeout is 0 (opt-in) — see
                // live-ingester.md.
                .withEnv("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_ATTEMPTS", "2")
                .withEnv("HYLANDEXPERIENCE_STORAGE_UPLOAD_RETRY_INITIALDELAY", "200")
                .withEnv("HYLANDEXPERIENCE_STORAGE_UPLOAD_RESPONSETIMEOUTMS", "3000");

        if (withTransformTopology)
        {
            // Connector's SFS read goes through Toxiproxy; transform-core-aio's writes keep using the real
            // shared-file-store alias so chaos on toxic-sfs doesn't disturb them.
            liveIngester.withEnv("ALFRESCO_TRANSFORM_SHAREDFILESTORE_BASEURL",
                    "http://" + TOXIC_SFS_ALIAS + ":" + TOXIC_SFS_LISTEN_PORT);
            // Force text/plain into ATS (target=application/pdf); catch-all [*]=* keeps every other MIME on
            // passthrough. JVM-startup config so injected via JAVA_TOOL_OPTIONS — repeats the agentlib flag
            // from DockerContainers.createLiveIngesterContainerWithin since this overrides it.
            liveIngester.withEnv("JAVA_TOOL_OPTIONS",
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
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_ATTEMPTS", "2");
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_INITIALDELAY", "200");
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_RETRYINGESTION_DELAYMULTIPLIER", "1");
        }

        if (withTransformResponseDeadLetterEnabled)
        {
            // Opt-in route-level deadLetterChannel on transform-response (operator doc:
            // docs/live-ingester.md#transform-response-dead-letter-channel-recommended). Without this,
            // post-201 failures (e.g. SFS download failure after retry exhaustion) silently ACK. Test-only
            // fast profile (1 attempt, 200 ms gap) so the IT settles sub-second; production defaults
            // are 6 redeliveries with exponential 1s -> 32s. Route shape — bounded retries, DLQ, metric —
            // is identical to production.
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_DEADLETTERENABLED", "true");
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_MAXIMUMREDELIVERIES", "1");
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_REDELIVERYDELAYMS", "200");
        }

        if (withTransformResponseThrowFailedTransforms)
        {
            // Opt-in to surfacing ATS-reported transform failures (status=400 on the response queue) as
            // FailedTransformResponseException instead of the by-design silent ACK. Pairs with the DLC
            // opt-in above so the exception flows through the route's error handler all the way to the DLQ
            // + Micrometer counter — without that, the exception just exhausts the retry budget and the
            // message is ACK'd anyway (with ERROR logs).
            liveIngester.withEnv("ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS", "true");
        }

        if (withRepoEventsDeadLetterUnsupportedTypes)
        {
            // Opt-in to dead-lettering repo events whose eventType matches no dispatch predicate. By default
            // EventProcessor logs INFO + increments live_ingester_repo_events_unhandled_total{type=...} and
            // ACKs the message (preserving forward-compat with new ACS event types — they don't flood the DLQ).
            // The opt-in re-throws UnsupportedEventTypeException so the existing repo-events DeadLetterChannel
            // routes the message to ActiveMQ.DLQ with a live_ingester_repo_events_dlq_total increment.
            liveIngester.withEnv("ALFRESCO_REPOSITORY_EVENTSSUBSCRIPTION_DEADLETTERUNSUPPORTEDTYPES", "true");
        }
    }

    public void start() throws IOException, InterruptedException
    {
        log.info("[reliability] Starting infrastructure containers (transformTopology={})", withTransformTopology);
        postgres.start();
        activemq.start();
        toxiproxy.start();
        hxInsightMock.start();
        awsMock.start();

        awsMock.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET_NAME);
        awsS3Client = new AwsS3Client(awsMock.getHost(), awsMock.getFirstMappedPort(), BUCKET_NAME);

        if (withTransformTopology)
        {
            log.info("[reliability] Starting transform topology: SFS, transform-core-aio, transform-router");
            sfs.start();
            transformCoreAio.start();
            transformRouter.start();
        }

        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_ACTIVEMQ_ALIAS, ACTIVEMQ_PORT, ACTIVEMQ_ALIAS, ACTIVEMQ_PORT);
        ToxiproxyClient toxiClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());
        activemqProxy = toxiClient.createProxy(
                "amq",
                "0.0.0.0:" + ACTIVEMQ_PORT,
                ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT);

        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_HXI_ALIAS, TOXIC_HXI_LISTEN_PORT, HXI_MOCK_ALIAS, HXI_MOCK_PORT);
        hxiProxy = toxiClient.createProxy(
                "hxi",
                "0.0.0.0:" + TOXIC_HXI_LISTEN_PORT,
                HXI_MOCK_ALIAS + ":" + HXI_MOCK_PORT);

        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_S3_ALIAS, LOCALSTACK_PORT, LOCALSTACK_ALIAS, LOCALSTACK_PORT);
        s3Proxy = toxiClient.createProxy(
                "s3",
                "0.0.0.0:" + LOCALSTACK_PORT,
                LOCALSTACK_ALIAS + ":" + LOCALSTACK_PORT);

        if (withTransformTopology)
        {
            log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                    TOXIC_SFS_ALIAS, TOXIC_SFS_LISTEN_PORT, SFS_ALIAS, SFS_PORT);
            sfsProxy = toxiClient.createProxy(
                    "sfs",
                    "0.0.0.0:" + TOXIC_SFS_LISTEN_PORT,
                    SFS_ALIAS + ":" + SFS_PORT);
        }

        activemqOpenWireHostPort = activemq.getMappedPort(ACTIVEMQ_PORT);
        activemqJolokiaHostPort = activemq.getMappedPort(ACTIVEMQ_JOLOKIA_PORT);
        jolokia = new JolokiaProbe(activemq.getHost(), activemqJolokiaHostPort);

        log.info("[reliability] Starting repository and live-ingester");
        repository.start();
        // ACS proxy must be created after repository.start() so the upstream alias is resolvable.
        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_ACS_ALIAS, REPOSITORY_PORT, REPOSITORY_ALIAS, REPOSITORY_PORT);
        acsProxy = toxiClient.createProxy(
                "acs",
                "0.0.0.0:" + REPOSITORY_PORT,
                REPOSITORY_ALIAS + ":" + REPOSITORY_PORT);

        if (withTransformTopology)
        {
            // Wait for ACS to populate its transform registry before any test fires a transform request —
            // see {@link #waitForTransformConfigPropagation} for the rationale. 90s ceiling: observed first-GET
            // is 5–25s on a warm ACS image, with headroom for cold-start and the 60s config-poll cron interval.
            waitForTransformConfigPropagation(Duration.ofSeconds(90));
        }

        liveIngester.start();
        log.info("[reliability] Live-ingester JDWP listening — attach IntelliJ to localhost:{} (container 5007)",
                liveIngester.getMappedPort(5007));
        actuatorMetrics = new ActuatorMetricsProbe(liveIngester.getHost(), liveIngester.getMappedPort(8080));

        repositoryHostPort = repository.getMappedPort(REPOSITORY_PORT);
        repositoryClient = new RepositoryClient(repository.getBaseUrl(), RepositoryClient.ADMIN_USER);
        WireMock.configureFor(hxInsightMock.getHost(), hxInsightMock.getPort());

        log.info("[reliability] Environment ready");
    }

    public Proxy activemqProxy()
    {
        return activemqProxy;
    }

    /**
     * Toxiproxy proxy in front of ACS REST. Use to inject latency / partition on the live-ingester ↔ ACS path without affecting the test JVM's direct {@link RepositoryClient} (which uses the host port).
     */
    public Proxy acsProxy()
    {
        return acsProxy;
    }

    /**
     * Toxiproxy proxy in front of the HX Insight WireMock. Use to inject latency / partition on the live-ingester ↔ HXI path (covers both {@code /presigned-urls}, {@code /ingestion-events}, and the auth {@code /token} endpoint) without affecting the test JVM's direct WireMock administration (which uses the host port).
     */
    public Proxy hxiProxy()
    {
        return hxiProxy;
    }

    /**
     * Toxiproxy proxy in front of Localstack S3. Use to inject latency / partition / reset on the live-ingester ↔ S3 upload path. {@link BaseReliabilityIT} re-installs a higher-priority WireMock stub on every reset so {@code POST /presigned-urls} returns a URL whose host is {@code toxic-s3} (rather than the file-based default {@code aws-mock}); the connector therefore PUTs uploads through this proxy. The test JVM's direct {@link AwsS3Client} continues to use the host port and is unaffected.
     */
    public Proxy s3Proxy()
    {
        return s3Proxy;
    }

    /**
     * Toxiproxy proxy in front of the Shared File Store. {@code null} unless this environment was built via {@link Builder#withTransformTopology()}. Use to inject latency / partition / reset on the live-ingester ↔ SFS rendition-download path. Transform-core-aio's writes to SFS bypass this proxy (they use the real {@code shared-file-store} alias) so chaos here only affects the connector's read path.
     */
    public Proxy sfsProxy()
    {
        return sfsProxy;
    }

    /**
     * SFS container handle. {@code null} unless this environment was constructed with the transform topology toggle on. Used by chaos tests that drive container lifecycle directly through the Docker client (e.g. {@code docker stop shared-file-store}).
     */
    public GenericContainer<?> sfsContainer()
    {
        return sfs;
    }

    /**
     * Whether this environment booted the SFS / transform-router / transform-core-aio containers and the {@code toxic-sfs} Toxiproxy listener. Test helpers can guard on this to skip transform-only assertions when running against a minimal env.
     */
    public boolean hasTransformTopology()
    {
        return withTransformTopology;
    }

    public ToxiproxyContainer toxiproxyContainer()
    {
        return toxiproxy;
    }

    public GenericContainer<?> activemqContainer()
    {
        return activemq;
    }

    /**
     * Live-ingester container handle. Used by chaos tests that drive container lifecycle directly through the Docker client.
     */
    public GenericContainer<?> liveIngesterContainer()
    {
        return liveIngester;
    }

    /**
     * ACS repository container handle. Used by chaos tests that drive container lifecycle directly through the Docker client.
     */
    public AlfrescoRepositoryContainer repositoryContainer()
    {
        return repository;
    }

    /**
     * Current base URL for ACS over its host port. Reads the port refreshed by {@link #refreshAfterRepositoryRestart()} so it stays routable after a chaos restart.
     */
    public String repositoryBaseUrl()
    {
        return "http://%s:%s".formatted(repository.getHost(), repositoryHostPort);
    }

    /**
     * OpenWire URL to the broker on its host-mapped port, bypassing Toxiproxy. Used to publish synthetic messages directly to the broker without involving the repository or a network proxy. Reads the host port refreshed by {@link #refreshAfterBrokerRestart()} so it stays routable after a chaos restart.
     */
    public String activemqDirectBrokerUrl()
    {
        return "tcp://" + activemq.getHost() + ":" + activemqOpenWireHostPort;
    }

    /**
     * Re-query the Docker daemon for the repository container's current host port mapping and rebuild {@link RepositoryClient}. Must be called after a chaos test stops and restarts the ACS container — Docker re-allocates ephemeral host ports on each {@code docker start} and Testcontainers caches the original allocation, so subsequent {@link #repositoryClient()} calls would otherwise hit a stale port.
     */
    public void refreshAfterRepositoryRestart()
    {
        InspectContainerResponse info = DockerClientFactory.lazyClient()
                .inspectContainerCmd(repository.getContainerId())
                .exec();
        repositoryHostPort = readHostPort(info, REPOSITORY_PORT);
        String newBaseUrl = "http://%s:%s".formatted(repository.getHost(), repositoryHostPort);
        repositoryClient = new RepositoryClient(newBaseUrl, RepositoryClient.ADMIN_USER);
        log.info("[reliability] Refreshed repository host port mapping: {} (baseUrl={})", repositoryHostPort, newBaseUrl);
    }

    /**
     * Re-query the Docker daemon for the broker container's current host port mappings and rebuild {@link JolokiaProbe}. Must be called after a chaos test stops and restarts the broker container — Docker re-allocates ephemeral host ports on each {@code docker start} and Testcontainers caches the original allocation.
     */
    public void refreshAfterBrokerRestart()
    {
        InspectContainerResponse info = DockerClientFactory.lazyClient()
                .inspectContainerCmd(activemq.getContainerId())
                .exec();
        activemqOpenWireHostPort = readHostPort(info, ACTIVEMQ_PORT);
        activemqJolokiaHostPort = readHostPort(info, ACTIVEMQ_JOLOKIA_PORT);
        jolokia = new JolokiaProbe(activemq.getHost(), activemqJolokiaHostPort);
        log.info("[reliability] Refreshed broker host port mappings: openwire={}, jolokia={}",
                activemqOpenWireHostPort, activemqJolokiaHostPort);
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
        return hxInsightMock;
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
        stopQuietly(liveIngester);
        stopQuietly(repository);
        // Transform-path containers must stop after the connector and ACS that depend on them. Order within the path
        // mirrors the boot order in reverse: router → core-aio → SFS so each tier shuts down with its dependency
        // still up.
        stopQuietly(transformRouter);
        stopQuietly(transformCoreAio);
        stopQuietly(sfs);
        stopQuietly(awsMock);
        stopQuietly(hxInsightMock);
        stopQuietly(toxiproxy);
        stopQuietly(activemq);
        stopQuietly(postgres);
        try
        {
            network.close();
        }
        catch (RuntimeException e)
        {
            log.warn("[reliability] Closing network failed", e);
        }
    }

    private static void stopQuietly(GenericContainer<?> container)
    {
        try
        {
            if (container != null && container.isRunning())
            {
                container.stop();
            }
        }
        catch (RuntimeException e)
        {
            log.warn("[reliability] Stopping container {} failed", container.getContainerName(), e);
        }
    }

    /**
     * Polls transform-router's stdout buffer for the first inbound {@code GET Transform Config} line and returns once it appears or {@code timeout} elapses. transform-router serves that line only when something asks it for the transform-config document — and the only "something" in this topology that does so is ACS (transform-router's own boot fetch goes the other way: router → core-aio). The line's appearance therefore proves ACS has polled transform-router and populated its local TransformRegistry, so a subsequent {@code text/plain → application/pdf} request will resolve to a real transformer rather than short-circuiting with status=400 (the empty-registry path that {@link TransformPipelineUnreachableReliabilityIT} pins as a known silent-drop bug).
     * <p>
     * Why not poll transform-core-aio's logs: core-aio also logs {@code GET Transform Config} but for the router → core-aio fetch, which fires at router boot independent of ACS — so a hit there proves nothing about ACS readiness. Polling the router-side line is the cheapest signal that's actually in the right hop.
     * <p>
     * Returning early after {@code timeout} is intentional: if the chain never warms up, {@link SfsOutageReliabilityIT#shouldTransformAndUploadWhenSfsAvailable} will fail loud with a clear "only the metadata POST landed" assertion message that points back to this hand-off.
     */
    private void waitForTransformConfigPropagation(Duration timeout)
    {
        log.info("[reliability] Waiting up to {}s for ACS → transform-router config poll (proves ACS has registered transformers)",
                timeout.toSeconds());
        long deadlineMillis = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadlineMillis)
        {
            String routerLogs = transformRouter.getLogs();
            if (routerLogs != null && routerLogs.contains("GET Transform Config"))
            {
                log.info("[reliability] Transform config chain warm: transform-router has served a config GET (ACS-driven)");
                return;
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for transform config propagation", e);
            }
        }
        log.warn("[reliability] Transform config chain did NOT warm within {}s — first transform request will likely race the registry and silent-drop on status=400. Bumping the timeout, or warming up the env with an extra ATS transform on @BeforeAll, would address it.",
                timeout.toSeconds());
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

        public ReliabilityEnvironment build()
        {
            return new ReliabilityEnvironment(this);
        }
    }
}
