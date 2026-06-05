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
 * Inventory of every Micrometer counter the live-ingester exposes via {@code /actuator/metrics}. Producers (route builders, listeners, recorders) reference these constants instead of hard-coding names so operators have a single file to read when wiring dashboards / Prometheus scrape configs. Sister modules (bulk-ingester, nucleus-sync, prediction-applier) should mirror this shape with their own holder classes if/when they grow custom counters.
 */
@SuppressWarnings({"PMD.MissingStaticMethodInNonInstantiatableClass", "PMD.LongVariable"})
public final class LiveIngesterMetrics
{
    private LiveIngesterMetrics()
    {}

    /**
     * Tag keys shared across multiple counters. Tag values are produced at the call site (typically a class name or event type) and are not enumerated here.
     */
    public static final class Tag
    {
        /** The simple class name of the exception associated with the increment. */
        public static final String EXCEPTION = "exception";
        /** The repo-event {@code type} string for events that bypass dispatch. */
        public static final String EVENT_TYPE = "type";
        /** The source MIME type of a content event that was skipped because the configured mapping returned no target. */
        public static final String MIME_TYPE = "mime_type";

        private Tag()
        {}
    }

    /**
     * Counters incremented when a message lands on the configured dead-letter queue after the route's bounded redelivery policy is exhausted. Tagged with {@link Tag#EXCEPTION}.
     */
    public static final class Dlq
    {
        public static final String REPO_EVENTS = "live_ingester_repo_events_dlq_total";
        public static final String REPO_EVENTS_DESCRIPTION = "Repository events moved to the dead-letter queue after the route exhausted its bounded redelivery policy";

        public static final String BULK_EVENTS = "live_ingester_bulk_events_dlq_total";
        public static final String BULK_EVENTS_DESCRIPTION = "Bulk-ingester events moved to the dead-letter queue after the route exhausted its bounded redelivery policy";

        public static final String TRANSFORM_RESPONSE = "live_ingester_transform_response_dlq_total";
        public static final String TRANSFORM_RESPONSE_DESCRIPTION = "Transform-response messages moved to the dead-letter queue after the route exhausted its bounded redelivery policy";

        private Dlq()
        {}
    }

    /**
     * Counters incremented when an event bypasses normal processing — either silently dropped on a known by-design path (status=400 transform-response when the throw default is opted out) or skipped because no dispatch predicate matched (unknown {@code eventType}).
     */
    public static final class Drop
    {
        public static final String REPO_EVENTS_UNHANDLED = "live_ingester_repo_events_unhandled_total";
        public static final String REPO_EVENTS_UNHANDLED_DESCRIPTION = "Repository events whose eventType matched no dispatch predicate and were therefore skipped without being routed to any command handler";

        public static final String TRANSFORM_RESPONSE_SILENT = "live_ingester_transform_response_silent_drop_total";
        public static final String TRANSFORM_RESPONSE_SILENT_DESCRIPTION = "Failed transform-responses (status=400) silently dropped by the legacy branch in ATSTransformResponseHandler.ingestContent (ALFRESCO_TRANSFORM_RESPONSE_THROWFAILEDTRANSFORMS=false). Zero under the default-on throw contract — failed transforms land on " + Dlq.TRANSFORM_RESPONSE + " instead. Non-zero only when deployments opt out.";

        public static final String CONTENT_NO_MIME_MAPPING = "live_ingester_content_no_mime_mapping_total";
        public static final String CONTENT_NO_MIME_MAPPING_DESCRIPTION = "Content events skipped because MimeTypeMapper returned EMPTY_MIME_TYPE — either no rule matched the source MIME type or the matching rule had an empty target. Tagged with the source MIME type so operators can spot misconfigured alfresco.transform.mime-type.mapping entries.";

        private Drop()
        {}
    }

    /**
     * Counters tracking {@code @Retryable} in-delivery retry behaviour, distinct from any JMS-broker-side redelivery the route's DLC also performs.
     */
    public static final class Retry
    {
        public static final String ATTEMPTS = "live_ingester_retry_attempts_total";
        public static final String ATTEMPTS_DESCRIPTION = "Total @Retryable attempts that threw, tagged with the exception class.";

        private Retry()
        {}
    }
}
