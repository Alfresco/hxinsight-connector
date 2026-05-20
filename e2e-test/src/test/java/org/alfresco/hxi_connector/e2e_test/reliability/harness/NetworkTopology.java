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

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Network;

/**
 * Single source of truth for the reliability environment's container network — the Docker network handle plus all upstream / Toxiproxy alias names and ports referenced both by the live-ingester env-var map ({@link LiveIngesterEnvVars}) and by Toxiproxy listener creation ({@link ToxiproxyListeners}).
 *
 * <p>
 * Each upstream service has two aliases on the same Docker network: a real {@code <service>} alias the test JVM (and other test-only containers) talk to directly, and a {@code toxic-<service>} alias the live-ingester talks to so chaos can be injected without disturbing the test JVM's own admin connections. Listener ports inside Toxiproxy must be unique even though upstream services may collide on a port number — see {@link #TOXIC_HXI_LISTEN_PORT}.
 */
@Slf4j
@SuppressWarnings("PMD.LongVariable")
final class NetworkTopology implements AutoCloseable
{
    static final String ACTIVEMQ_ALIAS = "activemq";
    static final String TOXIC_ACTIVEMQ_ALIAS = "toxic-activemq";
    static final String REPOSITORY_ALIAS = "repository";
    static final String TOXIC_ACS_ALIAS = "toxic-acs";
    static final String HXI_MOCK_ALIAS = "hxinsight-mock";
    static final String TOXIC_HXI_ALIAS = "toxic-hxi";
    static final String LOCALSTACK_ALIAS = "aws-mock";
    static final String TOXIC_S3_ALIAS = "toxic-s3";
    static final String SFS_ALIAS = "shared-file-store";
    static final String TOXIC_SFS_ALIAS = "toxic-sfs";

    static final int ACTIVEMQ_PORT = 61616;
    static final int ACTIVEMQ_JOLOKIA_PORT = 8161;
    static final int REPOSITORY_PORT = 8080;
    static final int HXI_MOCK_PORT = 8080;
    /**
     * Port the {@code toxic-hxi} Toxiproxy listener binds inside the Toxiproxy container. Distinct from {@link #REPOSITORY_PORT} so the ACS and HXI proxies can coexist in the single Toxiproxy container — a Toxiproxy listener cannot share a port with another listener even if the network aliases differ.
     */
    static final int TOXIC_HXI_LISTEN_PORT = 8081;
    static final int LOCALSTACK_PORT = 4566;
    /**
     * Inside-Toxiproxy listen port for the {@code toxic-sfs} proxy. Matches the SFS upstream port — Toxiproxy listeners can share a port number across distinct aliases (each alias resolves to the same Toxiproxy IP and the listener port selects the proxy), but each listener still needs a unique port within the Toxiproxy container.
     */
    static final int TOXIC_SFS_LISTEN_PORT = 8099;
    static final int SFS_PORT = 8099;

    private final Network network = Network.newNetwork();

    Network network()
    {
        return network;
    }

    /**
     * Network aliases the Toxiproxy container should bind on the shared Docker network. Each alias resolves to the same Toxiproxy IP; the listener port selects which proxy a connection lands on. The {@code toxic-sfs} alias is only included when the env was built with the transform topology toggle on, since the live-ingester only talks to SFS in that case.
     */
    String[] toxiproxyAliases(boolean withTransformTopology)
    {
        return withTransformTopology
                ? new String[]{TOXIC_ACTIVEMQ_ALIAS, TOXIC_ACS_ALIAS, TOXIC_HXI_ALIAS, TOXIC_S3_ALIAS, TOXIC_SFS_ALIAS}
                : new String[]{TOXIC_ACTIVEMQ_ALIAS, TOXIC_ACS_ALIAS, TOXIC_HXI_ALIAS, TOXIC_S3_ALIAS};
    }

    @Override
    public void close()
    {
        try
        {
            network.close();
        }
        catch (RuntimeException e)
        {
            log.warn("[reliability] Closing network failed", e);
        }
    }
}
