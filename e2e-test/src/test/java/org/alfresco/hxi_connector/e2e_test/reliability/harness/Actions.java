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

import static eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.model.Toxic;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

/**
 * Atomic chaos actions for Toxiproxy proxies and Testcontainers containers. Each factory returns a {@link Runnable} so callers can compose plans (see {@link ToxicPlans}) without coupling to the Toxiproxy API directly.
 */
@Slf4j
@UtilityClass
public class Actions
{
    public static final Runnable NO_OP = () -> {};

    public static Runnable addCutBandwidth(Proxy proxy)
    {
        return () -> {
            log.info("[toxi sim] Adding toxic cut_bandwidth");
            try
            {
                proxy.toxics().bandwidth("cut_bandwidth", DOWNSTREAM, 0);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[toxi sim] Adding toxic cut_bandwidth failed", e);
            }
        };
    }

    public static Runnable removeCutBandwidth(Proxy proxy)
    {
        return () -> removeToxicIfExists(proxy, "cut_bandwidth");
    }

    public static Runnable disable(Proxy proxy)
    {
        return () -> {
            log.info("[toxi sim] Disabling proxy");
            try
            {
                proxy.disable();
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[toxi sim] Disabling proxy failed", e);
            }
        };
    }

    public static Runnable enable(Proxy proxy)
    {
        return () -> {
            log.info("[toxi sim] Enabling proxy");
            try
            {
                proxy.enable();
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[toxi sim] Enabling proxy failed", e);
            }
        };
    }

    public static Runnable stop(GenericContainer<?> container)
    {
        return () -> {
            log.info("[toxi sim] Stopping container");
            container.stop();
            log.info("[toxi sim] Stopped container");
        };
    }

    public static Runnable start(GenericContainer<?> container)
    {
        return () -> {
            log.info("[toxi sim] Starting container");
            container.start();
            log.info("[toxi sim] Started container");
        };
    }

    public static Runnable addLatencyAndJitter(Proxy proxy)
    {
        return () -> {
            log.info("[toxi sim] Adding toxic latency_and_jitter");
            try
            {
                proxy.toxics().latency("latency_and_jitter", DOWNSTREAM, 300).setJitter(200);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[toxi sim] Adding toxic latency_and_jitter failed", e);
            }
        };
    }

    public static Runnable removeLatencyAndJitter(Proxy proxy)
    {
        return () -> removeToxicIfExists(proxy, "latency_and_jitter");
    }

    public static Runnable addResetPeer(Proxy proxy)
    {
        return () -> {
            log.info("[toxi sim] Adding toxic reset_peer");
            try
            {
                proxy.toxics().resetPeer("reset_peer", DOWNSTREAM, 0);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("[toxi sim] Adding toxic reset_peer failed", e);
            }
        };
    }

    public static Runnable removeResetPeer(Proxy proxy)
    {
        return () -> removeToxicIfExists(proxy, "reset_peer");
    }

    public static Runnable waitFor(Duration duration)
    {
        return () -> {
            try
            {
                log.info("[toxi sim] Waiting for {}", duration);
                Thread.sleep(duration.toMillis());
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("[toxi sim] Waiting interrupted", e);
            }
        };
    }

    private static void removeToxicIfExists(Proxy proxy, String name)
    {
        getAllToxics(proxy).stream()
                .filter(toxic -> name.equals(toxic.getName()))
                .findFirst()
                .ifPresent(Actions::removeToxic);
    }

    private static List<? extends Toxic> getAllToxics(Proxy proxy)
    {
        try
        {
            return proxy.toxics().getAll();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[toxi sim] Getting all toxics failed", e);
        }
    }

    private static void removeToxic(Toxic toxic)
    {
        try
        {
            log.info("[toxi sim] Removing toxic {}", toxic.getName());
            toxic.remove();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("[toxi sim] Removing toxic " + toxic.getName() + " failed", e);
        }
    }
}
