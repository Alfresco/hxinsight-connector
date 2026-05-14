/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.util;

/**
 * Common shape that route subscription/config records expose to {@link DeadLetterChannels#forRoute} so the helper can build a Camel {@code deadLetterChannel} without needing a dedicated DTO conversion. Each implementor (e.g. {@code Repository.EventsSubscription}, {@code BulkIngester}, {@code Transform.Response}) declares these accessors with matching names so they can be passed by their abstract role.
 *
 * <p>
 * {@link #deadLetterEnabled()} is the per-route on/off switch; route {@code configure()} methods are expected to gate the {@code errorHandler(DeadLetterChannels.forRoute(...))} call on it. Every implementor today defaults the field to {@code false} (see {@code @DefaultValue("false")} on the records) so the route ships with Camel's {@code DefaultErrorHandler} and exhausted retries fall back to broker-side redelivery + the broker's own dead-letter strategy. Operators opt the route-scoped DLC in via the matching {@code ALFRESCO_..._DEADLETTERENABLED=true} env var (see {@code docs/live-ingester.md}); reliability ITs flip the same switch to assert DLQ inventory and metrics. The default-off applies to all three routes uniformly to preserve master parity for the bulk-ingester test topology — see {@code BulkIngesterE2eTest} — and is documented per route in the operator guide.
 *
 * <p>
 * No {@code default} on {@link #deadLetterEnabled()}: every implementor must declare the field explicitly so the operator-facing default lives next to the other {@code @ConfigurationProperties} bindings (where the rest of the route's DLC tunables — {@link #deadLetterUri()}, {@link #maximumRedeliveries()}, {@link #redeliveryDelayMs()} — are also declared) rather than silently inheriting from the interface and drifting away from the docs.
 */
public interface DeadLetterChannelConfig
{
    boolean deadLetterEnabled();

    String deadLetterUri();

    int maximumRedeliveries();

    long redeliveryDelayMs();
}
