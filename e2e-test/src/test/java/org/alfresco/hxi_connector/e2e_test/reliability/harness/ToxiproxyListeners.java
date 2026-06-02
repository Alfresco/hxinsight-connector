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

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.utility.DockerImageName;

import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.*;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.NUCLEUS_ALIAS;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.NUCLEUS_LISTEN_PORT;
import static org.alfresco.hxi_connector.e2e_test.reliability.harness.NetworkTopology.NUCLEUS_MOCK_PORT;

/**
 * Owns the Toxiproxy container plus every {@link Proxy} listener fronting an upstream container in the reliability environment. Creation is split into two phases mirroring the dependency chain — see {@link #createPreRepositoryProxies(boolean)} and {@link #createAcsProxy()}.
 *
 * <p>
 * All listeners live in a single Toxiproxy container. The Docker-network alias selects which listener a connector request hits; the listener port disambiguates within the proxy. The {@code sfs} listener is created only when the env was built with the transform topology toggle on.
 */
@Slf4j
@SuppressWarnings("PMD.LongVariable")
final class ToxiproxyListeners
{
    private static final String TOXIPROXY_IMAGE = "ghcr.io/shopify/toxiproxy:2.12.0";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    @SuppressWarnings("PMD.CloseResource") // ToxiproxyContainer lifecycle managed by ReliabilityEnvironment
    private final ToxiproxyContainer container;
    private ToxiproxyClient client;

    private Proxy activemqProxy;
    private Proxy hxiProxy;
    private Proxy s3Proxy;
    private Proxy acsProxy;
    private Proxy sfsProxy;
    private Proxy nucleusproxy;

    ToxiproxyListeners(NetworkTopology topology, boolean withTransformTopology)
    {
        this.container = new ToxiproxyContainer(DockerImageName.parse(TOXIPROXY_IMAGE))
                .withNetwork(topology.network())
                .withNetworkAliases(topology.toxiproxyAliases(withTransformTopology))
                .withStartupTimeout(STARTUP_TIMEOUT);
    }

    ToxiproxyContainer container()
    {
        return container;
    }

    void start()
    {
        container.start();
        client = new ToxiproxyClient(container.getHost(), container.getControlPort());
    }

    /**
     * Create the listeners whose upstream containers are already running by the time {@link ReliabilityEnvironment#start()} reaches this hand-off (broker, HX Insight WireMock, Localstack, optionally SFS). The ACS listener is deferred to {@link #createAcsProxy()} because the {@code repository} alias is only bound on the network once the ACS container starts, and {@link ToxiproxyClient#createProxy} rejects upstreams it cannot resolve.
     */
    void createPreRepositoryProxies(boolean withTransformTopology) throws IOException
    {
        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_ACTIVEMQ_ALIAS, ACTIVEMQ_PORT, ACTIVEMQ_ALIAS, ACTIVEMQ_PORT);
        activemqProxy = client.createProxy(
                "amq",
                "0.0.0.0:" + ACTIVEMQ_PORT,
                ACTIVEMQ_ALIAS + ":" + ACTIVEMQ_PORT);

        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_HXI_ALIAS, TOXIC_HXI_LISTEN_PORT, HXI_MOCK_ALIAS, HXI_MOCK_PORT);
        hxiProxy = client.createProxy(
                "hxi",
                "0.0.0.0:" + TOXIC_HXI_LISTEN_PORT,
                HXI_MOCK_ALIAS + ":" + HXI_MOCK_PORT);

        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_S3_ALIAS, LOCALSTACK_PORT, LOCALSTACK_ALIAS, LOCALSTACK_PORT);
        s3Proxy = client.createProxy(
                "s3",
                "0.0.0.0:" + LOCALSTACK_PORT,
                LOCALSTACK_ALIAS + ":" + LOCALSTACK_PORT);

        if (withTransformTopology)
        {
            log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                    TOXIC_SFS_ALIAS, TOXIC_SFS_LISTEN_PORT, SFS_ALIAS, SFS_PORT);
            sfsProxy = client.createProxy(
                    "sfs",
                    "0.0.0.0:" + TOXIC_SFS_LISTEN_PORT,
                    SFS_ALIAS + ":" + SFS_PORT);
        }
    }

    void createNucleusProxy() throws IOException{
        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_NUCLEUS_ALIAS, NUCLEUS_LISTEN_PORT, NUCLEUS_ALIAS, NUCLEUS_MOCK_PORT);
        nucleusproxy = client.createProxy(
                    NUCLEUS_ALIAS,
                    "0.0.0.0:" + NUCLEUS_LISTEN_PORT,
                    NUCLEUS_ALIAS + ":" + NUCLEUS_MOCK_PORT);
    }

    /**
     * Create the ACS listener. Must be called after the repository container has started so its {@code repository} alias is resolvable on the Docker network — see {@link #createPreRepositoryProxies(boolean)}.
     */
    void createAcsProxy() throws IOException
    {
        log.info("[reliability] Configuring Toxiproxy: {}:{} -> {}:{}",
                TOXIC_ACS_ALIAS, REPOSITORY_PORT, REPOSITORY_ALIAS, REPOSITORY_PORT);
        acsProxy = client.createProxy(
                "acs",
                "0.0.0.0:" + REPOSITORY_PORT,
                REPOSITORY_ALIAS + ":" + REPOSITORY_PORT);
    }

    Proxy activemqProxy()
    {
        return activemqProxy;
    }

    Proxy acsProxy()
    {
        return acsProxy;
    }

    Proxy hxiProxy()
    {
        return hxiProxy;
    }

    Proxy s3Proxy()
    {
        return s3Proxy;
    }

    /**
     * {@code null} unless the env was built with the transform topology toggle on.
     */
    Proxy sfsProxy()
    {
        return sfsProxy;
    }

    // Proxy to the Nucleus WireMock Container
    Proxy nucleusProxy() {return nucleusproxy;}
}
