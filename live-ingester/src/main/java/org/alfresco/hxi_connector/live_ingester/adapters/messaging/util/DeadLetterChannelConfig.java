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
 * {@link #deadLetterEnabled()} is the per-route on/off switch; route {@code configure()} methods are expected to gate the {@code errorHandler(DeadLetterChannels.forRoute(...))} call on it. The default of {@code true} preserves the always-on behaviour for routes whose dead-letter wiring is mandatory (repo-events, bulk-ingester) — i.e. any unhandled exception during in-delivery processing must land the message on the configured DLQ. Routes whose default is opt-in (transform-response) override the field on the record so deployments that don't want a DLQ entry per failed transform-response can keep the silent-drop semantics. With the DLC disabled the route falls back to Camel's {@code DefaultErrorHandler} + JMS-broker-side redelivery, which surfaces failures as broker-side redeliveries and ultimately to the broker's own dead-letter strategy rather than the route-scoped DLQ this helper builds.
 */
public interface DeadLetterChannelConfig
{
    default boolean deadLetterEnabled()
    {
        return true;
    }

    String deadLetterUri();

    int maximumRedeliveries();

    long redeliveryDelayMs();
}
