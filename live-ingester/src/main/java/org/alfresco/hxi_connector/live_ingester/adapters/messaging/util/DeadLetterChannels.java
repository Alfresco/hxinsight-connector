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

import org.apache.camel.builder.DeadLetterChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Single source of truth for the {@code errorHandler(deadLetterChannel(...))} shape used by every JMS-fed Camel route in the live-ingester. The defaults bundled here — {@code useOriginalMessage()}, exponential back-off, {@code logExhausted(true)}, masked-state error logging via {@link LoggingUtils#logMaskedExchangeState}, and a Micrometer counter increment via {@link DlqMetricsRecorder} — are the operational baseline every route is expected to inherit. A route that needs different semantics can either override individual builder calls on the returned instance or skip the helper.
 *
 * <p>
 * Call sites read as a single line:
 *
 * <pre>{@code
 * errorHandler(DeadLetterChannels.forRoute(subscription, dlqMetricsRecorder, REPO_EVENTS_DLQ_METRIC, log));
 * }</pre>
 */
public final class DeadLetterChannels
{
    private DeadLetterChannels()
    {
    }

    /**
     * Build the project-standard dead-letter channel for a single Camel route.
     *
     * @param config Route's redelivery + dead-letter URI knobs (typically the route's Spring config record).
     * @param recorder Shared Micrometer recorder; injected as a singleton bean.
     * @param metric Per-route counter metadata (name + description).
     * @param log Route's own logger; used for the masked-state {@code ERROR} log line on exhaustion.
     */
    public static DeadLetterChannelBuilder forRoute(
            DeadLetterChannelConfig config,
            DlqMetricsRecorder recorder,
            DlqMetric metric,
            Logger log)
    {
        DeadLetterChannelBuilder builder = new DeadLetterChannelBuilder(config.deadLetterUri());
        builder.useOriginalMessage();
        builder.maximumRedeliveries(config.maximumRedeliveries());
        builder.redeliveryDelay(config.redeliveryDelayMs());
        builder.useExponentialBackOff();
        builder.logExhausted(true);
        builder.logExhaustedMessageHistory(true);
        builder.onPrepareFailure(exchange -> {
            LoggingUtils.logMaskedExchangeState(exchange, log, Level.ERROR);
            recorder.record(exchange, metric);
        });
        return builder;
    }
}
