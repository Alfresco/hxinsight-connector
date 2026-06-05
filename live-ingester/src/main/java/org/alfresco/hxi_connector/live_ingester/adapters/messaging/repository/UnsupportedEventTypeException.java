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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static java.lang.String.format;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.util.LiveIngesterMetrics;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

/**
 * Raised when {@link EventProcessor} receives a syntactically-valid {@code RepoEvent} whose {@code type} matches none of the dispatch predicates ({@code Created} / {@code Updated} / {@code PermissionUpdated} / {@code Deleted} / prediction variants). The default-deployment path re-throws the abandonment as this exception so the route's error handler routes it through the existing repo-events {@code DeadLetterChannel} for a structured operator signal — DLQ entry, exception-tagged DLQ counter, masked exchange-state log line. Deployments that opt out via {@code alfresco.repository.events-subscription.dead-letter-unsupported-types=false} fall back to the legacy shape: an INFO log plus a {@link LiveIngesterMetrics.Drop#REPO_EVENTS_UNHANDLED} counter increment and the event is silently ACK'd.
 */
public class UnsupportedEventTypeException extends LiveIngesterRuntimeException
{
    public UnsupportedEventTypeException(String eventId, String eventType)
    {
        super(format("Repository event id=%s carries unsupported eventType=%s", eventId, eventType));
    }
}
