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
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.HXI_MOCK_ALIAS;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;

/**
 * Owns the {@link GenericContainer} handles for every infrastructure dependency the reliability env boots, along with their construction-time wiring (network attachment, depends-on, ACS java opts, live-ingester env vars). Lifecycle is exposed as four staged hand-offs ({@link #startInfrastructure()}, {@link #startTransformTopology()}, {@link #startRepository()}, {@link #startLiveIngester()}) so {@link ReliabilityEnvironment#start()} can interleave Toxiproxy listener creation between stages — see {@link ToxiproxyListeners#createAcsProxy()} for why ACS proxy creation is deferred.
 *
 * <p>
 * Transform-topology containers ({@code sfs}, {@code transformCoreAio}, {@code transformRouter}) are populated only when the spec was built with {@link ReliabilityEnvironment.Builder#withTransformTopology()}; otherwise the corresponding fields stay {@code null} and the staged starters return early.
 */
@Slf4j
@SuppressWarnings("PMD.LongVariable")
final class ContainerComposition implements AutoCloseable
{
    private final ReliabilityEnvironmentSpec spec;

    private final PostgreSQLContainer<?> postgres;
    private final GenericContainer<?> activemq;
    private final WireMockContainer hxInsightMock;
    private final LocalStackContainer awsMock;
    private final AlfrescoRepositoryContainer repository;
    private final GenericContainer<?> liveIngester;
    private final GenericContainer<?> sfs;
    private final GenericContainer<?> transformCoreAio;
    private final GenericContainer<?> transformRouter;

    ContainerComposition(ReliabilityEnvironmentSpec spec, NetworkTopology topology)
    {
        this.spec = spec;
        postgres = DockerContainers.createPostgresContainerWithin(topology.network());
        activemq = DockerContainers.createActiveMqContainerWithin(topology.network());

        if (spec.withTransformTopology())
        {
            sfs = DockerContainers.createSfsContainerWithin(topology.network());
            // LibreOffice (text/plain → application/pdf path) needs more heap than the default 1024m;
            // matching the docker-compose-minimal.yml + ATSTransformE2eTest sizing.
            transformCoreAio = DockerContainers.createTransformCoreAioContainerWithin(topology.network())
                    .withEnv("JAVA_OPTS", "-Xms256m -Xmx3000m")
                    .dependsOn(activemq, sfs);
            transformRouter = DockerContainers.createTransformRouterContainerWithin(topology.network())
                    .dependsOn(activemq, transformCoreAio);
        }
        else
        {
            sfs = null;
            transformCoreAio = null;
            transformRouter = null;
        }

        hxInsightMock = DockerContainers.createWireMockContainerWithin(topology.network())
                // Pin an explicit alias so the {@code toxic-hxi} Toxiproxy upstream is legible at the call site
                // ({@link DockerContainers#createWireMockContainerWithin} otherwise leaves the container with
                // only Testcontainers' auto-generated alias).
                .withNetworkAliases(HXI_MOCK_ALIAS)
                .withFileSystemBind(
                        "src/test/resources/wiremock/hxinsight",
                        "/home/wiremock",
                        BindMode.READ_ONLY);

        awsMock = DockerContainers.createLocalStackContainerWithin(topology.network());

        // ACS java opts switch with the toggle: transform.service.enabled=true plus transform-router / SFS URLs
        // are needed for the transform-capability registry to populate; otherwise the registry stays empty
        // and ACS short-circuits every transform request with a status=400 transform-response (the path
        // exercised by TransformPipelineUnreachableReliabilityIT, intentionally distinct from the SFS-outage
        // topology in SfsOutageReliabilityIT where real transforms run and only the connector's SFS read is
        // proxied through Toxiproxy).
        String repoJavaOpts = spec.withTransformTopology()
                ? getRepoJavaOptsWithTransforms(postgres, activemq)
                : getMinimalRepoJavaOpts(postgres, activemq);
        repository = DockerContainers.createExtendedRepositoryContainerWithin(topology.network(), ENTERPRISE)
                .withJavaOpts(repoJavaOpts);

        liveIngester = DockerContainers.createLiveIngesterContainerForWireMock(hxInsightMock, repository, topology.network())
                .withEnv(LiveIngesterEnvVars.forSpec(spec));
    }

    /**
     * Boot the always-on infrastructure tier: postgres (ACS metadata), ActiveMQ (event transport), HX Insight WireMock (mock control plane), Localstack S3 (mock object store). Toxiproxy is a peer of these but lives in {@link ToxiproxyListeners} so the env can sequence its listener creation between hand-offs.
     */
    void startInfrastructure()
    {
        postgres.start();
        activemq.start();
        hxInsightMock.start();
        awsMock.start();
    }

    /**
     * Boot the transform tier (SFS + transform-core-aio + transform-router). No-op when the env was constructed without {@link ReliabilityEnvironment.Builder#withTransformTopology()}.
     */
    void startTransformTopology()
    {
        if (!spec.withTransformTopology())
        {
            return;
        }
        log.info("[reliability] Starting transform topology: SFS, transform-core-aio, transform-router");
        sfs.start();
        transformCoreAio.start();
        transformRouter.start();
    }

    void startRepository()
    {
        log.info("[reliability] Starting repository");
        repository.start();
    }

    void startLiveIngester()
    {
        log.info("[reliability] Starting live-ingester");
        liveIngester.start();
    }

    /**
     * Polls transform-router's stdout buffer for the first inbound {@code GET Transform Config} line and returns once it appears or {@code timeout} elapses. transform-router serves that line only when something asks it for the transform-config document — and the only "something" in this topology that does so is ACS (transform-router's own boot fetch goes the other way: router → core-aio). The line's appearance therefore proves ACS has polled transform-router and populated its local TransformRegistry, so a subsequent {@code text/plain → application/pdf} request will resolve to a real transformer rather than short-circuiting with status=400 (the empty-registry path that {@link TransformPipelineUnreachableReliabilityIT} pins as a known silent-drop bug).
     * <p>
     * Why not poll transform-core-aio's logs: core-aio also logs {@code GET Transform Config} but for the router → core-aio fetch, which fires at router boot independent of ACS — so a hit there proves nothing about ACS readiness. Polling the router-side line is the cheapest signal that's actually in the right hop.
     * <p>
     * Returning early after {@code timeout} is intentional: if the chain never warms up, {@link SfsOutageReliabilityIT#shouldTransformAndUploadWhenSfsAvailable} will fail loud with a clear "only the metadata POST landed" assertion message that points back to this hand-off.
     */
    void waitForTransformConfigPropagation(Duration timeout)
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

    GenericContainer<?> activemq()
    {
        return activemq;
    }

    AlfrescoRepositoryContainer repository()
    {
        return repository;
    }

    GenericContainer<?> liveIngester()
    {
        return liveIngester;
    }

    WireMockContainer hxInsightMock()
    {
        return hxInsightMock;
    }

    LocalStackContainer awsMock()
    {
        return awsMock;
    }

    /**
     * {@code null} unless the env was built with {@link ReliabilityEnvironment.Builder#withTransformTopology()}.
     */
    GenericContainer<?> sfs()
    {
        return sfs;
    }

    @Override
    public void close()
    {
        // Reverse boot order so each tier shuts down with its dependency still up.
        stopQuietly(liveIngester);
        stopQuietly(repository);
        stopQuietly(transformRouter);
        stopQuietly(transformCoreAio);
        stopQuietly(sfs);
        stopQuietly(awsMock);
        stopQuietly(hxInsightMock);
        stopQuietly(activemq);
        stopQuietly(postgres);
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
}
