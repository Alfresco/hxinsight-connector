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

import java.time.Duration;
import java.util.function.Consumer;

import eu.rekawek.toxiproxy.Proxy;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

/**
 * Reusable chaos scenarios for Toxiproxy proxies and Testcontainers containers, ready to be fed into a {@link ToxicPlanner}. 
 */
@Slf4j
@UtilityClass
public class ToxicPlans
{
    public static Consumer<Proxy> disableAndEnableProxyContinuously()
    {
        return proxy -> {
            try
            {
                while (!Thread.currentThread().isInterrupted())
                {
                    Thread.sleep((long) (Math.random() * 1234));
                    Actions.disable(proxy).run();
                    Thread.sleep((long) (Math.random() * 1234));
                    Actions.enable(proxy).run();
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                Actions.enable(proxy).run();
            }
        };
    }

    public static Consumer<Proxy> afterInitialDelayCutBandwidthForDelay(Duration initialDelay, Duration delay)
    {
        return proxy -> {
            try
            {
                Thread.sleep(initialDelay.toMillis());
                Actions.addCutBandwidth(proxy).run();
                Thread.sleep(delay.toMillis());
                Actions.removeCutBandwidth(proxy).run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                Actions.removeCutBandwidth(proxy).run();
            }
        };
    }

    public static Consumer<Proxy> afterInitialDelayDisableProxyForDelay(Duration initialDelay, Duration delay)
    {
        return proxy -> {
            try
            {
                Thread.sleep(initialDelay.toMillis());
                Actions.disable(proxy).run();
                Thread.sleep(delay.toMillis());
                Actions.enable(proxy).run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                if (!proxy.isEnabled())
                {
                    Actions.enable(proxy).run();
                }
            }
        };
    }

    public static Consumer<Proxy> afterInitialDelaySetLatencyAndJitterForDelay(Duration initialDelay, Duration delay)
    {
        return proxy -> {
            try
            {
                Thread.sleep(initialDelay.toMillis());
                Actions.addLatencyAndJitter(proxy).run();
                Thread.sleep(delay.toMillis());
                Actions.removeLatencyAndJitter(proxy).run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                Actions.removeLatencyAndJitter(proxy).run();
            }
        };
    }

    public static Consumer<Proxy> afterInitialDelayResetPeerForDelay(Duration initialDelay, Duration delay)
    {
        return proxy -> {
            try
            {
                Thread.sleep(initialDelay.toMillis());
                Actions.addResetPeer(proxy).run();
                Thread.sleep(delay.toMillis());
                Actions.removeResetPeer(proxy).run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                Actions.removeResetPeer(proxy).run();
            }
        };
    }

    public static Consumer<GenericContainer<?>> restartContainer(Duration initialDelay, Duration delay)
    {
        return container -> {
            try
            {
                Thread.sleep(initialDelay.toMillis());
                Actions.stop(container).run();
                Thread.sleep(delay.toMillis());
                Actions.start(container).run();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            finally
            {
                if (!container.isRunning())
                {
                    Actions.start(container).run();
                }
            }
        };
    }
}
