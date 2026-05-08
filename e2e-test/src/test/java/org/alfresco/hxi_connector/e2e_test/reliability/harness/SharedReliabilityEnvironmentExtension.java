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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

/**
 * JUnit 5 extension that boots a single {@link ReliabilityEnvironment} for the entire Failsafe JVM and shares it across every reliability IT that extends {@link BaseReliabilityIT}. Saves the ~85 s startup cost on every IT after the first.
 *
 * <p>
 * Lifecycle is driven through the JUnit {@link Store} at the root namespace: the first {@code @BeforeAll} that fires across the JVM computes the env, every subsequent class reuses the same instance, and JUnit invokes {@link CloseableResource#close()} once at JVM shutdown. No need for a static field, an explicit shutdown hook, or any test-class coordination.
 *
 * <p>
 * Tests that intentionally tear down infrastructure (e.g. process-level chaos that stops the broker container) must <b>not</b> use this extension, because killing a shared container would cascade-fail every subsequent test in the run. Such tests should keep their own per-class {@code @BeforeAll}/{@code @AfterAll} environment lifecycle.
 */
@Slf4j
@SuppressWarnings("deprecation") // ExtensionContext.Store.CloseableResource + getOrComputeIfAbsent(Object,Function,Class) deprecated in JUnit 5.13/6.0; replacements not adopted yet repo-wide.
public final class SharedReliabilityEnvironmentExtension implements BeforeAllCallback
{
    private static final Namespace NAMESPACE = Namespace.create(SharedReliabilityEnvironmentExtension.class);
    private static final String STORE_KEY = "shared-reliability-env";

    /**
     * Cached reference to the shared environment, populated on first {@code @BeforeAll}. Kept as a static so {@link BaseReliabilityIT#environment()} can be a no-argument accessor; lifecycle (creation, shutdown) is still managed by the JUnit {@link Store} below — the static is just a fast read-side cache.
     */
    private static volatile ReliabilityEnvironment instance;

    @Override
    public void beforeAll(ExtensionContext context)
    {
        Store store = context.getRoot().getStore(NAMESPACE);
        SharedEnvironment shared = store.getOrComputeIfAbsent(STORE_KEY, key -> startSharedEnvironment(), SharedEnvironment.class);
        instance = shared.environment();
    }

    static ReliabilityEnvironment sharedEnvironment()
    {
        ReliabilityEnvironment env = instance;
        if (env == null)
        {
            throw new IllegalStateException("[reliability] Shared environment requested before SharedReliabilityEnvironmentExtension#beforeAll fired — make sure your test extends BaseReliabilityIT");
        }
        return env;
    }

    @SneakyThrows
    private static SharedEnvironment startSharedEnvironment()
    {
        log.info("[reliability] Booting shared ReliabilityEnvironment for the Failsafe JVM");
        ReliabilityEnvironment environment = ReliabilityEnvironment.builder().build();
        environment.start();
        return new SharedEnvironment(environment);
    }

    /**
     * Wrapper that ties the shared {@link ReliabilityEnvironment} to JUnit's resource-cleanup protocol; {@link CloseableResource#close()} is invoked exactly once when the root extension context is torn down at JVM shutdown.
     */
    /**
     * Implements both {@link CloseableResource} (the JUnit 5.0–5.12 store-cleanup contract) and {@link AutoCloseable} (the contract JUnit 5.13+ favours after deprecating {@code CloseableResource}). Implementing both keeps the extension forward-compatible without forcing a JUnit upgrade.
     */
    private record SharedEnvironment(ReliabilityEnvironment environment) implements CloseableResource, AutoCloseable
    {
        @Override
        public void close()
        {
            log.info("[reliability] Closing shared ReliabilityEnvironment at JVM shutdown");
            instance = null;
            environment.close();
        }
    }
}
